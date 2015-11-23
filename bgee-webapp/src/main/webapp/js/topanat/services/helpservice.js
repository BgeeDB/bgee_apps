/**
 * Created by sduvaud on 26/10/15.
 */
(function () {

    'use strict';
    angular
        .module('app')
        .factory('helpservice', helpservice);

    helpservice.$inject = ['$http', '$cookies'];

    function helpservice($http, $cookies) {


        var service = {
            getHelpCookie : getHelpCookie,
            setHelpCookie: setHelpCookie,
            getOnlineHelp: getOnlineHelp
        };

        return service;

        function getHelpCookie () {
            var valueFromCookieKey = readTopAnatCookie('topanat-show-help');

            if (typeof valueFromCookieKey !== 'undefined') {
                /* SD: We need booleans in order to show/hide the panel whereas we get strings!
                 * Hence the hack... */
                if (valueFromCookieKey === 'true') {
                    return true;
                }
                else if (valueFromCookieKey === 'false') {
                    return false;
                }
                else {
                    logger.error("getShowHelpCookie - Problem with cookies: " + valueFromCookieKey + " not recognized");
                    return true;
                }
            }
            return true;
        }

        function readTopAnatCookie(name) {
            return $cookies.get(name);
        }

        function setHelpCookie(flag) {
            $cookies.put('topanat-show-help', flag);
        }

        function getOnlineHelp(url) {
            return $http.get(url)
                .then(getResults)

            function getResults(response) {
                console.log(response.data)
                return response.data
            }
        }
    }
})();



