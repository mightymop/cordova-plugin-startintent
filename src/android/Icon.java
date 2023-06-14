package de.mopsdom.startintent;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Icon {

    public static JSONArray getIcons(Context ctx, String packageNames[])
    {
        JSONArray resultArray = new JSONArray();
        for (String itm : packageNames)
        {
            // Icon aus dem App-Paketnamen abrufen
            Drawable appIcon = getAppIcon(ctx,itm);

            if (appIcon!=null) {
                // Drawable in Data-URI konvertieren
                try
                {
                    String datauri = convertDrawableToDataUri(appIcon);

                    if (datauri!=null) {
                        JSONObject obj = new JSONObject();
                        obj.put("packagename", itm);
                        obj.put("datauri", datauri);
                        resultArray.put(obj);
                    }
                } catch (Exception e) {
                    Log.e(ctx.getPackageName(), e.getMessage(), e);
                }
            }
        }

        return resultArray;
    }

    private static List<String> getInstalledAppPackageNames(Context context) {
        List<String> packageNames = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> appInfos = packageManager.getInstalledApplications(0);
        for (ApplicationInfo appInfo : appInfos) {
            packageNames.add(appInfo.packageName);
        }
        return packageNames;
    }

    public static JSONArray getAllIcons(Context ctx)
    {
        List<String> packageNames = getInstalledAppPackageNames(ctx);

        return getIcons(ctx,packageNames.toArray(new String[packageNames.size()]));
    }

    private static Drawable getAppIcon(Context ctx, String packageName) {
        try {
            PackageManager packageManager = ctx.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return appInfo.loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(ctx.getPackageName(),e.getMessage(),e);
            return null;
        }
    }

    private static String convertDrawableToDataUri(Drawable drawable) {
        if (drawable instanceof VectorDrawable) {
            // Icon als Vector XML vorliegend
            return convertVectorDrawableToDataUri((VectorDrawable) drawable);
        } else {
            // Icon als Bitmap oder PNG vorliegend
            return convertBitmapDrawableToDataUri((BitmapDrawable) drawable);
        }
    }

    private static  String convertVectorDrawableToDataUri(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return "data:image/svg+xml;base64," + base64String;
    }

    private static String convertBitmapDrawableToDataUri(BitmapDrawable bitmapDrawable) {
        Bitmap bitmap = bitmapDrawable.getBitmap();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return "data:image/png;base64," + base64String;
    }
}
