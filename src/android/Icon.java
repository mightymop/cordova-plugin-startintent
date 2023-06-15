package de.mopsdom.startintent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Icon {

    public static JSONArray getIcons(Context ctx, String packageNames[])
    {
        return getAllIcons(ctx, Arrays.asList(packageNames));
    }

    public static JSONArray getAllIcons(Context ctx)
    {
        return getAllIcons(ctx,null);
    }

    private static JSONArray getAllIcons(Context ctx,List<String> packageNames)
    {
        JSONArray resultArray = new JSONArray();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : resolveInfos) {
            if (packageNames==null||packageNames.contains(resolveInfo.activityInfo.packageName))
            {
                ComponentName componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                try {
                    Drawable appIcon = packageManager.getActivityIcon(componentName);

                    if (appIcon!=null) {
                        // Drawable in Data-URI konvertieren
                        try
                        {
                            String datauri = convertDrawableToDataUri(ctx,appIcon);

                            if (datauri!=null) {
                                String label = resolveInfo.activityInfo.nonLocalizedLabel!=null?resolveInfo.activityInfo.nonLocalizedLabel.toString():null;
                                if (label==null)
                                {
                                    ApplicationInfo appInfo = resolveInfo.activityInfo.applicationInfo;
                                    if (appInfo==null) {
                                        appInfo = packageManager.getApplicationInfo(componentName.getPackageName(), 0);
                                    }
                                    label = packageManager.getApplicationLabel(appInfo).toString();
                                }

                                JSONObject obj = new JSONObject();
                                obj.put("packagename", componentName.getPackageName());
                                obj.put("activity_name",resolveInfo.activityInfo.name);
                                obj.put("application_label",label);
                                obj.put("datauri", datauri);
                                resultArray.put(obj);
                            }
                        } catch (Exception e) {
                            Log.e(ctx.getPackageName(), e.getMessage(), e);
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(ctx.getPackageName(), e.getMessage(), e);
                }
            }
        }

        return resultArray;
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

    private static Bitmap drawableToBitmap(Context ctx,Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof AdaptiveIconDrawable) {
            AdaptiveIconDrawable adaptiveIconDrawable = (AdaptiveIconDrawable) drawable;
            Bitmap bitmap = Bitmap.createBitmap(
                    adaptiveIconDrawable.getIntrinsicWidth(),
                    adaptiveIconDrawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            adaptiveIconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            adaptiveIconDrawable.draw(canvas);
            return bitmap;
        } else {
            Log.e(ctx.getPackageName(),"Unsupported drawable type: " + drawable.getClass().getSimpleName());
            return null;
        }
    }

    private static String convertDrawableToDataUri(Context ctx,Drawable drawable) {
        if (drawable instanceof VectorDrawable) {
            // Icon als Vector XML vorliegend
            return convertVectorDrawableToDataUri((VectorDrawable) drawable);
        } else {
            // Icon als Bitmap oder PNG vorliegend
            if (drawable instanceof AdaptiveIconDrawable)
            {
                Bitmap bmp = drawableToBitmap(ctx,drawable);
                if (bmp!=null)
                {
                    return convertBitmapToDataUri(bmp);
                }
                else
                {
                    return null;
                }
            }
            else {
                return convertBitmapDrawableToDataUri((BitmapDrawable) drawable);
            }
        }
    }

    private static String convertBitmapToDataUri(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        return "data:image/png;base64," + base64String;
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
        String base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        return "data:image/png;base64," + base64String;
    }

    private static String convertBitmapDrawableToDataUri(BitmapDrawable bitmapDrawable) {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        return convertBitmapToDataUri(bitmap);
    }
}
