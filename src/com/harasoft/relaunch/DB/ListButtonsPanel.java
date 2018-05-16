package com.harasoft.relaunch.DB;

import android.provider.BaseColumns;

/**
 * Created by anat on 16.09.17.
 * Описание кнопок на панеле
 */
public abstract class ListButtonsPanel implements BaseColumns {
    // наименование таблицы
    public static final String TABLE_NAME = "LIST_BUTTONS";
    // уникальный идентификатор панели
    public  static final String COLUMN_NUMBER_PANEL = "NUMBERS_PANELS";
    // ниже действия повешенные  на кнопки
    // действия при одинарном клике
    public  static final String COLUMN_RUN_ONE_CLICK = "RUN_ONE_CLICK";
    public  static final String COLUMN_NAME_ONE_CLICK = "NAME_ONE_CLICK";
    // действия при двойном клике
    public  static final String COLUMN_RUN_DOUBLE_CLICK = "RUN_DOUBLE_CLICK";
    public  static final String COLUMN_NAME_DOUBLE_CLICK = "NAME_DOUBLE_CLICK";
    // действия при длинном клике
    public  static final String COLUMN_RUN_LONG_CLICK = "RUN_LONG_CLICK";
    public  static final String COLUMN_NAME_LONG_CLICK = "NAME_LONG_CLICK";
    // идентификатор типа иконки. 0 - OneClick, 1 - DoubleClick, 2 - LongClick, 3 - image + text, 4 - TextButton
    public  static final String COLUMN_IDENT_ICON = "IDENT_ICON";
    // позиция исонки на панели. Отсчёт слева.
    public  static final String COLUMN_BUTTON_POSITION = "ICON_POSITION";
    // ---------------------------------
    // для COLUMN_IDENT_ICON определяет иконку
    public static final int ICON_ONE_CLICK = 0;
    //public static final int ICON_DOUBLE_CLICK = 1;
    //public static final int ICON_LONG_CLICK = 2;
    public static final int ICON_IMAGE_TEXT = 3;
    public static final int ICON_TEXT = 4;
    //===============================================
    // строка создания для списка кнопок с действиями
    // ========общие строки===============
    private static final String TEXT_TYPE = " TEXT";
    private static final String NUMBER_TYPE = " INTEGER";
    private static final String CREATE = "CREATE TABLE IF NOT EXISTS ";
    private static final String COMMA_SEP = ", ";
    private static final String STRING_DEFAULT = " DEFAULT ''";
    // строка создания таблицы
    static final String SQL_CREATE_ENTRIES =
                    CREATE + TABLE_NAME + " ( " +
                    _ID + NUMBER_TYPE + "  PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    COLUMN_NUMBER_PANEL + NUMBER_TYPE + COMMA_SEP +
                    COLUMN_RUN_ONE_CLICK + TEXT_TYPE + STRING_DEFAULT + COMMA_SEP +
                    COLUMN_NAME_ONE_CLICK + TEXT_TYPE + STRING_DEFAULT + COMMA_SEP +
                    COLUMN_RUN_DOUBLE_CLICK + TEXT_TYPE + STRING_DEFAULT + COMMA_SEP +
                    COLUMN_NAME_DOUBLE_CLICK + TEXT_TYPE + STRING_DEFAULT + COMMA_SEP +
                    COLUMN_RUN_LONG_CLICK + TEXT_TYPE + STRING_DEFAULT + COMMA_SEP +
                    COLUMN_NAME_LONG_CLICK + TEXT_TYPE + STRING_DEFAULT + COMMA_SEP +
                    COLUMN_IDENT_ICON + NUMBER_TYPE + " DEFAULT " + ICON_ONE_CLICK + COMMA_SEP +
                    COLUMN_BUTTON_POSITION + NUMBER_TYPE +
            " )";
}
