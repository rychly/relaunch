package com.harasoft.relaunch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HttpBasicAuthentication {

    private String StrUrl, Username, Password;

    public HttpBasicAuthentication(String strUrl, String username, String password){

        StrUrl=strUrl;
        Username=username;
        Password=password;
    }
    public HttpURLConnection Connect(){

        HttpURLConnection result;
        URL url;
        String base64EncodedCredentials;
        int responseCode;

        try {
            url = new URL(StrUrl);
        } catch (MalformedURLException e) {
            return  null;
        }

        try {
            result = (HttpURLConnection)url.openConnection();
        } catch (IOException e) {
            return null;
        }


        if(!Username.equals("") && !Username.trim().equals("") && !Password.equals("")){
            base64EncodedCredentials = Base64Coder.encodeString(Username + ":" + Password);
            result.setRequestProperty("Authorization", "Basic " + base64EncodedCredentials);
            try {
                result.setRequestMethod("POST");
            } catch (ProtocolException e) {
                return null;
            }
            result.setDoOutput(true);
            result.setDoInput(true);
            try {
                responseCode = result.getResponseCode();
            } catch (IOException e) {
                return null;
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }
        }else{
            result.setReadTimeout(10000);
            result.setConnectTimeout(15000);
            try {
                result.setRequestMethod("GET");
            } catch (ProtocolException e) {
                return null;
            }
            result.setDoInput(true);
        }
        return result;
    }
}