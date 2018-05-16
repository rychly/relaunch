package com.harasoft.relaunch.DB;

import android.provider.BaseColumns;

/**
 * Created by anat on 11.10.17.
 * Таблица для избранных папок и файлов
 */
public abstract class ListFavorites implements BaseColumns {
    public static final String TABLE_NAME = "LIST_FAVORITES";
    // здесь содержится полный путь к папке
    public static final String COLUMN_FULL_PATH = "FULL_PATH";
    public static final String COLUMN_NAME = "NAME";
    // type resours
    public static final String COLUMN_TYPE_RESOURCE = "TYPE";
    // resours location
    public static final String COLUMN_RESOURCE_LOCATION  = "TYPE_RESOURCE";
    // создаем таблицы в базе
    // ========общие строки===============
    private static final String TEXT_TYPE = " TEXT";
    private static final String NUMBER_TYPE = " INTEGER";
    private static final String CREATE = "CREATE TABLE IF NOT EXISTS ";
    private static final String COMMA_SEP = ", ";
    //===============================================
    // строка создания таблицы для числа колонок
    static final String SQL_CREATE_ENTRIES =
            CREATE + TABLE_NAME + " ( " +
                    _ID + NUMBER_TYPE + "  PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    COLUMN_FULL_PATH + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_TYPE_RESOURCE + NUMBER_TYPE + COMMA_SEP +
                    COLUMN_RESOURCE_LOCATION + NUMBER_TYPE +
                    " )";
}

