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
        .controller('MainCtrl', MainCtrl, ['ui.bootstrap', 'angularFileUpload']);

    MainCtrl.$inject = ['$scope', 'bgeedataservice', 'helpservice', 'DataTypeFactory', 'configuration', 'logger', 'FileUploader', '$timeout'];

    function MainCtrl ($scope, bgeedataservice, helpservice, DataTypeFactory, configuration, logger, FileUploader, $timeout) {

        var vm = this;

        /************** No default values ***********************/
        vm.genes = '';
        vm.background = '';
        vm.sid = 'session-id-12345'; // should be built dynamically
        vm.message = ''; // Defining empty message variable

        // We have to use an array for the selected
        // stages - See:
        // http://stackoverflow.com/questions/14514461/how-can-angularjs-bind-to-list-of-checkbox-values
        // Moreover, the stages have to be checked once retrieved from the server
        vm.selectedDevelopmentStages = [];
        vm.developmentStages = [];

        vm.isBackgroundChecked = 'checked';

        vm.formSubmitted = false;
        vm.isAdvancedOptionsChecked = false;

        /* species are set by a web service to bgee
         * should be identical for fg and bg */
        vm.selected_species = '';
        vm.background_species = '';
        vm.selected_taxid = ''; // for the picture (see Issue #27)

        /************** Default values ***********************/
        vm.dataQuality = configuration.dataQuality;
        vm.decorrelationType = configuration.decorrelationType;
        vm.expressionType = configuration.expressionType;

        vm.fdrThreshold = vm.fdrThresholdDefault = configuration.fdrThreshold;
        vm.nbNodes = vm.nbNodesDefault = configuration.nbNodes;
        vm.nodeSize = vm.nodeSizeDefault = configuration.nodeSize;
        vm.pvalueThreshold = vm.pvalueThresholdDefault = configuration.pvalueThreshold;

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

        /********************** Cookies ****************************/

        /* Help panel: Remember whether the panel is shown or not */
        vm.showHelp = getShowHelpCookie();
        vm.quickstart = getQuickStartStatus();

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

        vm.updateShowHelpCookie = function (flag) {
            helpservice.setHelpCookie(flag);
            vm.showHelp = flag;
            vm.quickstart = getQuickStartStatus();
        }
        /********************** End Cookies ****************************/


        getAllDataTypes();

        /* dependencies */
        vm.allowedDataTypes = getAllowedDataTypes(vm.expressionType);

        function getAllDataTypes() {
            if(DataTypeFactory.allDataTypes()) {
                // check by default, add checked: true to the model

                vm.dataTypes = DataTypeFactory.allDataTypes();

                // check every datatype by default
                angular.forEach(vm.dataTypes.names, function(obj){
                    obj.checked = true;

                });

            } else {
                logger.error('Datatypes not available', 'No Datatypes.');
            }

        }

        function getSelected(data) {

            console.log("getSelectedDataTypes")
            console.log(data)
            var selectedData = [];
            data.forEach(function(obj) {
                if (obj.checked) {
                    selectedData.push(obj.name)
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
                angular.forEach(vm.dataTypes.names, function(obj){

                    if (vm.selectedDataTypes.indexOf(obj.key) === -1) {
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
            /*getSelected(vm.dataTypes.names);
             getSelected(vm.developmentStages);*/
        }

        vm.filterByDataType = function(expressionType) {
            getAllowedDataTypes(expressionType);

            vm.message = 'datatypes filtered!';
            console.log("filterByDataType")
            console.log(vm.selectedDataTypes)
            logger.success('Datatype filtering was successful', 'datatype filter OK');

        }

        /************** Result panel Handling ****************/
        vm.gridOptions = {

            enableFiltering: false,
            onRegisterApi: function (gridApi) {
                vm.gridApi = gridApi;
                vm.gridApi.grid.registerRowsProcessor(vm.singleFilter, 200);
            },
            columns: [
                {field: 'OrganId', width: "*" },
                {field: 'OrganName', width: "*" },
                {field: 'Annotated', width: "*" },
                {field: 'Significant', width: "*" },
                {field: 'Expected', width: "*" },
                {field: 'foldEnrichment', width: "*" },
                {field: 'p', width: "*" },
                {field: 'fdr', width: "*" }
            ]
        };

        vm.filteredRows=[];
        vm.getFilteredRows=function(){
            vm.filteredRows = vm.gridApi.core.getVisibleRows(vm.gridApi.grid);
        }

        vm.filter = function () {
            vm.gridApi.grid.refresh();
            vm.getFilteredRows();
        };

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

        vm.viewResultsBy = function(stage) {
            logger.info("View by " + stage);
        }

        vm.species_img = function(taxid) {
            if (taxid !== "") {
                return 'http://bgee.org/img/species/'+ taxid + '_light.jpg';
            } else {
                return "//:0";
            }
        };
        function checkConsistency()
        {
            if (vm.selected_species === vm.background_species) {
                logger.success("Foreground/background species are identical.");
            }
            else {
                logger.error("Foreground and background species differ. It is either you change your background or the default one will be used.");
                vm.background_species = vm.background = '';
            }
        }

        function disableForm() {
            vm.formSubmitted = true; /* Shows the image + message panel - locks the form */
            vm.isAdvancedOptionsChecked = false; /* SD: Close the Advanced option panel */
            vm.showHelp = false; /* SD: Close the help panel - not remembered */
        }

        /************** End Result panel Handling ****************/

        /********************** Action buttons *************************/
        var request = null;

        vm.startNewJob = function startNewJob ()
        {
            vm.formSubmitted = false;
            vm.jobDone = false;
        }

        vm.cancelJob = function cancelJob()
        {
            request.cancel("User cancellation");
            vm.jobDone = true;
            vm.message = "Job stopped by the user";
            vm.gridOptions.data = '';
        }

        vm.postForm = function () {

            vm.jobDone = false; /* When true -> Show the "New job" button */

            disableForm();

            vm.message = 'Running...';
            logger.info('Posting...')

            var data = {
                genes: vm.genes,
                background: vm.background,
                expressionType: vm.expressionType,
                dataQuality: vm.dataQuality,
                dataTypes: getSelected(vm.dataTypes.names),
                developmentStage: getSelected(vm.developmentStages),
                decorrelationType: vm.decorrelationType,
                nodeSize: vm.nodeSize,
                fdrThreshold: vm.fdrThreshold,
                pvalueThreshold: vm.pvalueThreshold,
                nbNodes: vm.nbNodes
            };

            request = bgeedataservice.postGeneData(configuration.mockupUrl, data);

            request.promise.then(function (data) {
                    vm.gridOptions.data = data.data;
                    logger.success('TopAnat request successful', 'TopAnat ok');
                    vm.message = 'TopAnat request successful. Found ' + data.data.length + ' records.';
                    vm.jobDone = true;
                },
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
        /********************** End Action buttons *********************/

        vm.getDevStages = function getDevStages(type, list) {
            console.log("getting dev stages");
            if (type && list) {
                return bgeedataservice.getDevStages(configuration.mockupUrl + "?type=" + type + "&sid=" + vm.sid + "&list=" + list)
                    .then(function (data) {
                        handleDevStages(data, type);
                    });
            }
        }

        function handleDevStages(data, type){

            var stages = []
            angular.forEach(data.dev_stages, function(devStage, key){

                stages.push({
                    name : devStage,
                    checked : true
                });

            });

            vm.developmentStages = angular.copy(stages);

            var species = data.selected_species;
            // Should pop up only when the gene list is checked
            // not for the background gene list's checking...
            if (type === 'fg') {
                vm.selected_species = species;
                vm.selected_taxid = data.selected_taxid;
                logger.info('Development stages updated for species "' + vm.selected_species + '"', 'Information');

            }
            else if (type === 'bg') {
                vm.background_species = species;
                //vm.background_species = 'foo';
                logger.info('Found species ' + vm.background_species + ' for the background', 'Information');
                checkConsistency();
            }

            /*SD: The user should now click on the cross to close the help panel. Less confusing for me. */
            //vm.showHelp = false;
        }

        //vm.uploader = new FileUploader({ url: configuration.mockupUrl, autoUpload: true, formData: [{filetype: vm.fileType}]});
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
            //console.info('onAfterAddingFile fq', $scope.fq);
            vm.hideq[vm.fileType] = false;
            //console.info('onAfterAddingFile hideq', $scope.hideq);
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

            if(response.status_code == "0"){
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

                if(response.status_code != "0" && response.msg){
                    vm.processUploadError(response.msg);
                } else {
                    logger.error("Unknnown file upload error");
                }



            }

            vm.fileitem.status = response.status_code;
        };

        vm.processUploadError = function(message){

            vm.uploadError[vm.fileType] = true;
            logger.error("File upload error: "+message);

        };

    }

})();
