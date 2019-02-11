package id.sentuh.digitalsignageagent.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cz.msebera.android.httpclient.Header;
import id.sentuh.digitalsignageagent.helper.ExtractDataFile;
import id.sentuh.digitalsignageagent.svc.VersionService;
import ir.mahdi.mzip.zip.ZipArchive;

/**
 * Created by sony on 3/21/2018.
 */

public abstract class AppUtils {
    public static final String TAG = "Digital Signage Agent";
    public static final String APP_PACKAGE = "id.sentuh.digitalsignage";
    static final String libs = "LD_LIBRARY_PATH=/vendor/lib:/system/lib ";
    private String commands = libs + "pm install -r " + EndPoints.APK_LOCAL_PATH;
    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    public static int getPackageVersion(String packageName, PackageManager packageManager) {
        try {
//           ApplicationInfo info= packageManager.getApplicationInfo(packageName, 0);
           PackageInfo info = packageManager.getPackageInfo(packageName,0);

            return info.versionCode;
        }
        catch (Exception e) {
            return 0;
        }
    }
    public static int getApkVersion(String apk_path, PackageManager packageManager) {
        try {
//           ApplicationInfo info= packageManager.getApplicationInfo(packageName, 0);
            PackageInfo info = packageManager.getPackageArchiveInfo(apk_path,0);

            return info.versionCode;
        }
        catch (Exception e) {
            return 0;
        }
    }
    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static void downloadAPK(final Context context, final int appVersion){
//        Log.d(TAG,"Sentuh Download apk");
        final PackageManager pm = context.getPackageManager();
        String apkUrl = EndPoints.APK_FILE_URL;
        try {
            AsyncHttpClient client = new AsyncHttpClient();

            client.get(apkUrl, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
//                        .String json_string = new String(responseBody);
                        //String filepath = ext_path+"publish.zip";
                        File fileDest = new File(EndPoints.APK_LOCAL_PATH);
                        FileOutputStream fos = new FileOutputStream(fileDest);
                        fos.write(responseBody);
                        fos.close();
//                        Log.d(TAG,"apk file:"+EndPoints.APK_LOCAL_PATH);
                        int apk_version = AppUtils.getApkVersion(fileDest.getAbsolutePath(),pm);
                        if(apk_version>appVersion){
//                            silentUnInstall();
//                            silentInstall();
                            Toast.makeText(context,"Install New Version of Sentuh DS",Toast.LENGTH_SHORT).show();
                            installAPK(context);
                        } else {
                            Toast.makeText(context,"Appication already The Newest!",Toast.LENGTH_SHORT).show();
                        }
//                        installAPK(context);
//
                    } catch (Exception ex){
                        Log.e(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                    int progress = (int)(bytesWritten * 100/totalSize);
                    if(progress%5==0){
//                        Log.d(TAG,"apk download progress "+Integer.toString(progress));
                    }

                }

                @Override
                public void onStart() {
                    super.onStart();
//                    Log.d(TAG,"start download file");
                    Toast.makeText(context,"Start Download new APK",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinish() {
                    super.onFinish();
//                    Log.d(TAG,"finish download file");
                    Toast.makeText(context,"Download APK Finish",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e(TAG, error.getMessage());
                }
            });
        } catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }

    }
    public static void installAPK(Context context){


        File file = new File(EndPoints.APK_LOCAL_PATH);
        Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
        context.startActivity(promptInstall);
        int pid = AppUtils.getPID(context);
        if(pid>0){
            android.os.Process.killProcess(pid);
        }
    }
    public static void killApp(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(EndPoints.APP_PACKAGE_NAME);
    }
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean NisConnected = activeNetwork != null && activeNetwork.isConnected();
        if (NisConnected) {
            //  if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE || activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
            else
                return false;
        }
        return false;
    }
    public static void lookupServer(final Context context, int layout_id){
        final Configurate config = new Configurate(context);
        final int old_version = config.getVersion();
        AsyncHttpClient client = new AsyncHttpClient();
        String BaseServer = EndPoints.BASE_SERVER;
        String UrlUpdateServer = String.format(EndPoints.UPDATE_VERSION,BaseServer,Integer.toString(layout_id));
        Log.d(TAG,"url update : "+UrlUpdateServer);
        try {
            client.get(UrlUpdateServer, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers,
                                      byte[] responseBody) {
                    try {
                        String json_string = new String(responseBody);
                        Log.d(TAG,"json version : "+json_string);
                        JSONObject json = new JSONObject(json_string);
                        int version = json.getInt("version");
                        config.setVersion(version);

                        String url_download = json.getString("url");
                        Log.d(TAG,"downloading new version : "+url_download);
                        File zip = new File(EndPoints.ZIP_DEST_FILE);
                        if(zip.exists()){
                            zip.delete();
                        }
                        downloadZip(url_download,context);


                    } catch (Exception ex){
                        Log.e(TAG,"error lookup server : "+ex.getMessage());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    // displayNotificationMessage("Error Checking Server!");
                    Log.e(TAG,"error getting lookup server");

                }
            });
        } catch (Exception ex){
            Log.e(TAG,"error lookup server : "+ex.getMessage());

        }
    }
    public static void uninstallApp(Context context){
        Intent intent=new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:"+APP_PACKAGE));
        context.startActivity(intent);
    }
    public static synchronized void silentUnInstall() {
        // Install Updated APK
        try {
            String command = "pm uninstall -k " + APP_PACKAGE;
            Process proc = Runtime.getRuntime().exec(new String[] {"su", "-c", command});
            int test = proc.waitFor(); // Error is here.

            if (proc.exitValue() == 0) {
                // Successfully installed updated app
//                Log.d(TAG,"Sentuh DS Installation Success!");
            } else {
                // Fail
                throw new Exception("Root installation failed");
            }
        } catch (Exception ex){
            Log.e(TAG,"Sentuh Error :"+ ex.toString());
        }

    }
    public static synchronized void silentInstall() {
        // Install Updated APK
        try {
            String command = "pm install -r " + EndPoints.APK_LOCAL_PATH;
            Process proc = Runtime.getRuntime().exec(new String[] {"su", "-c", command});
            int test = proc.waitFor(); // Error is here.

            if (proc.exitValue() == 0) {
                // Successfully installed updated app
//                Log.d(TAG,"Sentuh DS Installation Success!");
            } else {
                // Fail
                throw new Exception("Root installation failed");
            }
        } catch (Exception ex){
            Log.e(TAG,"Sentuh Error :"+ ex.toString());
        }

    }
    private static void instalarApk( String commands ) {
        try {

            Process p = Runtime.getRuntime().exec( commands );
//            InputStream es = p.getErrorStream();
//            DataOutputStream os = new DataOutputStream(p.getOutputStream());
//
//            os.writeBytes(commands + "\n");
//
//            os.writeBytes("exit\n");
//            os.flush();
//
//            int read;
//            byte[] buffer = new byte[4096];
//            String output = new String();
//            while ((read = es.read(buffer)) > 0) {
//                output += new String(buffer, 0, read);
//            }
//
//            p.waitFor();

        } catch (IOException e) {
            Log.e(TAG,"Sentuh Error :"+ e.toString());
        }
//        catch (InterruptedException e) {
//            Log.d(TAG, "Sentuh Error :"+ e.toString());
//        }
    }


    public static boolean isAppInstalled(Context context) {
        try {
            context.getPackageManager().getApplicationInfo(EndPoints.APP_PACKAGE_NAME, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void startApp(Context context){
        if(isAppInstalled(context)){
            Intent i = context.getPackageManager().
                    getLaunchIntentForPackage(EndPoints.APP_PACKAGE_NAME);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);

            context.startActivity(i);
//            Toast.makeText(context,"Application start!",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context,"Application not Installed!",Toast.LENGTH_SHORT).show();
        }
    }
    public static void restartApp(Context context){

        int app_processid = AppUtils.getPID(context);
//        Log.d(TAG,"process id killing : "+Integer.toString(app_processid));
        android.os.Process.killProcess(app_processid);
        startApp(context);
    }
    public static Typeface getFont(final Context context, String font_name) {
        return Typeface.createFromAsset(context.getAssets(),"fonts/"+font_name+".ttf");
    }
    public static String getSthxFile(){
        File dirs = new File("/mnt/usbhost2");
        if(dirs.exists()){
            File[] files = dirs.listFiles();
            if(files!=null){
                for(File file:dirs.listFiles()){
                    if(file.getName().contains(".sthx")){
                        return file.getAbsolutePath();
                    }
                }

            } else {
                return null;
            }
        } else {
            return null;
        }
        return null;
    }
    public static void hideSystemUI(View decorView) {
        //View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
    public static void showSystemUI(View decorView) {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    public static void downloadZip(String downloadURL,final Context context){
        try {
            AsyncHttpClient client = new AsyncHttpClient();

            client.get(downloadURL, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
//                        .String json_string = new String(responseBody);
                        //String filepath = ext_path+"publish.zip";
                        File fileDest = new File(EndPoints.ZIP_DEST_FILE);
                        FileOutputStream fos = new FileOutputStream(fileDest);
                        fos.write(responseBody);
                        fos.close();
//                        Log.d(TAG,"unzip file:"+EndPoints.ZIP_DEST_FILE);
                        new Unzipping(context).execute();
                        //AppUtils.insertResources(context);
                        //Log.d(TAG,"response:"+json_string);

                    } catch (Exception ex){
                        Log.e(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                    int progress = (int)(bytesWritten * 100/totalSize);
//                    Log.d(TAG,"download progress "+Integer.toString(progress));
                }

                @Override
                public void onStart() {
                    super.onStart();
//                    Log.d(TAG,"start download file");
                    //Toast.makeText(context,"Start Download",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinish() {
                    super.onFinish();
//                    Log.d(TAG,"finish download file");
                    //Toast.makeText(context,"Download Finish",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e(TAG, error.getMessage());
                }
            });
        } catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }
    public static class Unzipping extends AsyncTask<Void,Void,Void>{
        Context mC;
        public Unzipping(Context context){
            this.mC = context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            ZipArchive zipArchive = new ZipArchive();
            File zip = new File(EndPoints.ZIP_DEST_FILE);
            if(zip.exists()){
                zipArchive.unzip(EndPoints.ZIP_DEST_FILE,EndPoints.STORAGE_DATA_PATH,"");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            restartApp(this.mC);
        }
    }

    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null)
        {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static int getPID(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> pids = am.getRunningAppProcesses();
        int processid = 0;
        for (int i = 0; i < pids.size(); i++) {
            ActivityManager.RunningAppProcessInfo info = pids.get(i);
            if (info.processName.equalsIgnoreCase(EndPoints.APP_PACKAGE_NAME)) {
                processid = info.pid;
                return processid;
            }
        }
        return processid;
    }
    public static boolean configFileExist(File zipFile) {
        HashMap<String, List<String>> contents = new HashMap<>();
        try  {
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                if(ze.isDirectory()) {
                    String directory = ze.getName();
                    if (!contents.containsKey(directory)) {
                        contents.put(directory, new ArrayList<String>());
                    }
                } else {
                    String file = ze.getName();
                    int pos = file.lastIndexOf("/");
                    if (pos != -1) {
                        String directory = file.substring(0, pos+1);
                        String fileName = file.substring(pos+1);
                        if (!contents.containsKey(directory)) {
                            contents.put(directory, new ArrayList<String>());
                            List<String> fileNames = contents.get(directory);
                            fileNames.add(fileName);
                        } else {
                            List<String> fileNames = contents.get(directory);
                            fileNames.add(fileName);
                        }
                    } else {
                        if (!contents.containsKey("root")) {
                            contents.put("root", new ArrayList<String>());
                        }
                        List<String> fileNames = contents.get("root");
                        fileNames.add(file);
                    }
                }
                zin.closeEntry();
            }

            zin.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static String readTextFile(File file) {
        String aBuffer = "";
        try {
            FileInputStream fIn = new FileInputStream(file);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow;
            }
            myReader.close();

        } catch (IOException ex){

        }
        return aBuffer;
    }
}
