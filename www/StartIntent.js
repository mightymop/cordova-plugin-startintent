var exec = require('cordova/exec');
var PLUGIN_NAME = 'StartIntent';

var startintent = {

	startActivity : function (params, success, error ) {
		exec(success, error, 'StartIntent', 'startActivityFromCordova', [params]);
	},
	startApplication : function (params, success, error ) {
		exec(success, error, 'StartIntent', 'startApplicationFromCordova', [params]);
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
	},
	killApp : function (success, error ) {
		exec(success, error, 'StartIntent', 'killApp', []);
	},
	closeApp : function (success, error ) {
		exec(success, error, 'StartIntent', 'closeApp', []);
	},
	open: function(filename, cb)
    {
        exec(cb, cb, 'StartIntent', 'open', [filename]);
    },
    getUriForFile: function(filename, cb)
    {
        exec(cb, cb, 'StartIntent', 'getUriForFile', [filename]);
    },
    deleteUri: function(uri, cb)
    {
        exec(cb, cb, 'StartIntent', 'deleteUri', [uri]);
    },
	isPackageAvailable: function(params,success, error)
	{
		exec(success, error, 'StartIntent', 'isPackageAvailable', [params]); //params = array
	},
	isActionAvailable: function(params,success, error)
	{
		exec(success, error, 'StartIntent', 'isActionAvailable', [params]); //params = array
	},
	getIcons: function(params,success, error)
	{
		exec(success, error, 'StartIntent', 'getIcons', params); //params = array
	},
	getAllIcons: function(success, error)
	{
		exec(success, error, 'StartIntent', 'getAllIcons', []); 
	}
};

module.exports = startintent;
