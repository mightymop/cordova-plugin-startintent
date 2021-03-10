var exec = require('cordova/exec');
var PLUGIN_NAME = 'StartIntent';

var startintent = {

	startActivity : function (params, success, error ) {
		exec(success, error, 'StartIntent', 'startActivityFromCordova', [params]);
	},
	readDataFromContentUri : function (uri, success, error ) {
		exec(success, error, 'StartIntent', 'readDataFromContentUri', [uri]);
	},
	setNewIntentHandler : function (uri, success, error ) {
		exec(success, error, 'StartIntent', 'setNewIntentHandler', [uri]);
	},
	getCordovaIntent : function (uri, success, error ) {
		exec(success, error, 'StartIntent', 'getCordovaIntent', [uri]);
	},
	getRealPathFromContentUrl : function (uri, success, error ) {
		exec(success, error, 'StartIntent', 'getRealPathFromContentUrl', [uri]);
	}
};

module.exports = startintent;
