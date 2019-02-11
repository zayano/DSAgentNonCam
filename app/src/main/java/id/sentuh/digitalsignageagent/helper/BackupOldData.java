package id.sentuh.digitalsignageagent.helper;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

import id.sentuh.digitalsignageagent.app.EndPoints;
import ir.mahdi.mzip.zip.ZipArchive;

/**
 * Created by sony on 31-May-18.
 */

public class BackupOldData extends AsyncTask<Void,Void,Void> {
    Context mContext;
    String filePath;
    public BackupOldData(Context context, String file_path){
        this.mContext = context;
        this.filePath = file_path;
    }
    @Override
    protected Void doInBackground(Void... voids) {
        File file = new File(EndPoints.ZIP_OLD_FILE);
        if(file.exists()){
            file.delete();
        }
        try {
            Thread.sleep(2500);
        } catch (Exception ex){

        }
        ZipArchive zipArchive = new ZipArchive();
        zipArchive.zip(EndPoints.STORAGE_DATA_PATH,EndPoints.ZIP_OLD_FILE,"");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }
}
