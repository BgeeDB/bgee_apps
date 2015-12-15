/**
 * Created by sduvaud on 26/10/15.
 */
(function () {

    'use strict';
    angular
        .module('app')
        .factory('helpservice', helpservice);

    helpservice.$inject = ['$http','logger'];

    function helpservice($http, logger) {


        var service = {
            getHelp : getHelp,
            setHelp: setHelp,
            getOnlineHelp: getOnlineHelp
        };

        return service;

        function getHelp () {
            var value = readTopAnatLocalStorage('topanat-show-help');

            if (typeof value !== 'undefined') {
                /* SD: We need booleans in order to show/hide the panel whereas we get strings!
                 * Hence the hack... */
                if (value === 'true') {
                    return true;
                }
                else if (value === 'false') {
                    return false;
                }
            }
            return true;
        }

        function readTopAnatLocalStorage(name) {
            var value = localStorage.getItem('topanat-show-help');
            return value;
        }

        function setHelp(flag) {
            localStorage.setItem('topanat-show-help', flag);
        }

        function getOnlineHelp(url) {
            return $http.get(url)
                .then(getResults)

            function getResults(response) {
                return response.data
            }
        }
    }
})();



