angular.module('services.config', [])
    .constant('configuration', {
        mockupUrl: 'http://localhost:8080',
        expr_type: 'EXPRESSED',
        data_type: ['RNA-Seq', 'Affymetrix data', 'In situ hybridization', 'EST'],
        selectedDataTypes: ['RNA-Seq', 'Affymetrix data', 'In situ hybridization', 'EST'],
        data_qual: 'all',
        decorr_type: 'parentchild',
        node_size: 20,
	nb_node: 20,
        fdr_thr: '0.99',
        pvalueThreshold: 'e-10',
        p_value_thr: 10
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
