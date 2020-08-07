(function () {
    'use strict';

    /**
     * @ngdoc function
     * @name app.controller:resultController
     * @description
     * # resultcontroller
     */

    angular.module('app')
        .controller('resultController', resultController, ['ui.bootstrap']);

    resultController.$inject = ['$scope', '$location', '$http', '$routeParams', '$interval', 'bgeejobservice', 'helpservice',  'configuration', 'logger', '$timeout'];



    function resultController ($scope, $location, $http, $routeParams, $interval, bgeejobservice, helpservice,  configuration, logger, $timeout) {

        var vm = this;

        /********************** Cookies ****************************/

        /* Help panel: Remember whether the panel is shown or not */
        vm.showHelp = getShowHelpCookie();

        function getShowHelpCookie() {
            return helpservice.getHelpCookie();
        }

        vm.updateShowHelpCookie = function (flag) {
            helpservice.setHelpCookie(flag);
            vm.showHelp = flag;
        }
        /********************** End Cookies ****************************/

        vm.jobId = $routeParams.jobid;


        vm.filterByDataType = function(expr_type) {
            getAllowedDataTypes(expr_type);

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
            if (typeof message !== 'undefined') {
                var matcher = new RegExp('Found [0-9]+ records');
                if (message.match(matcher)) {
                    return true;
                }
            }
            return false;
        }

        vm.viewResultsBy = function(stage) {
            logger.info("View by " + stage);
        }


        if($scope.jobStatus == "DONE" && vm.jobId){

            console.log(vm.gridOptions);

            bgeejobservice.getdata(vm.jobId).success(function(data){
                console.log("getdata data");
                console.log(data);
                vm.jobSuccess = data.success;

                if(vm.jobSuccess < 0){
                    vm.jobResult = false;
                    vm.errorMsg = data.errorMsg;
                }

                if(vm.jobSuccess){

                    vm.gridOptions.data = data.data;
                    logger.success('TopAnat request successful', 'TopAnat ok');
                    vm.message = 'TopAnat request successful. Found ' + data.data.length + ' records.';
                    vm.jobDone = true;

                }



            }).error(function(data, status){
                //console.log("getdata had error: "+data);
                vm.jobStatus = 'error';
                vm.jobInfo.errorMessage = data;
                //console.log(data);
            });

        }



    }

})();
