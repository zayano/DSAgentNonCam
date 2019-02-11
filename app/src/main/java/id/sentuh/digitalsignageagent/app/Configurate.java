package id.sentuh.digitalsignageagent.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sonywibisono on 5/21/16.
 */
public class Configurate {

    public static final String PREF_NAME="digital-signage-agent";
    public static final String GCM_ID="gcm_id";
    public static final String VERSION="page_version";
    public static final String TOKEN="device_token";
    public static final String SERIAL_NO="serial_no";
    public static final String PAGE_ID="page_id";
    public static final String PAGE_PASSWORD="page_password";
    public static final String SERVER_URL="server_url";
    public static final String STARTUP_TIME="startup_time";
    public static final String SHUTDOWN_TIME="sthutdown_time";

    SharedPreferences pref;
    Context mContext;
    //private static String YOUR_DEVELOPER_KEY="AIzaSyBYFKajSqhRyrzx8Y869RbfYP5KbF_u8KE";
    public Configurate(Context context){
        this.mContext = context;

        pref = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

    }
    public void setKeyInt(String key, int value){
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key,value);
        editor.commit();
    }
    public void setKeyString(String key, String value){
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key,value);
       // editor.apply();
        editor.commit();
    }
    public void setKeyFloat(String key, float value){
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(key,value);
        // editor.apply();
        editor.commit();
    }
    public int getKeyInt(String key){
        return pref.getInt(key,0);
    }
    public float getKeyFloat(String key){
        return pref.getFloat(key,0.0f);
    }
    public String getKeyString(String key){
        return pref.getString(key,"");
    }
    public String getToken(){
        return getKeyString(TOKEN);
    }
    public void setToken(String value){
        setKeyString(TOKEN,value);
    }
    public void setPagePassword(String value){
        setKeyString(PAGE_PASSWORD,value);
    }
    public String getPagePassword(){
        return getKeyString(PAGE_PASSWORD);
    }
    public void setPageId(int value){
        setKeyInt(PAGE_ID,value);
    }
    public int getPageId() { return getKeyInt(PAGE_ID);}
    public void setVersion(int version) {
        setKeyInt(VERSION,version);
    }
    public int getVersion(){
        return getKeyInt(VERSION);
    }
    public String getServerUrl() { return getKeyString(SERVER_URL);}
    public void setServerUrl(String value){
        setKeyString(SERVER_URL,value);
    }
    public void setSerialNo(String value ){
        setKeyString(SERIAL_NO,value);
    }
    public String getSerialNo(){
        return getKeyString(SERIAL_NO);
    }
    public String getShutdownTime(){
        return getKeyString(SHUTDOWN_TIME);
    }
    public void setStartupTime(String value){
        setKeyString(STARTUP_TIME,value);
    }
    public String getStartupTime(){
        return getKeyString(STARTUP_TIME);
    }
    public void setShutdownTime(String value){
        setKeyString(SHUTDOWN_TIME,value);
    }
}
