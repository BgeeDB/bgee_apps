(function() {
	'use strict';
	angular.module("app")

	.directive('bgeeLoading', function () {
		return {
			restrict: 'C',
			scope: true,
			link: function (scope, element, attr) {
				var loadingMessageCallback = function() {
					console.log("Show waiting message from directive");
					//TODO: do it the angular way
					$('#appLoading').show();
				};
				element.on('click', loadingMessageCallback);
//            scope.$watch('loading', function (val) {
//
//                $('#appLoading').hide();
//                $(element).hide();
//                // don't show loading message for anything for now
///*
//
//                if (val){
//                    $(element).show();
//                } else{
//                    $(element).hide();
//                }
//*/
//
//            });
			}
		}
	});
})();
