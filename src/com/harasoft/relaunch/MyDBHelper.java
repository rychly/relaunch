package com.harasoft.relaunch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {

    final static int DB_VER = 1;
    static String DB_NAME = "";
    //=============   FTP ========
    final String TABLE_NAME_FTP = "FTP";
    final String TABLE_UNIQUE_FTP = "ID";
    final String CREATE_TABLE_FTP = "create table if not exists FTP ("
            + "ID integer primary key autoincrement, "
            + "SERVER text default '', "
            + "PORT integer default 21, "
            + "PATH text default '', "
            + "LOGIN text default 'anonymous', "
            + "PASSWORD text default 'anonymous') ";
    //=============   OPDS ========
    final String TABLE_NAME_OPDS = "OPDS";
    final String TABLE_UNIQUE_OPDS = "TITLE";
    final String CREATE_TABLE_OPDS = "create table if not exists OPDS ("
            + "ID integer primary key autoincrement, "
            + "TITLE text unique, "
            + "URE text default '', "
            + "EN_PASS text default 'false', "
            + "LOGIN text default '', "
            + "PASSWORD text default '',"
            + "EN_PROXY text default 'false', "
            + "TYPE_PROXY text default 1, "
            + "PROXY_NAME text default '127.0.0.1',"
            + "PROXY_PORT integer default 8080) ";
    //=============   BOOKS ========
    final String TABLE_NAME_BOOKS = "BOOKS";
    final String TABLE_UNIQUE_BOOKS = "FILE";
    final String CREATE_TABLE_BOOKS = "create table if not exists BOOKS ("
            + "ID integer primary key autoincrement, "
            + "FILE text unique, "
            + "TITLE text default '', "
            + "FIRSTNAME text default '', "
            + "LASTNAME text default '', "
            + "SERIES text default '', "
            + "NUMBER text default '')";
    //=============   PANELS ========
    final String TABLE_NAME_PANELS = "PANELS";
    final String TABLE_UNIQUE_PANELS = "ID";
    final String CREATE_TABLE_PANELS = "create table if not exists PANELS ("
            + "ID integer primary key autoincrement, "
            + "PANEL integer, "
            + "RUNJOB text default '', "
            + "NAMEJOB text default '',"
            + "RUNJOB_DC text default '', "
            + "NAMEJOB_DC text default '',"
            + "RUNJOB_LC text default '', "
            + "NAMEJOB_LC text default '')";
    //=============   SCREEN ========
    final String TABLE_NAME_SCREEN = "SCREEN";
    final String TABLE_UNIQUE_SCREEN = "ID";
    final String CREATE_TABLE_SCREEN = "create table if not exists SCREEN ("
            + "ID integer primary key autoincrement, "
            + "ID_PANEL integer)";
    //=============   LIST_PANELS ========
    final String TABLE_NAME_LIST_PANELS = "LIST_PANELS";
    final String TABLE_UNIQUE_LIST_PANELS = "NUMBER";
    final String CREATE_TABLE_LIST_PANELS = "create table if not exists LIST_PANELS ("
            + "ID integer primary key autoincrement, "
            + "PANEL_NAME text, "
            + "NUMBER integer unique)";
    //=============   CURRENT ========
    static String TABLE_CURRENT;
    static String TABLE_CURRENT_CREATE;
    static String TABLE_CURRENT_UNIQUE;
    static String dbFileName;

    Context mContext;

    public MyDBHelper(Context context, String nameTable) {
        super(context, nameTable + ".db", null, DB_VER);
        dbFileName = nameTable + ".db";
        DB_NAME = dbFileName;
        if (nameTable.equals("FTP")){
            TABLE_CURRENT = TABLE_NAME_FTP;
            TABLE_CURRENT_CREATE = CREATE_TABLE_FTP;
            TABLE_CURRENT_UNIQUE = TABLE_UNIQUE_FTP;
        }else if (nameTable.equals("OPDS")) {
            TABLE_CURRENT = TABLE_NAME_OPDS;
            TABLE_CURRENT_CREATE = CREATE_TABLE_OPDS;
            TABLE_CURRENT_UNIQUE = TABLE_UNIQUE_OPDS;
        }else if (nameTable.equals("BOOKS")) {
            TABLE_CURRENT = TABLE_NAME_BOOKS;
            TABLE_CURRENT_CREATE = CREATE_TABLE_BOOKS;
            TABLE_CURRENT_UNIQUE = TABLE_UNIQUE_BOOKS;
        }else if (nameTable.equals("PANELS")) {
            TABLE_CURRENT = TABLE_NAME_PANELS;
            TABLE_CURRENT_CREATE = CREATE_TABLE_PANELS;
            TABLE_CURRENT_UNIQUE = TABLE_UNIQUE_PANELS;
        }else if (nameTable.equals("SCREEN")) {
            TABLE_CURRENT = TABLE_NAME_SCREEN;
            TABLE_CURRENT_CREATE = CREATE_TABLE_SCREEN;
            TABLE_CURRENT_UNIQUE = TABLE_UNIQUE_SCREEN;
        }else if (nameTable.equals("LIST_PANELS")) {
            TABLE_CURRENT = TABLE_NAME_LIST_PANELS;
            TABLE_CURRENT_CREATE = CREATE_TABLE_LIST_PANELS;
            TABLE_CURRENT_UNIQUE = TABLE_UNIQUE_LIST_PANELS;
        }
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CURRENT_CREATE);
        db.execSQL("create index if not exists INDEX_" + TABLE_CURRENT_UNIQUE +" on " + TABLE_CURRENT +"(" + TABLE_CURRENT_UNIQUE + ")");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void resetDb(SQLiteDatabase db) {
        db.execSQL("delete from " + TABLE_CURRENT);
        db.execSQL("reindex INDEX_" + TABLE_CURRENT_UNIQUE);
    }

}