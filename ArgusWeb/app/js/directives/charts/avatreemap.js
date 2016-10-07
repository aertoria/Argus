angular.module('argus.directives.charts.avatreemap', [])
.directive('agAvaTreemap', ['DashboardService', 'growl', 'VIEWELEMENT', function(DashboardService, growl, VIEWELEMENT) {
    var treemapNameIndex=1;
    return {
        restrict: 'E',
        transclude: true,
        scope: {},
        require: '^agDashboard',
        controller: 'ViewElements',
        template: '<div ng-transclude=""> </div>',
        link: function(scope, element, attributes, dashboardCtrl){
            DashboardService.buildViewElement(scope, element, attributes, dashboardCtrl, VIEWELEMENT.avatreemap, treemapNameIndex++, DashboardService);
        }
    }
}]);