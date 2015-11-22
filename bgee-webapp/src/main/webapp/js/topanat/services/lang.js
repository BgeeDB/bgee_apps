(function() {
    'use strict';

angular.module('services.lang', [])
    .constant('lang', {
        jobProgressStart: 'Processing job, waiting for a job id. Please do not close this window.',
        jobProgress: 'Job has been submitted as jobid: ',
        jobProgressBookmark: 'After bookmarking this page it\'s safe to close this window.'

    });

})();