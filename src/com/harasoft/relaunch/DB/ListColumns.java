package com.harasoft.relaunch.DB;

import android.provider.BaseColumns;

/**
 * Created by anat on 06.10.17.
 * Число колонок в папке.
 */
    /* Inner class that defines the table contents */
public abstract class ListColumns implements BaseColumns {
    public static final String TABLE_NAME = "LIST_COLUMNS";
    // здесь содержится полный путь к папке
    public static final String COLUMN_PATH_ID = "FULL_PATH_ID";
    // если число = 0 - то это Автоматически, > 0 - это число колонок, == -1 - это по умолчанию
    public static final String COLUMN_NAME_NUMBER = "NUMBERS_OF_COLUMNS";
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
                    _ID + NUMBER_TYPE + "  PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    COLUMN_PATH_ID + TEXT_TYPE + UNIQUE + COMMA_SEP +
                    COLUMN_NAME_NUMBER + NUMBER_TYPE +
                    " )";
}
