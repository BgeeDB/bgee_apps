(function () {
    'use strict';

    /**
     * @ngdoc function
     * @name app.controller:MainCtrl
     * @description
     * # MainCtrl
     * Controller of the app
     */

    angular.module('app')
        .controller('MainCtrl', MainCtrl, ['ui.bootstrap', 'angularFileUpload', 'ngLocationUpdate', 'ngFileSaver', 'ngSanitize']);

    MainCtrl.$inject = ['$scope', '$sce', 'bgeedataservice', 'bgeejobservice', 'helpservice', 'DataTypeFactory', 'configuration', 'logger', 'FileUploader', '$timeout', '$location', '$interval', 'lang', 'jobStatus', '$filter', 'FileSaver', 'Blob', '$route', '$window'];

    function MainCtrl ($scope, $sce, bgeedataservice, bgeejobservice, helpservice, DataTypeFactory, configuration, logger, FileUploader, $timeout, $location, $interval, lang, jobStatus, $filter, FileSaver, Blob, $route, $window) {

        var vm = this;

        //showMessage($scope, "bgee app");
        
        $scope.$on('$routeChangeStart', function(event, next, current) {
            console.log("route changed");
            showMessage($scope, "bgee app");
            //this only works thanks to the "hack" of the trailing slash (see topanat.js): 
            //there is a route change only when clicking on the recent job or example links, 
            //because there is no trailing slash in them. 
            //Also, there are cases where a route change, but we don't want to display the message: 
            //notably, when getting to a page with a pre-fill form, and clicking "start new job", 
            //the hash in URL is reset, but Angular still keeps it in memory. So, if we modify 
            //the parameters of the form and submit the job, the associated hash will change, 
            //and Angular will see that as a change of route, from the old hash in memory 
            //to the new hash following job submission. So, we never display the waiting message 
            //when there is a form submission involved. And, also, we only display the message 
            //when there is a hash used (potentially slow page to load).
//            console.log(next.params.jobid + " - " + current.params.jobid + " - " + vm.formSubmitted
//            		+ " - " + current.params.hash  + " - " + next.params.hash);
            //FIXME: this actually doesn't work, there is too many route change, using update_path 
            //without updating the route, etc. Bottom line, we don't want to display 
            //the waiting message when there is a form submission involved, but it's almost impossible 
            //to determine reliably (even the variable vm.formSubmitted is inconsistently used)
//            if (!(typeof next.params.hash == 'undefined' || current.params.jobid || next.params.jobid)) {
//            	console.log('display waiting message');
//                $timeout(function() {$('#appLoading').show();});
//            }
        });
        $scope.$on('$routeChangeSuccess', function() {
            showMessage($scope, false);
            $('#appLoading').hide();
        });
        $scope.$on('$routeChangeError', function() {
            showMessage($scope, false);
            $('#appLoading').hide();
        });


        vm.isFormValidDataTypes = vm.isFormValidDevStages = 'yes';

        vm.fileType = "";
        vm.isOpen = true;

        // uploader file queue
        vm.fq = [];
        vm.fq['fg'] = [];
        vm.fq['bg'] = [];

        // show file upload queue for filetype
        vm.hideq = [];
        vm.hideq['fg'] = true;
        vm.hideq['bg'] = true;

        vm.showUploaded = [];
        vm.showUploaded['fg'] = false;
        vm.showUploaded['bg'] = false;

        vm.fileitem = [];
        vm.fileitem.fg = [];
        vm.fileitem.bg = [];
        vm.uploadError = [];

        vm.email = '';
        vm.job_title = '';
        vm.resultUrl = '';

        // SD: equals to false when:
        // no species is entered
        // the entered species is not correct
        //
        // in such case:
        // the background + dev stages + data types are not shown
        // the submit button is disabled
        // the spinner+message in the Gene list's header is not shown
        //
        // I had to add yet another flag because:
        // the spinner and the waiting message
        // are visible until species = '' (in case genes list is not empty)
        // so when the returned result is "no species found"
        // we have to set species to something (in this case None)
        // in order to hide the message+spinner
        vm.isValidSpecies = false;
        vm.isValidBackground = false;
        vm.dataTypeMissing = false;
        vm.devStageMissing = false;
        vm.showDevStageError = false;

        /* Help panel: Remember whether the panel is shown or not */
        vm.showHelp = getShowHelp();

        // bindable
        vm.getDevStages = getDevStages;
        vm.displayResults = displayResults;
        vm.cancelJob = cancelJob;
        vm.startNewJob = startNewJob;
        vm.getOnlineHelpForItem = getOnlineHelpForItem;
        vm.changeNumberOfRowsToShow = changeNumberOfRowsToShow;
        vm.getResultNumber = getResultNumber;

        /************** No default values ***********************/


        vm.message = ''; // Defining empty message variable
        // message from the gene validation query.
        // Filled only when more than one species detected in fg
        vm.geneValidationMessage = '';
        vm.geneModalMessage = '';
        //bg gene list info
        vm.bgGeneValidationMessage = '';
        vm.bgGeneModalMessage = '';

        // getDevStage checks whether all the FG genes are in the BG.
        // In case of issue, isValidBackground is set to FALSE
        // BUT as we had to get rid of the return statement
        // to fix issue #112, the handleDevStage function
        // gets executed. And if the species are the same, then the
        // BG is considered as valid. In such cases, we have to check
        // whether the BG is really valid by checking whether isValidBackgroundMessage
        // is empty or not.
        // TODO: This should be revised once we refactor the getDevStages/handleDevStages functions
        vm.isValidBackgroundMessage = '';

        // We have to use an array for the selected
        // stages - See:
        // http://stackoverflow.com/questions/14514461/how-can-angularjs-bind-to-list-of-checkbox-values
        // Moreover, the stages have to be checked once retrieved from the server
        vm.developmentStages = [];

        vm.isBackgroundChecked = 'checked';
        vm.isStageChecked = 'checked';

        vm.formSubmitted = false;
        vm.isAdvancedOptionsChecked = false;
        vm.jobStatus = null;

        /* result filtering */
        vm.filterValue = ''; // single filter (on organ names and ids)
        vm.filterByStage = '';
        vm.filterbyExpression = '';
        vm.analysisList = []; // store the list of analysis
        vm.isActive = 'ALL'; // selected analysis to show

        /************** Default values ***********************/
        vm.data_qual = vm.data_qualDefault = configuration.data_qual;
        vm.data_qualities = configuration.data_qualities;
        vm.decorrelation_types = configuration.decorrelation_types;
        vm.decorr_type = vm.decorr_typeDefault = configuration.decorr_type;
        vm.expr_type = configuration.expr_type;

        vm.fdr_thr = vm.fdrThresholdDefault = configuration.fdr_thr;
        vm.nb_node = vm.nbNodesDefault = configuration.nb_node;
        vm.node_size = vm.nodeSizeDefault = configuration.node_size;
        vm.p_value_thr = vm.pvalueThresholdDefault = configuration.p_value_thr;
        vm.nb_rows = configuration.nb_rows;
        vm.row_height = configuration.row_height;

        vm.nbRows = {
            availableOptions: [
                {id: 10,  name: 10},
                {id: 20,  name: 20},
                {id: 50,  name: 50},
                {id: 100, name: 100}
            ],
            selectedOption: {id: vm.nb_rows, name: vm.nb_rows} //This sets the default value of the select in the ui
        };

        showMessage($scope, false);

        if(jobStatus){
            showMessage($scope, "job status");
            console.info("got jobstatus");
            console.info(jobStatus);

            angular.forEach(jobStatus.requestParameters, function(val, key) {

                if(key == "nb_node" || key == "node_size"){
                    val = parseInt(val);
                }

                // we use hash instead of data mainly to make it clear what data it is
                if(key == "data"){
                    vm['hash'] = val;
                }

                if ((key == "fg_list" || key == "bg_list") && val) {
                    //the gene list is returned as an array in the requestParameters object,
                    //AngularJS will translate it as a comma-separated list in the textarea.
                    //XXX: is there a way to tell AngularJS to translate as list with line return?
                    //XXX: is this the place to manage this replacement? I don't see such a handling
                    //of other parameters...
                    var newVal = "";
                    var listLength = val.length;

                    for (var i = 0; i < listLength; i++) {
                        newVal += val[i] + "\n";
                    }

                    val = newVal;
                }

                if(key == "data_type"){
                    key = "selectedDataTypes"
                }
                if (key == "expr_type" &&
                        typeof val !== "undefined" && val != null && val.length != null &&
                        val.length > 0) {
                    //expr_type as returned by RequestParameters is an array,
                    //but we use a string in vm, need to convert
                    val = val[0];
                }

                //requested stages will be stored with key 'stage_id',
                //and will be used by "handleStages" to check the correct stages
                vm[key] = val;

                console.debug("vm."+key+" = "+vm[key]);

            });

            angular.forEach(jobStatus.data.jobResponse, function(val, key) {

                // we use hash instead of data mainly to make it clear what data it is
                if(key == "data"){
                    vm['hash'] = val;
                }

                vm[key] = val;

                console.debug("vm."+key+" = "+vm[key]);

            });

            //This function has been updated not to redo a query to the server
            //to validate the gene lists, all info necessary is already present in the response
            getDevStages('fg', vm.fg_list, jobStatus);
            getDevStages('bg', vm.bg_list, jobStatus);

            showMessage($scope, "developmental and life stages");

            $timeout(function(){

                console.log("get, display or submit");
                console.log(jobStatus);
                // open advanced options if non default values were used
                shouldOpenAdvancedOptions(vm, configuration);

                // jobId check is a magic number we use to get used parameters when result is not available
                // prevents us getting results again when we already know it's missing

                if(vm.jobId != -1 && (typeof jobStatus.code !== 'undefined' &&
                    jobStatus.code !== 400 &&
                    typeof jobStatus.data.topAnatResults !== 'undefined' ||
                    (typeof jobStatus.data.jobResponse !== 'undefined' &&
                    jobStatus.data.jobResponse.jobStatus.toLowerCase() == "undefined"))
                ){

                    console.log(jobStatus.data);

                    console.log("job is done, either get result or display it");
                    //XXX: I see that it is easy for you to display the results at any moment,
                    //would you like to disable the 'get_results' query, and to always retrieve
                    //the results from a 'tracking_job' or 'form_prefill' query directly?
                    if(!jobStatus.data.topAnatResults){
                        console.log("Get results");
                        getResults();
                    } else {
                        console.log("Display results");
                        console.log(jobStatus.data.topAnatResults);
                        displayResults(jobStatus);
                    }

                } else {

                    console.log("no result, send form or check Jobstatus");

                    // no result, check if we have requestParameters and resubmit
                    if(vm.jobId == -1 || (typeof jobStatus.requestParameters !== 'undefined' && jobStatus.code == 400)) {
                        console.log("no result or job found for the parameters, resubmit");
                        logger.info("Job result was missing, resubmitting job");
                        $timeout(function(){
                            $window.document.getElementById('resultContainer').scrollIntoView();
                        }, 500);
                        vm.sendForm();
                    } else {
                        console.log("job is not done, checkjobstatus");
                        // This fixes issue #111
                        // Do not remove the trailing slash, see comments in topanat.js
                        vm.resultUrl = '/result/'+vm.hash+'/'+vm.jobId + '/';
                        vm.formSubmitted = true;
                        checkJobStatus();
                    }

                }
            }, 100);

        } else {

            showMessage($scope, false);
            console.info("no jobstatus, skipped statusquery");
            vm.fg_list = '';
            vm.bg_list = '';
            /* species are set by a web service to bgee
             * should be identical for fg and bg */
            vm.selected_species = '';
            vm.background_species = '';
            vm.selected_taxid = ''; // for the picture (see Issue #27)


        }

        // cleanup function
        $scope.$on("$destroy", function() {

            showMessage($scope, false);

            if (timer) {
                $timeout.cancel(timer);
            }
        });

        // open advanced options if non default values were used
        shouldOpenAdvancedOptions(vm, configuration);

        /***************************** View result by stage and expression type **************************/
        function getCombinedDevStageAndExpressionType() {

            vm.analysisList = []; // reset the array
            var specificStagesAnalysisList = []; // reset the array
            
            var developmentStagesCheckedCount = 0;
            
            angular.forEach(vm.developmentStages, function(stage, key) {
                if (stage.checked) {
                    var object = buildStage(stage.id, stage.name);
                    if (object.size !== 0) {
                        vm.analysisList.push(object);
                        developmentStagesCheckedCount += 1;
                    }
                }
            });

            if (developmentStagesCheckedCount === 0) {
                var object = buildStage('ALL-STAGES', 'all stages');
                if (object.size !== 0) {
                    vm.analysisList.push(object);
                }
            }
            // We add at the beginning of the array 'All' only if there are severals checked stages
            else if (developmentStagesCheckedCount > 1) {
                var all = {id: 'ALL', name: 'All'};
                vm.analysisList.unshift(all);
            }
            console.log("analysisList");
            console.log(vm.analysisList);
        }
        
        function buildStage(stageId, stageName) {
            var combined = '';
            var object = {};
            /* SD: The correspondence between expressionType and their values should probably be stored somewhere */
            if (vm.expr_type === 'ALL' || vm.expr_type === 'EXPRESSED') {
                combined = stageName + ', expression type "Present"';
            }
            if (vm.expr_type === 'ALL' || vm.expr_type === 'OVER_EXPRESSED') {
                combined = stageName + ', expression type "Over-/Under-expression"';
            }

            if (combined !== '') {
                object.id = stageId + " ; " + vm.expr_type;
                object.name = combined;
            }
            console.log("buildStage object");
            console.log(object);
            return object;
        }
        /***************************** End View result by stage and expression type **************************/

        /************************************* RECENT JOBS **************************************************/
        vm.showRecentJobsPanel = false;
        console.time("getJobHistory");
        updateRecentJobs();
        console.timeEnd("getJobHistory");

        vm.toggleRecentJobs = function() {

            vm.showRecentJobsPanel = !vm.showRecentJobsPanel

        };

        function updateRecentJobs(){
            vm.recentJobs = bgeejobservice.getJobHistory();
        }

        vm.removeJob = function (job) {
            // Delete from LS
            bgeejobservice.removeJobFromHistory(job);

            // Refresh the Recent job panel
            var hash = job.hash;
            for(var i=0; i < vm.recentJobs.length ; i++) {
                if (vm.recentJobs[i].hash == hash) {
                    vm.recentJobs.splice(i, 1);
                }
            }
        };
        /************************************* END RECENT JOBS **********************************************/

        /**************** Instantiate online help ************/
        vm.onlineHelp = {};
        vm.help = '';

        console.time("getonlineHelp");
        getAllOnlineHelpItems();
        console.timeEnd("getonlineHelp");

        function getAllOnlineHelpItems() {
            //webapp: return helpservice.getOnlineHelp('js/topanat/json/help.json')
            //topanat: return helpservice.getOnlineHelp('json/help.json')
            return helpservice.getOnlineHelp('js/topanat/json/help.json')
                .then(function (data) {
                    vm.onlineHelp = data;
                    showMessage($scope, false);
                });

        }
        /**************** End Instantiate online help ************/

        function getShowHelp() {
            return helpservice.getHelp();
        }

        vm.updateShowHelp = function(flag) {
            helpservice.setHelp(flag);
            vm.showHelp = flag;
        };

        function getAllDataTypes() {
            console.time("getAllDataTypes");
            if(DataTypeFactory.allDataTypes()) {
                // check by default, add checked: true to the model

                vm.data_type = DataTypeFactory.allDataTypes();

                // check every datatype by default
                angular.forEach(vm.data_type.names, function(obj){
                    obj.checked = true;

                });

            } else {
                logger.error('Datatypes not available', 'No Datatypes.');
            }
            console.timeEnd("getAllDataTypes");

        }

        /***************************** End View result by stage and expression type **************************/

        function getAllowedDataTypes(expressiontype) {
            console.time("getAllowedDataTypes");
            // TODO: caching
            console.log("getAllowedDataTypes");
            showMessage($scope, "allowed data types");

            vm.allowedDataTypes = DataTypeFactory.allowedDataTypes(expressiontype);

            console.log("vm.allowedDataTypes");
            console.log(vm.allowedDataTypes);
            console.log("vm.data_type");
            console.log(vm.data_type);

            if(!angular.isArray(vm.allowedDataTypes)) {
                logger.error('No allowed Datatypes available', 'No allowed Datatypes.');

            // by default check all datatype boxes
            } else if(angular.isArray(vm.allowedDataTypes) && !angular.isArray(vm.selectedDataTypes)) {

                console.log("No selected datatypes, check all.");
                vm.selectedDataTypes = vm.allowedDataTypes.slice(0);

            } else {

                console.log("vm.selectedDataTypes");
                console.log(vm.selectedDataTypes);

                // uncheck disabled datatype checkbox
                angular.forEach(vm.data_type.names, function(obj){
                    console.log("obj");
                    console.log(obj);

                    if (vm.selectedDataTypes.indexOf(obj.id) === -1) {
                        obj.checked = false;
                    } else {
                        obj.checked = true;
                    }
                    console.log(obj);

                });

            }

            console.log("vm.selectedDataTypes");
            console.log(vm.selectedDataTypes);
            console.log("vm.data_type after");
            console.log(vm.data_type);
            showMessage($scope, false);
            console.timeEnd("getAllowedDataTypes");
        }

        vm.filterByDataType = function(expr_type) {

            // TODO need to have several expression types at the same time
            vm.expr_type = expr_type;
            getAllowedDataTypes(expr_type);

            vm.message = 'datatypes filtered!';
            console.log("filterByDataType");
            console.log(vm.selectedDataTypes);
            logger.success('Datatype filtering was successful', 'datatype filter OK');
        };

        /************** Result panel Handling ****************/
        function splitSelectedOption(selected) {

            console.log("splitSelectedOption");
            console.log(selected);
            console.log(vm.analysisList);

            var array = [];
            if (typeof selected === 'undefined') {
                return array;
            }
            var matcher = new RegExp('(.+) ; (.+)');
            var match = selected.match(matcher);

            if (typeof match !== 'undefined')
            {
                array.push(match[1]);
                array.push(match[2]);
            }
            return array;
        }

        function getResultNumber (analysisId) {

            console.log("getResultNumber");
            console.log(analysisId);
            console.log(vm.analysisList);

            if (analysisId === 'ALL') {
                console.log("length: " + vm.gridOptions.data.length);
                return vm.gridOptions.data.length;
            }
            else {
                var splitted = splitSelectedOption(analysisId);
                var rows = vm.gridOptionsByAnalysis[splitted[0]][splitted[1]];
                console.log("length: " + rows.length);
                return rows.length;
            }
        }

        vm.getResultByAnalysis = function(analysis) {

            console.log("getResultByAnalysis");
            console.log(analysis);

            vm.isActive = analysis;

            if (analysis === 'ALL') {
                vm.filterByStage = analysis;
                vm.filterbyExpression = analysis;
            }
            else {
                var splitted = splitSelectedOption(analysis);
                vm.filterByStage = splitted[0];
                vm.filterbyExpression = splitted[1];
            }
            console.log(vm.filterByStage);
            console.log(vm.filterbyExpression);
            vm.gridApi.grid.refresh();
        };

        vm.searchResults = function () {
            vm.gridApi.grid.refresh();
        };

        vm.gridOptions = {
            enableFiltering: false,
            enableColumnMenus: false,
            minRowsToShow: vm.nb_rows,
            rowHeight: vm.row_height,
            onRegisterApi: function (gridApi) {
                vm.gridApi = gridApi;
                vm.gridApi.grid.registerRowsProcessor(vm.resultFilter, 200);
            },
            columnDefs: [
                {field: 'anatEntityId', width: "14%", cellTemplate: '<div style="text-align:center;  position: relative;  top: 50%;  -webkit-transform: translateY(-50%);  -ms-transform: translateY(-50%);  transform: translateY(-50%);"><a href="http://purl.obolibrary.org/obo/{{row.entity[col.field].replace(\':\',\'_\')}}" target="_blank" rel="noopener">{{row.entity[col.field]}}</a></div>' },
                {field: 'anatEntityName', width: "34%" },
                {field: 'annotated', width: "8%", type: 'number'},
                {field: 'significant', width: "8%", type: 'number'},
                {field: 'expected', width: "8%", type: 'number'},
                {field: 'foldEnrichment', width: "8%", type: 'number'},
                {field: 'pValue', width: "10%", type: 'number'},
                {field: 'FDR', width: "10%", type: 'number'}
            ]
        };

        vm.getFilteredRows=function(){
            vm.filteredRows=[];
            // vm.gridApi.grid.refresh();
            vm.filteredRows = vm.gridApi.core.getVisibleRows(vm.gridApi.grid);
        };

        function filterByAnalysis() {
            if (vm.filterByStage == '' || vm.filterbyExpression == ''){
                return false;
            }

            return !(vm.filterByStage == 'ALL' || vm.filterbyExpression == 'ALL');

        }

        vm.resultFilter = function (renderableRows) {

            var matcher = null;
            if (filterByAnalysis() == true) {

                var filtered = vm.gridOptionsByAnalysis[vm.filterByStage][vm.filterbyExpression];
                var match = false;
                renderableRows.forEach(function (row) {
                    match = false;
                    for (var i = 0 ; i < filtered.length ; i++) {
                        if (filtered[i] === row.entity) {
                            match = true;
                        }
                    }
                    if (!match) {
                        row.visible = false;
                    }
                });

                if (vm.filterValue != '') {
                    matcher = new RegExp(vm.filterValue, 'i');
                    matchResultsToRegex(renderableRows, matcher);
                }
            }
            else
            {
                if (vm.filterValue != '') {
                    matcher = new RegExp(vm.filterValue, 'i');
                    matchResultsToRegex(renderableRows, matcher);
                }
                else {
                    renderableRows.forEach(function (row) {
                        row.visible = true;
                    });
                }
            }
            return renderableRows;
        };

        function matchResultsToRegex (renderableRows, matcher){

            renderableRows.forEach(function (row) {
                var match = false;
                ['anatEntityId', 'anatEntityName'].forEach(function (field) {
                    if (row.entity[field].match(matcher)) {
                        match = true;
                    }
                });
                if (!match) {
                    row.visible = false;
                }
            });
        }

        function changeNumberOfRowsToShow (number) {

            if (typeof number === 'undefined') {
                number = vm.nbRows.selectedOption.id;
            }

            // Issue 126
            if (number > vm.gridOptions.data.length) {
                number = vm.gridOptions.data.length;
            }

            // number + 1 for the header and + 17 for the scroll footer
            var height = (number + 1) * vm.row_height + 17;
            angular.element(document.getElementsByClassName('grid')[0]).css('height', height + 'px');
        }

        vm.isSuccessfulMessage = function(message) {
            var matcher = new RegExp('Found [0-9]+ records');
            if (typeof message !== 'undefined') {
                if (message.match(matcher)) {
                    return true;
                }
            }
            return false;
        };

        vm.devStagesChecked = function() {
            console.log("vm.devStageChecked");
            if(typeof vm.developmentStages == 'undefined'){ return 1;}
            var checked = vm.getChecked(vm.developmentStages);
            console.log(checked);
            vm.isFormValidDevStages = checked.length ? 'yes' : '';
            return checked.length;
        };

        vm.dataTypesChecked = function() {
            console.log("vm.dataTypesChecked");
            if(typeof vm.data_type == 'undefined'){ return [];}
            var checked = vm.getChecked(vm.data_type.names);
            console.log(checked);
            vm.isFormValidDataTypes = checked.length ? 'yes' : '';
            getCheckedIDs(vm.data_type.names);
            return checked.length;
        };

        vm.getChecked = function(obj){
            console.log("vm.getChecked");
            if(typeof obj == 'undefined'){ return [];}
            return $filter("filter")( obj , {checked:true} );
        };

        function getCheckedIDs(obj, idField){
            console.log("in checkedIDs");
            var checked = vm.getChecked(obj);
            if(!idField){ idField = "id";}

            var c = checked.map(function(c) { return c.id });
            console.log(c);
            return c;
        }

        vm.viewResultsBy = function(stage) {
            logger.info("View by " + stage);
        };

        vm.species_img = function(taxid) {
            if (taxid !== "" && typeof (taxid) !== 'undefined') {
                return GLOBAL_PROPS.getImgURLStart() + 'species/'+ taxid + '_light.jpg';
            } else {
                return "//:0";
            }
        };

        vm.selectBackground = function(value) {
            vm.isBackgroundChecked = value;
            checkFgBg();
        };
        
        vm.selectStage = function(value) {
            if (value === 'checked') {
                // Uncheck stages when click on 'All stages'
                angular.forEach(vm.developmentStages, function(stage, key) {
                    stage.checked = '';
                    vm.isFormValidDevStages = 'yes';
                });
            } else {
                // Check all stages when click on 'Custom stages'
                angular.forEach(vm.developmentStages, function(stage, key) {
                    stage.checked = true;
                });
            }
            vm.isStageChecked = value;
        };

        function checkConsistency()
        {
            if (vm.selected_species === vm.background_species) {
                logger.success("Foreground/background species are identical.");
                // Valid background only of checkFgBG return true
                // See comments and TODOs about the variable isValidBackgroundMessage
                if (vm.isValidBackgroundMessage === '') {
                    vm.isValidBackground = true;
                }
            }
            else {
                logger.error("Foreground and background species differ. You can either change your background or the" +
                " default one will be used.",'',"Error");
                // Issue 60: In case there is a discrepancy btw FG and BG, the BG list should not be reset. The
                // default BG should be selected.
                // BG checked takes precedence over BG list (see sendForm function).
                vm.background_species = '';
                vm.isValidBackground = false;
                vm.isValidBackgroundMessage = 'Species differ between your gene list and your custom background. Please, check your data.';
                // TODO
            }
        }

        function disableForm() {
            vm.formSubmitted = true; /* Shows the image + message panel - locks the form */
            // vm.isAdvancedOptionsChecked = false; /* SD: Close the Advanced option panel */
            vm.showHelp = false; /* SD: Close the help panel - not remembered */
        }

        function getOnlineHelpForItem (topic) {
            angular.forEach(vm.onlineHelp, function(value, key) {
                if (value.topic == topic)
                {
                    vm.help = value.help;
                }
            });
        }

        vm.downloadFilteredResults = function() {

            var fileName = getFileName();
            var textArray = getFilteredRowsAsText();
            var data;

            if (window.navigator.userAgent.indexOf("Safari") != -1 && window.navigator.userAgent.indexOf("Chrome") == -1) {
                data = new Blob(textArray, { type: 'application/octet-stream' });
            }
            else {
                data = new Blob(textArray, { type: 'text/plain;charset=UTF-16LE' });
            }
            FileSaver.saveAs(data, fileName);
        };

        function getFileName() {
            var name = 'topanat-';
            var date = new Date();
            var now = date.toLocaleString().replace(/ /g, '_').replace(',', '');

            if (vm.filterByStage === '' && vm.filterbyExpression === '') {
                name += "all-" + now;
            }
            else {
                name += vm.filterByStage + "-" + vm.filterbyExpression + "-" + now;
            }

            //append the search name if defined
            if (vm.filterValue != '')
            {
                name += '-' + vm.filterValue.replace(/ /g, '_');
            }
            name += '.tsv';
            return name;
        }

        function getFilteredRowsAsText() {
            var stringArray = [];

            // First, the header:
            for (var i = 0; i < vm.gridOptions.columnDefs.length; i++) {
                stringArray.push(vm.gridOptions.columnDefs[i].field + "\t");
            }
            stringArray.push("\n");

            // Then, the result themselves:
            var field;
            angular.forEach(vm.filteredRows, function (row, index) {
                for (var i = 0; i < vm.gridOptions.columnDefs.length; i++) {
                    field = row.entity[vm.gridOptions.columnDefs[i].field];
                    if (typeof field !== 'undefined')
                        stringArray.push(field + "\t");
                }
                stringArray.push("\n");
            });

            return stringArray;
        }
        /************** End Result panel Handling ****************/

        /********************** Action buttons *************************/
        var request = null;

        function checkFgBg(){

            console.log("checkFgBg");

            // if we don't have both gene lists we can't test
            if(!vm.bg_list || !vm.fg_list){
                console.log("fg or bg was missing, not compared");
                return true;
            }

            var lines = vm.fg_list.split('\n');

            var found = true;

            for (var i = 0, len = lines.length; i < len; i++) {
                if(!isIn(lines[i], vm.bg_list)) {
                    console.log(lines[i]+' was not found in background!');
                    found = false;
                    vm.isValidBackground = false;
                    break;
                }
            }
            return found;
        }

        function startNewJob(){

            showMessage($scope, false);
            vm.formSubmitted = false;
            vm.jobDone = false;
            vm.jobStatus = false;
           
            /* Reset the filters of the results */
            vm.filterByStage = '';
            vm.filterbyExpression = '';
            vm.filterValue = '';
            
            //clear URL, otherwise results from a previous analyses might be retrieved,
            //from the hash in the URL.
            //XXX: maybe there is a better way to handle this.
            vm.hash = '';
            vm.resultUrl = '/';
            $location.update_path("/", false);
            //also reinit result table, otherwise, if the new analysis give no results,
            //we will still see the results from the previous analysis
            //XXX: maybe we should reinit the results only when pressing "submit job"?
            //Or when modifying the form?
            vm.filteredRows = [];
            //reinit displayed messages
            vm.message = '';
            //Doing "vm.gridOptions.data = ''" would not work, the grid will keep its pointer
            //to the previous results, see https://github.com/angular-ui/ui-grid/issues/1302
            vm.gridOptions.data.length = 0;
        }

        function cancelJob(){

            showMessage($scope, false);
            if(request){
                request.cancel("User cancellation");
            }
            console.info("Calling cancelJob, jobId = " + vm.jobId);

            bgeejobservice.cancelJob(vm.jobId)
                .then(function (data) {
                    console.log("got data from cancelJob");
                },
                function(data){
                    logger.error("Error canceling job");
                }
            );

            vm.jobDone = true;
            vm.message = "Job stopped by the user";
            vm.gridOptions.data = [];

            if (timer) {
                $interval.cancel(timer);
            }
        }

        /*
         jobProgressStart: 'Processing job, waiting for a job id. Please do not close this window.',
         jobProgress: 'Job is running - Job ID: ',
         jobProgressBookmark: 'After bookmarking this page it\'s safe to close this window.'
         */

        vm.postForm = function() {

            disableForm();

            vm.jobDone = false; /* When true -> Show the "New job" button */
            vm.jobId   = -1;    /* When > 0 -> Enable the cancel button */

            vm.message = lang.jobProgressStart;
            vm.messageSeverity = "warning";
            $timeout(function(){
                $window.document.getElementById('resultContainer').scrollIntoView();
            }, 500);
            //logger.info('Posting...');
            //$timeout(vm.sendForm, 3000);
            vm.sendForm();

        };


        vm.sendForm = function() {

            // BG checked takes precedence over BG list.
            vm.bg_list = vm.isBackgroundChecked === 'checked' ? '' : vm.bg_list;

            // Stages checked takes precedence over stages list.
            var devStageIDs = vm.isStageChecked === 'checked' ? '' : getCheckedIDs(vm.developmentStages);

            //
            //expr_type: vm.expr_type,
            var formData = {
                page: "top_anat",
                fg_list: vm.fg_list,
                bg_list: vm.bg_list,

                expr_type: vm.expr_type,
                data_qual: vm.data_qual,
                data_type: getCheckedIDs(vm.data_type.names),
                stage_id: devStageIDs,
                decorr_type: vm.decorr_type,
                node_size: vm.node_size,
                fdr_thr: vm.fdr_thr,
                p_value_thr: vm.p_value_thr,
                nb_node: vm.nb_node,
                job_title: vm.job_title,
                job_creation_date: new Date().toLocaleString(), 
                email: vm.email,
                submitted: true,
                display_type: "json",
                action: "submit_job",
                ajax: 1
            };

            vm.formSubmitted = true;

            request = bgeedataservice.postGeneData(configuration.mockupUrl, formData);

            request.promise.then(function (data) {

                    console.log("submit response");
                    console.log(data.data.data);
                    // move this into function
                    vm.hash = data.data.data.jobResponse.data;
                    //if the results already exist, a jobId = 0 is returned
                    if(data.data.data.jobResponse.jobId == 0 && data.data.data.jobResponse.data){
                        //XXX: Should we rather send the results immediately if they already exist?
                        //I thought it was more convenient for you, but it is easy to change.
                        //See same remarks when retrieving results from a 'jab completed' response.
                        console.log("Results already exist.");
                        vm.jobDone = true;
                        // Do not remove the trailing slash, see comments in topanat.js
                        vm.resultUrl = '/result/'+vm.hash+'/';
                        getResults();

                    } else if(data.data.data.jobResponse.jobId && data.data.data.jobResponse.data) {

                        console.log("Job launched.");
                        vm.jobId = data.data.data.jobResponse.jobId;
                        // Do not remove the trailing slash, see comments in topanat.js
                        vm.resultUrl = '/result/'+vm.hash+'/'+vm.jobId+'/';

                        vm.jobStatus = data.data.data.jobResponse.jobStatus;
                        logger.success('TopAnat request successful', 'TopAnat ok');
                        vm.message = lang.jobProgressBookmark+"<br/>"+lang.jobProgress+vm.jobId;
                        vm.jobDone = false;

                        console.log("calling checkJobStatus");
                        checkJobStatus();

                    } else {
                        //Don't know how you manage illegal states ;)
                        console.log("Error");
                        request.abort();
                    }

                    //$location.update_path(vm.resultUrl, false);
                    //console.log("processing post resultUrl: "+vm.resultUrl);

                    console.log("end promise");

                },

                // do we need to pass data?
                function(data){
                    // Do not consider the user cancellation as an error, 
                	// or the user submitting too many jobs
                    var matcherJobStopped = new RegExp('Job stopped by the user');
                    var cancel = 0;
                    if (data.data.code == 429) {
                        vm.message = data.data.message 
                        + " Please note that we also propose a R package for performing programmatic TopAnat analyses, "
                        + "see <a href='https://www.bioconductor.org/packages/BgeeDB/' target='_blank' rel='noopener'>"
                        + "BgeeDB Bioconductor package</a>";
                    	cancel = 1;
                    } else if (!vm.message.match(matcherJobStopped)) {
                        console.log('error from bgeedataservice');
                        console.log(data);
                        logger.error('TopAnat request not successful', 'TopAnat fail');
                        vm.message = 'TopAnat request failed. Response from server: '+data.data.message;
                        cancel = 1;
                    }
                    if (cancel) {
                    	vm.jobDone = true;
                        vm.gridOptions.data = [];

                        if (timer) {
                            $interval.cancel(timer);
                        }
                    }
                });
        };

        var timer = null;
        var statuscounter = 0;

        function checkJobStatus(){

            console.log("checkjobstatus of "+vm.jobId+" -> "+vm.resultUrl);

            if(!vm.jobId) return "EXIT";
            vm.jobDone = false;

            // set result url without reloading the page
            //FB: we store the current timestamp because I couldn't find something that worked
            //to prevent initialization to be triggered by location change, even with
            //this 'update_path' function. See topanat.js for explanations about use of this timestamp.
            //The 'update_path' function seems better anyway, at least when the intialization
            //is triggered, it sees the proper current hash value, with 'path' function
            //it sees the hash used at page landing.
            window.localStorage['topAnatRouteChangeWithoutReloadTimestamp'] = new Date().getTime();
            $location.update_path(vm.resultUrl, false);
            //$location.path(vm.resultUrl, false);

            // check for status every 10 secs
            timer = $interval(function(){
                console.info("Calling jobstatus, jobId = " + vm.jobId);

                bgeejobservice.getJobStatus(vm.hash, vm.jobId, false)

                    .then(function (data) {
                        console.log("got data from jobstatus");
                        console.log(data);

                        // move this into function
                        //if(data.status == "DONE" && data.result && !vm.jobDone){
                        if(data.data.jobResponse.jobStatus == "UNDEFINED" && !vm.jobDone){

                            vm.jobDone = true;
                            $interval.cancel(timer);
                            //XXX: again, should we rather send the results immediately
                            //in the job tracking response?
                            getResults();

                        } else {

                            statuscounter = statuscounter + 1;
                            vm.jobStatus = data.status;
                            vm.message = lang.jobProgressBookmark+"<br/>"+lang.jobProgress+vm.jobId;
                            //scroll to result container with information about job,
                            //otherwise it is possible to miss it.
                            if (statuscounter == 1) {
                                $timeout(function(){
                                    $window.document.getElementById('resultContainer').scrollIntoView();
                                }, 500);
                            }
                        }
                    },

                    function(data){
                        logger.error("Error getting jobstatus");
                        $interval.cancel(timer);
                    }
                );

            }, 10000);
        }

        function getResults() {
            showMessage($scope, "results");

            bgeejobservice.getJobResult(vm.hash)
                .then(function (data) {
                    displayResults(data);
                    storeJob();
                    updateRecentJobs();
                },

                function(data){
                    showMessage($scope, false);
                    if (data.message) {

                        logger.error('Getting result failed. error: '+data.message, 'TopAnat fail');
                        vm.message = data.message;
                    } else {
                        logger.error('Getting result failed. Unknown error.', 'TopAnat fail');
                        vm.message = 'Getting result failed. Unknown error.';
                    }
                });

        }

        function storeJob(){
            // store the hash in the local storage
            bgeejobservice.storeJobData(vm.hash, vm.selected_species, vm.selected_taxid, vm.job_title);
        }

        function parseResults(data) {

            console.log("parsing results");

            /* Structure of the result data (no rp, no gene info):
             - code
             - status
             - data []
             - topAnatResults []
             - zipFile
             - devStageId
             - callType
             - results []
             - anatEntityId
             - anatEntityName
             - annotated
             - significant
             - expected
             - foldEnrichment
             - pValue
             - FDR
             * */

            // for sorting all results from all analyses by p-values, to get correct ordering
            // when displaying all results
            var allResultArr = [];
            vm.gridOptionsByAnalysis = [];

            // Issue #99:
            // Array of URLs used to get the job zip files
            vm.zipFileByAnalysis = [];
            // First add the zip of zips to the array
            //vm.zipFileByAnalysis['ALL'] = configuration.mockupUrl + '/?page=top_anat&action=download&data=' + vm.hash;
            vm.zipFileByAnalysis['ALL'] = '?page=top_anat&action=download&data=' + vm.hash;
            for (var i = 0; i < data.data.topAnatResults.length; i++) {
                var devStageId = data.data.topAnatResults[i].devStageId;
                devStageId = !devStageId ? 'ALL-STAGES' : devStageId;
                var callType = data.data.topAnatResults[i].callType;

                // we could probably use the same logic as for the zip file and put a composed key
                // no time for now
                vm.gridOptionsByAnalysis[devStageId] = [];
                vm.gridOptionsByAnalysis[devStageId][callType] = data.data.topAnatResults[i].results;

                Array.prototype.push.apply(allResultArr, data.data.topAnatResults[i].results);

                // zip files by analysis
                //vm.zipFileByAnalysis[devStageId + " ; " + callType] = configuration.mockupUrl + data.data.topAnatResults[i].zipFile;
                vm.zipFileByAnalysis[devStageId + " ; " + callType] = data.data.topAnatResults[i].zipFile;
            }

            //sort all results by p-val and FDR
            allResultArr.sort(function(a, b){
                if (a.pValue !== b.pValue) {
                    return a.pValue - b.pValue;
                }
                if (a.FDR !== b.FDR) {
                    return a.FDR - b.FDR;
                }
                return 0;
            });

            var allResultCount = allResultArr.length;
            for (var i = 0; i < allResultCount; i++) {

                var grid = vm.gridOptions.data;
                if (typeof grid !== 'undefined') {

                    vm.gridOptions.data = grid.concat(allResultArr[i]); // show all
                }
                else {
                    vm.gridOptions.data = allResultArr[i];
                }
            }
            vm.getFilteredRows(); // In order to get the total number of rows before filtering of results
            vm.jobDone = true; // in order to display the result array

            // Issue 126: Change the default value of the nb of rows to show
            // if the number of results is less than the default value
            changeNumberOfRowsToShow(vm.nb_rows);
        }

        function displayResults(result) {

            showMessage($scope, "results");
            console.log("displayResults");
            console.debug(result);
            vm.jobStatus = "DONE";

            getCombinedDevStageAndExpressionType();

            //vm.gridOptions.data = result.data.topAnatResults[0].results;
            //vm.jobStatus = result.status;
            parseResults(result);

            //count number of result over all analyses
            var analysisCount = result.data.topAnatResults.length;
            var analysisWithResults = 0;
            var resultCount = 0;
            for (var i = 0; i < analysisCount; i++) {
                var iterateCount = result.data.topAnatResults[i].results.length;
                console.log("Number of results in analysis " + (i + 1) + ": " + iterateCount);
                resultCount += iterateCount;
                if (iterateCount > 0) {
                    analysisWithResults++;
                }
            }

            showMessage($scope, false);

            //TODO: the 'formSubmitted = true' is a hack to display the message when retrieving
            //already existing results, and not only following a form submission, this should be improved
            vm.formSubmitted = true;
            vm.message = 'TopAnat request successful. Found ' + resultCount + ' records, from '
                + analysisWithResults + (analysisWithResults > 1? " analyses": " analysis")
                + ' with results, over '
                + analysisCount + (analysisCount > 1? " analyses": " analysis") + ' launched.';
            vm.messageSeverity = "success";
            console.log("Message generated from results: " + vm.message);

            // Do not remove the trailing slash, see comments in topanat.js
            vm.resultUrl = '/result/' + vm.hash + "/";
            console.log("ready resultUrl: " + vm.resultUrl + " - current path: " + $location.path());

            if($location.path() !== vm.resultUrl) {
                console.info("updating path...");
                //set result url without reloading the page
                //FB: we store the current timestamp because I couldn't find something that worked
                //to prevent initialization to be triggered by location change, even with
                //this 'update_path' function. See topanat.js for explanations about use of this timestamp.
                //The 'update_path' function seems better anyway, at least when the intialization
                //is triggered, it sees the proper current hash value, with 'path' function
                //it sees the hash used at page landing.
                window.localStorage['topAnatRouteChangeWithoutReloadTimestamp'] = new Date().getTime();
                $location.update_path(vm.resultUrl, false);
            }

            showMessage($scope, false);
            vm.jobDone = vm.jobStatus;

            $timeout(function(){
                $window.document.getElementById('resultContainer').scrollIntoView();
            }, 500);

        }

        /********************** End Action buttons *********************/

        function getDevStages(type, list, responseData) {

            showMessage($scope, "developmental and life stages");

            console.log("Comparing fg and bg");
            if(!checkFgBg()){
                // TODO should BG be cleared and bgee data activated? (might be confusing to the user)
                console.log("fg and bg did not match");
                // fg must be in bg
                logger.error("Gene list contains genes not found in background genes");

                // Issue 112: same behavior as in issue 60
                vm.background_species = '';
                vm.isBackgroundChecked = 'checked';
                vm.isValidBackgroundMessage = 'One or more genes not found in the background. Please, check your data.';

                // See comment about isValidBackgroundMessage variable at the begining of the code.
                // TODO refactor and make this work slightly better
                // return false;
            }
            else {
                vm.isValidBackgroundMessage = '';
            }

            console.log("getting dev stages");
            vm.showDevStageError = false;

            if(list == ""){ return false;}
            if (type && list) {

                var data = {
                    page: 'top_anat',
                    action: 'gene_validation',
                    display_type: 'json',
                    ajax: 1
                };

                if(type == "fg"){
                    data['fg_list'] = list;
                    //For correctly hiding the gene list message and species image
                    //and displaying the loading message
                    vm.selected_species = '';
                    vm.selected_taxid = '';
                    vm.geneValidationMessage = '';
                    vm.geneModalMessage = '';
                } else {
                    data['bg_list'] = list;
                    //For correctly hiding the gene list message and displaying the loading message
                    vm.background_species = '';
                    vm.bgGeneValidationMessage = '';
                    vm.bgGeneModalMessage = '';
                    vm.isValidBackgroundMessage = '';
                }

                console.log("data to getdevstages with type: " + type);
                console.log(data);

                if (!responseData) {
                    return bgeedataservice.getDevStages(configuration.mockupUrl, data)
                        .then(function (data) {
                            thenGetDevStages(data, type);
                        },

                        function(data){
                            failGetDevStages(data, type);
                        });
                } else {
                    //In that case, we don't need to call bgeedataservice.getDevStages,
                    //All the information needed is already present in the response
                    thenGetDevStages(responseData, type);
                }
            }

            //showMessage($scope, false);

        }

        function thenGetDevStages(data, type) {
            handleDevStages(data, type);
            getAllDataTypes();
            vm.allowedDataTypes = getAllowedDataTypes(vm.expr_type);
        }

        function failGetDevStages(data, type) {
            showMessage($scope, false);
            console.info("could not get result");
            console.info(data);

            // Issue 117: I don't see why the species for FG should
            // be reset when BG is incorrect.
            if (type == 'fg') {
                vm.selected_species = '';
                vm.isValidSpecies = false;
            }
            else
            {
                vm.background_species = '';
                vm.isBackgroundChecked = 'checked';
                vm.isValidBackground = false;
                vm.isValidBackgroundMessage = 'Error with your custom background. Please, check your data.';
            }

            if (typeof data.message !== 'undefined') {
                logger.error('Getting result failed. error: ' + data.message, 'TopAnat fail');
                vm.message = data.message;
            } else {
                if(data.data.message !== 'undefined'){
                    logger.error(data.data.message, 'TopAnat fail');
                    vm.message = data.data.message;
                    vm.showDevStageError = data.data.message;
                } else {
                    logger.error('Getting development stages failed. Unknown error.', 'TopAnat fail');
                    vm.message = 'Getting development stages failed. Unknown error.';
                    vm.showDevStageError = 'Getting development stages failed. Unknown error.';
                }

            }
        }

        function parseDataForMessage(data, fgList) {

            if (!data || !data.data || fgList && !data.data.fg_list || !fgList && !data.data.bg_list) {
                if (fgList) {
                    vm.geneValidationMessage = '';
                    vm.geneModalMessage = '';
                } else {
                    vm.bgGeneValidationMessage = '';
                    vm.bgGeneModalMessage = '';
                }
                console.log('no gene message');
                return;
            }

            var listInfo = data.data.fg_list;
            console.log(data.data.fg_list);
            if (!fgList) {
                console.log('Loading bg_list info');
                listInfo = data.data.bg_list;
            }
            console.log('listInfo: ');
            console.log(listInfo);
            if (!listInfo ||
                    !listInfo.geneCount || !Object.keys(listInfo.geneCount).length ||
                    !listInfo.detectedSpecies || !Object.keys(listInfo.detectedSpecies).length) {
                if (fgList) {
                    vm.geneValidationMessage = '';
                    vm.geneModalMessage = '';
                } else {
                    vm.bgGeneValidationMessage = '';
                    vm.bgGeneModalMessage = '';
                }
                console.log('no gene list info');
                return;
            }

            var lines = [];
            if (fgList && vm.fg_list) {
                lines = vm.fg_list.split('\n');
            } else if (!fgList && vm.bg_list) {
                lines = vm.bg_list.split('\n');
            }
            var listLength = 0;
            for (var i = 0; i < lines.length; i++) {
                if (lines[i]) {
                    listLength++;
                }
            }
            if (!fgList && listLength) {
                vm.isBackgroundChecked = '';
            }
            var modalMessage = '';
            if (listInfo.selectedSpecies && listInfo.detectedSpecies[listInfo.selectedSpecies]) {
                var species = listInfo.detectedSpecies[listInfo.selectedSpecies];
                var geneCount = listInfo.geneCount[listInfo.selectedSpecies];
                console.log('Selected species and gene count: ' + species + ' - ' + geneCount);

                var message = listLength + ' ID' + (listLength > 1? 's': '') + ' provided, ' +
                    geneCount + ' unique gene' + (geneCount > 1? 's': '') + ' found in ' + species['name'];
                if (fgList) {
                    vm.geneValidationMessage = message;
                } else {
                    vm.bgGeneValidationMessage = message;
                }

                modalMessage += 'Selected species: <i>' + species['genus'] + " " +
                    species['speciesName'] + '</i>' +
                    (geneCount? ', ' + geneCount + ' unique gene' + (geneCount > 1? 's': '') +
                         ' identified in Bgee': '');
            }
            if (Object.keys(listInfo.geneCount).length <= 1) {
                console.log("No need for extra gene list info, modalMessage not needed, validationMessage done");
                return;
            }
            var otherSpecies = false;
            var undetermined = false;
            angular.forEach(listInfo.geneCount, function(value, key) {
                if (key == -1) {
                    console.log('Undetermined found');
                    undetermined = true;
                } else if (key && (!listInfo.selectedSpecies || key != listInfo.selectedSpecies) &&
                        listInfo.detectedSpecies[key]) {
                    console.log('Other species found');
                    otherSpecies = true;
                }
            });
            if (otherSpecies) {
                //we put the species count objects in an Array to order them
                //per descending number of genes
                var orderedCount = [];
                angular.forEach(listInfo.geneCount, function(value, key) {
                    orderedCount.push( { key: key, value: value } );
                });
                orderedCount.sort(function(a, b) {
                    return b.value - a.value;
                });
                modalMessage += "\n" + '<br />Other species detected in ID list: ' + "\n" + '<ul>';
                for (var i = 0; i < orderedCount.length; i++) {
                    if ((!listInfo.selectedSpecies || orderedCount[i].key != listInfo.selectedSpecies) &&
                            listInfo.detectedSpecies[orderedCount[i].key]) {
                        var species = listInfo.detectedSpecies[orderedCount[i].key];
                        var geneCount = orderedCount[i].value;
                        console.log('Other species and gene count: ' + species + ' - ' + geneCount);
                        modalMessage += '<li><i>' + species['genus'] + " " +
                        species['speciesName'] + '</i>: ' +
                        (geneCount? geneCount + ' gene' + (geneCount > 1? 's': ''):
                            '0 gene') + ' identified</li>';
                    }
                }
                modalMessage += '</ul>';
            }
            if (undetermined && listInfo.geneCount[-1]) {
                modalMessage += "\n" + '<br />ID' + (listInfo.geneCount[-1] > 1? 's': '') +
                    ' not identified: ' + listInfo.geneCount[-1];
            }
            if (otherSpecies && listInfo.notInSelectedSpeciesGeneIds &&
                    listInfo.notInSelectedSpeciesGeneIds.length) {
                modalMessage += "\n" + '<br />ID' +
                        (listInfo.notInSelectedSpeciesGeneIds.length > 1? 's': '') +
                        ' in other species: ' + "\n" + '<ul>';
                for (var i = 0; i < 10 && i < listInfo.notInSelectedSpeciesGeneIds.length; i++) {
                    modalMessage += '<li>' + listInfo.notInSelectedSpeciesGeneIds[i] + '</li>';
                }
                if (listInfo.notInSelectedSpeciesGeneIds.length > 10) {
                        modalMessage += '<li>...</li>';
                }
                modalMessage += '</ul>';
            }
            if (undetermined && listInfo.undeterminedGeneIds &&
                    listInfo.undeterminedGeneIds.length) {
                modalMessage += "\n" + '<br />ID' +
                (listInfo.undeterminedGeneIds.length > 1? 's': '') +
                ' not identified: ' + "\n" + '<ul>';
                for (var i = 0; i < 10 && i < listInfo.undeterminedGeneIds.length; i++) {
                        modalMessage += '<li>' + listInfo.undeterminedGeneIds[i] + '</li>';
                }
                if (listInfo.undeterminedGeneIds.length > 10) {
                    modalMessage += '<li>...</li>';
                }
                modalMessage += '</ul>';
            }

            console.log('Gene modal message: ' + modalMessage);
            if (fgList) {
                vm.geneModalMessage = modalMessage;
            } else {
                vm.bgGeneModalMessage = modalMessage;
            }
        }

        /*function getNbDetectedSpecies(data, type) {
            var species = type === 'fg_list' ? data.data.fg_list.detectedSpecies : data.data.bg_list.detectedSpecies;
            var number = Object.keys(species).length;
            return number;
        }*/

        function getGeneCount(data, type) {


            var geneCountObj = type === 'fg_list' ? data.data.fg_list.geneCount : data.data.bg_list.geneCount;
            var count = 0;
            angular.forEach(geneCountObj, function(value, key) {
                if (key == vm.background_selected_taxid) {
                    count = value;
                }
            });
            return count;
        }

        function mapIdtoName(data, type){

            var speciesName = "Not defined";
            // map to name
            var species = type === 'fg_list' ? data.data.fg_list.detectedSpecies : data.data.bg_list.detectedSpecies;
            var taxid = type === 'fg_list' ? vm.selected_taxid : vm.background_selected_taxid;

            angular.forEach(species, function(sp, key) {

                if(sp.id == taxid){
                    speciesName = sp.name.toString();
                }
            });
            return speciesName;
        }

        // Probably needs refactoring and renaming!
        // TODO: separate btw species and dev stages
        function handleDevStages(data, type){

            // Should pop up only when the gene list is checked
            // not for the background gene list's checking...
            if (type === 'fg') {

                // species id
                vm.selected_taxid = data.data.fg_list.selectedSpecies;

                if (typeof vm.selected_taxid === 'undefined') {
                    logger.error('No species associated to your gene list. Please check your data.');
                    vm.selected_taxid = '';
                    vm.selected_species = 'None';
                    vm.isValidSpecies = false;
                    vm.geneValidationMessage = '';
                }
                else {
                    vm.selected_species = mapIdtoName(data, type + "_list");
                    console.log("selected species: "+vm.selected_species);
                    vm.isValidSpecies = true;
                    parseDataForMessage(data, true);
                    //getNbDetectedSpecies(data, type + "_list") > 1 ? vm.geneValidationMessage = parseMessage(data.message) : vm.geneValidationMessage = '';
                }

                console.log(data.data.fg_list.stages);
                var stages = [];
                var newStageChecked = 'checked';
                angular.forEach(data.data.fg_list.stages, function(devStage, key){

                    // do we already have something from server
                    var isChecked = false;
                    if (vm.stage_id && vm.stage_id.indexOf(devStage.id) !== -1) {
                        isChecked = true;
                        newStageChecked = '';
                    }
                    // or are we updating a gene list and there was already a stage selection
                    else if (vm.developmentStages && vm.developmentStages.length) {
                        angular.forEach(vm.developmentStages, function(currentStage, currentKey){
                            if (currentStage.id === devStage.id && currentStage.checked) {
                                isChecked = true;
                                if (vm.isStageChecked === '') {
                                    newStageChecked = '';
                                }
                            }
                        });
                    }

                    stages.push({
                        name : devStage.name,
                        id : devStage.id,
                        checked : isChecked
                    });

                });

                //We used the stage_ids retrieved from response once already,
                //now we can delete them so that the form can be modified gracefully
                vm.stage_id = [];
                vm.isStageChecked = newStageChecked;
                vm.developmentStages = angular.copy(stages);
                if (vm.isStageChecked === 'checked') {
                    vm.isFormValidDevStages = 'yes';
                }
                if (vm.isBackgroundChecked === '' && vm.bg_list !== '') {
                    checkConsistency();
                }

            } else if (type === 'bg') {

                vm.background_selected_taxid = data.data.bg_list.selectedSpecies;
                vm.background_species = mapIdtoName(data, type + "_list");

                // Issue 108: The number of nodes cannot be bigger than the
                // number of bg genes submitted
                vm.max_node_size = getGeneCount(data, type + "_list");
                if ((vm.max_node_size > 0 && vm.max_node_size < vm.node_size) || vm.node_size == 0) {
                	vm.node_size = vm.max_node_size;
                	vm.isAdvancedOptionsChecked = 'checked';
                	logger.info(vm.node_size + " valid genes in custom background. Updating the node size.");
                }
                
                checkConsistency();
                parseDataForMessage(data, false);
            }
        }

        vm.uploader = new FileUploader({ url: configuration.mockupUrl, autoUpload: true});
        vm.uploader.queue = [];
        vm.uploader.onCompleteAll = function() {
            console.info(vm.fileType+" upload complete!");
            console.log(vm.uploader.queue);
        };

        vm.uploader.onAfterAddingFile = function(fileItem) {
            fileItem.formData.push({filetype: vm.fileType});
            console.info('onAfterAddingFile fileItem', fileItem);
            vm.fq[vm.fileType][fileItem.file.name] = true;
            vm.showUploaded[vm.fileType] = false;
            vm.hideq[vm.fileType] = false;
            vm.uploadError[vm.fileType] = false;
            console.info('onAfterAddingFile queue', vm.uploader.queue);
            vm.fileitem[vm.fileType].name = fileItem.file.name;
            console.log(vm.fileitem);
            console.log(vm.uploader.queue);
        };

        vm.uploader.onCompleteItem = function(fileItem, response, status, headers) {

            console.info('onCompleteItem', fileItem, response, status, headers);
            console.log("response from upload");
            console.log(response);

            if(response.statusCode == "0"){
                vm.uploadError[vm.fileType] = false;
                vm.showUploaded[vm.fileType] = true;

                console.info(vm.fileitem['fg'].name + "was uploaded");
                console.info(vm.showUploaded[vm.fileType]);

                handleDevStages(response, vm.fileType);

                // clear the progress queue after 4 sec
                $timeout(function(){

                    vm.fq[vm.fileType][fileItem.file.name] = false;
                    vm.hideq[vm.fileType] = true;

                    //vm.uploader.queue = []
                    vm.uploader.clearQueue();
                }, 4000);
                logger.info("File <strong>"+fileItem.file.name+"</strong> successfully uploaded");

            } else {

                if(response.statusCode != "0" && response.msg){
                    vm.processUploadError(response.msg);
                } else {
                    logger.error("Unknown file upload error");
                }



            }

            vm.fileitem.status = response.statusCode;
        };

        vm.processUploadError = function(message){

            vm.uploadError[vm.fileType] = true;
            logger.error("File upload error: "+message);

        };

        $scope.trusted = {};
        $scope.to_trusted = function(html_code) {
            return $scope.trusted[html_code] || ($scope.trusted[html_code] = $sce.trustAsHtml(html_code));
        };

    }

    function isIn(str, str_list) {
        return (str_list.indexOf(str) !== -1);
    }

    function shouldOpenAdvancedOptions(vm, configuration){
        console.log("shouldOpenAdvancedOptions");
        console.log(configuration);
        console.log(vm);
        if((typeof vm.data_qual !== 'undefined' && vm.data_qual !== null &&
        		vm.data_qual.toUpperCase() !== configuration.data_qual.toUpperCase()) ||
            vm.decorr_type != configuration.decorr_type ||
            vm.node_size != configuration.node_size ||
            vm.nb_node != configuration.nb_node ||
            vm.fdr_thr != configuration.fdr_thr ||
            vm.p_value_thr != configuration.p_value_thr ||
            vm.isStageChecked !== 'checked')
        {
            vm.isAdvancedOptionsChecked = true;
        }
    }

    function showMessage($scope, msg){

        if(msg){
            console.log("loading message: "+msg);
            $scope.loadMsg = ' '+msg;
            $scope.loading = true;
            return false;
        }

        console.log("canceling loading message");
        $scope.loading = false;
        $scope.loadMsg = '';
        return false;
    }
})();
