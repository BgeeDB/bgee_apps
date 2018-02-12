(function () {

    'use strict';
    angular
        .module('app')
        .factory('bgeejobservice', bgeejobservice);

    bgeejobservice.$inject = ['$http', '$q', 'logger', 'configuration'];

    function bgeejobservice($http, $q, logger, configuration) {


        var service = {
            getJobStatus: getJobStatus,
            cancelJob: cancelJob,
            getJobResult: getJobResult,
            storeJobData: storeJobData,
            getJobHistory: getJobHistory,
            removeJobFromHistory: removeJobFromHistory
        };

        return service;

        function getJobStatus(hash, jobid, full) {

            console.time("getJobStatus");

            var defer = $q.defer();

            if(!hash || !jobid){
                // TODO return real error object. Return null in the mean time,
                // avoid to log an error server side
                console.log("Problem, no jobId nor data hash provided. "+hash+" : "+jobid);
                return defer.reject("Error, no jobId nor data hash provided.");
            }

            console.log("jobid: "+jobid);

            var params = "?page=top_anat&ajax=1&action=tracking_job&display_type=json";
            // we don't want geneinfo on jobstatus after submit
            if(full){
                params += "&display_rp=1&gene_info=1";
            }
            if (hash) {
                params += "&data=" + hash;
            }
            if(jobid){
                params += "&job_id="+jobid;
            }

            console.log("getJobstatus params: "+params);

            // TODO handle error states, return $q.reject(response.data.message)

            return $http.get(params)
                .then(function(response){
                    console.log("response ok");
                    console.timeEnd("getJobStatus");
                    return response.data;
                },
                function(){
                    console.log("response not ok");
                    return defer.reject("Error, no job tracking data available");
                }

            );
        }
        

        function cancelJob(jobid) {

            console.time("cancelJob");

            var defer = $q.defer();

            if(!jobid){
                // TODO return real error object. Return null in the mean time,
                // avoid to log an error server side
                console.log("Problem, no jobId provided");
                return defer.reject("Error, no jobId provided.");
            }

            console.log("jobid: "+jobid);
            var params = "?page=job&action=cancel&job_id=" + jobid + "&ajax=1&display_type=json";
            console.log("cancelJob params: " + params);

            // TODO handle error states, return $q.reject(response.data.message)

            return $http.get(params)
                .then(function(response){
                    console.log("response ok");
                    console.timeEnd("cancelJob");
                    return response.data;
                },
                function(){
                    console.log("response not ok");
                    console.timeEnd("cancelJob");
                    return defer.reject("Error, no cancel job data available");
                }
            );
        }

        function getJobResult(hash){

            var defer = $q.defer();

            if(!hash){
                // TODO return real error object. Return null in the mean time,
                // avoid to log an error server side
                console.log("Problem, no data hash provided.");
                $defer.reject("Error, no data hash provided.");
            } else {

              $http.get("?page=top_anat&gene_info=1&display_rp=1&ajax=1&action=get_results&display_type=json&data="+hash)
                //.then(getJobData);
                .then(function(response){
                    console.log("thenresponse");
                    console.log(response.data);
                    defer.resolve(response.data);

                },
                function(response){
                    console.log("error, server did not find result");
                    console.log(response);
                    // try to fill missing job's parameters
                    if (typeof response.data.requestParameters !== 'undefined' && response.data.requestParameters !== null &&
                    		response.data.code == 400) {
                        console.debug(response.data.requestParameters);
                        defer.resolve(response.data);
                    }

                    // TODO handle this better. Is it serious enough to create a label in the UI, or is toast enough?
                    logger.error("Job does not exist or it has expired");
                    defer.reject(response);

                }
              );
            }

            return defer.promise;
        }

        function removeJobFromHistory (job) {
            var history = getJobHistory(); // history is an array of Objects
            var hash = job.hash;

            for(var i=0; i < history.length ; i++) {
                if (history[i].hash == hash) {
                    history.splice(i, 1);
                    localStorage.setItem('topanat-history', JSON.stringify(history));
                }
            }
        }

        function storeJobData (hash, species, taxid, title) {
            var history = getJobHistory(); // history is an array of Jobs

            if (history == null || jobNotFound(history, hash)) { // First job/new job
                var date = new Date();
                var now = date.toLocaleString();

                // use hash as the key, easier to search
                var newObj = { hash: hash,  creationDate: now, species: species, taxid: taxid, title: title};

                if (history == null) {
                    history = [];
                }
                history.push(newObj);
                localStorage.setItem('topanat-history', JSON.stringify(history))
            }
        }

        function getJobHistory () {
            return JSON.parse(localStorage.getItem('topanat-history'));
        }

        function jobNotFound (history, hash) {
            for(var i=0; i < history.length ; i++) {
                if (history[i].hash == hash) {
                    return false;
                }
            }
            return true;
        }
    }
})();