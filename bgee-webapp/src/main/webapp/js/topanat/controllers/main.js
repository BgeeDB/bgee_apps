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
        .controller('MainCtrl', MainCtrl, ['ui.bootstrap', 'angularFileUpload', 'ngLocationUpdate']);

    MainCtrl.$inject = ['$scope', '$sce', 'bgeedataservice', 'bgeejobservice', 'helpservice', 'DataTypeFactory', 'configuration', 'logger', 'FileUploader', '$timeout', '$location', '$interval', 'lang', 'jobStatus', '$filter'];

    function MainCtrl ($scope, $sce, bgeedataservice, bgeejobservice, helpservice, DataTypeFactory, configuration, logger, FileUploader, $timeout, $location, $interval, lang, jobStatus, $filter) {

        var vm = this;

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

        /********************** Cookies ****************************/

        /* Help panel: Remember whether the panel is shown or not */
        vm.showHelp = getShowHelpCookie();
        vm.quickstart = getQuickStartStatus();


        // bindable
        vm.getDevStages = getDevStages;
        vm.displayResults = displayResults;
        vm.cancelJob = cancelJob;
        vm.startNewJob = startNewJob;
        vm.getOnlineHelpForItem = getOnlineHelpForItem;
        vm.filterResultFromViewSelector = filterResultFromViewSelector;

        /************** No default values ***********************/


        vm.message = ''; // Defining empty message variable
        // message from the gene validation query.
        // Filled only when more than one species detected in fg
        vm.geneValidationMessage = '';

        // We have to use an array for the selected
        // stages - See:
        // http://stackoverflow.com/questions/14514461/how-can-angularjs-bind-to-list-of-checkbox-values
        // Moreover, the stages have to be checked once retrieved from the server
        vm.selectedDevelopmentStages = [];
        vm.developmentStages = [];

        vm.isBackgroundChecked = 'checked';

        vm.formSubmitted = false;
        vm.isAdvancedOptionsChecked = false;


        /* result filtering */
        vm.filterValue = ''; // single filter (on organ names and ids)
        vm.filterByStage = '';
        vm.filterbyExpression = '';

        /************** Default values ***********************/
        vm.data_qual = configuration.data_qual;
        vm.decorr_type = configuration.decorr_type;
        vm.expr_type = configuration.expr_type;

        if(jobStatus){
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

                vm[key] = val;

                console.info("vm."+key+" = "+vm[key]);

            });

            angular.forEach(jobStatus.data.jobResponse, function(val, key) {

                // we use hash instead od data mainly to make it clear what data it is
                if(key == "data"){
                    vm['hash'] = val;
                }

                vm[key] = val;

                console.info("vm."+key+" = "+vm[key]);

            });


            // did we have result


            /* jobstatus
            "data": {
                "jobResponse": {
                    "jobId": 22,
                        "jobStatus": "UNDEFINED",
                        "data": "4537cd7223361f0683ef91b579da820f21381dba"
                },
            */

            var jobres = {};
            $timeout(function(){

                if(jobStatus.data.topAnatResults || (jobStatus.data.jobResponse.jobStatus && jobStatus.data.jobResponse.jobStatus.toLowerCase() == "undefined")){

                    console.log("job is done, either get result or display it");
                    if(jobStatus.data.topAnatResults || (jobStatus.data.jobResponse.jobStatus && jobStatus.data.jobResponse.jobStatus.toLowerCase() == "undefined")){
                        getResults();
                    } else {
                        console.log(jobStatus.data.topAnatResults);
                        displayResults(jobStatus);
                    }

                } else {

                    console.log("job is not done, checkjobstatus");
                    vm.formSubmitted = true;
                    checkJobStatus();

                }
            }, 1000);

            getDevStages('fg', vm.fg_list);

        } else {

            console.info("no jobstatus, skipped statusquery");
            vm.fg_list = '';
            vm.bg_list = '';
            /* species are set by a web service to bgee
             * should be identical for fg and bg */
            vm.selected_species = '';
            vm.background_species = '';
            vm.selected_taxid = ''; // for the picture (see Issue #27)

            /************** Default values ***********************/
            vm.data_qual = configuration.data_qual;
            vm.decorr_type = configuration.decorr_type;
            vm.expr_type = configuration.expr_type;

            vm.fdr_thr = vm.fdrThresholdDefault = configuration.fdr_thr;
            vm.nb_node = vm.nbNodesDefault = configuration.nb_node;
            vm.node_size = vm.nodeSizeDefault = configuration.node_size;
            vm.p_value_thr = vm.pvalueThresholdDefault = configuration.p_value_thr;

        }

        /***************************** View result by stage and expression type **************************/
        function getCombinedDevStageAndExpressionType() {

            vm.viewSelectorData = {
                availableOptions: [
                    {id: 'ALL', name: 'All'}
                ],
                selectedOption: {id: 'ALL', name: 'All'} //This sets the default value of the select in the ui
            };
            var index = 1;
            angular.forEach(vm.developmentStages, function(stage, key) {
                index++;
                var combined = '';
                var object = {};
                if (stage.checked) {
                    /* SD: The correspondence between expressionType and their values should probably be stored somewhere */
                    if (vm.expr_type === 'ALL') {
                        combined = stage.name + ' stage, expression type "Present"';
                        object = {};
                        object.id = stage.id + " ; EXPRESSED";
                        object.name = combined;
                        vm.viewSelectorData.availableOptions.push(object);

                        index++;

                        combined = stage.name + ' stage, expression type "Over/Under expression"';
                        object = {};
                        object.id = stage.id + " ; DIFF_EXPRESSION";
                        object.name = combined;
                        vm.viewSelectorData.availableOptions.push(object);

                    }
                    else {
                        var expr = vm.expr_type === 'EXPRESSED' ? 'Present' : 'Over/Under expression';
                        combined = stage.name + ' stage, expression type "' + expr + '"';
                        object = {};
                        object.id = stage.id + " ; " + vm.expr_type;
                        object.name = combined;
                        vm.viewSelectorData.availableOptions.push(object);
                    }
                }
            })
        }
        /***************************** End View result by stage and expression type **************************/

        /**************** Instantiate online help ************/
        vm.onlineHelp = {};
        vm.help = '';

        getAllOnlineHelpItems();

        function getAllOnlineHelpItems() {
            return helpservice.getOnlineHelp('js/topanat/json/help.json')
                .then(function (data) {
                    vm.onlineHelp = data;
                });
        }
        /**************** End Instantiate online help ************/

        function getQuickStartStatus()
        {
            if (vm.showHelp) {
                return 'Close';
            }
            else {
                return 'Open';
            }
        }

        function getShowHelpCookie() {
            return helpservice.getHelpCookie();
        }

        vm.updateShowHelpCookie = function(flag) {
            helpservice.setHelpCookie(flag);
            vm.showHelp = flag;
            vm.quickstart = getQuickStartStatus();
        }
        /********************** End Cookies ****************************/

        function getAllDataTypes() {
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

        }

        function getSelected(data) {
            console.log("get selected data");
            console.log(data);

            var selectedData = [];
            data.forEach(function(obj) {
                if (obj.checked) {
                    selectedData.push(obj.id)
                }
            })
            console.log(selectedData)
            return selectedData;
        }

        function getAllowedDataTypes(expressiontype) {
            // TODO: caching
            console.log("getAllowedDataTypes")
            vm.selectedDataTypes = []
            vm.allowedDataTypes = DataTypeFactory.allowedDataTypes(expressiontype);
            console.log(vm.allowedDataTypes)
            if(angular.isArray(vm.allowedDataTypes)){
                vm.allowedDataTypes.forEach(function(entry) {
                    vm.selectedDataTypes.push(entry);
                });

                // uncheck disabled datatype checkbox
                angular.forEach(vm.data_type.names, function(obj){

                    if (vm.selectedDataTypes.indexOf(obj.id) === -1) {
                        obj.checked = false;
                    } else {
                        obj.checked = true;
                    }

                });

            } else {
                logger.error('No allowed Datatypes available', 'No allowed Datatypes.');
            }

            // SD: Commented because I cannot see what those are used for.
            // Maybe those should be resurrected at some point.
            /*getSelected(vm.data_type.names);
             getSelected(vm.developmentStages);*/
        }

        vm.filterByDataType = function(expr_type) {
            vm.expr_type = expr_type;
            getAllowedDataTypes(expr_type);

            vm.message = 'datatypes filtered!';
            console.log("filterByDataType")
            console.log(vm.selectedDataTypes)
            logger.success('Datatype filtering was successful', 'datatype filter OK');

        }

        /************** Result panel Handling ****************/
        function filterResultFromViewSelector() {
            var selectedView = vm.viewSelectorData.selectedOption.id;

            if (selectedView === 'ALL')
            {
                vm.filterByStage = 'ALL';
                vm.filterbyExpression = 'ALL';
            }
            else {
                splitSelectedOption(selectedView);
            }

            vm.gridApi.grid.refresh();
            vm.getFilteredRows();
        }

        function splitSelectedOption(selected) {

            var matcher = new RegExp('(.+) ; (.+)');
            var match = selected.match(matcher);

            if (typeof match !== 'undefined')
            {
                vm.filterByStage = match[1];
                vm.filterbyExpression = match[2];
            }
        }

        vm.gridOptions = {

            enableFiltering: false,
            onRegisterApi: function (gridApi) {
                vm.gridApi = gridApi;
                vm.gridApi.grid.registerRowsProcessor(vm.resultFilter, 200);
            },
            columns: [
                {field: 'anatEntityId', width: "*" },
                {field: 'anatEntityName', width: "*" },
                {field: 'annotated', width: "*" },
                {field: 'significant', width: "*" },
                {field: 'expected', width: "*" },
                {field: 'foldEnrichment', width: "*" },
                {field: 'pValue', width: "*" },
                {field: 'FDR', width: "*" }
            ]
        };

        vm.filteredRows=[];
        vm.getFilteredRows=function(){
            vm.filteredRows = vm.gridApi.core.getVisibleRows(vm.gridApi.grid);
        }

        vm.filter = function() {
            vm.gridApi.grid.refresh();
            vm.getFilteredRows();
        };

        vm.resultFilter = function (renderableRows) {

            if ((vm.filterByStage !== '' && vm.filterbyExpression !== '') && (vm.filterByStage !== 'ALL' && vm.filterbyExpression !== 'ALL')) {

                var filtered = vm.gridOptionsByAnalysis[vm.filterByStage][vm.filterbyExpression];
                var match = false;
                renderableRows.forEach(function (row) {
                    match = false;
                    for (var i = 0 ; i < filtered.length ; i++) {
                        if (checkMatch(filtered[i], row)) {
                            match = true;
                        }
                    }
                    if (!match) {
                        row.visible = false;
                    }
                });
            }
            else
            {
                renderableRows.forEach(function (row) {
                    row.visible = true;
                });
            }
            return renderableRows;
        }

        function checkMatch(row1, row2) {

            var field1, field2;

            // anatEntityId
            field1 = row1.anatEntityId;
            field2 = row2.entity['anatEntityId'];
            if (field1 !== field2) {
                return false;
            }

            // anatEntityName
            field1 = row1.anatEntityName;
            field2 = row2.entity['anatEntityName'];
            if (field1 !== field2) {
                return false;
            }

            // annotated
            field1 = row1.annotated;
            field2 = row2.entity['annotated'];
            if (field1 !== field2) {
                return false;
            }

            // significant
            field1 = row1.significant;
            field2 = row2.entity['significant'];
            if (field1 !== field2) {
                return false;
            }

            // expected
            field1 = row1.expected;
            field2 = row2.entity['expected'];
            if (field1 !== field2) {
                return false;
            }

            // foldEnrichment
            field1 = row1.foldEnrichment;
            field2 = row2.entity['foldEnrichment'];
            if (field1 !== field2) {
                return false;
            }

            // pValue
            field1 = row1.pValue;
            field2 = row2.entity['pValue'];
            if (field1 !== field2) {
                return false;
            }

            // FDR
            field1 = row1.FDR;
            field2 = row2.entity['FDR'];
            if (field1 !== field2) {
                return false;
            }

            return true;
        }


        vm.singleFilter = function (renderableRows) {

            var matcher = new RegExp(vm.filterValue);
            renderableRows.forEach(function (row) {
                var match = false;

                ['OrganId', 'OrganName'].forEach(function (field) {
                    if (row.entity[field].match(matcher)) {
                        match = true;
                    }
                });
                if (!match) {
                    row.visible = false;
                }
            });
            return renderableRows;
        };

        vm.isSuccessfulMessage = function(message) {
            var matcher = new RegExp('Found [0-9]+ records');
            if (typeof message !== undefined) {
                if (message.match(matcher)) {
                    return true;
                }
            }
            return false;
        }

        vm.devStagesChecked = function(){
            //console.log("in devStagesChecked");
            //console.log(vm.developmentStages);
            var flag = true;
            if (typeof vm.developmentStages !== 'undefined') {
                //console.log(vm.developmentStages);
                var selected = getSelected(vm.developmentStages);
                flag = selected.length > 0 ? true:false;
            }
            vm.isFormValidDevStages = flag ? 'yes' : '';
            return flag;
        };

        vm.devStageChecked = function() {
            var numchecked = $filter("filter")( vm.developmentStages , {checked:true} );
            vm.isFormValidDevStages = numchecked.length ? 'yes' : '';
            return numchecked.length;
        }

        vm.dataTypesChecked = function(){
            console.log("in dataTypesChecked");
            var flag = true;
            if (typeof vm.data_type !== 'undefined' && vm.data_type.names) {
                var selected = getSelected(vm.data_type.names);
                flag = selected.length > 0 ? true:false;
            }
            vm.isFormValidDataTypes = flag ? 'yes' : '';
            return flag;
        };


        vm.viewResultsBy = function(stage) {
            logger.info("View by " + stage);
        }

        vm.species_img = function(taxid) {
            if (taxid !== "" && typeof (taxid) !== 'undefined') {
                return 'http://bgee.org/img/species/'+ taxid + '_light.jpg';
            } else {
                return "//:0";
            }
        };

        vm.selectBackground = function(value) {
            vm.isBackgroundChecked = value;
        };

        function checkConsistency()
        {
            /*vm.background_species = 'mouse';
             vm.background_selected_taxid = '10090';*/

            if (vm.selected_species === vm.background_species) {
                logger.success("Foreground/background species are identical.");
            }
            else {
                logger.error("Foreground and background species differ. It is either you change your background or the default one will be used.");
                vm.background_species = vm.bg_list = '';
                vm.isBackgroundChecked = 'checked';
            }
        }

        function disableForm() {
            vm.formSubmitted = true; /* Shows the image + message panel - locks the form */
            vm.isAdvancedOptionsChecked = false; /* SD: Close the Advanced option panel */
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
            var textArray = getFilteredRowsAsText();
            var myBlob = new Blob(textArray, {type: 'plain/text'})
            var blobURL = (window.URL || window.webkitURL).createObjectURL(myBlob);
            var anchor = document.createElement("a");
            anchor.download = getFileName();
            anchor.href = blobURL;
            anchor.click();
        }

        function getFileName() {
            var name = '';
            var date = new Date();
            var now = date.toLocaleString();

            if (vm.filterByStage === '' && vm.filterbyExpression === '') {
                name = "all-" + now;
            }
            else {
                name = vm.filterByStage + "-" + vm.filterbyExpression + "-" + now;
            }
            return name;
        }

        function getFilteredRowsAsText() {
            var stringArray = [];

            // First, the header:
            for(var i = 0; i < vm.gridOptions.columns.length ; i++) {
                stringArray.push(vm.gridOptions.columns[i].field + "\t");
            }
            stringArray.push("\n");

            // Then, the result themselves:
            var field;
            angular.forEach(vm.filteredRows, function (row, index) {
                for(var i = 0; i < vm.gridOptions.columns.length ; i++) {
                    field = row.entity[vm.gridOptions.columns[i].field];
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
                console.log("fg or bg wsa missing, not compared");
                return true;
            }

            var lines = vm.fg_list.split('\n');

            angular.forEach(lines, function(gene){
                // if gene is not on bg list return false
                if(!isIn(gene, vm.bg_list)) {
                    logger.error("Not all foreground genes contained in the provided background");
                    return false;
                }
            });

            return true;
        }

        function startNewJob(){

            vm.formSubmitted = false;
            vm.jobDone = false;
            vm.jobStatus = false;
            /* Reset the filters of the results */
            vm.filterByStage = '';
            vm.filterbyExpression = '';
            vm.filterValue = '';
        }

        function cancelJob(){
            if(request){
                request.cancel("User cancellation");
            }

            vm.jobDone = true;
            vm.message = "Job stopped by the user";
            vm.gridOptions.data = '';
            $interval.cancel(timer);
            statuscounter = 0;

        }

        /*
         jobProgressStart: 'Processing job, waiting for a job id. Please do not close this window.',
         jobProgress: 'Job has been submitted as jobid: ',
         jobProgressBookmark: 'After bookmarking this page it\'s safe to close this window.'
         */

        vm.postForm = function() {

            disableForm();
            getCombinedDevStageAndExpressionType();

            vm.jobDone = false; /* When true -> Show the "New job" button */

            vm.message = lang.jobProgressStart;
            vm.messageSeverity = "warning";
            //logger.info('Posting...');
            $timeout(vm.sendForm, 3000);

        }


        vm.sendForm = function() {

            var formData = {
                page: "top_anat",
                fg_list: vm.fg_list,
                bg_list: vm.bg_list,
                expr_type: vm.expr_type,
                data_qual: vm.data_qual,
                data_type: getSelected(vm.data_type.names),
                stage_id: getSelected(vm.developmentStages),
                decorr_type: vm.decorr_type,
                node_size: vm.node_size,
                fdr_thr: vm.fdr_thr,
                p_value_thr: vm.p_value_thr,
                nb_node: vm.nb_node,
                submitted: true,
                display_type: "json",
                action: "submit_job",
                ajax: 1
            };

            request = bgeedataservice.postGeneData(configuration.mockupUrl, formData);

            request.promise.then(function (data) {


                    /*{
                        "code": 200,
                        "status": "SUCCESS",
                        "message": "Job is RUNNING",
                        "data": {
                        "jobResponse": {
                            "jobId": 28,
                                "jobStatus": "RUNNING",
                                "data": "f85816a4b21ef060d76ae1acc7bf6ccfa7f8c7ea"
                        }
                    }
                    }

                     {
                     "code": 200,
                     "status": "SUCCESS",
                     "message": "The results already exist.",
                     "data": {
                     "jobResponse": {
                     "jobId": 0,
                     "jobStatus": "UNDEFINED",
                     "data": "cd7866af530e8a12d10207f696b8a84fe26bf8be"
                     }
                     }
                     }



                */
                console.log("submit response");
                console.log(data.data.data);
                    // move this into function
                    if(data.data.result){

                        vm.jobDone = true;
                        displayResults(data);

                    } else if(data.data.data.jobResponse.jobId && data.data.data.jobResponse.data) {

                        vm.jobId = data.data.data.jobResponse.jobId;
                        vm.hash = data.data.data.jobResponse.data
                        vm.resultUrl = '/result/'+vm.hash+'/'+vm.jobId;
                        $location.update_path(vm.resultUrl, false);
                        console.log("processing post resultUrl: "+vm.resultUrl);

                        vm.jobStatus = data.data.data.jobResponse.jobStatus;
                        logger.success('TopAnat request successful', 'TopAnat ok');
                        vm.message = lang.jobProgressBookmark+"<br/>"+lang.jobProgress+vm.jobId+'. ('+vm.jobStatus+') ';
                        vm.jobDone = false;

                        console.log("calling checkJobStatus");
                        checkJobStatus();
                        //vm.getFilteredRows();

                    }



                },

                // do we need to pass data?
                function(data){
                    // Do not consider the user cancellation as an error
                    var matcher = new RegExp('Job stopped by the user');
                    if (!vm.message.match(matcher))
                    {
                        console.log('error from bgeedataservice')
                        logger.error('TopAnat request not successful', 'TopAnat fail')
                        vm.message = 'TopAnat request failed.';
                    }
                });
        }

        var timer = null;
        var statuscounter = 0;

        function checkJobStatus(){

            console.log("checkjobstatus");
            console.log(vm.jobId);

            if(!vm.jobId) return "EXIT";
            var jobid_status = vm.jobId;
            vm.jobDone = false;

            // set result url without reloading the page
            //$location.update_path(vm.resultUrl, false);

            // check for status every 5 secs
            timer = $interval(function(){
                console.info("Calling jobstatus");

                bgeejobservice.getJobStatus(vm.hash, vm.jobId)

                    .then(function (data) {
                        console.log("got data from jobstatus");
                        console.log(data);

                        // move this into function
                        //if(data.status == "DONE" && data.result && !vm.jobDone){
                        if(data.status == "UNDEFINED" && !vm.jobDone){

                            vm.jobDone = true;
                            $interval.cancel(timer);

                            displayResults();

                        } else {

                            statuscounter = statuscounter + 1;
                            vm.jobStatus = data.status;
                            vm.message = lang.jobProgressBookmark+"<br/>"+lang.jobProgress+vm.jobId+'. ('+vm.jobStatus+') ';

                        }
                    },

                    function(data){
                        $interval.cancel(timer);
                    }
                    );

            }, 4000);

        };

        function getResults() {



            bgeejobservice.getJobData(vm.hash)
                .then(function (data) {

                    displayResults(data);

                    /*
                    console.info("Job done, displaying results");
                    console.info(data);

                    vm.gridOptions.data = data.data;
                    //vm.jobStatus = data.data.jobStatus;
                    vm.jobStatus = data.status;

                    vm.message = 'TopAnat request successful. Found ' + data.data.length + ' records.';
                    vm.messageSeverity = "success";


                    // change URL only if we have submitted the job
                    if (!jobStatus.hash) {

                        vm.hash = data.hash
                        vm.resultUrl = '/result/' + vm.hash;
                        console.log("ready resultUrl: " + vm.resultUrl);
                        $location.update_path(vm.resultUrl, false);

                    }

                    vm.message = 'TopAnat request successful. Found ' + data.data.length + ' records.';
                    vm.messageSeverity = "success";
                    vm.jobDone = vm.jobStatus;
                    */

                },

                function(data){
                    console.log('error getting result from bgeejobservice');
                    if (data.message) {

                        logger.error('Getting result failed. error: '+data.message, 'TopAnat fail')
                        vm.message = data.message;
                    } else {
                        logger.error('Getting result failed. Unknown error.', 'TopAnat fail')
                        vm.message = 'Getting result failed. Unknown error.';
                    }
                });

        }

        function parseResults(data) {

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

            // for the filtering (the dev stage and data types are not the results anymore, see above)
            vm.gridOptionsByAnalysis = [];

            for (var i = 0; i < data.data.topAnatResults.length; i++) {
                var devStageId = data.data.topAnatResults[i].devStageId;
                var callType = data.data.topAnatResults[i].callType;

                vm.gridOptionsByAnalysis[devStageId] = [];
                vm.gridOptionsByAnalysis[devStageId][callType] = data.data.topAnatResults[i].results;

                // SD: Ugly! There should be a better way
                // no time to investigate right now!
                var topAnat = data.data.topAnatResults[i].results;
                var grid = vm.gridOptions.data;
                if (typeof grid !== 'undefined') {
                    vm.gridOptions.data = grid.concat(topAnat); // show all
                }
                else {
                    vm.gridOptions.data = data.data.topAnatResults[i].results;
                }
            }
            vm.getFilteredRows(); // In order to get the total number of rows before filtering of results
            vm.jobDone = true; // in order to display the result array

        }

        function displayResults(result) {
            /*
            "topAnatResults": [
                {
                    "zipFile": "bgee/TopAnatFiles/results/topAnat_d2ce16b29eabcea98217b5c3aa43b03b7162cb54.zip",
                    "devStageId": "UBERON:0000068",
                    "callType": "EXPRESSED",
                    "results": [
                        {
                            "anatEntityId": "UBERON:0000073",
                            "anatEntityName": "regional part of nervous system",
                            "annotated": 14917.0,
                            "significant": 265.0,
                            "expected": 254.68,
                            "foldEnrichment": 1.04,
                            "pValue": 0.134,
                            "FDR": 0.936
                        },

            */


            vm.jobStatus = "DONE";

            console.info("Job done, displaying results");
            console.info(result);

            //vm.gridOptions.data = result.data.topAnatResults[0].results;
            //vm.jobStatus = result.status;
            parseResults(result);
            vm.message = 'TopAnat request successful. Found ' + result.data.topAnatResults[0].results.length + ' records.';
            vm.messageSeverity = "success";


            // change URL only if we have submitted the job
            //if (!jobStatus.hash) {

                vm.hash = result.requestParameters.data;
                vm.resultUrl = '/result/' + vm.hash;
                console.log("ready resultUrl: " + vm.resultUrl);
                $location.update_path(vm.resultUrl, false);

            //}
            vm.jobDone = vm.jobStatus;


        }


        /********************** End Action buttons *********************/

        function getDevStages(type, list) {

            console.log("Comparing fg and bg");
            if(!checkFgBg()){
                console.log("fg and bg did not match");
                // fg must be in bg
                logger.error("Genelist contains genes not found in background genes");
                return false;
            }

            console.log("getting dev stages");
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

                console.log("data to getdevstages");
                console.log(data);

                return bgeedataservice.getDevStages(configuration.mockupUrl, data)
                    .then(function (data) {
                        handleDevStages(data, type);

                        getAllDataTypes();
                        vm.allowedDataTypes = getAllowedDataTypes(vm.expr_type);
                    }
                );
            }
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

        function getNbDetectedSpecies(data, type) {
            var species = type === 'fg_list' ? data.data.fg_list.detectedSpecies : data.data.bg_list.detectedSpecies;
            var number = Object.keys(species).length;
            return number;
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
                    getNbDetectedSpecies(data, type + "_list") > 1 ? vm.geneValidationMessage = parseMessage(data.message) : vm.geneValidationMessage = '';


                }

                var stages = [];
                var isChecked = true;
                console.log(data.data.fg_list.stages);
                angular.forEach(data.data.fg_list.stages, function(devStage, key){


                    // do we already have something from server
                    if(vm.stage_id && vm.stage_id.indexOf(devStage.id) == -1){
                        isChecked = false;
                    } else {
                        isChecked = true;
                    }

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
                checkConsistency();
            }
        };

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

    function isIn(str_to_match, str) {
        console.log("bg: "+str+" fg: "+str_to_match);
        return (str.indexOf(str_to_match) !== -1);
    }

})();
