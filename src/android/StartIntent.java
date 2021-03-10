package de.mopsdom.startintent;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import android.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.provider.MediaStore;
import android.database.Cursor;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.content.ContentResolver;
import android.content.Context;
import android.webkit.MimeTypeMap;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import android.content.ComponentName;
import org.apache.cordova.PluginResult.Status;
import android.os.Build;
import android.os.Bundle;

public class StartIntent extends CordovaPlugin {

    private final String pluginName = "cordova-plugin-startintent";
    private CallbackContext onNewIntentCallbackContext = null;

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
	
    /**
     * Generic plugin command executor
     *
     * @param action
     * @param data
     * @param callbackContext
     * @return
     */
    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) {
        Log.d(pluginName, pluginName + " called with options: " + data);

        Class params[] = new Class[2];
        params[0] = JSONArray.class;
        params[1] = CallbackContext.class;

        try {
            Method method = this.getClass().getDeclaredMethod(action, params);
            method.invoke(this, data, callbackContext);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * Send a JSON representation of the cordova intent back to the caller
     *
     * @param data
     * @param context
     */
    public boolean getCordovaIntent (final JSONArray data, final CallbackContext context) {
        if(data.length() != 0) {
            context.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        }

        Intent intent = cordova.getActivity().getIntent();
        context.sendPluginResult(new PluginResult(PluginResult.Status.OK, getIntentJson(intent)));
        return true;
    }

    /**
     * Register handler for onNewIntent event
     *
     * @param data
     * @param context
     * @return
     */
    public boolean setNewIntentHandler (final JSONArray data, final CallbackContext context) {
        if(data.length() != 1) {
            context.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        }

        this.onNewIntentCallbackContext = context;

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        context.sendPluginResult(result);
        return true;
    }

    /**
     * Triggered on new intent
     *
     * @param intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        if (this.onNewIntentCallbackContext != null) {

            PluginResult result = new PluginResult(PluginResult.Status.OK, getIntentJson(intent));
            result.setKeepCallback(true);
            this.onNewIntentCallbackContext.sendPluginResult(result);
        }
    }

    /**
     * Return JSON representation of intent attributes
     *
     * @param intent
     * @return
     */
    private JSONObject getIntentJson(Intent intent) {
        JSONObject intentJSON = null;
        ClipData clipData = null;
        JSONObject[] items = null;
        ContentResolver cR = this.cordova.getActivity().getApplicationContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            clipData = intent.getClipData();
            if(clipData != null) {
                int clipItemCount = clipData.getItemCount();
                items = new JSONObject[clipItemCount];

                for (int i = 0; i < clipItemCount; i++) {

                    ClipData.Item item = clipData.getItemAt(i);

                    try {
                        items[i] = new JSONObject();
                        items[i].put("htmlText", item.getHtmlText());
                        items[i].put("intent", item.getIntent());
                        items[i].put("text", item.getText());
                        items[i].put("uri", item.getUri());

                        if(item.getUri() != null) {
                            String type = cR.getType(item.getUri());
                            String extension = mime.getExtensionFromMimeType(cR.getType(item.getUri()));

                            items[i].put("type", type);
                            items[i].put("extension", extension);
                        }

                    } catch (JSONException e) {
                        Log.d(pluginName, pluginName + " Error thrown during intent > JSON conversion");
                        Log.d(pluginName, e.getMessage());
                        Log.d(pluginName, Arrays.toString(e.getStackTrace()));
                    }

                }
            }
        }

        try {
            intentJSON = new JSONObject();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(items != null) {
                    intentJSON.put("clipItems", new JSONArray(items));
                }
            }

            intentJSON.put("type", intent.getType());

            intentJSON.put("extras", toJsonObject(intent.getExtras()));
            intentJSON.put("action", intent.getAction());
            intentJSON.put("categories", intent.getCategories());
            intentJSON.put("flags", intent.getFlags());
            intentJSON.put("component", intent.getComponent());
            intentJSON.put("data", intent.getData());
            intentJSON.put("package", intent.getPackage());

            return intentJSON;
        } catch (JSONException e) {
            Log.d(pluginName, pluginName + " Error thrown during intent > JSON conversion");
            Log.d(pluginName, e.getMessage());
            Log.d(pluginName, Arrays.toString(e.getStackTrace()));

            return null;
        }
    }

    private static JSONObject toJsonObject(Bundle bundle) {
        try {
            return (JSONObject) toJsonValue(bundle);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Cannot convert bundle to JSON: " + e.getMessage(), e);
        }
    }

    private static Object toJsonValue(final Object value) throws JSONException {
        if (value == null) {
            return null;
        } else if (value instanceof Bundle) {
            final Bundle bundle = (Bundle) value;
            final JSONObject result = new JSONObject();
            for (final String key : bundle.keySet()) {
                result.put(key, toJsonValue(bundle.get(key)));
            }
            return result;
        } else if (value.getClass().isArray()) {
            final JSONArray result = new JSONArray();
            int length = Array.getLength(value);
            for (int i = 0; i < length; ++i) {
                result.put(i, toJsonValue(Array.get(value, i)));
            }
            return result;
        } else if (
                value instanceof String
                        || value instanceof Boolean
                        || value instanceof Integer
                        || value instanceof Long
                        || value instanceof Double) {
            return value;
        } else {
            return String.valueOf(value);
        }
    }

    public boolean getRealPathFromContentUrl(final JSONArray data, final CallbackContext context) {
        if(data.length() != 1) {
            context.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        }
        ContentResolver cR = this.cordova.getActivity().getApplicationContext().getContentResolver();
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = cR.query(Uri.parse(data.getString(0)),  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            context.sendPluginResult(new PluginResult(PluginResult.Status.OK, cursor.getString(column_index)));
            return true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            context.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        }
    }
}
