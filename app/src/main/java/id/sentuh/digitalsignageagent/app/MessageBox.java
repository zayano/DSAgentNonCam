package id.sentuh.digitalsignageagent.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

/**
 * Created by sonywibisono on 5/21/16.
 */
public class MessageBox {
    Context mC;
    String title;
    String message;
    AlertDialog.Builder builder;
    public MessageBox(Context context, String judul, String pesan) {
        //super(context);
        this.mC = context;
        this.title = judul;
        this.message = pesan;


    }
    public void show(){
        builder = new AlertDialog.Builder(mC);
        builder.setTitle(this.title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();
            }
        });
        AlertDialog dialog= builder.create();
        dialog.show();
    }
    public void showAndClose(final Activity act){
        builder = new AlertDialog.Builder(mC);
        builder.setTitle(this.title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();
                act.finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog= builder.create();
        dialog.show();
    }

    public void showAndNext(final Activity current,Class activity){
        final Intent intent = new Intent(mC,activity);
        builder = new AlertDialog.Builder(mC);
        builder.setTitle(this.title);
        builder.setMessage(message);
        builder.setPositiveButton("Selanjutnya", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                //dialog.dismiss();
                current.finish();
                current.startActivity(intent);
                current.overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            }
        });
        AlertDialog dialog= builder.create();
        dialog.show();
    }
    public void showAndInstall(final Activity act){
        builder = new AlertDialog.Builder(mC);
        builder.setTitle(this.title);
        builder.setMessage(message);
        builder.setPositiveButton("Install", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                //dialog.dismiss();
//                act.finish();
                openAgent(act);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog= builder.create();
        dialog.show();
    }
    public static void openAgent(Activity context){
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("id.sentuh.digitalsignageagent");
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);//null pointer check in case package name was not found
        context.finish();
    }
}
