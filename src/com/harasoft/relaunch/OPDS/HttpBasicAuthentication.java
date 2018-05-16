package com.harasoft.relaunch.OPDS;

import android.content.Context;
import android.os.Build;
import com.harasoft.relaunch.Utils.InfoConnectOPDS;
import com.harasoft.relaunch.Utils.UtilOPDS;
import biz.source_code.base64Coder.Base64Coder;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

class HttpBasicAuthentication {

    private String StrUrl, Username, Password;
    private InfoConnectOPDS infoConnectOPDS;


    HttpBasicAuthentication(String strUrl, int opdsID, Context context){
        StrUrl=strUrl;
        infoConnectOPDS = (new UtilOPDS(context)).getInfoOPDS(opdsID);
        Username = infoConnectOPDS.getLogin();
        Password = infoConnectOPDS.getPassword();
    }

    InputStream Connect(){

        HttpURLConnection result;
        URL url;
        String base64EncodedCredentials;
        int responseCode;
        InputStream inpStream;
        int READ_TIMEOUT = 60000;
        int CONNECT_TIMEOUT = 60000;
        Proxy proxy = null;

        if (infoConnectOPDS.isEnable_proxy()) {
            Proxy.Type type_proxy = null;
            String ipAddresProxy;
            int portProxy;
            // устанавливаем тип прокси
            if (infoConnectOPDS.getProxy_type() == 0) {
                type_proxy = Proxy.Type.DIRECT;
            }
            if (infoConnectOPDS.getProxy_type() == 1) {
                type_proxy = Proxy.Type.HTTP;
            }
            if (infoConnectOPDS.getProxy_type() == 2) {
                type_proxy = Proxy.Type.SOCKS;
            }
            // IP прокси
            ipAddresProxy = infoConnectOPDS.getProxy_name();
            portProxy = infoConnectOPDS.getProxy_port();
            proxy = new Proxy(type_proxy, new InetSocketAddress(ipAddresProxy, portProxy));
        }


        try {
            url = new URL(StrUrl);
            // определяем использование proxy
            URL newURL = url;

            String oldAddress = url.toString();
            if (oldAddress.startsWith("orobot://")) {
                newURL = new URL("http://" + oldAddress.substring(9)); // skip orobot://
            } else if (oldAddress.startsWith("orobots://")) {
                newURL = new URL("https://" + oldAddress.substring(10)); // skip orobots://
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
            }else if(!Username.trim().equals("") && !Password.equals("")){// работаем без куков
                base64EncodedCredentials = Base64Coder.encodeString(Username + ":" + Password);
                result.setRequestProperty("Authorization", "Basic " + base64EncodedCredentials);
            }

            //result.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");
            result.setInstanceFollowRedirects(true);
            result.setUseCaches(false);
            result.setAllowUserInteraction(false);
            result.setConnectTimeout(CONNECT_TIMEOUT);
            result.setReadTimeout(READ_TIMEOUT);
            result.setDoOutput(true);
            result.setDoInput(true);

        } catch (MalformedURLException e) {
            System.out.println("- Error -- ");
            System.out.println("- result.setRequestProperty -- " + e);
            return null;
        }catch (IOException e) {
            System.out.println("- Error -- ");
            System.out.println("- result.setRequestProperty -- " + e);
            return null;
        }

        try {
            result.connect();
        } catch (IOException e) {
            System.out.println("- Error -- ");
            System.out.println("- result.connect() -- " + e);
            return null;
        }

        try {
            responseCode = result.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }
        } catch (IOException e) {
            System.out.println("- Error -- ");
            System.out.println("- result.getResponseCode() -- " + e);
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
            System.out.println("- Error -- ");
            System.out.println("- inpStream -- " + e);

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
}

