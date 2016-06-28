package com.harasoft.relaunch;

import android.content.Context;
import android.content.SharedPreferences;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.FileInputStream;
import java.io.FileOutputStream;


public class MyDropboxClient {
    final static private String APP_KEY = "2vfpyoojj4rsi5t";
    final static private String APP_SECRET = "m6okqlqhi1nugq1";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    private static DropboxAPI<AndroidAuthSession> mDBApi;
    private SharedPreferences prefs;
    private boolean sessionIs = false;
    private Context mContext;

    public MyDropboxClient(SharedPreferences preferencess, Context context){
        prefs = preferencess;
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        mContext = context;
    }

    public DropboxAPI.Entry metadata(String path) {
        DropboxAPI.Entry entries;
        try {
            entries = mDBApi.metadata(path, 0, null, true, null);
        } catch (DropboxException e) {
            entries = null;
        }
        return entries;
    }
    public boolean delete(String path) {
        try {
            mDBApi.delete(path);
        } catch (DropboxException e) {
            return false;
        }
        return true;
    }
    public boolean putFile(String path, FileInputStream fileInputStream, long fileLength) {
        try {
            mDBApi.putFile(path, fileInputStream, fileLength, null, null);
        } catch (DropboxException e) {
            return false;
        }
        return true;
    }
    public boolean getFile(String path, FileOutputStream fileOutputStream) {
        try {
            mDBApi.getFile(path, null, fileOutputStream, null);
        } catch (DropboxException e1) {
            return false;
        }
        return true;
    }
    public boolean createFolder(String path) {
        try {
            mDBApi.createFolder(path);
        } catch (DropboxException e) {
            return false;
        }
        return true;
    }
    public boolean copy(String pathSrc, String pathDst) {
        try {
            mDBApi.copy(pathSrc, pathDst);
        } catch (DropboxException e) {
            return false;
        }
        return true;
    }
    public boolean move(String pathSrc, String pathDst) {
        try {
            mDBApi.move(pathSrc, pathDst);
        } catch (DropboxException e) {
            return false;
        }
        return true;
    }


    // новая версия с усилившимся шифрованием
    public void logOut() {
        // Remove credentials from the session
        mDBApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
    }
    private void clearKeys() {
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void loadAuth(AndroidAuthSession session) {
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0){
            sessionIs = false;
            return;
        }
        sessionIs = true;
        session.setOAuth2AccessToken(secret);
    }
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }
    public void logIn() {
        mDBApi.getSession().startOAuth2Authentication(mContext);
    }
    public boolean logFinish() {
        AndroidAuthSession session = mDBApi.getSession();
        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                storeAuth(session);
            } catch (IllegalStateException e) {
                return false;
            }
            return true;
        }else{
            return false;
        }

    }


    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
        }
    }

    public boolean getSession(){
        return sessionIs;
    }
}
