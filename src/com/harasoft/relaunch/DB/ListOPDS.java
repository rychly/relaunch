package com.harasoft.relaunch.DB;

import android.provider.BaseColumns;

/**
 * Created by anat on 07.10.17.
 * Список OPDS серверов
 */
public abstract class ListOPDS implements BaseColumns {
    public static final String TABLE_NAME = "LIST_OPDS";
    public static final String COLUMN_TITLE = "TITLE";
    // здесь содержится путь к OPDS
    public static final String COLUMN_LINK = "OPDS_SERVER";
    // "true" or "false"
    public static final String COLUMN_ENABLE_PASS = "ENABLE_PASS";
    public static final String COLUMN_LOGIN = "LOGIN";
    public static final String COLUMN_PASSWORD = "PASSWORD";
    // "true" or "false"
    public static final String COLUMN_ENABLE_PROXY = "ENABLE_PROXY";
    public static final String COLUMN_PROXY_TYPE = "TYPE_PROXY";
    public static final String COLUMN_PROXY_NAME = "PROXY_NAME";
    public static final String COLUMN_PROXY_PORT = "PROXY_PORT";

    // ========общие строки===============
    private static final String TEXT_TYPE = " TEXT";
    private static final String NUMBER_TYPE = " INTEGER";
    private static final String NUMERIC_TYPE = " NUMERIC";
    private static final String CREATE = "CREATE TABLE IF NOT EXISTS ";
    private static final String UNIQUE = " UNIQUE";
    private static final String COMMA_SEP = ", ";
    private static final String STRING_DEFAULT = " DEFAULT ''";
    //==============================================
    // строка создания для списка серверов OPDS
    static final String SQL_CREATE_ENTRIES =
            CREATE + TABLE_NAME + " ( " +
                    _ID + NUMBER_TYPE + "  PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    COLUMN_TITLE + TEXT_TYPE + UNIQUE + COMMA_SEP +
                    COLUMN_LINK + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ENABLE_PASS + NUMERIC_TYPE + " DEFAULT 0" + COMMA_SEP +
                    COLUMN_LOGIN + TEXT_TYPE + STRING_DEFAULT + COMMA_SEP +
                    COLUMN_PASSWORD + TEXT_TYPE + STRING_DEFAULT + COMMA_SEP +
                    COLUMN_ENABLE_PROXY + NUMERIC_TYPE + " DEFAULT 0" + COMMA_SEP +
                    COLUMN_PROXY_TYPE + NUMBER_TYPE + " DEFAULT 1" + COMMA_SEP +
                    COLUMN_PROXY_NAME + TEXT_TYPE + " DEFAULT '127.0.0.1'" + COMMA_SEP +
                    COLUMN_PROXY_PORT + NUMBER_TYPE + " DEFAULT 8118" +
                    " )";
}
