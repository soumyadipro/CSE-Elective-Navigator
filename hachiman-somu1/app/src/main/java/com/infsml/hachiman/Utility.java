package com.infsml.hachiman;

import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Utility {
    public static final String api_base="https://192.168.152.237:8080";
    public static JSONObject postJSON(String url_link,String jsonString) throws Exception{
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL"); // Add in try catch block if you get error.
        sc.init(null, trustAllCerts, new java.security.SecureRandom()); // Add in try catch block if you get error.
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        Log.i("PostJSON","Init");
        URL url = new URL(url_link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("content-type","application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        Log.i("PostJSON","connecting");
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(jsonString.getBytes());
        outputStream.flush();
        outputStream.close();
        InputStream inputStream = connection.getInputStream();
        Log.i("PostJSON","downloading");
        int i;StringBuffer stringBuffer=new StringBuffer();
        while ((i=inputStream.read())!=-1){
            stringBuffer.append((char)i);
        }
        String download = stringBuffer.toString();
        return new JSONObject(download);
    }
}
