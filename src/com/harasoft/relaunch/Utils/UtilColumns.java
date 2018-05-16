package com.harasoft.relaunch.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import com.harasoft.relaunch.Adapter.ViewItem;
import com.harasoft.relaunch.DB.ListColumns;
import com.harasoft.relaunch.DB.RelaunchDBHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anat on 07.10.17.
 * Работа с колонками в списках
 */
public class UtilColumns {
    private Context context;
    // сами списки
    private HashMap<String, Integer> listColumns; // ключем является имя папки, значением ключа число колонок
    private int columnsDirsDefault;
    private String columnsAlgIntensity;

    public UtilColumns(Context context) {
        this.context = context;
        // загрузка списков
        listColumns = loadListFromDB(ListColumns.TABLE_NAME);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.columnsDirsDefault = Integer.parseInt(prefs.getString("columnsDirsFiles", "1"));
        columnsAlgIntensity = prefs.getString("columnsAlgIntensity", "70 3:5 7:4 15:3 48:2");
    }
    // ================ Число колонок в папке =========================
    // для числа колонок
    public int getNumberColumns(String fullPathToDir) {
        if (isNumberColumnDir(fullPathToDir)) {
            return listColumns.get(fullPathToDir);
        }else {
            return columnsDirsDefault;
        }
    }
    public void addListColumns(String fullPathToDir, int numberColumns) {
        if (isNumberColumnDir(fullPathToDir)) {
            listColumns.remove(fullPathToDir);
            delete(fullPathToDir, ListColumns.TABLE_NAME);
        }
        listColumns.put(fullPathToDir, numberColumns);
        insert(fullPathToDir, numberColumns,  ListColumns.TABLE_NAME);
    }
    private boolean isNumberColumnDir (String fullPathToDir ) {
        if (listColumns == null) {
            listColumns = loadListFromDB (ListColumns.TABLE_NAME);
        }
        return listColumns.containsKey(fullPathToDir);
    }
    public void delNumberColumns(String fullPathToDir) {
        if (isNumberColumnDir(fullPathToDir)) {
            listColumns.remove(fullPathToDir);
            delete(fullPathToDir, ListColumns.TABLE_NAME);
        }
    }
    // автоматический подбор числа колонок
    // автоматический подбор числа колонок
    public int getAutoColsNum(List<HashMap<String, String>> itemsArray, String polName) {
        // implementation - via percentiles len
        int auto_cols_num = 1;
        ArrayList<Integer> tmp = new ArrayList<>();

        if (itemsArray.size() > 0) {
            int factor ;
            for (HashMap<String, String> anItemsArray : itemsArray) {
                if (anItemsArray != null) {
                    tmp.add(anItemsArray.get(polName).length());
                }
            }
            String[] spat = columnsAlgIntensity.split("[\\s\\:]+");
            int quantile = Integer.parseInt(spat[0]);
            factor = Percentile(tmp, quantile);
            for (int i = 1; i < spat.length; i = i + 2) {
                try {
                    double fval = Double.parseDouble(spat[i]);
                    int cval = Integer.parseInt(spat[i + 1]);
                    if (factor <= fval) {
                        auto_cols_num = cval;
                        break;
                    }
                } catch (Exception e) {
                    // emply
                }
            }
        }
        if (auto_cols_num > itemsArray.size())
            auto_cols_num = itemsArray.size();
        return auto_cols_num;
    }
    public int getAutoColsNum(List<ViewItem> all_view_items) {
        // implementation - via percentiles len
        int auto_cols_num = 1;
        ArrayList<Integer> tmp = new ArrayList<>();
        int arr_size = all_view_items.size();

        if (arr_size > 0) {
            int factor ;
            for (ViewItem view_item : all_view_items) {
                if (view_item.getFirst_string() != null) {
                    tmp.add(view_item.getFirst_string().length());
                }
            }
            String[] spat = columnsAlgIntensity.split("[\\s\\:]+");
            int quantile = Integer.parseInt(spat[0]);
            factor = Percentile(tmp, quantile);
            for (int i = 1; i < spat.length; i = i + 2) {
                try {
                    double fval = Double.parseDouble(spat[i]);
                    int cval = Integer.parseInt(spat[i + 1]);
                    if (factor <= fval) {
                        auto_cols_num = cval;
                        break;
                    }
                } catch (Exception e) {
                    // emply
                }
            }
        }
        if (auto_cols_num > arr_size) {
            auto_cols_num = arr_size;
        }
        return auto_cols_num;
    }
    private int Percentile(ArrayList<Integer> values, int Quantile){
        // not fully "mathematical proof", but not too difficult and working
        Collections.sort(values);
        int index = (values.size() * Quantile) / 100;
        return values.get(index);
    }
    // ================================================================
    // общие методы для числа колонок
    private HashMap<String, Integer> loadListFromDB (String listName) {
        // временный массив
        HashMap<String, Integer> tempHashMap = new HashMap<>();
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        // получаем список колонок для чтения
        String[] projection;
        switch (listName) {
            case (ListColumns.TABLE_NAME):
                projection = new String[]{ListColumns.COLUMN_PATH_ID,  ListColumns.COLUMN_NAME_NUMBER};
                break;
            default:
                projection = null;
        }
        // получаем курсор
        Cursor c;
        if (projection != null) {
            c = db.query(
                    listName,  // The table to query
                    projection,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                 // The sort order
            );
        }else {
            closeDb(DbHelper, db, null);
            return tempHashMap;
        }
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            // в таблице базы есть строки с данными. Начинаем их разбирать
            // начинаем заполнять временный массив
            int first = c.getColumnIndex(projection[0]);
            int second = c.getColumnIndex(projection[1]);
            do {
                tempHashMap.put(c.getString(first), c.getInt(second));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }else {
            closeDb(DbHelper, db, c);
            return tempHashMap;
        }
        closeDb(DbHelper, db, c);
        return tempHashMap;
    }
    private void insert (String fullPath, int number, String tableName) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        //подготавливаем данные для вставки в таблицу
        ContentValues tmpVal = new ContentValues();
        String keyName = "";
        String keyValume = "";
        switch (tableName) {
            case (ListColumns.TABLE_NAME):
                keyName = ListColumns.COLUMN_PATH_ID;
                keyValume = ListColumns.COLUMN_NAME_NUMBER;
                break;
        }
        if (keyName.equals("") || keyValume.equals("")) {
            return;
        }
        tmpVal.put(keyName, fullPath);
        tmpVal.put(keyValume, number);

        db.insert(tableName, null, tmpVal);
        closeDb(DbHelper, db, null);
    }
    private void delete (String keyDelete, String tableName) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();

        String keyName = "";
        switch (tableName) {
            case (ListColumns.TABLE_NAME):
                keyName = ListColumns.COLUMN_PATH_ID;
                break;
        }

        db.delete(tableName, keyName + " = ?", new String[]{keyDelete});
        closeDb(DbHelper, db, null);
    }
    // ================================================================
    // ====================================================
    //  общие методы
    private void closeDb(RelaunchDBHelper DbHelper, SQLiteDatabase db, Cursor c) {
        if (c != null) {
            c.close();
        }
        db.close();
        DbHelper.close();
    }
    public void resetDb() {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        db.execSQL("delete from " + ListColumns.TABLE_NAME);

        closeDb(DbHelper, db, null);
    }
}
