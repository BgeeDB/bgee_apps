angular.module("bgeeLoading", [])

.directive('loading', function () {
    return {
        restrict: 'E',
        scope: false,
        replace:true,
        template: '<span><i class="fa fa-circle-o-notch fa-spin"></i> Loading...</span>',
        link: function (scope, element, attr) {
            scope.$watch('loading', function (val) {

                $('#appLoading').hide();
                $(element).hide();
                // don't show loading message for anything for now
/*

                if (val){
                    $(element).show();
                } else{
                    $(element).hide();
                }
*/

            });
        }
    }
})

.directive('bgeeLinkreload', ['$location', '$route', function($location, $route){
    return function(scope, element, attrs) {
        element.bind('click',function(){
            $('#appLoading').show();
            $route.reload();
        });
    }
}]);
