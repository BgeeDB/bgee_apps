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
        .controller('MainCtrl', MainCtrl, ['ui.bootstrap', 'angularFileUpload', 'ngLocationUpdate', 'ngFileSaver']);

    MainCtrl.$inject = ['$scope', '$sce', 'bgeedataservice', 'bgeejobservice', 'helpservice', 'DataTypeFactory', 'configuration', 'logger', 'FileUploader', '$timeout', '$location', '$interval', 'lang', 'jobStatus', '$filter', 'FileSaver', 'Blob', '$route', '$window'];

    function MainCtrl ($scope, $sce, bgeedataservice, bgeejobservice, helpservice, DataTypeFactory, configuration, logger, FileUploader, $timeout, $location, $interval, lang, jobStatus, $filter, FileSaver, Blob, $route, $window) {

        var vm = this;

        //showMessage($scope, "bgee app");

        $scope.$on('$routeChangeStart', function() {
            console.log("route changed");
            showMessage($scope, "bgee app");
        });
        $scope.$on('$routeChangeSuccess', function() {
            showMessage($scope, false);
        });
        $scope.$on('$routeChangeError', function() {
            showMessage($scope, false);
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
        vm.selectedDevelopmentStages = [];
        vm.developmentStages = [];

        vm.isBackgroundChecked = 'checked';

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

                // we use hash instead od data mainly to make it clear what data it is
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

                vm[key] = val;

                console.debug("vm."+key+" = "+vm[key]);

            });

            angular.forEach(jobStatus.data.jobResponse, function(val, key) {

                // we use hash instead od data mainly to make it clear what data it is
                if(key == "data"){
                    vm['hash'] = val;
                }

                vm[key] = val;

                console.debug("vm."+key+" = "+vm[key]);

            });

            var gotDevStages = getDevStages('fg', vm.fg_list);


            showMessage($scope, "development stages");

            gotDevStages.then(function(){
                // Issue #109
                // In case we load an existing result page, the background selected species
                // and checked flag were not assigned any value.
                // We assume that, because the job exists, the bg and fg species are the same.
                // We don't want to run another call to the server or to parse the server's response.
                if (vm.bg_list) {
                    vm.background_species = vm.selected_species;
                    vm.background_selected_taxid = vm.selected_taxid;
                    vm.isBackgroundChecked = '';
                    vm.isValidBackground = true;
                    vm.isValidBackgroundMessage = '';
                    // Issue 108
                    vm.max_node_size = getGeneCount(jobStatus, 'bg_list');
                }


                $timeout(function(){

                    console.log("get, display or submit");
                    console.log(jobStatus);

                    // jobId check is a magic number we use to get used parameters when result is not available
                    // prevents us getting results again when we already know it's missing

                    if(vm.jobId != 999999 && (typeof jobStatus.code !== 'undefined' &&
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
                        if(vm.jobId == 999999 || (typeof jobStatus.requestParameters !== 'undefined' && jobStatus.code == 400)) {
                            console.log("no result or job found for the parameters, resubmit");
                            logger.info("Job result was missing, resubmitting job");
                            vm.sendForm();
                        } else {
                            console.log("job is not done, checkjobstatus");
                            // This fixes issue #111
                            vm.resultUrl = '/result/'+vm.hash+'/'+vm.jobId;
                            vm.formSubmitted = true;
                            checkJobStatus();
                        }

                    }
                }, 100);

            });

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

            var all = {id: 'ALL', name: 'All'};
            vm.analysisList.push(all);

            angular.forEach(vm.developmentStages, function(stage, key) {
                var combined = '';
                var object = {};
                if (stage.checked) {
                    /* SD: The correspondence between expressionType and their values should probably be stored somewhere */
                    if (vm.expr_type == 'ALL' || vm.expr_type == 'EXPRESSED') {
                        combined = stage.name + ', expression type "Present"';
                    }
                    if (vm.expr_type == 'ALL' || vm.expr_type == 'OVER_EXPRESSED') {
                        combined = stage.name + ', expression type "Over-/Under-expression"';
                    }

                    if (combined != '') {
                        object = {};
                        object.id = stage.id + " ; " + vm.expr_type;
                        object.name = combined;
                        vm.analysisList.push(object);
                    }
                }
            });
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

            var array = [];
            var matcher = new RegExp('(.+) ; (.+)');
            var match = selected.match(matcher);

            if (typeof match !== 'undefined')
            {
                array.push(match[1]);
                array.push(match[2]);
            }
            return array;
        }

        function getResultNumber (analysis) {

            if (analysis === 'ALL') {
                return vm.gridOptions.data.length;
            }
            else {
                var splitted = splitSelectedOption(analysis);
                var rows = vm.gridOptionsByAnalysis[splitted[0]][splitted[1]];
                return rows.length;
            }
        }

        vm.getResultByAnalysis = function(analysis) {

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
                {field: 'anatEntityId', width: "14%", cellTemplate: '<div style="text-align:center;  position: relative;  top: 50%;  -webkit-transform: translateY(-50%);  -ms-transform: translateY(-50%);  transform: translateY(-50%);"><a href="http://purl.obolibrary.org/obo/{{row.entity[col.field].replace(\':\',\'_\')}}" target="_blank">{{row.entity[col.field]}}</a></div>' },
                {field: 'anatEntityName', width: "34%" },
                {field: 'annotated', width: "8%" },
                {field: 'significant', width: "8%" },
                {field: 'expected', width: "8%" },
                {field: 'foldEnrichment', width: "8%" },
                {field: 'pValue', width: "10%" },
                {field: 'FDR', width: "10%" }
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

            if (number == undefined) {
                number = vm.nbRows.selectedOption.id;
            }
            // +1 for the header
            var height = (number +1) * vm.row_height;
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
            if(typeof vm.developmentStages == 'undefined'){ return [];}
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
                return 'http://bgee.org/img/species/'+ taxid + '_light.jpg';
            } else {
                return "//:0";
            }
        };

        vm.selectBackground = function(value) {
            vm.isBackgroundChecked = value;
            checkFgBg();
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
                vm.isBackgroundChecked = 'checked';
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
            vm.resultUrl = '/';
            $location.update_path("/", false);
            //also reinit resul table, otherwise, if the new analysis give no results,
            //we will still see the results from the previous analysis
            //XXX: maybe we should reinit the results only when pressing "submit job"?
            //Or when modifying the form?
            vm.filteredRows = [];
            //Doing "vm.gridOptions.data = ''" would not work, the grid will keep its pointer
            //to the previous results, see https://github.com/angular-ui/ui-grid/issues/1302
            vm.gridOptions.data.length = 0;
        }

        function cancelJob(){

            showMessage($scope, false);
            if(request){
                request.cancel("User cancellation");
            }

            vm.jobDone = true;
            vm.message = "Job stopped by the user";
            vm.gridOptions.data = [];

            if (timer) {
                $interval.cancel(timer);
            }

            statuscounter = 0;

        }

        /*
         jobProgressStart: 'Processing job, waiting for a job id. Please do not close this window.',
         jobProgress: 'Job has been submitted as jobid: ',
         jobProgressBookmark: 'After bookmarking this page it\'s safe to close this window.'
         */

        vm.postForm = function() {

            disableForm();

            vm.jobDone = false; /* When true -> Show the "New job" button */

            vm.message = lang.jobProgressStart;
            vm.messageSeverity = "warning";
            $window.document.getElementById('resultContainer').scrollIntoView();
            //logger.info('Posting...');
            $timeout(vm.sendForm, 3000);

        };


        vm.sendForm = function() {

            // BG checked takes precedence over BG list.
            vm.bg_list = vm.isBackgroundChecked == 'checked' ? '' : vm.bg_list;
            //
            //expr_type: vm.expr_type,
            var formData = {
                page: "top_anat",
                fg_list: vm.fg_list,
                bg_list: vm.bg_list,

                expr_type: vm.expr_type,
                data_qual: vm.data_qual,
                data_type: getCheckedIDs(vm.data_type.names),
                stage_id: getCheckedIDs(vm.developmentStages),
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
                        vm.resultUrl = '/result/'+vm.hash;
                        getResults(data);

                    } else if(data.data.data.jobResponse.jobId && data.data.data.jobResponse.data) {

                        console.log("Job launched.");
                        vm.jobId = data.data.data.jobResponse.jobId;
                        vm.resultUrl = '/result/'+vm.hash+'/'+vm.jobId;

                        vm.jobStatus = data.data.data.jobResponse.jobStatus;
                        logger.success('TopAnat request successful', 'TopAnat ok');
                        vm.message = lang.jobProgressBookmark+"<br/>"+lang.jobProgress+vm.jobId+'. ('+vm.jobStatus+') ';
                        vm.jobDone = false;
                        $window.document.getElementById('resultContainer').scrollIntoView();

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
                    // Do not consider the user cancellation as an error
                    var matcher = new RegExp('Job stopped by the user');
                    if (!vm.message.match(matcher))
                    {
                        console.log('error from bgeedataservice');
                        console.log(data);
                        logger.error('TopAnat request not successful', 'TopAnat fail');
                        vm.message = 'TopAnat request failed. Message from the server: '+data.data.message;
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
                            vm.message = lang.jobProgressBookmark+"<br/>"+lang.jobProgress+vm.jobId+'. ('+vm.jobStatus+') ';
                            
                            //scroll to result container with information about job, 
                            //otherwise it is possible to miss it.
                            if (statuscounter == 1) {
                                $window.document.getElementById('resultContainer').scrollIntoView();
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
                var callType = data.data.topAnatResults[i].callType;

                // we could probably use the sanme logic as for the zip file and put a composed key
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
            //already existing results, and not only following a form submission, this sould be improved
            vm.formSubmitted = true;
            vm.message = 'TopAnat request successful. Found ' + resultCount + ' records, from '
                + analysisWithResults + (analysisWithResults > 1? " analyses": " analysis")
                + ' with results, over '
                + analysisCount + (analysisCount > 1? " analyses": " analysis") + ' launched.';
            vm.messageSeverity = "success";
            console.log("Message generated from results: " + vm.message);

            vm.resultUrl = '/result/' + vm.hash;
            console.log("ready resultUrl: " + vm.resultUrl);

            if($location.path() !== vm.resultUrl) {
                console.info("updating path...");
                $location.update_path(vm.resultUrl, false);
            }

            showMessage($scope, false);
            vm.jobDone = vm.jobStatus;

            $timeout(function(){
                $window.document.getElementById('resultContainer').scrollIntoView();
            }, 500);

        }

        /********************** End Action buttons *********************/

        function getDevStages(type, list) {

            showMessage($scope, "development stages");

            console.log("Comparing fg and bg");
            if(!checkFgBg()){
                // TODO should BG be cleared and bgee data activated? (might be confusing to the user)
                console.log("fg and bg did not match");
                // fg must be in bg
                logger.error("Genelist contains genes not found in background genes");

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
                } else {
                    data['bg_list'] = list;
                }

                console.log("data to getdevstages with type: " + type);
                console.log(data);

                return bgeedataservice.getDevStages(configuration.mockupUrl, data)
                    .then(function (data) {
                        handleDevStages(data, type);
                        getAllDataTypes();
                        vm.allowedDataTypes = getAllowedDataTypes(vm.expr_type);
                    },

                    function(data){
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
                        } else if (typeof data.topAnatResults === 'undefined' && typeof data !== 'undefined' && vm.jobStatus === 'UNDEFINED') {
                            // job is done, but the result is missing,
                            // data probably had a message for us too, but we already show it
                            logger.error('Getting result failed. Result may have been deleted. Resubmitting job, please stand by.', 'TopAnat fail')
                            vm.message = data;
                            vm.showDevStageError = data;
                            vm.sendForm();
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
                    });
            }

            //showMessage($scope, false);

        }

        function parseMessage(message) {
            var matcher = new RegExp('(.+) for fg_list');
            var match = message.match(matcher);

            if (match != null && typeof match !== 'undefined') {
                return match[1];
            }
            else {
                return message;
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
                }
                else {
                    vm.selected_species = mapIdtoName(data, type + "_list");
                    console.log("selected species: "+vm.selected_species);
                    vm.isValidSpecies = true;
                    vm.geneValidationMessage = parseMessage(data.message);
                    //getNbDetectedSpecies(data, type + "_list") > 1 ? vm.geneValidationMessage = parseMessage(data.message) : vm.geneValidationMessage = '';


                }

                var stages = [];
                var isChecked = true;
                console.log(data.data.fg_list.stages);
                angular.forEach(data.data.fg_list.stages, function(devStage, key){


                    // do we already have something from server
                    isChecked = !(vm.stage_id && vm.stage_id.indexOf(devStage.id) == -1);

                    stages.push({
                        name : devStage.name,
                        id : devStage.id,
                        checked : isChecked
                    });

                });

                vm.developmentStages = angular.copy(stages);

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
        if(vm.data_qual != configuration.data_qual ||
            vm.decorr_type != configuration.decorr_type ||
            vm.node_size != configuration.node_size ||
            vm.nb_node != configuration.nb_node ||
            vm.fdr_thr != configuration.fdr_thr ||
            vm.p_value_thr != configuration.p_value_thr)
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
