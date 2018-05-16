package com.harasoft.relaunch.DB;

import android.provider.BaseColumns;

/**
 * Created by anat on 17.09.17.
 * Список панелей и их порядок на экране
 */
public abstract class ListPanels implements BaseColumns {
    public static final String TABLE_NAME = "LIST_PANELS";
    // уникальный идентификатор панели
    public static final String COLUMN_NUMBER_PANEL = "NUMBERS_PANELS";
    // название (имя) панели
    public static final String COLUMN_PANEL_NAME = "PANEL_NAME";
    // порядковый номер при выводе на экран
    public static final String COLUMN_NUMBER_ON_SCREEN = "NUMBER_ON_SCREEN";
    // тип панели. 0 - обычный для кнопок, 1 - список файлов
    public static final String COLUMN_TYPE_PANEL = "TYPE_PANEL";
    // ==============================
    // типы панелей
    public static final int PANEL_FOR_BUTTONS = 0;
    public static final int PANEL_FOR_LIST_FILES = 1;

    // строка создания таблицы для списка панелей
    // ========общие строки===============
    private static final String TEXT_TYPE = " TEXT";
    private static final String NUMBER_TYPE = " INTEGER";
    private static final String CREATE = "CREATE TABLE IF NOT EXISTS ";
    private static final String UNIQUE = " UNIQUE";
    private static final String COMMA_SEP = ", ";
    // строка создания таблицы
    static final String SQL_CREATE_ENTRIES =
                    CREATE + TABLE_NAME + " ( " +
                    _ID + NUMBER_TYPE + "  PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    COLUMN_NUMBER_PANEL + NUMBER_TYPE + UNIQUE + COMMA_SEP +
                    COLUMN_PANEL_NAME + TEXT_TYPE + " DEFAULT 'New panel'" + COMMA_SEP +
                    COLUMN_NUMBER_ON_SCREEN + NUMBER_TYPE + COMMA_SEP +
                    COLUMN_TYPE_PANEL + NUMBER_TYPE + " DEFAULT " + PANEL_FOR_BUTTONS +
            " )";
}
