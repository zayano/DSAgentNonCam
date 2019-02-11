package id.sentuh.digitalsignageagent.app;

import android.os.Environment;

import java.io.File;

/**
 * Created by sony on 3/21/2018.
 */

public abstract class EndPoints {
    public static final String TAG = "Signage Agent";
    public static File extdir = Environment.getExternalStorageDirectory();
    public static String BASE_SERVER = "https://cms.sentuh.id";
    //    public static String API_SERVER =  "%s/api";
    public static String UPDATE_VERSION = "%s/api/version/template/%s";
    public static String DEVICE_REGISTER = "%s/api/device/store"; //POST
    public static String PUBLISH_ZIP_FILE = "%s/storage/publish.sthx";
    public static String STORAGE_BASE_PATH = extdir.getAbsolutePath(); ///mnt/udisk on device signage
    public static String STORAGE_DATA_PATH = STORAGE_BASE_PATH + "/SentuhOS";
    public static String VIEW_PATH = STORAGE_DATA_PATH + "/Views";
    public static String SYSTEM_PATH = STORAGE_DATA_PATH + "/System";
    public static String RESOURCE_PATH = STORAGE_DATA_PATH + "/Resources";
    public static String POPUP_PATH = STORAGE_DATA_PATH + "/Models/popup.txt";
    public static String EVENT_PATH = STORAGE_DATA_PATH + "/Events";
    public static String EVENT_FILE_PATH = EVENT_PATH + "/events.txt";
//    public static String ZIP_DEST_FILE = STORAGE_BASE_PATH +"/publish.sthx";
    public static String APK_NAME = "latest.apk";
    public static String USB_STORAGE_PATH = "/storage/usbhost2/SentuhOS";
//    public static String ZIP_FILE_URL = BASE_SERVER + "/storage/publish.zip";
    public static String APK_FILE_URL = BASE_SERVER + "/apk/release/"+APK_NAME;
    public static String CONFIG_FILE_PATH = STORAGE_DATA_PATH + "/agent.json";
    public static String APK_LOCAL_PATH = STORAGE_BASE_PATH + "/" + APK_NAME;
    public static String ZIP_DEST_FILE = STORAGE_BASE_PATH +"/publish.sthx";
    public static String ZIP_OLD_FILE = STORAGE_BASE_PATH +"/backup.sthx";
    public static String CONFIG_APP_PATH = STORAGE_DATA_PATH + "/Config/config.txt";
    public static String APP_PACKAGE_NAME = "id.sentuh.digitalsignage";
    public static String CONFIG_USB_PATH = USB_STORAGE_PATH + "/Config/config.txt";
    public static String ZIP_USB_FILE = "/storage/usbhos2/publish.sthx";

}
