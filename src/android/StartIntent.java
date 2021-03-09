package de.mopsdom.startintent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

public class StartIntent extends CordovaPlugin {
    //public CallbackContext mCallbackContext;

    public void initialize(org.apache.cordova.CordovaInterface cordova, org.apache.cordova.CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    private boolean startActivityFromCordova(JSONObject params) throws JSONException
    {
        // Intent erstellen
        Intent i = new Intent();
  
        //Kategorie setzen
        i.addCategory(Intent.CATEGORY_DEFAULT);

        // Flags für Anwendungsstart übergeben: weil externe APP: NEW_TASK und SINGLE_TOP
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        
        if (params!=null) {

            Iterator<String> keys = params.keys();

            while(keys.hasNext()) {
                String key = keys.next();
                  
                if (key.equals("action"))
                {
                    i.setAction(params.getString(key));
                }
                else
                if (key.equals("datauri"))
                {
                    i.setData(Uri.parse(params.getString(key)));
                }
                else
                if (key.equals("componentname"))
                {
                    JSONObject jsonObject = (JSONObject) params.get(key);
                    jsonObject.getString("package");
                    ComponentName componentName = new ComponentName(jsonObject.getString("package"),jsonObject.getString("class"));
                    i.putExtra("cmpname",this.cordova.getActivity().getComponentName());
                    i.setComponent(componentName);
                }
                else
                {
                    i.putExtra(key, params.getString(key));
                }
            }
        }

        try {
            //Anwendung starten
            this.cordova.getActivity().startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String readDataFromContentUri(Uri uri)
    {
        if (uri!=null)
        {
            try
            {
                android.os.ParcelFileDescriptor inputPFD = this.cordova.getActivity().getContentResolver().openFileDescriptor(uri, "r");

                java.io.FileDescriptor fd = inputPFD.getFileDescriptor();

                java.io.BufferedInputStream bin = new java.io.BufferedInputStream(new  java.io.FileInputStream(fd));
                byte[] buffer = new byte[1024];
                int length;
                java.io.ByteArrayOutputStream bout = new  java.io.ByteArrayOutputStream();
                while((length = bin.read(buffer)) > 0)
                {
                    bout.write(buffer, 0, length);
                }

                bout.flush();
                bin.close();
                bout.close();
                this.cordova.getActivity().getContentResolver().delete(uri,null,null);
                return new String(bout.toByteArray());
            } catch (Exception e) {

            }

            return null;
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException  {

        if (action.equals("startActivityFromCordova")) {
            if (startActivityFromCordova(new JSONObject(data.getString(0))))
            {
                callbackContext.sendPluginResult(new PluginResult(Status.OK, ""));
                return true;
            }
            else
            {
                callbackContext.sendPluginResult(new PluginResult(Status.ERROR, ""));
                return true;
            }
        }
		else
		if (action.equals("readDataFromContentUri")) {
			String resultstring=null;
            if ((resultstring=readDataFromContentUri(Uri.parse(data.getString(0))))!=null)
            {
                callbackContext.sendPluginResult(new PluginResult(Status.OK, resultstring));
                return true;
            }
            else
            {
                callbackContext.sendPluginResult(new PluginResult(Status.ERROR, ""));
                return true;
            }
        }
        return false;
    }
}
