package de.mopsdom.startintent;

import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

public class OpenfileProvider extends FileProvider {

    private static String pluginName = "cordova-plugin-startintent";
    private boolean translatepath_internal = false;
    private boolean translatepath_external = false;
	private String internal = null;
	private String external = null;

    public String getProviderURI(){
        return "content://"+getContext().getPackageName()+".startintent.provider";
    }

	private void pathCheck()
	{
        internal = getContext().getFilesDir().getAbsolutePath();
        external = getContext().getExternalFilesDir(null).getAbsolutePath();
		String packageid = getContext().getPackageName();
        if (!(new File(internal)).exists())
        {
            Log.w(pluginName,internal+" does not exist, try to recover internal path...");
            if (internal.startsWith("/data/user"))
            {
                translatepath_internal=true;
                internal = "/data/data/"+packageid;
                Log.w(pluginName,"Using "+internal+" as new (internal) root");
            }
            else
            {
                Log.e(pluginName,"Could not recover internal path...");
            }
        }
        else {
            Log.i(pluginName,"Path (internal) translation not needed");
        }

        if (!(new File(external)).exists())
        {
            Log.w(pluginName,external+" does not exist, try to recover external path...");
            if (external.startsWith("/storage/emulated"))
            {
                translatepath_external=true;
                external = "/sdcard/Android/data/"+packageid;
                Log.w(pluginName,"Using "+external+" as new (external) root");
            }
            else
            {
                Log.e(pluginName,"Could not recover external path...");
            }
        }
        else {
            Log.i(pluginName,"Path (external) translation not needed");
        }
	}

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        pathCheck();

        File ffile = !translatepath_internal ? new File(getContext().getFilesDir(),uri.getPath()):new File(internal+"/files",uri.getPath());
        Log.i(pluginName,"TEST: "+ffile.getAbsolutePath());
        if (!ffile.exists())
        {
            ffile = !translatepath_internal ? new File(getContext().getCacheDir(),uri.getPath()):new File(internal+"/cache",uri.getPath());
            Log.i(pluginName,"TEST: "+ffile.getAbsolutePath());
            if (!ffile.exists())
            {
                ffile = !translatepath_internal ? new File(getContext().getDataDir(),uri.getPath()):new File(internal,uri.getPath());
                Log.i(pluginName,"TEST: "+ffile.getAbsolutePath());
                if (!ffile.exists())
                {
                    ffile =  !translatepath_external ? new File(getContext().getExternalFilesDir(null),uri.getPath()):new File(external+"/files",uri.getPath());
                    Log.i(pluginName,"TEST: "+ffile.getAbsolutePath());
                    if (!ffile.exists())
                    {
                        ffile =  !translatepath_external ? new File(getContext().getExternalCacheDir(),uri.getPath()):new File(external+"/cache",uri.getPath());
                        Log.i(pluginName,"TEST: "+ffile.getAbsolutePath());
                        if (!ffile.exists())
                        {
                            Log.e(pluginName, "getUri: file ("+uri.getPath()+") does not exist");
                            return 0;
                        }
                    }
                }
            }
        }

        Log.i(pluginName,"DELETE FILE FROM PROVIDER URI="+uri.toString()+" FILE="+ffile.getAbsolutePath());
        ffile.delete();
        return 1;

    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        Log.i(pluginName,"uri="+uri.toString()+" mode="+mode);

        String uristr = uri.toString();
        File fname = null;

        pathCheck();

        if (uristr.contains(getProviderURI()+"/internal/")) {
            uristr = uristr.replace(getProviderURI()+"/internal/", "");
            fname = !translatepath_internal ? new File(getContext().getFilesDir(),uristr):new File(internal+"/files",uristr); new File(getContext().getFilesDir(),"files/"+uristr);
        }
        else
        if (uristr.contains(getProviderURI()+"/external/")) {
            uristr = uristr.replace(getProviderURI()+"/external/", "");
            fname =  new File(Environment.getExternalStorageDirectory(),uristr);
        }
        else
        if (uristr.contains(getProviderURI()+"/external2/")) {
            uristr = uristr.replace(getProviderURI()+"/external2/", "");
            fname =  !translatepath_external ? new File(getContext().getExternalFilesDir(null),uristr):new File(external+"/files",uristr);
        }
        else
        if (uristr.contains(getProviderURI()+"/cache/")) {
            uristr = uristr.replace(getProviderURI()+"/cache/", "");
            fname =  !translatepath_internal ? new File(getContext().getCacheDir(),uristr):new File(internal+"/cache",uristr);
        }
        else
        if (uristr.contains(getProviderURI()+"/cache2/")) {
            uristr = uristr.replace(getProviderURI()+"/cache2/", "");
            fname =  !translatepath_external ? new File(getContext().getExternalCacheDir(),uristr):new File(external+"/cache",uristr);
        }

        if (fname==null)
        {
            throw new FileNotFoundException(uristr);
        }

        ParcelFileDescriptor pfd;

        int imode = 0;
        if (mode.equalsIgnoreCase("r"))
        {
            imode|=ParcelFileDescriptor.MODE_READ_ONLY;
        }
        else
        if (mode.equalsIgnoreCase("rt")||mode.equalsIgnoreCase("tr"))
        {
            imode|=ParcelFileDescriptor.MODE_READ_ONLY|ParcelFileDescriptor.MODE_TRUNCATE;
        }
        else
        if (mode.toLowerCase().contains("rw")||mode.toLowerCase().contains("wr"))
        {
            imode|=ParcelFileDescriptor.MODE_READ_WRITE;
            if (mode.toLowerCase().contains("t"))
            {
                imode|=ParcelFileDescriptor.MODE_TRUNCATE;
            }
        }
        else
        if (mode.equalsIgnoreCase("w"))
        {
            imode|=ParcelFileDescriptor.MODE_WRITE_ONLY;
        }
        else
        if (mode.equalsIgnoreCase("tw"))
        {
            imode|=ParcelFileDescriptor.MODE_WRITE_ONLY|ParcelFileDescriptor.MODE_TRUNCATE;
        }

        try {
            Log.i(pluginName,"fname="+fname);
            pfd = ParcelFileDescriptor.open(fname, imode);
            return pfd;
        }
        catch (Exception e)
        {
            Log.e(pluginName,e.getMessage(),e);
            try {
                String strname = fname.getAbsolutePath();
                strname=strname.replace("/data/user/0/","/data/data/");
                strname=strname.replace("/storage/emulated/0/","/sdcard/");
                fname=new File(strname);
                Log.i(pluginName,"try path="+strname);
                pfd = ParcelFileDescriptor.open(fname, imode);
                return pfd;
            }
            catch (Exception e2)
            {
                Log.e(pluginName,e.getMessage(),e);
                return null;
            }
        }
    }
}
