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
        'ngCookies',
        'logger',
        'ui.grid',
        'ui.grid.resizeColumns',
        'ui.grid.autoResize',
        'services.config',
        'services.lang',
        'angularFileUpload',
        'ui.bootstrap',
        'ngLocationUpdate'
    ])

    //.value('DataType', DataType)
    //.value('CallType', CallType)

    //.config(function($locationProvider) { $locationProvider.html5Mode(true); })

    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'js/topanat/views/main.html',
                controller: 'MainCtrl',
                controllerAs: 'vm',
                resolve: {
                    "jobStatus": function() {
                        return null;
                    }

                }
            })

            .when("/result/:hash/:jobid", {
                templateUrl: 'js/topanat/views/main.html',
                /*templateUrl:'views/bgeeResult.html',*/
                controller:'MainCtrl',
                controllerAs: 'vm',
                /* controller:'resultController', */
                reloadOnSearch: false,
                resolve:

                {
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
                    jobStatus: function($q, $route, bgeejobservice){
                        var deferred = $q.defer();
                        var jobid = $route.current.params.jobid;
                        var hash = $route.current.params.hash;
                        console.log("Tracking job page, jobId = " + jobid + " - hash = " + hash);
                        bgeejobservice.getJobStatus(hash, jobid, true)
                            .then(function(data, status){
                                //console.log(data);
                                deferred.resolve(data)
                            })
                            .catch(function(data, status){
                                deferred.reject(data)
                            });
                        return deferred.promise;
                    }
                }
            })

            .when("/result/:hash", {
                templateUrl: 'js/topanat/views/main.html',
                /*templateUrl:'views/bgeeResult.html',*/
                controller:'MainCtrl',
                controllerAs: 'vm',
                /* controller:'resultController', */
                reloadOnSearch: false,
                resolve:

                {
                	//FIXME: same problem of routing triggering as described above
                    jobStatus: function($q, $route, bgeejobservice){
                        var deferred = $q.defer();
                        var hash = $route.current.params.hash;
                        bgeejobservice.getJobData(hash)
                            .then(function(data, status){
                                //console.log(data);
                                deferred.resolve(data)
                            })
                            .catch(function(data, status){
                                deferred.reject(data)
                            });
                        return deferred.promise;
                    }
                }
            })

            .otherwise({
                redirectTo: '/'
            });

    });
