package com.harasoft.relaunch.DB;

import android.provider.BaseColumns;

public abstract class ListHistory implements BaseColumns {
    public static final String TABLE_NAME = "LIST_HISTORY";
    // resours
    public static final String COLUMN_RESOURCE_LOCATION = "RESOURCE";
    // path
    public static final String COLUMN_DIR = "NAME_DIR";
    // file
    public static final String COLUMN_NAME = "NAME_FILE";
    // state
    public static final String COLUMN_STATE = "FILE_STATE";
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
                    COLUMN_RESOURCE_LOCATION + NUMBER_TYPE + COMMA_SEP +
                    COLUMN_DIR + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_STATE + NUMBER_TYPE +
                    " )";
}