package com.cadergator10.advancedbasesecurity.util;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.List;
import java.util.function.Consumer;

public class HTTPBinRequest extends Thread { //TODO: MULTITHREAD THIS 100%! THIS WILL SLOW DOWN SERVER
    public enum ReqType {GET, POSTPARAM, POSTJSON, POSTPLAIN}
    public ReqType type;
    public String targetURL;
    public List<NameValuePair> params;
    public String text;
    public Consumer<ResponseHolder> callback;
    public String[] args;

    //Consumer<String> callback,
    public HTTPBinRequest(Consumer<ResponseHolder> callback, ReqType type, String targetURL, String[] args){
        this.type = type;
        this.targetURL = targetURL;
        this.callback = callback;
        this.args = args;
    }
    public HTTPBinRequest(Consumer<ResponseHolder> callback, ReqType type, String targetURL, List<NameValuePair> params, String[] args){
        this.type = type;
        this.targetURL = targetURL;
        this.params = params;
        this.callback = callback;
        this.args = args;
    }
    public HTTPBinRequest(Consumer<ResponseHolder> callback, ReqType type, String targetURL, String text, String[] args){
        this.type = type;
        this.targetURL = targetURL;
        this.text = text;
        this.callback = callback;
        this.args = args;
    }

    public void run()
    {
        try {
            // Displaying the thread that is running
            System.out.println(
                    "Thread " + Thread.currentThread().getId()
                            + " is performing an http request to " + targetURL);
            CloseableHttpClient httpclient = HttpClients.createDefault();
            if(type != ReqType.GET){
                HttpPost httppost = new HttpPost(targetURL);
                switch(type){
                    case POSTPARAM:
                        httppost.setEntity(new UrlEncodedFormEntity(params));
                        break;
                    case POSTPLAIN:
                        httppost.setEntity(new StringEntity(text));
                        httppost.setHeader("Accept", "text/plain");
                        httppost.setHeader("Content-type", "text/plain");
                        break;
                    case POSTJSON:
                        httppost.setEntity(new StringEntity(text));
                        httppost.setHeader("Accept", "application/json");
                        httppost.setHeader("Content-type", "application/json");
                        break;
                }
                callback.accept(new ResponseHolder(httpclient.execute(httppost), args));
            }
            else{
                HttpGet httpget = new HttpGet(targetURL);
                callback.accept(new ResponseHolder(httpclient.execute(httpget), args));
            }
        }
        catch (Exception e) {
            // Throwing an exception
            System.out.println("Exception is caught");
        }
    }


//    public HttpResponse executeGet(Consumer<String> callback , String targetURL) throws IOException {
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        HttpGet httpget = new HttpGet(targetURL);
//        return httpclient.execute(httpget);
//    }
//    public HttpResponse executePost(String targetURL, List<NameValuePair> params) throws IOException {
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        HttpPost httppost = new HttpPost(targetURL);
//        httppost.setEntity(new UrlEncodedFormEntity(params));
//        return httpclient.execute(httppost);
//    }
//    public HttpResponse executePostJson(String targetURL, String json) throws IOException {
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        HttpPost httppost = new HttpPost(targetURL);
//        httppost.setEntity(new StringEntity(json));
//        httppost.setHeader("Accept", "application/json");
//        httppost.setHeader("Content-type", "application/json");
//        return httpclient.execute(httppost);
//    }
//    public HttpResponse executePostText(String targetURL, String txt) throws IOException {
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        HttpPost httppost = new HttpPost(targetURL);
//        httppost.setEntity(new StringEntity(txt));
//        httppost.setHeader("Accept", "text/plain");
//        httppost.setHeader("Content-type", "text/plain");
//        return httpclient.execute(httppost);
//    }

}
