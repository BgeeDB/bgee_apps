angular.module('services.config', [])
    .constant('configuration', {
        mockupUrl: 'http://swt-dev.vital-it.ch/bgee/topanat_mockup_srv/topanat_mockup.php',
        expressionType: 'EXPRESSION',
        dataTypes: ['RNA-Seq', 'Affymetrix data', 'In situ hybridization', 'EST'],
        selectedDataTypes: ['RNA-Seq', 'Affymetrix data', 'In situ hybridization', 'EST'],
        dataQuality: 'all',
        decorrelationType: 'parent-child',
        nodeSize: 20,
        fdrThreshold: '0.99',
        pvalueThreshold: 'e-10',
        nbNodes: 10
    });



(function () {
    'use strict';

    var core = angular.module('app');
    console.log("config")
    core.config(toastrConfig);

    toastrConfig.$inject = ['toastr'];
    /* @ngInject */
    function toastrConfig(toastr) {
        console.log("toastrconfig")
        toastr.options.timeOut = 4000;
        toastr.options.positionClass = 'toast-top-right';
    }

    var config = {
        appErrorPrefix: '[bgeeNg Error] ',
        appTitle: 'bgeeNg'
    };

    core.value('config', config);

    core.config(configure);

    configure.$inject = ['$logProvider'];
    /* @ngInject */
    function configure($logProvider) {
        console.log("configure")
        if ($logProvider.debugEnabled) {
            $logProvider.debugEnabled(true);
        }
        //exceptionHandlerProvider.configure(config.appErrorPrefix);
        //routerHelperProvider.configure({docTitle: config.appTitle + ': '});
    }

})();