package com.harasoft.relaunch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpBasicAuthentication {

    private String StrUrl, Username, Password;

    public HttpBasicAuthentication(String strUrl, String username, String password){

        StrUrl=strUrl;
        Username=username;
        Password=password;
    }
    public HttpURLConnection Connect() throws IOException {

        HttpURLConnection result;
        URL url;
        String base64EncodedCredentials;
        int responseCode;

        try {
            url = new URL(StrUrl);
        } catch (MalformedURLException e) {
            throw new IOException("Неправильно указан URL");
        }

        try {
            result = (HttpURLConnection)url.openConnection();
        } catch (IOException e) {
            throw new IOException("Невозможно установить Интернет-соединение с источником данных");
        }


        if(Username!="" && Password!=""){
            base64EncodedCredentials = Base64Coder.encodeString(Username + ":" + Password);
        }else{
            result.setReadTimeout(10000);
            result.setConnectTimeout(15000);
            result.setRequestMethod("GET");
            result.setDoInput(true);
            return result;
        }
        result.setRequestProperty("Authorization", "Basic " + base64EncodedCredentials);
        result.setRequestMethod("POST");
        result.setDoOutput(true);
        result.setDoInput(true);
        try {
            responseCode = result.getResponseCode();
        } catch (IOException e) {
            throw new IOException("Невозможно установить Интернет-соединение с источником данных");
        }

        if (responseCode == HttpURLConnection.HTTP_OK) {
            return result;
        }else{
            throw new IOException("Доступ к источнику данных невозможен. Ошибка "+ responseCode);
        }

    }
}