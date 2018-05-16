package com.harasoft.relaunch.DB;

import android.provider.BaseColumns;

public abstract class ListHomeDir implements BaseColumns {
    public static final String TABLE_NAME = "LIST_HOME_DIR";
    // где находится
    public static final String COLUMN_RESOURCE_LOCATION = "RESOURCE";
    // полный путь к домашней папки
    public static final String COLUMN_PATH_HOME_DIR = "PATH_HOME_DIR";
    // имя домашней папки
    public static final String COLUMN_NAME_HOME_DIR = "NAME_HOME_DIR";

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
                    COLUMN_NAME_HOME_DIR + TEXT_TYPE + COMMA_SEP +
                    COLUMN_PATH_HOME_DIR + TEXT_TYPE +
                    " )";
}