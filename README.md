# Android:

### 1. Add plugin
cordova plugin add https://github.com/mightymop/cordova-plugin-startintent.git
### 2. For Typescript add following code to main ts file: 
/// &lt;reference types="cordova-plugin-startintent" /&gt;<br/>
### 3. Usage:
```
window.startintent.startActivity(option, success, error);
option = {
  action: string, //required
  callbackurl: string, //required
  componentname: {  //optional
    package: string,
    class: string
  }
  
  //custom params here
}

 window.startintent.readDataFromContentUri(uri: string, success :((result:any)=>void, error(()=>void);
```
Example:
```
 window.startintent.startActivity({action:'target.intent.action',callbackurl:'callback.filter.intent.of.caller.app',componentname:{"package":"target.apps.package.name","class":"target.apps.class.name.fqn"}},
                            function (res:any){
                                console.log(res);
                            },
                            function (err:any){
                                console.error(err);
                            });
```
