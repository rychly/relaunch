package com.harasoft.relaunch;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.dropbox.client2.DropboxAPI;

import java.io.*;
import java.util.ArrayList;


public class DropBoxActivity extends Activity {

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
    static MyDropboxClient dropboxClient;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!ReLaunch.haveNetworkConnection(getApplicationContext())){
            app.showToast(getString(R.string.srt_dbactivity_no_inet));//"No internet connection!");
            finish();
        }else{
            prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            app = ((ReLaunchApp) getApplicationContext());
            if(app == null ) {
                finish();
            }
            app.setFullScreenIfNecessary(this);
            setContentView(R.layout.dropsync);
            final Download_Dropbox[] downdrop = new Download_Dropbox[1];
            final Upload_Dropbox[] uploaddrop = new Upload_Dropbox[1];
            String str_f;

            N2EpdController.n2MainActivity = this;

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

            droplink_btn.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    dropboxClient.logIn();
                }
            } );

            dropunlink_btn.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    dropboxClient.logOut();
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
                            app.showToast(getString(R.string.srt_dbactivity_warn_create_folder));//"Error create folder");
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
                app.showToast(getString(R.string.srt_dbactivity_warn_no_config));//WARNING! No configure! Exit for configure.
                finish();
            }
            dropboxClient = new MyDropboxClient(prefs, DropBoxActivity.this);
            if (!dropboxClient.getSession()){
                droplink_btn.setEnabled(true);
                dropunlink_btn.setEnabled(false);
                download_btn.setEnabled(false);
                upload_btn.setEnabled(false);
                dbselect_btn.setEnabled(false);
            }else{
                droplink_btn.setEnabled(false);
                dropunlink_btn.setEnabled(true);
                download_btn.setEnabled(true);
                upload_btn.setEnabled(true);
                dbselect_btn.setEnabled(true);
            }
            if (flag_download == 1){
                download_btn.setEnabled(false);
                upload_btn.setEnabled(false);
                dbselect_btn.setEnabled(false);
            }
            dbselect_btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(DropBoxActivity.this, DropboxSelect.class);
                    intent.putExtra("DBLocalPath", DBLocalPath);
                    startActivity(intent);
                }
            });
        }
    }

    protected void onResume() {
        super.onResume();
        if (dropboxClient.logFinish()){
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
        }
    }

    private class Download_Dropbox extends AsyncTask<Void, Void, Void> {
        boolean flagErrorDownload = false;

        protected Void doInBackground(Void... params) {
            ArrayList<String> fullListFiles =  new ArrayList<String>();

            listFiles(DBLocalPath + File.separator, fullListFiles, DBLocalPath.length());

            count_download_files = 0;
            if(!loadFiles(DBPath + File.separator, fullListFiles, DBPath.length()))
                flagErrorDownload = true;
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
                app.showToast(getString(R.string.srt_dbactivity_err_down_dropbox));//"Error dowload files from DropBox");
            }
            app.showToast(getString(R.string.srt_dbactivity_file_down) + Integer.toString(count_download_files) + getString(R.string.srt_dbactivity_file_down2));//"Download " +" files"

            flag_download = 0;
        }

    }
    private boolean loadFiles(String DropBox_Path, ArrayList<String> fullListFiles, int start_pos_path_db) {
        DropboxAPI.Entry entries;
        FileOutputStream mFos;
        File file;

        entries = dropboxClient.metadata(DropBox_Path);
        if (entries == null){
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
                        if (!dropboxClient.getFile(e.path, mFos)){
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
                    if (!dropboxClient.delete(DBPath + aFullListFilesDB)) {
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
                app.showToast(getString(R.string.srt_dbactivity_err_upl_dropbox));//"Error upload files from DropBox");
            }
            app.showToast(getString(R.string.srt_dbactivity_file_down) + Integer.toString(count_download_files) + getString(R.string.srt_dbactivity_file_down2));
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
                    if (!dropboxClient.putFile(DBPath + "/" + strPath + File.separator + f1.getName(), mFos, f1.length())){
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

        entries = dropboxClient.metadata(PathDB);
        if (entries == null) {
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