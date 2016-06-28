package com.harasoft.relaunch;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class HttpBasicAuthentication {

    private String StrUrl, Username, Password;


    public HttpBasicAuthentication(String strUrl, int opdsID,  Context context){
        StrUrl=strUrl;

        getdbOPDS(opdsID, context);
    }

    public InputStream Connect(){

        HttpURLConnection result;
        URL url;
        String base64EncodedCredentials;
        int responseCode;
        InputStream inpStream;
        int READ_TIMEOUT = 60000;
        int CONNECT_TIMEOUT = 60000;
        Proxy.Type typeProxy = Proxy.Type.HTTP;
        String ipAddresProxy = "127.0.0.1";
        int portProxy = 8118;

        try {
            url = new URL(StrUrl);
            // определяем использование proxy
            URL newURL = url;
            boolean useOrobotProxy = false;
            String host = url.getHost();
            if (host.endsWith(".onion")) {
                useOrobotProxy = true;
            }
            String oldAddress = url.toString();
            if (oldAddress.startsWith("orobot://")) {
                newURL = new URL("http://" + oldAddress.substring(9)); // skip orobot://
                useOrobotProxy = true;
            } else if (oldAddress.startsWith("orobots://")) {
                newURL = new URL("https://" + oldAddress.substring(10)); // skip orobots://
                useOrobotProxy = true;
            }
            Proxy proxy = null;

            if (useOrobotProxy) {
                proxy = new Proxy(typeProxy, new InetSocketAddress(ipAddresProxy, portProxy)); // ORobot proxy running on this device
            }
            URLConnection conn;
            if (proxy == null){
                conn = newURL.openConnection();
            }else{
                conn = newURL.openConnection(proxy);
            }
            if ( conn instanceof HttpsURLConnection ) {
                // Install the all-trusting trust manager
                trustAllHosts();
                result = (HttpsURLConnection)conn;
            }else{
                result = (HttpURLConnection)conn;
            }

            // HTTP connection reuse which was buggy pre-froyo
            if (Integer.parseInt(Build.VERSION.SDK) < 8 ) { //Build.VERSION_CODES.FROYO) {
                System.setProperty("http.keepAlive", "false");
            }else{
                System.setProperty("http.keepAlive", "true");
            }


            if(OPDSActivity.sCookie!=null && OPDSActivity.sCookie.size()>0){// проверяем наличие куков
                for (String cookie : OPDSActivity.sCookie) {
                    result.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
                }
            }else if(!Username.equals("") && !Username.trim().equals("") && !Password.equals("")){// работаем без куков
                base64EncodedCredentials = Base64Coder.encodeString(Username + ":" + Password);
                result.setRequestProperty("Authorization", "Basic " + base64EncodedCredentials);
            }

            result.setRequestProperty("User-Agent", "ReLaunch(Android)");
            result.setInstanceFollowRedirects(true);
            result.setUseCaches(false);
            result.setAllowUserInteraction(false);
            result.setConnectTimeout(CONNECT_TIMEOUT);
            result.setReadTimeout(READ_TIMEOUT);
            result.setDoOutput(true);
            result.setDoInput(true);

        } catch (MalformedURLException e) {
            return null;
        }catch (IOException e) {
            return null;
        }

        try {
            result.connect();
        } catch (IOException e) {
            return null;
        }

        try {
            responseCode = result.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        List<String> cookies = result.getHeaderFields().get("Set-Cookie");
        if (cookies != null) {
            OPDSActivity.sCookie = cookies;
        }

        try {
            inpStream = result.getInputStream();
        } catch (IOException e) {
            inpStream = null;
        }
        return inpStream;
    }

    private static void trustAllHosts() {
        SSLContext Cur_SSL_Context;
        try{
            Cur_SSL_Context = SSLContext.getInstance("TLS");
            Cur_SSL_Context.init(null, new TrustManager[] { new X509_Trust_Manager() }, new SecureRandom());
        }
        catch (Exception e){
            return;
        }
        HostnameVerifier TRUST_ALL_CERTIFICATES = new HostnameVerifier(){
            @Override
            public boolean verify(String hostname, SSLSession session)
            {
                return true;
            }
        };
        HttpsURLConnection.setDefaultSSLSocketFactory(Cur_SSL_Context.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(TRUST_ALL_CERTIFICATES);

    }

    private static class X509_Trust_Manager implements X509TrustManager{
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {}
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {}
        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }
    }

    private void getdbOPDS(int id, Context context){
        MyDBHelper dbHelper = new MyDBHelper(context, "OPDS");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if(db == null){
            return;
        }
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query("OPDS", null, null, null, null, null, null);
        if (c.moveToPosition(id)) {
            // определяем номера столбцов по имени в выборке
            int enpassColIndex = c.getColumnIndex("EN_PASS");
            int loginColIndex = c.getColumnIndex("LOGIN");
            int passColIndex = c.getColumnIndex("PASSWORD");
            String temp = c.getString(enpassColIndex);

            if (temp != null && temp.equals("true")) {
                Username = c.getString(loginColIndex);
                Password = c.getString(passColIndex);
            }else{
                Username = "";
                Password = "";
            }
        }
        c.close();
        db.close();
        dbHelper.close();
    }
}

