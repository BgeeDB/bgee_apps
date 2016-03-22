(function() {
    'use strict';

angular.module('services.lang', [])
    .constant('lang', {
        jobProgressStart: 'Processing job, waiting for a job id. Please stand by until the job is started, ' 
        	            + 'or until results are retrieved, if they already exist on our server ' 
        	            + '(in that case, we won\'t send you a notification email, if you provided your address)',
        jobProgress: 'Job has been submitted as jobid: ',
        jobProgressBookmark: 'After bookmarking this page, it is safe to close this window. ' 
                            + 'Your analysis is being run on our server, and the results will appear '
                            + 'as soon as available. Please note that the results can be slow '                            
                            + 'to compute, typically from 1 to 30 minutes, depending on the amount of data to process. '
                            + 'It is not necessary to refresh this page, it will be automatically updated.'
    });

})();