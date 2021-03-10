var exec = require('cordova/exec');
var PLUGIN_NAME = 'StartIntent';

var startintent = {

	cordova.startActivity : function (params, success, error ) {
		exec(success, error, 'StartIntent', 'startActivityFromCordova', [params]);
	},
	cordova.readDataFromContentUri : function (uri, success, error ) {
		exec(success, error, 'StartIntent', 'readDataFromContentUri', [uri]);
	},
	cordova.getCordovaIntent : function (success, error) {
		exec(success, error, 'StartIntent', 'getCordovaIntent', []);
	},
	cordova.setNewIntentHandler : function (method ) {
		exec(method, null, 'StartIntent', 'setNewIntentHandler', [method]);
	},
	cordova.getRealPathFromContentUrl : function (uri, success, error ) {
		exec(success, error, 'StartIntent', 'getRealPathFromContentUrl', [uri]);
	}
};

module.exports = startintent;
