package id.sentuh.digitalsignageagent;

import android.Manifest;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import id.sentuh.digitalsignageagent.adapter.ImagePreviewAdapter;
import id.sentuh.digitalsignageagent.app.AppUtils;
import id.sentuh.digitalsignageagent.app.Configurate;
import id.sentuh.digitalsignageagent.app.EndPoints;
import id.sentuh.digitalsignageagent.model.MIBtree;
import id.sentuh.digitalsignageagent.service.AgentService;
import id.sentuh.digitalsignageagent.svc.VersionService;

public class MainActivity extends FragmentActivity {
    private SensorManager mSensorManager;
    private PowerManager mPowerManager;
    private WindowManager mWindowManager;
    private PowerManager.WakeLock mWakeLock;
    private String CHANNEL_ID = "Agent";
    private int NOTIFY_ID = 198821;
    private static String TAG = "Main";
    int app_version;
    int apk_version;
    Configurate config;
    Handler mHandler = new Handler();
    Messenger mService = null;
    boolean mIsBound;
    private int templateId;
//    boolean cameraExist;
    private NotificationManager mNotificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        config = new Configurate(this);
        templateId = config.getPageId();
        if(!AppUtils.isAppRunning(this,EndPoints.APP_PACKAGE_NAME)){
            AppUtils.startApp(this);
        } else {
            Toast.makeText(this,"Application already running!",Toast.LENGTH_SHORT).show();
        }
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }
        }
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay();

        // Create a bright wake lock
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
                .getName());
        mWakeLock.acquire();

        startAllServices();

    }
    private void startAllServices() {
        if(AppUtils.isInternetAvailable(this)){
            Intent intent = new Intent(this, AgentService.class);
            startService(intent);
            Intent intent2 = new Intent(this, VersionService.class);
            startService(intent2);
        }

//        doBindAgentService();
    }
    private void SleepDisplay() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.screenBrightness = -1;
        getWindow().setAttributes(params);
    }
    private void WakeDisplay() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.screenBrightness = 1;
        getWindow().setAttributes(params);
    }
    public static boolean isCameraAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_camera, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

      return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

    }

    @Override
    protected void onStop() {

        super.onStop();

    }
    @Override
    protected void onDestroy() {

//        doUnbindAgentService();
        super.onDestroy();

    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
//        if(!cameraExist) return;

    }
    void killApp(){
        try {
            //Runtime.getRuntime().exec(new String[]{ "su", "-c", "reboot -p" });
            String commands[] = {"am force-stop id.sentuh.digitalsignage"};
            CommandResult result = Shell.SU.run(commands);
            if(result.isSuccessful()){
                Toast.makeText(MainActivity.this,result.getStdout(),Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex){
            Log.e(TAG,"error killing : "+ex.getMessage());
        }
    }
    void shutDown(){
        try {
            String commands[] = {"-c","reboot -p"};
            CommandResult result = Shell.SU.run(commands);
            if(result.isSuccessful()){
                Toast.makeText(MainActivity.this,result.getStdout(),Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex){
            Log.e(TAG,"error killing : "+ex.getMessage());
        }

    }
    void reBoot(){
        try {
            String commands[] = {"-c","reboot"};
            CommandResult result = Shell.SU.run(commands);
            if(result.isSuccessful()){
                Toast.makeText(MainActivity.this,result.getStdout(),Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex){
            Log.e(TAG,"error killing : "+ex.getMessage());
        }

    }
    private void checkApk(){
        PackageManager pm = getPackageManager();
        boolean value=AppUtils.isPackageInstalled(AppUtils.APP_PACKAGE,pm);
//        File dir = new File(EndPoints.STORAGE_DATA_PATH);
//        if(!dir.exists()){
//            dir.mkdirs();
//        }
        File apk =new File(EndPoints.APK_LOCAL_PATH);
        if(value){
            Log.d(AppUtils.TAG,"Sentuh Digital signage installed");
            //AppUtils.silentInstall();

            if(apk.exists()){
                apk_version = AppUtils.getApkVersion(apk.getAbsolutePath(),pm);
//                Log.d(TAG,"apk version : "+Integer.toString(apk_version));
                app_version = AppUtils.getPackageVersion(AppUtils.APP_PACKAGE,pm);
//                Log.d(TAG,"app version : "+Integer.toString(app_version));
                if(apk_version>app_version){
//                AppUtils.uninstallApp(this);
                    AppUtils.installAPK(this);
                }
            } else {
                downloadAPK();
            }


        } else {
            Log.d(AppUtils.TAG,"Sentuh Digital signage not installed");
            if(apk.exists()){
                AppUtils.installAPK(this);
            } else {
                downloadAPK();
            }


        }
    }
    private void startBgService(){
        Log.d(AppUtils.TAG,"Starting Digital Signage Agent");
        Intent intent = new Intent(MainActivity.this,VersionService.class);
        startService(intent);
        Toast.makeText(this,"Starting DS Agent",
                Toast.LENGTH_SHORT).show();

    }
    private void downloadAPK(){
        if(AppUtils.isInternetAvailable(this)){
            Log.d(AppUtils.TAG,"download new version");
            AppUtils.downloadAPK(this,app_version);
        } else {
            Log.d(AppUtils.TAG,"Network Not Available");
            Toast.makeText(this,"Internet Not Available",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.KILL_BACKGROUND_PROCESSES);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.KILL_BACKGROUND_PROCESSES,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE

        }, 101);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    startBgService();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void handleSendDangerAlert() {
        Message msg = Message.obtain(null,
                AgentService.MSN_SEND_DANGER_TRAP);
        msg.replyTo = mMessenger;
        sendMessageToAgentService(msg);
    }

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AgentService.MSG_SET_VALUE:
                    break;

                case AgentService.MSG_SNMP_REQUEST_RECEIVED:
                    Log.d(TAG,"Request : "+AgentService.lastRequestReceived);

//                    Toast.makeText(MainActivity.this,AgentService.lastRequestReceived,Toast.LENGTH_SHORT).show();
                    break;

                case AgentService.MSG_MANAGER_MESSAGE_RECEIVED:
                    MIBtree miBtree = MIBtree.getInstance();
                    String message = miBtree.getNext(MIBtree.MNG_MANAGER_MESSAGE_OID).getVariable().toString();

                    Log.d(TAG,"message : "+message);
                    if(message.equals("sleep")){
                        SleepDisplay();
                    }
                    if(message.equals("wake")){
                        WakeDisplay();
                    }
                    if(message.equals("start")) {
                        AppUtils.startApp(MainActivity.this);
                    }
                    if(message.equals("stop")) {
                        killApp();
                    }
                    if(message.equals("shutdown")){
                        shutDown();
                    }
                    if(message.equals("restart")){
                        reBoot();
                    }
                    if(message.equals("update apk")){
                        downloadAPK();
                    }
                    if(message.equals("update content")){
                        AppUtils.lookupServer(MainActivity.this,templateId);
                    }
                    if(message.equals("uninstall")){
                        killApp();
                        AppUtils.uninstallApp(MainActivity.this);
//                        AppUtils.silentUnInstall();
                    }
                    if(message.equals("install")){

                        AppUtils.installAPK(MainActivity.this);
//                        AppUtils.silentUnInstall();
                    }
                    Toast.makeText(MainActivity.this,message.toUpperCase(),Toast.LENGTH_SHORT).show();
//                    Notifikasi(MainActivity.this,"Pesan",message);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMessageToAgentService(Message msg){
        try {
            mService.send(msg);
        } catch (RemoteException e) {

        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        AgentService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

                // Give it some value as an example.
                msg = Message.obtain(null,
                        AgentService.MSG_SET_VALUE, this.hashCode(), 0);
                mService.send(msg);
            } catch (RemoteException e) {

            }

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
        }
    };
    private void Notifikasi(Context context,String title,String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mNotificationManager.notify(NOTIFY_ID,mBuilder.build());
    }
    void doBindAgentService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this, AgentService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindAgentService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            AgentService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {

                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }


}
