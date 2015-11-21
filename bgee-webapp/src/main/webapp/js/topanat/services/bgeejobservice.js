(function () {

    'use strict';
    angular
        .module('app')
        .factory('bgeejobservice', bgeejobservice);

    bgeejobservice.$inject = ['$http', '$q', 'logger', 'configuration'];

    function bgeejobservice($http, $q, logger, configuration) {


        var service = {
            getJobStatus: getJobStatus,
            getJobData: getJobData,
        };

        return service;

        function getJobStatus(jobid) {
            var url = configuration.mockupUrl;
            return $http.get(url+"?action=status&jobid="+jobid)
                .then(getStatus)


            function getStatus(response) {
                console.log(response.data)
                return response.data
            }

        }

        function getJobData (jobid){

            var url = configuration.mockupUrl;
            return $http.get(url+"?action=getdata&jobid="+jobid)
                .then(getJobData)


            function getJobData(response) {
                console.log(response.data)
                return response.data
            }

        }
    }



})();