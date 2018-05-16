package com.harasoft.relaunch.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by anat on 02.09.17.
 * Реализация хелпера для ReLaunch
 */
public class RelaunchDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "RelaunchDB.db";

    public RelaunchDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        // создаем таблицы в базе
        //===============================================
        // строка создания таблицы для числа колонок
        db.execSQL(ListColumns.SQL_CREATE_ENTRIES);
        //==============================================
        // строка создания для списка серверов OPDS
        db.execSQL(ListOPDS.SQL_CREATE_ENTRIES);
        //==============================================
        // строка создания для списка панелей
        db.execSQL(ListPanels.SQL_CREATE_ENTRIES);
        //==============================================
        // строка создания для списка кнопок с действиями
        db.execSQL(ListButtonsPanel.SQL_CREATE_ENTRIES);
        //==============================================
        // строка создания таблицы для избранных приложений
        db.execSQL(ListAppFav.SQL_CREATE_ENTRIES);
        //==============================================
        // строка создания таблицы для последних запущенных приложений приложений
        db.execSQL(ListAppRun.SQL_CREATE_ENTRIES);
        //==============================================
        // строка создания таблицы для избранных файлов и папок
        db.execSQL(ListFavorites.SQL_CREATE_ENTRIES);
        //==============================================
        // строка создания таблицы для последних запущенных файлов
        db.execSQL(ListLastOpen.SQL_CREATE_ENTRIES);
        //==============================================
        // строка создания таблицы для домашних папок
        db.execSQL(ListHomeDir.SQL_CREATE_ENTRIES);
        //==============================================
        // строка создания таблицы для истории чтения
        db.execSQL(ListHistory.SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
