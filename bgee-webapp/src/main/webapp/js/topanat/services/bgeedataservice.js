(function () {

'use strict';
angular
    .module('app')
    .factory('bgeedataservice', bgeedataservice);

    bgeedataservice.$inject = ['$http', '$q', 'logger'];

    function bgeedataservice($http, $q, logger) {


        var service = {
            getDevStages: getDevStages,
            postGeneData: postGeneData,
        };

        return service;
        
        function getDevStages(url) {
            return $http.get(url)
                .then(getDevStagesResults)


            function getDevStagesResults(response) {
                console.log(response.data)
                return response.data
            }

        }

        // User cancellation enabled
        // Inspired from:
        // http://odetocode.com/blogs/scott/archive/2014/04/24/canceling-http-requests-in-angularjs.aspx
        function postGeneData(url, data) {
            var canceller = $q.defer();

            var cancel = function(reason){
                canceller.resolve(reason);
            };

            var promise = $http.post(url, data, {timeout: canceller.promise})
                .then(getResults);

            function getResults(response) {
                console.log('getresults'+response)
                return response;
            }

            return {
                promise: promise,
                cancel: cancel
            };
        }

    }





})();