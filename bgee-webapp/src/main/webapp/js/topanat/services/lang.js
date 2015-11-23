(function() {
    'use strict';

angular.module('services.lang', [])
    .constant('lang', {
        jobProgressStart: 'Processing job, waiting for a job id. Please do not close this window.',
        jobProgress: 'Job has been submitted as jobid: ',
        jobProgressBookmark: 'After bookmarking this page, it is safe to close this window. ' 
                             + 'Your analysis is being run on our server, and the results will appear '
                             + 'as soon as available. Please note that the results can be slow '
                             + 'to compute, typically from 1 to 10 minutes, depending on the amount of data to process. '
                             + 'It is not necessary to refresh this page, it will be automatically updated.'

    });

})();