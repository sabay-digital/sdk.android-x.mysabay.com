package kh.com.mysabay.sdk.pojo.login;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import kh.com.mysabay.sdk.utils.LogUtil;

public class CurrentCountry extends AsyncTask<String, Integer, String> {

    private TaskComplete listener;

    public CurrentCountry(TaskComplete listener){
        this.listener=listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        String str = "";
        try {
           URL url = new URL(strings[0]);
           HttpURLConnection connection = (HttpURLConnection) url.openConnection();
           connection.setRequestMethod("GET");
           InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            int c;
            while ((c = bufferedReader.read()) != -1) {
                str += String.valueOf((char) c);
            }
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        }
    }


    @Override
    protected void onPostExecute(String strings) {
        super.onPostExecute(strings);
        listener.onTaskCompleted(strings);
    }

}
