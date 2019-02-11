package id.sentuh.digitalsignageagent.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import id.sentuh.digitalsignageagent.app.EndPoints;

/**
 * Created by sony on 31-May-18.
 */

public class DestroyAllData extends AsyncTask<Void,Void,Void> {
    private static final String TAG = "Destroy";
    Context mContext;
    String filePath;
    Handler mHandler;
    ProgressDialog dialogProgress;
    Runnable mRunnableUpdateVersion;
    public DestroyAllData(Context context, String file_path,Runnable mRunnable){
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
            dialogProgress.setMessage("Cleaning directory...");
        }
        if(value){
            dialogProgress.show();
        } else {
            dialogProgress.dismiss();
        }
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
    protected Void doInBackground(Void... objects) {

        File dirs = new File(EndPoints.STORAGE_DATA_PATH);
        if (dirs.exists()) {
            for (File dir : dirs.listFiles()) {
                if (dir.getName().equals("Views") ||
                        dir.getName().equals("Resources") ||
                        dir.getName().equals("Events") ||
                        dir.getName().equals("Models") ||
                        dir.getName().equals("Config")) {
                    Log.d(TAG, "delete dir : " + dir.getName());
                    deleteRecursive(dir);
                }
            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
//        showProgress(false);
        Toast.makeText(mContext,"Extracting data!", Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                    loadMain();
                new ExtractDataFile(mContext,filePath,mRunnableUpdateVersion).execute();
            }
        },2000);

    }
}
