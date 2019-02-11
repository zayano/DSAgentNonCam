package id.sentuh.digitalsignageagent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.gifview.library.GifView;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.List;

import id.sentuh.digitalsignageagent.app.AppUtils;
import id.sentuh.digitalsignageagent.app.Configurate;
import id.sentuh.digitalsignageagent.app.EndPoints;
import id.sentuh.digitalsignageagent.helper.ExtractDataFile;
import id.sentuh.digitalsignageagent.svc.VersionService;
import ir.mahdi.mzip.zip.ZipArchive;

public class UpdateUSBActivity extends Activity {
    private static String TAG="Update Version";
    private static int DELAY = 6000;
    private Handler mHandler;
    private GifView gifView1;
    private TextView note;
    private int counter=0;
    private int max_counter=3;
    Typeface fontDefault;
    String fileSthxPath;
    Configurate config;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        AppUtils.hideSystemUI(decorView);
        setContentView(R.layout.activity_update);
        fontDefault = AppUtils.getFont(this,"OpenSans-Regular");
        mHandler = new Handler();
        gifView1 = (GifView) findViewById(R.id.gif1);
        note = (TextView)findViewById(R.id.note);
        note.setTypeface(fontDefault);
        gifView1.setVisibility(View.VISIBLE);
        gifView1.play();

        config = new Configurate(this);
        int pid = AppUtils.getPID(this);
        if(pid>0){
            android.os.Process.killProcess(pid);
        }
        fileSthxPath=AppUtils.getSthxFile();


        File fd = new File(EndPoints.ZIP_DEST_FILE);
        if(fd.exists()){
            fd.delete();
        }
        config.setKeyInt("reading",1);
//        stopService();
        mHandler.postDelayed(mCheckingUsbStorage,2000);
    }
    private void startBgService(){
        Toast.makeText(this,"Start Service...",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(UpdateUSBActivity.this,VersionService.class);
        startService(intent);
    }
    private void stopService(){
        Toast.makeText(this,"Stop Service...",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(UpdateUSBActivity.this,VersionService.class);
        stopService(intent);
    }
//    private void checkAPK(){
//        PackageManager pm = getPackageManager();
//        int app_version = AppUtils.getPackageVersion("id.sentuh.digitalsignage",pm);
//        Log.d(TAG,"app version : "+ Integer.toString(app_version));
//        File fdisk = new File("/mnt/udisk/latest.apk");
//        if(fdisk.exists()){
//            int apk_version = AppUtils.getApkVersion(fdisk.getAbsolutePath(),pm);
//            if(apk_version>app_version){
//                MessageBox messageBox =new MessageBox(UpdateUSBActivity.this,"Warning","Ingin install aplikasi baru!");
//                messageBox.showAndInstall(UpdateUSBActivity.this);
//            } else {
//                readConfigUsb(fileSthxPath);
//            }
//        } else {
//            readConfigUsb(fileSthxPath);
//        }
//
//    }
    private void readConfigUsb(final String filePath){

        File file = new File(filePath);
        Log.d(TAG,"file config : "+file.getAbsolutePath());
        if(file.exists()){
            note.setText("Membaca Konfigurasi ...");
            //new CopyAllData(this,filePath,"/sdcard/tmp",false).execute();
            new DestroyOldData(this,filePath).execute();
        } else {
            Toast.makeText(this,"Mencoba sinkronisasi!", Toast.LENGTH_SHORT).show();
            note.setText("Sinkronisasi Flash storage ...");
            mHandler.postDelayed(mCheckingUsbStorage,DELAY);
        }
    }
    private void backToScreen(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },2000);
    }
    private Runnable mCheckingUsbStorage = new Runnable() {
        @Override
        public void run() {
            String sthxFile = AppUtils.getSthxFile();
            try {
                Thread.sleep(2000);
            } catch (Exception ex){

            }
            if(sthxFile!=null){
//                gifView1.pause();
//                gifView1.setVisibility(View.GONE);
                final File file = new File(sthxFile);
                Log.d(TAG,"file sthx : "+file.getAbsolutePath());
                if(file.exists()){
                    Toast.makeText(UpdateUSBActivity.this,"reading : "+file.getAbsolutePath(),
                            Toast.LENGTH_SHORT).show();
                    mHandler.removeCallbacks(mCheckingUsbStorage);
//                    new ExtractDataFile(UpdateUSBActivity.this,file.getAbsolutePath()).execute();
                    new CheckingConfig(UpdateUSBActivity.this,sthxFile).execute();
                }

            } else {
                if(counter<max_counter){
                    counter++;
//                    Toast.makeText(UpdateUSBActivity.this,"Percobaan ke "+ Integer.toString(counter), Toast.LENGTH_SHORT).show();
                    Toast.makeText(UpdateUSBActivity.this,"Masih mencoba membaca usb ...", Toast.LENGTH_SHORT).show();
                    mHandler.postDelayed(mCheckingUsbStorage,DELAY);
                } else {
                    mHandler.removeCallbacks(mCheckingUsbStorage);
                    Toast.makeText(UpdateUSBActivity.this,"Data USB Tidak teridentifikasi", Toast.LENGTH_SHORT).show();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadMain();
                        }
                    },2000);

                }


            }

        }
    };

    @SuppressLint("WrongConstant")
    private void loadMain(){
        AppUtils.restartApp(this);
//        startBgService();
        finish();
    }
    private class DestroyOldData extends AsyncTask<Void,Void,Void> {

        Context mContext;
        String filePath;
        public DestroyOldData(Context context, String file_path){
            this.mContext = context;
            this.filePath = file_path;
        }
        private void deleteRecursive(File fileOrDirectory) {
            if (fileOrDirectory.isDirectory())
                for (File child : fileOrDirectory.listFiles()) {

                    deleteRecursive(child);
                }
            Log.d(TAG,"delete file : "+fileOrDirectory.getName());
            fileOrDirectory.delete();
        }
        @Override
        protected Void doInBackground(Void... params) {
//            AppUtils.eraseAllLocalData(mContext);
//            gifView1.play();
//            Storage storage = new Storage(mContext);
            File dirs = new File(EndPoints.STORAGE_DATA_PATH);
            for(File dir:dirs.listFiles()) {
                if (dir.getName().equals("Views") ||
                        dir.getName().equals("Resources") ||
                        dir.getName().equals("Events") ||
                        dir.getName().equals("Models")||
                        dir.getName().equals("Config")) {
                    Log.d(TAG, "delete dir : " + dir.getName());
//               List<File> files = storage.getFiles(dir.getAbsolutePath());
//               for(File file:files){
//                   storage.deleteFile(file.getAbsolutePath());
//               }
                    deleteRecursive(dir);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            gifView1.setVisibility(View.VISIBLE);
            gifView1.play();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            gifView1.play();
            Toast.makeText(mContext,"Copying File to local!", Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    loadMain();
                    new ExtractDataFile(mContext,filePath,null).execute();
                }
            },3000);

        }
    }
    private class CheckingConfig extends AsyncTask<Void,Boolean,Boolean> {

        Context mContext;
        String filePath;
        public CheckingConfig(Context context,String file_path){
            this.mContext = context;
            this.filePath = file_path;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
//
            ZipArchive zipArchive = new ZipArchive();
            File zip = new File(this.filePath);
            if(zip.exists()){
                // Toast.makeText(mContext,"Extract Zip File!",Toast.LENGTH_SHORT).show();
                zipArchive.unzip(filePath,"/sdcard/tmp","");
            } else {
                //Toast.makeText(mContext,"File Zip Not Exist!",Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            String fileconfig = "/sdcard/tmp/Config/config.txt";
            File file = new File(fileconfig);
            if(file.exists()){
                Toast.makeText(UpdateUSBActivity.this,"Destroying Old Data!",Toast.LENGTH_SHORT).show();
                new DestroyOldData(UpdateUSBActivity.this,filePath).execute();
            } else {
                Toast.makeText(UpdateUSBActivity.this,"USB Tidak teridentifikasi!",Toast.LENGTH_SHORT).show();
                loadMain();
            }

        }
    }

}
