angular.module('services.config', [])
    .constant('configuration', {
    	    //XXX: maybe we should stop using this mockUrl everywhere it is used
        mockupUrl: GLOBAL_PROPS.getWebAppURLStart(),
        expr_type: 'EXPRESSED',
        data_type: ['RNA-Seq', 'Affymetrix data', 'In situ hybridization', 'EST'],
        selectedDataTypes: ['RNA-Seq', 'Affymetrix data', 'In situ hybridization', 'EST'],
        data_qualities: [{"id":"all", "name":"All"},{"id":"gold", "name":"Gold confidence"}],
        decorrelation_types: [{"id":"classic", "name":"No decorrelation"},{"id":"elim", "name":"Elim"}, {"id":"weight", "name":"Weight"},{"id":"parentchild", "name":"Parent-child"}],
        data_qual: 'all',
        decorr_type: 'classic',
        node_size: 20,
	    nb_node: 20,
        fdr_thr: 0.2,
        pvalueThreshold: 1.0,
        p_value_thr: 1.0,
        nb_rows: 20,
        row_height: 30
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
