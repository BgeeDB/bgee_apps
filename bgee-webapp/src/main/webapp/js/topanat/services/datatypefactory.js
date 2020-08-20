(function () {
    'use strict';

    angular.module('app')
    .factory('DataTypeFactory', DataTypeFactory);

    DataTypeFactory.$inject = ['$http', 'logger'];

    function DataTypeFactory($http, logger) {
        var DataTypes = {};
        DataTypes.names =  [
            { id: "RNA_SEQ", name: 'RNA-Seq' },
            { id: "AFFYMETRIX", name:'Affymetrix data' },
            { id: "IN_SITU", name:'In situ hybridization' },
            { id: "EST", name: 'EST' }
        ];

        var SummaryCallTypes = {};
        SummaryCallTypes = {
            ALL: {allowed : ['AFFYMETRIX', 'EST', 'IN_SITU', 'RNA_SEQ'] },
            //any type of baseline present/absent expression calls
            EXPRESSION: {allowed : ['AFFYMETRIX', 'EST', 'IN_SITU', 'RNA_SEQ'] },
            //baseline present expression calls (presence of expression)
            EXPRESSED: {allowed : ['AFFYMETRIX', 'EST', 'IN_SITU', 'RNA_SEQ'] },
            //baseline no-expression calls (absence of expression explicitly reported)
            NOT_EXPRESSED: {allowed : ['AFFYMETRIX', 'IN_SITU', 'RNA_SEQ'] },
            //diff. expression calls of any kind (either over-expression or under-expression),
            //obtained from diff. expression analyses
            DIFF_EXPRESSION: {allowed : ['AFFYMETRIX', 'RNA_SEQ'] },
            //over-expression calls obtained from diff. expression analyses
            OVER_EXPRESSED: {allowed : ['AFFYMETRIX', 'RNA_SEQ'] },
            //under-expression calls obtained from diff. expression analyses
            UNDER_EXPRESSED: {allowed : ['AFFYMETRIX', 'RNA_SEQ'] },
            //means that a gene was studied in a diff. expression analysis,
            //but was *not* found to be differentially expressed.
            //This is different from NOT_EXPRESSED, as the gene could actually
            //be expressed, but not differentially.
            NOT_DIFF_EXPRESSED: {allowed : ['AFFYMETRIX', 'RNA_SEQ'] }

            };

        var service = {

            allowedDataTypes : allowedDataTypes,
            allDataTypes : allDataTypes

        }

        return service;

        function allDataTypes(){
            console.log("allDataTypes "+DataTypes.names)

            if(DataTypes) {
                return DataTypes;
            }

            if(!DataTypes || DataTypes.length == 0) {
                logger.error('No datatypes found!' , 'Datatypes missing');
                throw "No datatypes found!";
            }
        }

        function allowedDataTypes(callType) {
            console.log('allowedDataTypes ');
            console.log(callType);
            if (typeof callType === 'undefined' || callType.length == 0) {
                throw "A call type must be provided";
            }

            var allowedTypes = SummaryCallTypes[callType].allowed;

            if(!allowedTypes || allowedTypes.length == 0) {
                logger.error('No allowed datatypes for '+callType+' found!');
                throw "No allowed datatypes for "+callType+" found!";
            }
            return allowedTypes
        }

    }

})();
