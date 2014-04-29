package com.harasoft.relaunch;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;


public class DropBoxActivity extends Activity {

    final static private String APP_KEY = "2vfpyoojj4rsi5t";
    final static private String APP_SECRET = "m6okqlqhi1nugq1";
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

    final static public String ACCOUNT_PREFS_NAME = "prefs";
    final static public String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static public String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    public static DropboxAPI<AndroidAuthSession> mDBApi;
    ReLaunchApp app;

    Button droplink_btn;
    Button dropunlink_btn;
    Button download_btn;
    Button upload_btn;
    Button dbselect_btn;
    TextView str_dropbox_folder;
    TextView str_local_folder;
    static private int flag_download=0;
    SharedPreferences prefs;
    String DBLocalPath;
    String DBPath;
    int count_download_files = 0;
    boolean flagDelDB;
    boolean flagDelLocFile;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WifiManager wfm = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if(wfm.getWifiState() != WifiManager.WIFI_STATE_ENABLED){
            showToast("Wi-Fi off!");
            finish();
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        app = ((ReLaunchApp) getApplicationContext());
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.dropsync);
        final Download_Dropbox[] downdrop = new Download_Dropbox[1];
        final Upload_Dropbox[] uploaddrop = new Upload_Dropbox[1];
        String str_f;
        Locale locale;

        N2EpdController.n2MainActivity = this;

        str_f = prefs.getString("lang", "default");
        if (!str_f.equals("default")) {
            locale = new Locale(str_f);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, null);
        }

        DBPath = prefs.getString("DropBoxfolder", "none");
        str_f = DBPath.trim();
        if(str_f.lastIndexOf("/") == str_f.length()-1){
            DBPath = str_f.substring(0, str_f.length()-1);
        }else{
            DBPath = str_f;
        }
        DBLocalPath = prefs.getString("LocalfolderDropbox", "none");
        str_f = DBLocalPath.trim();
        if(str_f.lastIndexOf("/") == str_f.length()-1){
            DBLocalPath = str_f.substring(0, str_f.length()-1);
        }else{
            DBLocalPath = str_f;
        }
        flagDelDB = prefs.getBoolean("deleteDropboxFiles", false);
        flagDelLocFile = prefs.getBoolean("deleteLocalFiles", false);

        droplink_btn = (Button) findViewById(R.id.dblink_btn);
        dropunlink_btn = (Button) findViewById(R.id.bdunlink_btn);
        download_btn = (Button) findViewById(R.id.dowload_btn);
        upload_btn = (Button) findViewById(R.id.upload_btn);
        dbselect_btn = (Button) findViewById(R.id.select_and_dowload);

        ImageButton exit_btn = (ImageButton) findViewById(R.id.exitdb_btn);
        str_dropbox_folder = (TextView) findViewById(R.id.tV_dropbox_folder);
        str_local_folder = (TextView) findViewById(R.id.tV_local_folder);

        str_f = getString(R.string.pref_i_DropBox_folder);
        str_dropbox_folder.setText(str_f + ": " + DBPath);
        str_f = getString(R.string.pref_i_Local_folder_DropBox);
        str_local_folder.setText(str_f + ": " + DBLocalPath);
        checkAppKeySetup();
        AndroidAuthSession session = buildSession();

        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        droplink_btn.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                mDBApi.getSession().startAuthentication(DropBoxActivity.this);
            }
        } );

        dropunlink_btn.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                logOut();
                droplink_btn.setEnabled(true);
                dropunlink_btn.setEnabled(false);
                download_btn.setEnabled(false);
                upload_btn.setEnabled(false);
                dbselect_btn.setEnabled(false);
            }
        } );
        exit_btn.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                if(N2DeviceInfo.EINK_NOOK){
                    ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    am.restartPackage("Browser");
                }
                finish();
            }
        } );

        download_btn.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                File folder_check;
                folder_check = new File(DBLocalPath);
                if (!folder_check.exists()) {
                    if(!folder_check.mkdirs()){
                        showToast("Error create folder");
                        finish();
                    }
                }
                downdrop[0] = new Download_Dropbox();
                downdrop[0].execute();
            }
        } );

        upload_btn.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                uploaddrop[0] = new Upload_Dropbox();
                uploaddrop[0].execute();
            }
        } );

        if (DBPath.equals("none") || DBLocalPath.equals("none")){
            droplink_btn.setEnabled(false);
            dropunlink_btn.setEnabled(false);
            download_btn.setEnabled(false);
            upload_btn.setEnabled(false);
            dbselect_btn.setEnabled(false);
            showToast(getString(R.string.srt_dbactivity_warn_no_config));//WARNING! No configure! Exit for configure.
        }
        if (flag_download == 1){
            download_btn.setEnabled(false);
            upload_btn.setEnabled(false);
            dbselect_btn.setEnabled(false);
        }
        dbselect_btn.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(DropBoxActivity.this, DropboxSelect.class);
                intent.putExtra("DBLocalPath", DBLocalPath);
                startActivity(intent);
            }
        } );
    }

    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);

                droplink_btn.setEnabled(false);
                dropunlink_btn.setEnabled(true);
                if (flag_download == 1){
                    download_btn.setEnabled(false);
                    upload_btn.setEnabled(false);
                    dbselect_btn.setEnabled(false);
                }else{
                    download_btn.setEnabled(true);
                    upload_btn.setEnabled(true);
                    dbselect_btn.setEnabled(true);
                }
            } catch (IllegalStateException e) {
                showToast(getString(R.string.srt_dbactivity_err_authent));//"Error connect for Authentication");
                finish();
            }
        }
    }

    private void storeKeys(String key, String secret) {
        // Save the access key for later
        prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }
    private String[] getKeys() {
        prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
            String[] ret = new String[2];
            ret[0] = key;
            ret[1] = secret;
            return ret;
        } else {
            return null;
        }
    }
    private void clearKeys() {
        prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            droplink_btn.setEnabled(false);
            dropunlink_btn.setEnabled(true);
            if (flag_download == 0){
                download_btn.setEnabled(true);
                upload_btn.setEnabled(true);
                dbselect_btn.setEnabled(true);
            }
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            droplink_btn.setEnabled(true);
            dropunlink_btn.setEnabled(false);
            download_btn.setEnabled(false);
            upload_btn.setEnabled(false);
            dbselect_btn.setEnabled(false);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
    private void logOut() {
        // Remove credentials from the session
        mDBApi.getSession().unlink();
        // Clear our stored keys
        clearKeys();
    }
    private void checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
            finish();
            return;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            finish();
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    private class Download_Dropbox extends AsyncTask<Void, Void, Void> {
        boolean flagErrorDownload = false;

        protected Void doInBackground(Void... params) {
            ArrayList<String> fullListFiles =  new ArrayList<String>();

            listFiles(DBLocalPath + File.separator, fullListFiles, DBLocalPath.length());

            count_download_files = 0;
            if(!loadFiles(DBPath + File.separator, fullListFiles, DBPath.length()))
                flagErrorDownload = true;
            Log.i("Relaunch", "flagDelLocFile: "+ flagDelLocFile);
            if(flagDelLocFile && !flagErrorDownload){

                for (String fullListFile : fullListFiles) {
                    (new File(DBLocalPath + fullListFile)).delete();
                }
            }
            fullListFiles.clear();
            return null;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            download_btn.setEnabled(false);
            dropunlink_btn.setEnabled(false);
            upload_btn.setEnabled(false);
            dbselect_btn.setEnabled(false);
            flag_download = 1;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            download_btn.setEnabled(true);
            dropunlink_btn.setEnabled(true);
            upload_btn.setEnabled(true);
            dbselect_btn.setEnabled(true);
            if(flagErrorDownload){
                showToast(getString(R.string.srt_dbactivity_err_down_dropbox));//"Error dowload files from DropBox");
            }
            showToast(getString(R.string.srt_dbactivity_file_down) + Integer.toString(count_download_files) + getString(R.string.srt_dbactivity_file_down2));//"Download " +" files"

            flag_download = 0;
        }

    }
    private boolean loadFiles(String DropBox_Path, ArrayList<String> fullListFiles, int start_pos_path_db) {
        DropboxAPI.Entry entries;
        FileOutputStream mFos;
        File file;

        try {
            entries = mDBApi.metadata(DropBox_Path, 0, null, true, null);
        } catch (DropboxException e) {
            return false;
        }
        String FullPath;
        for (DropboxAPI.Entry e : entries.contents) {
            if (!e.isDeleted) {
                FullPath = e.path.substring(start_pos_path_db);
                file=new File(DBLocalPath+FullPath);

                if(e.isDir){
                    if (!file.exists()) {
                        if(!file.mkdirs()){
                            return false;
                        }
                    }
                    if(!loadFiles(e.path + File.separator, fullListFiles, start_pos_path_db)){
                        return false;
                    }
                }else{
                    if(fullListFiles.contains(FullPath)){
                        fullListFiles.remove(FullPath);
                    }else{
                        try {
                            mFos = new FileOutputStream(file);
                        } catch (FileNotFoundException e1) {
                            return false;
                        }
                        try {
                            mDBApi.getFile(e.path, null, mFos, null);
                        } catch (DropboxException e1) {
                            file.delete();
                            return false;
                        }
                        try {
                            mFos.close();
                        } catch (IOException e1) {
                            return false;
                        }
                        count_download_files++;
                    }
                }
            }
        }
        return true;
    }
    private void listFiles(String Path, ArrayList<String> fullListFiles, int start_pos_path){
        String[] strDirList = (new File(Path)).list();
        String strPath = Path.substring(start_pos_path);

        for (String aStrDirList : strDirList) {
            File f1 = new File(Path + aStrDirList);
            if (f1.isFile()) {
                fullListFiles.add(strPath + aStrDirList);
            } else {
                listFiles(Path + aStrDirList + File.separator, fullListFiles, start_pos_path);
            }
        }
    }

    private class Upload_Dropbox extends AsyncTask<Void, Void, Void> {
        boolean flagErrorDownload = false;

        protected Void doInBackground(Void... params) {
            ArrayList<String> fullListFilesDB =  new ArrayList<String>();

            if(!listFilesDB(DBPath+File.separator, fullListFilesDB, DBPath.length())){
                flagErrorDownload = true;
            }
            count_download_files = 0;
            if(!uploadFiles(DBLocalPath +File.separator, fullListFilesDB, DBLocalPath.length()) && !flagErrorDownload)
                flagErrorDownload = true;
            if(flagDelDB && !flagErrorDownload){
                for (String aFullListFilesDB : fullListFilesDB) {
                    try {
                        mDBApi.delete(DBPath + aFullListFilesDB);
                    } catch (DropboxException e) {
                        flagErrorDownload = true;
                        break;
                    }
                }
            }
            fullListFilesDB.clear();
            return null;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            download_btn.setEnabled(false);
            dropunlink_btn.setEnabled(false);
            upload_btn.setEnabled(false);
            dbselect_btn.setEnabled(false);
            flag_download = 1;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            download_btn.setEnabled(true);
            dropunlink_btn.setEnabled(true);
            upload_btn.setEnabled(true);
            dbselect_btn.setEnabled(true);
            if(flagErrorDownload){
                showToast(getString(R.string.srt_dbactivity_err_upl_dropbox));//"Error upload files from DropBox");
            }
            showToast("Upload " + Integer.toString(count_download_files) + " files");
            flag_download = 0;
        }
    }
    private boolean uploadFiles(String Path, ArrayList<String> fullListFilesDB, int start_pos_path) {
        String[] strDirList = (new File(Path)).list();
        String strPath = Path.substring(start_pos_path);
        FileInputStream mFos;

        for (String aStrDirList : strDirList) {
            File f1 = new File(Path + aStrDirList);
            if (f1.isFile()) {
                if (!fullListFilesDB.contains(strPath + f1.getName())) {

                    try {
                        mFos = new FileInputStream(f1);
                    } catch (FileNotFoundException e) {
                        return false;
                    }
                    try {
                        mDBApi.putFile(DBPath + "/" + strPath + File.separator + f1.getName(), mFos, f1.length(), null, null);
                    } catch (DropboxException e) {
                        return false;
                    }
                    count_download_files++;
                } else {
                    fullListFilesDB.remove(strPath + f1.getName());
                }
            } else {
                if (!uploadFiles(Path + aStrDirList + File.separator, fullListFilesDB, start_pos_path)) {
                    return false;
                }
            }
        }
        return true;
    }
    private boolean listFilesDB(String PathDB, ArrayList<String> fullListFilesDB, int start_pos_path_db){
        String strPath = PathDB.substring(start_pos_path_db);
        DropboxAPI.Entry entries;
        try {
            entries = mDBApi.metadata(PathDB, 0, null, true, null);
        } catch (DropboxException e) {
            return false;
        }

        for (DropboxAPI.Entry e : entries.contents) {
            if (!e.isDeleted) {
                if(e.isDir){
                    if(!listFilesDB(e.path+File.separator, fullListFilesDB, start_pos_path_db)){
                        return false;
                    }
                }else{
                    fullListFilesDB.add(strPath +e.fileName());
                }
            }
        }
        return true;
    }

}