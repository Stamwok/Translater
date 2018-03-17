package com.example.lungful.translator;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends FragmentActivity implements CompoundButton.OnCheckedChangeListener, AsyncResponse, LoaderCallbacks<Cursor>, AdapterView.OnItemLongClickListener {

    EditText etSource;
    TextView tvTranslate, tvLang1, tvLang2;
    String languagePair;
    Switch switch1;
    BD bd;
    SimpleCursorAdapter scAdapter;
    SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss\ndd.MM.yyyy");
    Date dateNow;
    SimpleCursorAdapter cvAdapter;
    ListView lvFrag;
    Timer myTimer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSource=(EditText)findViewById(R.id.etSource);
        tvTranslate=(TextView)findViewById(R.id.tvTranslate);
        switch1=(Switch)findViewById(R.id.switch1);
        tvLang1=(TextView)findViewById(R.id.tvLang1);
        tvLang2=(TextView)findViewById(R.id.tvLang2);
        switch1.setOnCheckedChangeListener(this);
        onCheckedChanged(switch1, false);
        bd=new BD(this);
        bd.open();

        String[] from=new String[]{"source","translated","date", "translated"};
        int[] to=new int[]{R.id.tvlSource, R.id.tvlTranslated, R.id.tvlDate, R.id.spinner};
        bd.open();
        cvAdapter=new SimpleCursorAdapter(this, R.layout.item, null,from,to,0);
        cvAdapter.setViewBinder(new MyViewBinder());
        lvFrag=(ListView)findViewById(R.id.lvFrag);
        lvFrag.setAdapter(cvAdapter);
       // registerForContextMenu(lvFrag);
        lvFrag.setOnItemLongClickListener(this);
        getSupportLoaderManager().initLoader(0, null, this);
        updateTable(this);


        }
        protected void onDestroy(){
            super.onDestroy();
            bd.close();
            myTimer.cancel();
        }

    public void onCheckedChanged(CompoundButton switch1, boolean isChecked){
        if(isChecked) {
                languagePair="ru-en";
                tvLang2.setText("Английский");
                tvLang1.setText("Русский");
            }
        else
            {
                languagePair = "en-ru";
                tvLang1.setText("Английский");
                tvLang2.setText("Русский");
            }

        }
       public boolean onItemLongClick(AdapterView<?> adapter, View v, int pos, long id){

            bd.delRec(id);
            getSupportLoaderManager().getLoader(0).forceLoad();
            return false;
       }

    public void onclick (View v) {
        if(!etSource.getText().toString().equals("")&& hasConnect(this)) {
            translate(etSource.getText().toString(), languagePair);

        }
        else if(!etSource.getText().toString().equals("")){

            dateNow = new Date();
            bd.addRec(etSource.getText().toString(), "", sdf.format(dateNow), languagePair);
            getSupportLoaderManager().getLoader(0).forceLoad();

        }
    }

    void translate(String text, String languages){

            TranslatorBackgroundTask translator = new TranslatorBackgroundTask(this);
            translator.execute(text, languages);
        }
    public void processFinish(String output){

            tvTranslate.setText(output);
            dateNow = new Date();
            bd.addRec(etSource.getText().toString(), output, sdf.format(dateNow), languagePair);
            getSupportLoaderManager().getLoader(0).forceLoad();


    }
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl){
        return new MyCursorLoader(this, bd);
    }
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
        cvAdapter.swapCursor(cursor);
    }
    public void onLoaderReset(Loader<Cursor> loader){

    }
    static class MyCursorLoader extends CursorLoader {
        BD bd;
        public MyCursorLoader(Context context, BD bd){
            super(context);
            this.bd=bd;
        }
        public Cursor loadInBackground(){

           return bd.getAllData();

        }
    }
    public static boolean hasConnect(Context context){
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo=cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(netInfo!=null && netInfo.isConnected()){
            return true;
        }
        netInfo=cm.getNetworkInfo((ConnectivityManager.TYPE_MOBILE));
        if(netInfo!=null && netInfo.isConnected()){
            return true;
        }
        return false;
    }

    public void updateTable(final Context context){
        myTimer=new Timer();
        final Handler uiHandler=new Handler(){
            public void handleMessage(android.os.Message msg){
                getSupportLoaderManager().getLoader(0).forceLoad();
            }
        };


        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                int id;
                String translated;
                String source;
                String lang;
                Cursor cursor = bd.getAllData();
                if (cursor.moveToFirst()) {
                    do {
                        id = cursor.getInt(cursor.getColumnIndex("_id"));
                        translated = cursor.getString(cursor.getColumnIndex("translated"));
                        source = cursor.getString(cursor.getColumnIndex("source"));
                        lang = cursor.getString(cursor.getColumnIndex("lang"));
                        if (translated.equals("")&&hasConnect(context)) {
                            try {

                                String yandexKey = "trnsl.1.1.20180225T204603Z.e362749a7ccf258d.dc3daac122a9d98a3d168a08b8340000dd61841e";
                                String yandexUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?key="+yandexKey+"&text="+source+"&lang="+lang;
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
                                translated=resultString;
                                bd.updRec(translated, id);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            uiHandler.sendEmptyMessage(1);
                        }

                    } while (cursor.moveToNext());

                }
                cursor.close();
            }

        },0L, 1000L);

    }
    class MyViewBinder implements SimpleCursorAdapter.ViewBinder{
        public boolean setViewValue(View v, Cursor c, int column) {
            if (v.getId() == R.id.spinner) {
                if (!c.getString(column).equals("")) {
                    v.setVisibility(View.GONE);
                }
                else v.setVisibility(View.VISIBLE);
                return true;
            }
            else return false;
        }
    }

}



