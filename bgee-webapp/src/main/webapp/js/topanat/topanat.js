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
        'angularFileUpload',
        'ui.bootstrap'
    ])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/main.html',
                controller: 'MainCtrl',
                controllerAs: 'vm'
            })
            .otherwise({
                redirectTo: '/'
            });
    });