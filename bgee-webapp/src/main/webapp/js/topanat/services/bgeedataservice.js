(function () {

    'use strict';
    angular
        .module('app')
        .config(function($httpProvider) {
            $httpProvider.defaults.headers.put['Content-Type'] =
                'application/x-www-form-urlencoded';
            $httpProvider.defaults.headers.post['Content-Type'] =
                'application/x-www-form-urlencoded';
        })
        .factory('bgeedataservice', bgeedataservice);



    bgeedataservice.$inject = ['$http', '$q', 'logger', '$httpParamSerializer'];

    function bgeedataservice($http, $q, logger, $httpParamSerializer) {


        var service = {
            getDevStages: getDevStages,
            postGeneData: postGeneData,
        };

        return service;

        function getDevStages(url, data) {

            console.time("getdevstages");
            console.log($httpParamSerializer(data));
            var config = "{headers: { 'Content-Type': 'application/x-www-form-urlencoded;'}}";
            return $http.post(url, $httpParamSerializer(data), config)
            //return $http.post(url, data)
            //return $http.post(url+"?page="+data.page, $httpParamSerializer(data))
                .then(getDevStagesResults)


            function getDevStagesResults(response) {
                console.log("getdevstageresults:");

                console.log(response);
                console.timeEnd("getdevstages");
                return response.data;
            }

        }

        // User cancellation enabled
        // Inspired from:
        // http://odetocode.com/blogs/scott/archive/2014/04/24/canceling-http-requests-in-angularjs.aspx
        function postGeneData(url, data) {
            console.time("postgenedata");
            var canceller = $q.defer();

            var cancel = function (reason) {
                canceller.resolve(reason);
            };

            var config = "{headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}";

            //var promise = $http.post(url, $httpParamSerializer(data), config)
            var promise = $http.post(url, $httpParamSerializer(data))
                .then(getResults);

            function getResults(response) {
                console.log('getresults' + response)
                console.timeEnd("postgenedata");
                return response;
            }

            return {
                promise: promise,
                cancel: cancel
            };
        }

    }


})();