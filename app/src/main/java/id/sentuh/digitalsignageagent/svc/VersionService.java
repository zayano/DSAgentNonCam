package id.sentuh.digitalsignageagent.svc;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import id.sentuh.digitalsignageagent.R;
import id.sentuh.digitalsignageagent.app.AppUtils;
import id.sentuh.digitalsignageagent.app.Configurate;
import id.sentuh.digitalsignageagent.app.EndPoints;
import id.sentuh.digitalsignageagent.helper.DestroyAllData;
import id.sentuh.digitalsignageagent.helper.ExtractDataFile;

/**
 * Created by sony on 2/19/2018.
 */

public class VersionService extends Service {
    private static String TAG = "Service Update";
    Handler mHandler;
    private static int DELAY_TIME = 60*1000;
    private long startTime;
    private String CHANNEL_ID = "Agent";
    private int NOTIFY_ID = 198821;
    Configurate config;
    private int layoutId;
    private boolean looKup=false;
    private int old_version;
    @android.support.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private NotificationManager mNotificationManager;
    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notifikasi(this,"DS Agent","Starting Service...");

        mHandler = new Handler();
        startTime = System.currentTimeMillis();
        config = new Configurate(this);
        readConfig(this);
        try {
            Thread.sleep(3000);
        } catch (Exception ex){

        }

        layoutId = config.getPageId();

        if(AppUtils.isInternetAvailable(this)){
            mHandler.postDelayed(mRunnableUpdateVersion,DELAY_TIME);
        } else {
            //Log.d(TAG,"Internet not available!");
            stopSelf();
            Notifikasi(this,"DS Agent Stop","Connection not found...");
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacks(mRunnableUpdateVersion);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    private Runnable mRunnableUpdateVersion = new Runnable() {
        @Override
        public void run() {
            long elapsed = System.currentTimeMillis() - startTime;
            long timeInSeconds = elapsed / 1000;
            int hours, minutes, seconds;
            hours = (int)timeInSeconds / 3600;
            timeInSeconds = timeInSeconds - (hours * 3600);
            minutes = (int)timeInSeconds / 60;
            timeInSeconds = timeInSeconds - (minutes * 60);
            seconds = (int)timeInSeconds;
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date now = new Date(System.currentTimeMillis());
            String jamskr = dateFormat.format(now);
            String[] times = jamskr.split(":");

            int menit = Integer.parseInt(times[1]);

            if(minutes>0){
//                Log.d(TAG,"second : "+Integer.toString(seconds));
                if ((minutes%3)==0 && !looKup){
                    readConfig(VersionService.this);
                    try {
                        Thread.sleep(3000);
                    } catch (Exception ex){

                    }
                    // displayNotificationMessage("Service Update Version");
                    looKup = true;
                    mHandler.removeCallbacks(mRunnableUpdateVersion);
                    layoutId = config.getPageId();
                    if(layoutId>0){
                        Notifikasi(VersionService.this,"Digital Signage Agent",
                                "Checking Update Content");
                        Log.d(TAG,"service lookup... every minute at "+jamskr+" minutes : "+Integer.toString(minutes));
                        lookupServer(VersionService.this,layoutId);
                    }

                } else {

                    mHandler.postDelayed(mRunnableUpdateVersion,DELAY_TIME);
                }

            } else {
                mHandler.postDelayed(mRunnableUpdateVersion,DELAY_TIME);

            }
//            if(!AppUtils.isAppRunning(VersionService.this,EndPoints.APP_PACKAGE_NAME)){
//                AppUtils.startApp(VersionService.this);
//            }
        }
    };

    private void lookupServer(final Context context, int layout_id){
//        final Configurate config = new Configurate(context);
//        final int old_version = config.getVersion();
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
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
                        int templateid= json.getInt("template_id");
                        config.setPageId(templateid);
                        old_version = config.getVersion();
                        if(version>old_version){
                            mHandler.removeCallbacks(mRunnableUpdateVersion);
                            config.setVersion(version);
                            String url_download = json.getString("url");
                            Notifikasi(context,"Digital Signage Agent","Update Content Progress");
                            Log.d(TAG,"downloading new version : "+url_download);
//                            File zip = new File(EndPoints.ZIP_DEST_FILE);
//                            if(zip.exists()){
//                                zip.delete();
//                            }
                            downloadZip(url_download,context);

                        } else {
                            Log.d(TAG,"not download new version ");
                            File file = new File(EndPoints.ZIP_DEST_FILE);
                            if(file.exists()){
                                new ExtractDataFile(context,EndPoints.ZIP_DEST_FILE,mRunnableUpdateVersion);
                            } else {
                                mHandler.postDelayed(mRunnableUpdateVersion,DELAY_TIME);
                            }
                            looKup = false;

//                            Log.d(TAG,"continuing lookup");
                        }


                    } catch (Exception ex){
                        Log.e(TAG,"error lookup server : "+ex.getMessage());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    // displayNotifikasiMessage("Error Checking Server!");
                    Log.e(TAG,"error getting lookup server");
                    mHandler.postDelayed(mRunnableUpdateVersion,DELAY_TIME);
                }
            });
        } catch (Exception ex){
            Log.e(TAG,"error lookup server : "+ex.getMessage());
            mHandler.postDelayed(mRunnableUpdateVersion,DELAY_TIME);
        }
    }
    public void readConfig(Context context){
        Configurate config = new Configurate(context);
        File file = new File(EndPoints.CONFIG_APP_PATH);
        if(file.exists()){

            String content = AppUtils.readTextFile(file);
            Log.d(TAG,"read config : "+content);
            try {
                JSONObject json = new JSONObject(content);
                int layoutId = json.getInt("id");
                int old_version = json.getInt("version");
                String password = json.getString("password");
                String server = json.getString("server");
                // String serialno = json.getString("serialno");
                config.setServerUrl(server);
                config.setPageId(layoutId);
                config.setPagePassword(password);
                config.setVersion(old_version);
                if(layoutId>0){
                    Log.d(TAG,"id template found : "+Integer.toString(layoutId));
                    lookupServer(context,layoutId);
                } else {
                    Log.d(TAG,"id template not found");
                    Toast.makeText(context,
                            "Content Not Install, Please Insert USB Data!",Toast.LENGTH_SHORT).show();
                }

            } catch (Exception ex){
                Log.e(TAG,"Error:"+ex.getMessage());
            }
        } else {
            Log.e(TAG,"File config not exist");
        }
    }
    private void downloadZip(String downloadURL,final Context context){
        try {
            AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);

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
                        //new AppUtils.Unzipping(context).execute();
//                        new CopyAllData(VersionService.this,EndPoints.ZIP_DEST_FILE,
//                                EndPoints.STORAGE_DATA_PATH,true).execute();
                        Log.d(TAG,"destroy data");
                        new DestroyAllData(VersionService.this,EndPoints.ZIP_DEST_FILE,mRunnableUpdateVersion).execute();
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
                    Log.d(TAG,"start download file");
//                    mHandler.postDelayed(mRunnableUpdateVersion,DELAY_TIME);
                    Toast.makeText(context,"Start Download",Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFinish() {
                    super.onFinish();
//                    Log.d(TAG,"finish download file");
                    looKup = false;

                    Intent intent = new Intent("Updated");
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.setComponent (new
                            ComponentName( "id.sentuh.digitalsignage",
                            "id.sentuh.digitalsignage.helper.ChangedApplication"));
                    sendBroadcast(intent);
                    Toast.makeText(context,"Download Finish",Toast.LENGTH_SHORT).show();
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

    private void Notifikasi(Context context,String title,String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mNotificationManager.notify(NOTIFY_ID,mBuilder.build());
    }


}
