
reportModule.config(function ($stateProvider, $urlRouterProvider) {

	$urlRouterProvider.otherwise('/');

	$stateProvider
		.state('report', {
			url: '/',
			templateUrl: 'templates/report.html',
			controller: 'ReportController'
		})
});

