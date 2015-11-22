(function () {

    'use strict';
    angular
        .module('app')
        .factory('bgeejobservice', bgeejobservice);

    bgeejobservice.$inject = ['$http', '$q', 'logger', 'configuration'];

    function bgeejobservice($http, $q, logger, configuration) {


        var service = {
            getJobStatus: getJobStatus,
            getJobData: getJobData
        };

        return service;

        /* as a promise
        function getJobStatus(jobid) {
            var deferred = $q.defer();
            var url = configuration.mockupUrl;
            return $http.get(url+"?action=status&jobid="+jobid)
                .success(function(data) { deferred.resolve(data); });
            return deferred.promise;
        }

        */

        function getJobStatus(hash, jobid, full) {

            if(!hash || !jobid){
                // todo return real error object
            }
            var url = configuration.mockupUrl;

            console.log("jobid: "+jobid);

            // we don't want geneinfo on jobstatus after submit
            if(full){
                var params = "?page=top_anat&ajax=1&action=tracking_job&display_type=json&display_rp=1&gene_info=1&data="+hash;
            } else {
                var params = "?page=top_anat&ajax=1&action=tracking_job&display_type=json&data="+hash;
            }

            if(jobid){
                params += "&job_id="+jobid;
            }

            console.log("getJobstatus params: "+params);

            return $http.get(url+params)
                .then(getStatus)

            function getStatus(response) {
                console.log(response.data);
                return response.data;
            }

        }

        function getJobData(hash){
            var url = configuration.mockupUrl;
            return $http.get(url+"?page=top_anat&gene_info=1&display_rp=1&ajax=1&action=get_results&display_type=json&data="+hash)
                .then(getJobData);


            function getJobData(response) {
                console.log(response.data);
                return response.data
            }

        }
    }



})();