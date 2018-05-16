package com.harasoft.relaunch.DB;

import android.provider.BaseColumns;

/**
 * Created by anat on 08.10.17.
 * Последние запущенные приложения
 */
public abstract class ListAppRun implements BaseColumns {
    public static final String TABLE_NAME = "LIST_APPRUN";
    // здесь содержится имя пакета
    public static final String COLUMN_APP_PACKAGE = "APP_PACKAGE";
    // создаем таблицы в базе
    // ========общие строки===============
    private static final String TEXT_TYPE = " TEXT";
    private static final String NUMBER_TYPE = " INTEGER";
    private static final String CREATE = "CREATE TABLE IF NOT EXISTS ";
    private static final String UNIQUE = " UNIQUE";
    private static final String COMMA_SEP = ", ";
    //===============================================
    // строка создания таблицы для числа колонок
    static final String SQL_CREATE_ENTRIES =
            CREATE + TABLE_NAME + " ( " +
                    _ID + NUMBER_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    COLUMN_APP_PACKAGE + TEXT_TYPE + UNIQUE +
                    " )";
}