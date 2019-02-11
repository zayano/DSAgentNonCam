package id.sentuh.digitalsignageagent.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.aware.WifiAwareManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;
import android.widget.Toast;

import java.io.File;

import id.sentuh.digitalsignageagent.app.AppUtils;
import id.sentuh.digitalsignageagent.app.EndPoints;
import ir.mahdi.mzip.zip.ZipArchive;

/**
 * Created by sony on 31-May-18.
 */

public class ExtractDataFile extends AsyncTask<Void,Void,Void> {
    Context mContext;
    String filePath;
    Handler mHandler;
    ProgressDialog dialogProgress;
    Runnable mRunnableUpdateVersion;
    public ExtractDataFile(Context context, String file_path,Runnable mRunnable){
        this.mContext = context;
        this.filePath = file_path;
        this.mHandler = new Handler();
        mRunnableUpdateVersion = mRunnable;
//        showProgress(true);
    }
    private void showProgress(boolean value){
        if(dialogProgress==null){
            dialogProgress = new ProgressDialog(mContext);
            dialogProgress.setIndeterminate(true);
            dialogProgress.setCancelable(false);
            dialogProgress.setMessage("Extracting Content ...");
        }
        if(value){
            dialogProgress.show();
        } else {
            dialogProgress.dismiss();
        }
    }
    @Override
    protected Void doInBackground(Void... params) {
//
        ZipArchive zipArchive = new ZipArchive();
        File zip = new File(this.filePath);
        if(zip.exists()){
            // Toast.makeText(mContext,"Extract Zip File!",Toast.LENGTH_SHORT).show();
            zipArchive.unzip(filePath, EndPoints.STORAGE_DATA_PATH,"");
        } else {
            //Toast.makeText(mContext,"File Zip Not Exist!",Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void Void) {
        super.onPostExecute(Void);
//        showProgress(false);
        File file = new File(EndPoints.ZIP_DEST_FILE);
        if(file.exists()){
            file.delete();
        }
        if(mRunnableUpdateVersion!=null){
            mHandler.postDelayed(mRunnableUpdateVersion,60000);
        }

        Toast.makeText(mContext,"Restarting Application!", Toast.LENGTH_SHORT).show();
//
//        Process.killProcess(AppUtils.getPID(mContext));
//        System.exit(1);
//        AppUtils.restartApp(mContext);
//        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        wifiManager.setWifiEnabled(false);

//        Intent intent = new Intent("Updated");
//        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//        intent.setComponent (new
//                ComponentName( "id.sentuh.digitalsignage",
//                "id.sentuh.digitalsignage.helper.ChangedApplication"));
//        mContext.sendBroadcast(intent);

//        Intent intent = new Intent(mContext, ExtractDataFile.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.setType("text/plain");
//        intent.putExtra("EXIT",true);
////        intent.setPackage(EndPoints.APP_PACKAGE_NAME);

//        mContext.startActivity(intent);

//            AppUtils.startApp(mContext);

        Intent i = mContext.getPackageManager().
                getLaunchIntentForPackage(EndPoints.APP_PACKAGE_NAME);
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        mContext.startActivity(i);
//        AppUtils.startApp(mContext);
    }
}
