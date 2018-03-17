package com.example.lungful.translator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Антон on 01.03.2018.
 */

public class BD {

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private final Context context;
    public BD(Context context){
        this.context=context;
    }
    public void open(){
        dbHelper=new DBHelper(context, "myDB", null, 1);
        db=dbHelper.getWritableDatabase();
    }
    public void close(){
        if(dbHelper!=null) dbHelper.close();
    }
    public Cursor getAllData(){
        return db.query("myTable", null, null,null,null,null,"_id DESC");
    }
    public void addRec(String source, String translated, String date, String lang){


            ContentValues cv = new ContentValues();
            cv.put("source", source);
            cv.put("translated", translated);
            cv.put("date", date);
            cv.put("lang", lang);
            db.insert("myTable", null, cv);

    }
    public void updRec(String translated, int id){
        ContentValues cv=new ContentValues();
        cv.put("translated", translated);
        db.update("myTable", cv, "_id=?", new String[]{id+""});
    }
    public void delRec(long id){
        db.delete("myTable", "_id="+id, null);
    }


    private class DBHelper extends SQLiteOpenHelper{
        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
            super(context, name, factory, version);
        }
        public void onCreate(SQLiteDatabase db){
            db.execSQL("create table myTable(_id integer primary key autoincrement, source text, translated text, date text, lang text);");
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        }
    }
}
