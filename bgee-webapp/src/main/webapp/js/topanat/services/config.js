angular.module('services.config', [])
    .constant('configuration', {
    	//XXX: mockupUrl has stopped being used in all URLs, but actually, this was maybe a bad idea, 
    	//as it would be nice to be ale to run the computations on a different server?
    	//If we want to resurrect this, we would need to go through all URLs used 
    	//to query the server. But maybe this should be part of a bigger update, 
    	//making use of the Bgee JS objects to generate URLs, or maybe by using URLs 
    	//provided by the server in the responses...
        mockupUrl: '/',
        expr_type: 'EXPRESSED',
        data_type: ['RNA-Seq', 'Affymetrix data', 'In situ hybridization', 'EST'],
        selectedDataTypes: ['RNA-Seq', 'Affymetrix data', 'In situ hybridization', 'EST'],
        data_qualities: [{"id":"all", "name":"All"},{"id":"highConfidence", "name":"High confidence"}],
        decorrelation_types: [{"id":"classic", "name":"No decorrelation"},{"id":"elim", "name":"Elim"}, {"id":"weight", "name":"Weigth"},{"id":"parentchild", "name":"Parent-child"}],
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
