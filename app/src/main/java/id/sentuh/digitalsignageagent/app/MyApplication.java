package id.sentuh.digitalsignageagent.app;

import android.app.Application;

import id.sentuh.digitalsignageagent.svc.ConnectivityReceiver;

/**
 * Created by sony on 3/21/2018.
 */

public class MyApplication extends Application {
    private static MyApplication mInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }
    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
