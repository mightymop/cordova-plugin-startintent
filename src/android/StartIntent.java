package de.mopsdom.startintent;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StartIntent extends CordovaPlugin {

  private final String pluginName = "cordova-plugin-startintent";
  private CallbackContext onNewIntentCallbackContext = null;

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

  private boolean startApplication(String packagename) {
    try {
      Intent launchIntent = this.cordova.getActivity().getPackageManager().getLaunchIntentForPackage(packagename);
      if (launchIntent != null) {
        this.cordova.getActivity().startActivity(launchIntent);
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      Log.e(pluginName, e.getMessage(), e);
      return false;
    }
  }

  private boolean startActivityFromCordova(JSONObject params) throws JSONException {
    // Intent erstellen
    Intent i = new Intent();

    //Kategorie setzen
    i.addCategory(Intent.CATEGORY_DEFAULT);

    boolean flagsset = false;
    int iflags = 0;
    String action = null;
    String datauri = null;
    String toPackage = null;
    Uri uri = null;
    if (params != null && params.length() > 0) {

      Iterator<String> keys = params.keys();

      while (keys.hasNext()) {
        String key = keys.next();
        Log.i(pluginName, "param key=" + key);

        if (key.equals("action")) {
          action = params.getString(key);
          Log.i(pluginName, "param value=" + params.getString(key));
        } else if (key.equals("topackage")) {
          toPackage = params.getString(key);
          Log.i(pluginName, "param value=" + params.getString(key));
        } else if (key.equals("datauri")) {
          datauri = params.getString(key);
          if (uri == null) {
            uri = Uri.parse(datauri);
          }
          Log.i(pluginName, "param value=" + params.getString(key));
        } else if (key.equals("componentname")) {
          JSONObject jsonObject = (JSONObject) params.get(key);
          jsonObject.getString("package");
          ComponentName componentName = new ComponentName(jsonObject.getString("package"), jsonObject.getString("class"));
          i.putExtra("cmpname", this.cordova.getActivity().getComponentName());
          i.setComponent(componentName);
        } else if (key.equals("flags")) {
          flagsset = true;
          JSONArray flags = (JSONArray) params.get(key);
          for (int n = 0; flags != null && n < flags.length(); n++) {
            try {
              String flag = flags.getString(n);
              Log.i(pluginName, "flag=" + flag);
              if (flag.contains("FLAG_ACTIVITY_CLEAR_TOP")) {
                iflags |= Intent.FLAG_ACTIVITY_CLEAR_TOP;
              } else if (flag.contains("FLAG_ACTIVITY_NO_HISTORY")) {
                iflags |= Intent.FLAG_ACTIVITY_NO_HISTORY;
              } else if (flag.contains("FLAG_ACTIVITY_CLEAR_TASK")) {
                iflags |= Intent.FLAG_ACTIVITY_CLEAR_TASK;
              } else if (flag.contains("FLAG_ACTIVITY_REORDER_TO_FRONT")) {
                iflags |= Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
              } else if (flag.contains("FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS")) {
                iflags |= Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
              } else if (flag.contains("FLAG_ACTIVITY_FORWARD_RESULT")) {
                iflags |= Intent.FLAG_ACTIVITY_FORWARD_RESULT;
              } else if (flag.contains("FLAG_ACTIVITY_MATCH_EXTERNAL")) {
                iflags |= Intent.FLAG_ACTIVITY_MATCH_EXTERNAL;
              } else if (flag.contains("FLAG_ACTIVITY_NEW_TASK")) {
                iflags |= Intent.FLAG_ACTIVITY_NEW_TASK;
              } else if (flag.contains("FLAG_ACTIVITY_NO_ANIMATION")) {
                iflags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
              } else if (flag.contains("FLAG_ACTIVITY_NO_USER_ACTION")) {
                iflags |= Intent.FLAG_ACTIVITY_NO_USER_ACTION;
              } else if (flag.contains("FLAG_ACTIVITY_PREVIOUS_IS_TOP")) {
                iflags |= Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;
              } else if (flag.contains("FLAG_ACTIVITY_RESET_TASK_IF_NEEDED")) {
                iflags |= Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
              } else if (flag.contains("FLAG_ACTIVITY_RETAIN_IN_RECENTS")) {
                iflags |= Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS;
              } else if (flag.contains("FLAG_ACTIVITY_SINGLE_TOP")) {
                iflags |= Intent.FLAG_ACTIVITY_SINGLE_TOP;
              } else if (flag.contains("FLAG_ACTIVITY_TASK_ON_HOME")) {
                iflags |= Intent.FLAG_ACTIVITY_TASK_ON_HOME;
              } else if (flag.contains("FLAG_GRANT_READ_URI_PERMISSION")) {
                iflags |= Intent.FLAG_GRANT_READ_URI_PERMISSION;
              } else if (flag.contains("FLAG_GRANT_WRITE_URI_PERMISSION")) {
                iflags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
              } else if (flag.contains("FLAG_GRANT_PERSISTABLE_URI_PERMISSION")) {
                iflags |= Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
              } else if (flag.contains("FLAG_GRANT_PREFIX_URI_PERMISSION")) {
                iflags |= Intent.FLAG_GRANT_PREFIX_URI_PERMISSION;
              }

            } catch (JSONException e) {
              Log.e(pluginName, e.getMessage(), e);
            }
          }
        } else {
          i.putExtra(key, params.getString(key));
          Log.i(pluginName, "param value=" + params.getString(key));
        }
      }
    }

    if (toPackage != null && uri != null) {
      this.cordova.getActivity().grantUriPermission(toPackage, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    if (uri != null) {
      android.content.ContentResolver cR = this.cordova.getActivity().getContentResolver();
      String mime = cR.getType(uri);

      Log.i(pluginName, "calling setDataAndType");
      i.setDataAndType(uri, mime);
    }

    if (action == null) {
      return false;
    }
    i.setAction(action);

    if (!flagsset) {
      // Flags für Anwendungsstart übergeben: weil externe APP: NEW_TASK und SINGLE_TOP
      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    } else {
      i.setFlags(iflags);
    }

    try {
      //Anwendung starten
      /* if (i.resolveActivity( this.cordova.getActivity().getPackageManager()) != null) {*/
      this.cordova.getActivity().startActivity(i);
      Log.i(pluginName, "Activity gestartet?!");
      return true;
          /*  }
            else {
                Log.e(pluginName, "Activity nicht gestartet, resolveActivity schlug fehl!");
                return false;
            }*/
    } catch (Exception e) {
      Log.e(pluginName, e.getMessage(), e);
      return false;
    }
  }

  private boolean deleteUri(Uri uri) {
    if (uri != null) {
      Log.i(pluginName, "deleteUri (uri = " + uri.toString() + ")");
      try {
        int result = this.cordova.getActivity().getContentResolver().delete(uri, null, null);
        return result == 1 ? true : false;
      } catch (Exception e) {
        Log.e(pluginName, e.getMessage(), e);
      }

      return false;
    } else {
      Log.i(pluginName, "deleteUri exit (uri was null)");
      return false;
    }
  }

  private String readDataFromContentUri(Uri uri) {
    Log.i(pluginName, "readDataFromContentUri start");
    if (uri != null) {
      Log.i(pluginName, "readDataFromContentUri (uri = " + uri.toString() + ")");
      try {
        android.os.ParcelFileDescriptor inputPFD = this.cordova.getActivity().getContentResolver().openFileDescriptor(uri, "r");

        if (inputPFD != null) {
          java.io.FileDescriptor fd = inputPFD.getFileDescriptor();

          java.io.BufferedInputStream bin = new java.io.BufferedInputStream(new java.io.FileInputStream(fd));
          byte[] buffer = new byte[1024];
          int length;
          java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();

          while ((length = bin.read(buffer)) > 0) {
            bout.write(buffer, 0, length);
          }

          bout.flush();
          bin.close();
          bout.close();
          this.cordova.getActivity().getContentResolver().delete(uri, null, null);
          return android.util.Base64.encodeToString(bout.toByteArray(), android.util.Base64.DEFAULT);
        } else {
          Log.i(pluginName, "FileDescriptor was null");
          return null;
        }
      } catch (Exception e) {
        Log.e(pluginName, e.getMessage(), e);
      }

      return null;
    } else {
      Log.i(pluginName, "readDataFromContentUri exit (uri was null)");
      return null;
    }
  }

  @Override
  public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) {

    try {
      if (action.equals("startActivityFromCordova")) {
        if (startActivityFromCordova(new JSONObject(data.getString(0)))) {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ""));
          return true;
        } else {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
          return true;
        }
      } else if (action.equals("startApplicationFromCordova")) {
        if (startApplication(data.getString(0))) {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ""));
          return true;
        } else {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
          return true;
        }
      } else if (action.equals("isPackageAvailable")) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, isPackageAvailable(data.getJSONArray(0))));
        return true;
      } else if (action.equals("isActionAvailable")) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, isActionAvailable(data.getJSONArray(0))));
        return true;
      } else if (action.equals("readDataFromContentUri")) {
        String resultstring = null;
        if ((resultstring = readDataFromContentUri(Uri.parse(data.getString(0)))) != null) {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, resultstring));
          return true;
        } else {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ""));
          return true;
        }
      } else if (action.equals("setNewIntentHandler")) {
        setNewIntentHandler(data, callbackContext);
        return true;
      } else if (action.equals("getCordovaIntent")) {
        getCordovaIntent(data, callbackContext);
        return true;
      } else if (action.equals("getRealPathFromContentUrl")) {
        getRealPathFromContentUrl(data, callbackContext);
        return true;
      } else if (action.equals("killApp")) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ""));
        Log.d(pluginName, "Exiting App...");
		this.cordova.getActivity().finishAffinity();  
		Log.d(pluginName, "finish() called");
        new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				System.exit(0);
			}
		}, 2000);
        return true;
      }
	  else if (action.equals("exitApp")) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ""));
        Log.d(pluginName, "Exiting App...");
		this.cordova.getActivity().finishAffinity();  
		Log.d(pluginName, "finish() called");
        return true;
      }	
	  else if (action.equals("open")) {
        this.openNewActivity(cordova.getActivity(), data.getString(0), callbackContext);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        return true;
      }
      else if (action.equals("openurl")) {
        this.openBrowser(cordova.getActivity(), data.getString(0), callbackContext);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        return true;
      }
      else if (action.equals("getUriForFile")) {
        Uri uri = getUri(data.getString(0));
        if (uri != null) {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, uri.toString()));
          return true;
        } else {
          return false;
        }
      } else if (action.equals("deleteUri")) {
        if (deleteUri(Uri.parse(data.getString(0)))) {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
          return true;
        } else {
          return false;
        }
      } else if (action.equals("getIcons")) {
        String[] stringArray = new String[data.length()];
        for (int i = 0; i < data.length(); i++) {
          try {
            stringArray[i] = data.getString(i);
          } catch (JSONException e) {
            Log.e(pluginName, e.getMessage(), e);
          }
        }

        if (stringArray.length > 0) {
          JSONArray result = Icon.getIcons(this.cordova.getActivity(), stringArray);
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result.toString()));
        } else {
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
        }
        return true;
      } else if (action.equals("getAllIcons")) {
        JSONArray result = Icon.getAllIcons(this.cordova.getActivity());
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result.toString()));
        return true;
      } else if (action.equals("closeApp")) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ""));
        this.cordova.getActivity().finish();
        return true;
      }

      return false;
    } catch (Exception e) {
      Log.e(pluginName, e.getMessage(), e);
      return false;
    }
  }

  /**
   * Send a JSON representation of the cordova intent back to the caller
   *
   * @param data
   * @param context
   */
  public boolean getCordovaIntent(final JSONArray data, final CallbackContext context) {
    if (data != null && data.length() != 0) {
      Log.w(pluginName, data.toString());
      //context.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
      //return false;
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
  public boolean setNewIntentHandler(final JSONArray data, final CallbackContext context) {
    if (data.length() != 1) {
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
    Context ctx = this.cordova.getActivity().getApplicationContext();
    if (ctx == null) {
      ctx = this.cordova.getActivity().getWindow().getContext();
    }
    ContentResolver cR = ctx.getContentResolver();
    MimeTypeMap mime = MimeTypeMap.getSingleton();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      clipData = intent.getClipData();
      if (clipData != null) {
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

            if (item.getUri() != null) {
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
        if (items != null) {
          intentJSON.put("clipItems", new JSONArray(items));
        }
      }

      String referrer = "";
      if (cordova.getActivity().getReferrer() != null && cordova.getActivity().getReferrer().getHost() != null) {
        referrer = cordova.getActivity().getReferrer().getHost();
      }
      intentJSON.put("type", intent.getType());
      intentJSON.put("caller", referrer);
      intentJSON.put("extras", intent.getExtras() != null ? toJsonObject(intent.getExtras()) : "");
      intentJSON.put("action", intent.getAction());
      intentJSON.put("categories", intent.getCategories());
      intentJSON.put("flags", intent.getFlags());
      intentJSON.put("component", intent.getComponent());
      intentJSON.put("data", intent.getData());
      intentJSON.put("package", intent.getPackage());
      Log.i(pluginName, intentJSON.toString());
      return intentJSON;
    } catch (Exception e) {
      Log.d(pluginName, pluginName + " Error thrown during intent > JSON conversion");
      Log.d(pluginName, e.getMessage());
      Log.d(pluginName, Arrays.toString(e.getStackTrace()));

      return null;
    }
  }

  private void openNewActivity(Context context, String file, CallbackContext callbackContext) {

    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_VIEW);

    Uri uri = getUri(file);
    android.content.ContentResolver cR = context.getContentResolver();
    String mime = cR.getType(uri);

    intent.setDataAndType(uri, mime);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    try {
      this.cordova.getActivity().startActivity(intent);
      callbackContext.success();
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
    }
  }

  private void openBrowser(Context context, String url, CallbackContext callbackContext) {

    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_VIEW);

    intent.setData(Uri.parse(url));

    try {
      this.cordova.getActivity().startActivity(intent);
      callbackContext.success();
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
    }
  }

  public JSONArray isPackageAvailable(JSONArray packageIds) {
    JSONArray result = new JSONArray();

    PackageManager pm = cordova.getActivity().getPackageManager();

    for (int n = 0; n < packageIds.length(); n++) {
      try {
        String packageId = packageIds.getString(n);
        PackageInfo info = pm.getPackageInfo(packageId, PackageManager.GET_META_DATA);
        if (info != null) {
          result.put(packageId);
        }
      } catch (Exception e) {
      }
    }

    return result;
  }

  public JSONArray isActionAvailable(JSONArray actionFilter) {
    JSONArray result = new JSONArray();

    PackageManager pm = cordova.getActivity().getPackageManager();
    List<ApplicationInfo> packages = pm.getInstalledApplications(0);

    for (int n = 0; n < actionFilter.length(); n++) {
      try {
        String filter = actionFilter.getString(n);
        List<ResolveInfo> res = pm.queryIntentActivities(new Intent(filter, null), 0);
        if (res != null && res.size() > 0) {
          result.put(filter);
        }
      } catch (Exception e) {
      }
    }

    return result;
  }

  public String getPackageID() {
    Log.i(pluginName, "getPackageID...");
    //return  (String) org.apache.cordova.BuildHelper.getBuildConfigValue(cordova.getActivity(), "APPLICATION_ID");
    return cordova.getActivity().getPackageName();
  }

  public Uri getUri(String file) {
    Log.i(pluginName, "getUri..." + (file != null ? file : "NULL"));
    Context context = cordova.getActivity();
    String packageid = getPackageID();
    try {
      file = java.net.URLDecoder.decode(file);
      if (new File(Uri.parse(file).getPath()).exists()) {
        return OpenfileProvider.getUriForFile(context, packageid + ".startintent.provider", new File(Uri.parse(file).getPath()));
      }
    } catch (Exception e) {

    }

    Log.i(pluginName, "packageid=" + packageid);
    if (file.contains(packageid)) {
      Log.i(pluginName, "file =" + file);
      file = file.substring(file.indexOf(packageid) + 1 + packageid.length());
      Log.i(pluginName, "file after substring=" + file);
    }

    boolean translatepath_internal = false;
    boolean translatepath_external = false;
    String path = cordova.getContext().getFilesDir().getAbsolutePath();
    String external = cordova.getContext().getExternalFilesDir(null).getAbsolutePath();
    if (!(new File(path)).exists()) {
      Log.w(pluginName, path + " does not exist, try to recover internal path...");
      if (path.startsWith("/data/user")) {
        translatepath_internal = true;
        path = "/data/data/" + packageid;
        Log.w(pluginName, "Using " + path + " as new (internal) root");
      } else {
        Log.e(pluginName, "Could not recover internal path...");
      }
    } else {
      Log.i(pluginName, "Path (internal) translation not needed");
    }

    if (!(new File(external)).exists()) {
      Log.w(pluginName, external + " does not exist, try to recover external path...");
      if (external.startsWith("/storage/emulated")) {
        translatepath_external = true;
        external = "/sdcard/Android/data/" + packageid;
        Log.w(pluginName, "Using " + external + " as new (external) root");
      } else {
        Log.e(pluginName, "Could not recover external path...");
      }
    } else {
      Log.i(pluginName, "Path (external) translation not needed");
    }

    File ffile = !translatepath_internal ? new File(cordova.getContext().getFilesDir(), file) : new File(path + "/files", file);
    Log.i(pluginName, "TEST: " + ffile.getAbsolutePath());
    if (!ffile.exists()) {
      ffile = !translatepath_internal ? new File(cordova.getContext().getCacheDir(), file) : new File(path + "/cache", file);
      Log.i(pluginName, "TEST: " + ffile.getAbsolutePath());
      if (!ffile.exists()) {
        ffile = !translatepath_internal ? new File(cordova.getContext().getDataDir(), file) : new File(path, file);
        Log.i(pluginName, "TEST: " + ffile.getAbsolutePath());
        if (!ffile.exists()) {
          ffile = !translatepath_external ? new File(cordova.getContext().getExternalFilesDir(null), file) : new File(external + "/files", file);
          Log.i(pluginName, "TEST: " + ffile.getAbsolutePath());
          if (!ffile.exists()) {
            ffile = !translatepath_external ? new File(cordova.getContext().getExternalCacheDir(), file) : new File(external + "/cache", file);
            Log.i(pluginName, "TEST: " + ffile.getAbsolutePath());
            if (!ffile.exists()) {
              Log.e(pluginName, "getUri: file (" + file + ") does not exist");
              return null;
            }
          }
        }
      }
    }

    Uri uri = null;

    try {
      uri = OpenfileProvider.getUriForFile(context, packageid + ".startintent.provider", ffile);
    } catch (Exception e) {
      Log.e(pluginName, e.getMessage(), e);
    }

    return uri;
  }

  public boolean getRealPathFromContentUrl(final JSONArray data, final CallbackContext context) {
    if (data.length() != 1) {
      context.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
      return false;
    }
    ContentResolver cR = this.cordova.getActivity().getApplicationContext().getContentResolver();
    Cursor cursor = null;
    try {
      String[] proj = {MediaStore.Images.Media.DATA};
      cursor = cR.query(Uri.parse(data.getString(0)), proj, null, null, null);
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
