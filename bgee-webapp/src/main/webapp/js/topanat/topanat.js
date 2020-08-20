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
        'ngFileSaver'
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

            //Don't remove the trailing slash, it is to correctly reload the view 
            //when clicking on a TopAnat link with no trailing slash (as recent job or example links), 
            //see http://stackoverflow.com/a/17588833/1768736
            .when("/result/:hash/:jobid/", {
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

                    jobStatus: ["$q","$route","bgeejobservice", function($q, $route, bgeejobservice){

                        var deferred = $q.defer();
                        console.log($route.current.params);
                        var jobid = $route.current.params.jobid;
                        var hash = $route.current.params.hash;
                        if(typeof jobid === 'undefined' || typeof hash === 'undefined'){
                            console.log("jobid or hash Parameter missing from jobStatus")
                            return deferred.reject("jobid or hash Parameter missing from jobStatus");
                        }

                        //Of note, when the URL is updated following a form submission,
                        //it triggers this routing, but AngularJS can't be aware of
                        //the hash and jobid vars
                        //(see second comment of this answer: http://stackoverflow.com/a/16266601/1768736)
                        //This generates an error on our server (query to server
                        //with 'undefined' as value for jobid and hash, see log below).
                        //The triggering of this routing is not even needed at this point,
                        //after form submission there is already a tracking of job advancement activated.
                        //Fix:
                        //FB: I couldn't find a way to avoid triggering the reload
                        //when updating the path in the controller. This led to the 'getJobStatus'
                        //function to be called again here after job retrieval,
                        //sometimes with the wrong hash/jobId value (the one used to land on the page,
                        //for instance by clicking a previous job link). It could cause incorrect results
                        //to be displayed, and anyway to have useless queries to the server.
                        //So, in the controller, we store the timestamp before changing the path.
                        //This timestamp will be reset anyway by this initialization below,
                        //we could have used a boolean, but just in case the code is interrupted
                        //before reseting the value (leading to initialization to never occur again),
                        //we use a timestamp to check it's not too old.
                        //If there is a timestamp set and is less than 5s, the 'getJobStatus'
                        //is not triggered. The risk of collision between tab is low,
                        //since the value is normally immediately reset.
                        var topAnatRouteChangeWithoutReloadTimestamp =
                            window.localStorage['topAnatRouteChangeWithoutReloadTimestamp'];
                        var currentTimestamp = new Date().getTime();
                        console.log('getJobStatus topAnatRouteChangeWithoutReloadTimestamp: '
                                + topAnatRouteChangeWithoutReloadTimestamp);
                        if (!topAnatRouteChangeWithoutReloadTimestamp ||
                                currentTimestamp - topAnatRouteChangeWithoutReloadTimestamp >= 5000) {
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
                        }
                        //reset the timestamp to trigger initialization when needed
                        window.localStorage['topAnatRouteChangeWithoutReloadTimestamp'] = 0;
                        return deferred.promise;
                    }]
                }
            })

            //Don't remove the trailing slash, it is to correctly reload the view 
            //when clicking on a TopAnat link with no trailing slash (as recent job or example links), 
            //see http://stackoverflow.com/a/17588833/1768736
            .when("/result/:hash/", {
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

                        if(typeof hash === 'undefined'){
                            console.log("Hash parameter missing from jobStatus")
                            return deferred.reject("hash parameter missing from jobStatus");
                        }

                        //FB: I couldn't find a way to avoid triggering the reload
                        //when updating the path in the controller. This led to the 'getJobResult'
                        //function to be called again here after result retrieval,
                        //sometimes with the wrong hash value (the one used to land on the page,
                        //for instance by clicking a previous job link). It could cause incorrect results
                        //to be displayed, and anyway to have useless queries to the server.
                        //So, in the controller, we store the timestamp before changing the path.
                        //This timestamp will be reset anyway by this initialization below,
                        //we could have used a boolean, but just in case the code is interrupted
                        //before reseting the value (leading to initialization to never occur again),
                        //we use a timestamp to check it's not too old.
                        //If there is a timestamp set and is less than 5s, the 'getJobResult'
                        //is not triggered. The risk of collision between tab is low,
                        //since the value is normally immediately reset.
                        var topAnatRouteChangeWithoutReloadTimestamp =
                            window.localStorage['topAnatRouteChangeWithoutReloadTimestamp'];
                        var currentTimestamp = new Date().getTime();
                        console.log('getJobResult topAnatRouteChangeWithoutReloadTimestamp: '
                                + topAnatRouteChangeWithoutReloadTimestamp);
                        if (!topAnatRouteChangeWithoutReloadTimestamp ||
                                currentTimestamp - topAnatRouteChangeWithoutReloadTimestamp >= 5000) {
                            console.time("getJobResultService");
                            console.log("reload getJobResult with hash: " + hash);

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
                                    bgeejobservice.getJobStatus(hash, -1, true)
                                        .then(function(data, status){
                                            //console.log(data);
                                            deferred.resolve(data)
                                        }
                                        );
                                    //deferred.resolve(data)
                                });
                        }
                        //reset the timestamp to trigger initialization when needed
                        window.localStorage['topAnatRouteChangeWithoutReloadTimestamp'] = 0;
                        return deferred.promise;
                    }]
                }
            })

            .otherwise({
                redirectTo: '/'
            });

    }]);
