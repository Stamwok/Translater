package com.example.lungful.translator;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class TranslatorBackgroundTask extends AsyncTask<String, Void, String> {


    public AsyncResponse delegate;
    Context ctx;
    TranslatorBackgroundTask(AsyncResponse delegate){
        this.delegate = delegate;
    }




    @Override
    protected String doInBackground(String... params) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND + android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
        String textToBeTranslated = params[0];
        String languagePair = params[1];



        try {

            String yandexKey = "trnsl.1.1.20180225T204603Z.e362749a7ccf258d.dc3daac122a9d98a3d168a08b8340000dd61841e";
            String yandexUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?key="+yandexKey+"&text="+textToBeTranslated+"&lang="+languagePair;
            URL yandexTranslateURL = new URL(yandexUrl);


            HttpURLConnection httpJsonConnection = (HttpURLConnection) yandexTranslateURL.openConnection();
            InputStream inputStream = httpJsonConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));


            String resultString = bufferedReader.readLine().toString().trim();


            bufferedReader.close();
            inputStream.close();
            httpJsonConnection.disconnect();


            resultString = resultString.substring(resultString.indexOf("[")+1);
            resultString = resultString.substring(0,resultString.indexOf("]"));

            resultString = resultString.substring(resultString.indexOf("\"")+1);
            resultString = resultString.substring(0,resultString.indexOf("\""));
            return resultString;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String resultString) {
        super.onPostExecute(resultString);
        delegate.processFinish(resultString);
    }
    
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
