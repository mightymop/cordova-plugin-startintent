var exec = require('cordova/exec');
var PLUGIN_NAME = 'StartIntent';

var startintent = {

	startActivity : function (params, success, error ) {
		exec(success, error, 'StartIntent', 'startActivityFromCordova', [params]);
	},
	readDataFromContentUri : function (uri, success, error ) {
		exec(success, error, 'StartIntent', 'readDataFromContentUri', [uri]);
	},
	getCordovaIntent : function (success, error) {
		exec(success, error, 'StartIntent', 'getCordovaIntent', []);
	},
	setNewIntentHandler : function (method ) {
		exec(method, null, 'StartIntent', 'setNewIntentHandler', [method]);
	},
	getRealPathFromContentUrl : function (uri, success, error ) {
		exec(success, error, 'StartIntent', 'getRealPathFromContentUrl', [uri]);
	}
};

module.exports = startintent;
