'use strict';

/**
 * @ngdoc overview
 * @name app
 * @description
 * # app
 *
 * Main module of the application.
 */
angular
    .module('app', [
        'ngRoute',
        'ngAnimate',
        'logger',
        'ui.grid',
        'ui.grid.resizeColumns',
        'ui.grid.autoResize',
        'services.config',
        'services.lang',
        'angularFileUpload',
        'ui.bootstrap',
        'ngLocationUpdate',
        'ngFileSaver',
        'bgeeLoading'
    ])


    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/', {
                /*webapp: templateUrl: 'js/topanat/views/main.html',*/
                /*topanat: templateUrl: 'views/main.html',*/
                templateUrl: 'js/topanat/views/main.html',
                controller: 'MainCtrl',
                controllerAs: 'vm',
                resolve: {
                    appInit: function(){console.log("appinit = true");return true;},
                    loading: function(){console.log("loading = 1");return 1;},
                    whatIsLoading: function(){return ' jobstatus';},
                    "jobStatus": function() {
                        return null;
                    }

                }
            })

            .when("/result/:hash/:jobid", {
                /*webapp: templateUrl: 'js/topanat/views/main.html',*/
                /*topanat: templateUrl: 'views/main.html',*/
                templateUrl: 'js/topanat/views/main.html',
                controller:'MainCtrl',
                controllerAs: 'vm',
                reloadOnSearch: false,
                resolve:

                {

                    appInit: function(){console.log("appinit = true");return true;},
                    loading: function(){console.log("loading = true");return true;},

                    //FIXME: Of note, when the URL is updated following a form submission,
                    //it triggers this routing, but AngularJS can't be aware of
                    //the hash and jobid vars
                    //(see second comment of this answer: http://stackoverflow.com/a/16266601/1768736)
                    //This generates an error on our server (query to server
                    //with 'undefined' as value for jobid and hash, see log below).
                    //The triggering of this routing is not even needed at this point,
                    //after form submission there is already a tracking of job advancement activated.
                    //Is there a way to prevent this routing to be triggered when URL updated
                    //following job submission?
                    jobStatus: ["$q","$route","bgeejobservice", function($q, $route, bgeejobservice){

                        var deferred = $q.defer();
                        console.log($route.current.params);
                        var jobid = $route.current.params.jobid;
                        var hash = $route.current.params.hash;
                        if(typeof jobid == 'undefined' || typeof hash == 'undefined'){
                            console.log("jobid or hash Parameter missing from jobStatus")
                            return deferred.reject("jobid or hash Parameter missing from jobStatus");
                        }

                        console.log("Tracking job page, jobId = " + jobid + " - hash = " + hash);
                        bgeejobservice.getJobStatus(hash, jobid, true)
                            .then(function(data, status){
                                //console.log(data);
                                deferred.resolve(data)
                            },
                            function(data){
                            //.catch(function(data, status){
                                deferred.reject(data)
                            });
                        return deferred.promise;
                    }]
                }
            })

            .when("/result/:hash", {
                /*webapp: templateUrl: 'js/topanat/views/main.html',*/
                /*topanat: templateUrl: 'views/main.html',*/
                templateUrl: 'js/topanat/views/main.html',
                /*templateUrl:'views/bgeeResult.html',*/
                controller:'MainCtrl',
                controllerAs: 'vm',
                /* controller:'resultController', */
                reloadOnSearch: false,
                resolve:

                {
                    appInit: function(){return true;},
                    loading: function(){return true;},
                    whatIsLoading: function(){console.log("whatIsLoading = results");return 'results.';},

                    jobStatus: ["$q","$route","bgeejobservice", function($q, $route, bgeejobservice){

                        var deferred = $q.defer();
                        var hash = $route.current.params.hash;

                        if(typeof hash == 'undefined'){
                            console.log("Hash parameter missing from jobStatus")
                            console.timeEnd("getJobResultService");
                            return deferred.reject("hash parameter missing from jobStatus");
                        }

                        console.time("getJobResultService");

                        bgeejobservice.getJobResult(hash)
                            .then(function(data){
                                console.log("thendata");
                                console.log(data);
                                console.timeEnd("getJobResultService");
                                deferred.resolve(data)
                            },
                            function(data){
                                console.log("catchdata");
                                console.log(data);
                                console.timeEnd("getJobResultService");
                                bgeejobservice.getJobStatus(hash, 999999, true)
                                    .then(function(data, status){
                                        //console.log(data);
                                        deferred.resolve(data)
                                    }
                                    );
                                //deferred.resolve(data)
                            });
                        console.timeEnd("getJobResultService");
                        return deferred.promise;
                    }]
                }
            })

            .otherwise({
                redirectTo: '/'
            });

    }]);
