package com.harasoft.relaunch.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import com.harasoft.relaunch.DB.ListLastOpen;
import com.harasoft.relaunch.DB.RelaunchDBHelper;
import com.harasoft.relaunch.Support.TypeResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anat on 28.10.17.
 * Последние открытые
 */
public class UtilLastOpen {
    private Context context;
    private int lruMax;

    public UtilLastOpen(Context context) {
        this.context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            lruMax = Integer.parseInt(prefs.getString("lruSize", "30"));
        } catch (NumberFormatException e) {
            lruMax = 30;
        }
    }
    public List<HashMap<String, String>> getArrayLastOpen() {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        List<HashMap<String, String>> outArray = new ArrayList<>();
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListLastOpen.TABLE_NAME, null, null, null, null, null, null);
        // смотрим, есть ли данные
        if (c.moveToLast()) { // на последнюю запись
            // определяем номера столбцов по имени в выборке
            int pathColIndex = c.getColumnIndex(ListLastOpen.COLUMN_FULL_PATH);
            int nameColIndex = c.getColumnIndex(ListLastOpen.COLUMN_NAME);
            int typeColIndex = c.getColumnIndex(ListLastOpen.COLUMN_TYPE_RESOURCE);
            int resourceColIndex = c.getColumnIndex(ListLastOpen.COLUMN_RESOURCE_LOCATION);

            do {
                HashMap<String, String> item = new HashMap<>();
                item.put("firstLine", c.getString(nameColIndex));
                item.put("secondLine", c.getString(pathColIndex));
                item.put("type", c.getString(typeColIndex));
                item.put("resource", c.getString(resourceColIndex));

                outArray.add(item);
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToPrevious());
        }

        closeDb(DbHelper, db, c);
        return outArray;
    }
    public void addLastOpen(String fullFileName, int resource){
        int type = TypeResource.FILE;
        String path = fullFileName.substring(0, fullFileName.lastIndexOf("/"));
        String fileName = fullFileName.substring(fullFileName.lastIndexOf("/")+1);
        // если уже есть, то удаляем и затем вставим новое
        if (isLastOpen(path, fileName, type, resource)) {
            delLastOpen(path, fileName, type, resource);
        }
        // здесь проверяем на размер списка
        int numAppList = getNumLastOpen();
        if (numAppList >= lruMax) {
            delItemsDB ((lruMax + 1 ) - numAppList );
        }
        // добавляем запись
        addLastOpenDB(path, fileName, type, resource);
    }
    private void addLastOpenDB(String path, String fileName, int type, int resource){
        // добавляем запись
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // создаем объект для данных
        ContentValues cv = new ContentValues();
        if(db == null){
            return;
        }
        cv.put(ListLastOpen.COLUMN_FULL_PATH, path);
        cv.put(ListLastOpen.COLUMN_NAME, fileName);
        cv.put(ListLastOpen.COLUMN_TYPE_RESOURCE, type);
        cv.put(ListLastOpen.COLUMN_RESOURCE_LOCATION, resource);

        // вставляем запись и получаем ее ID
        db.insert(ListLastOpen.TABLE_NAME, null, cv);

        closeDb(DbHelper, db, null);
    }

    private int getNumLastOpen() {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return 0;
        }
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListLastOpen.TABLE_NAME, null, null, null, null, null, null);
        int count = c.getCount();

        closeDb(DbHelper, db, c);
        return count;
    }
    private void delItemsDB (int countItems) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        if(db == null){
            return;
        }
        int count = 0;

        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListLastOpen.TABLE_NAME, null, null, null, null, null, null);

        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            String selection = ListLastOpen._ID + " = ?";
            String[] selectionArgs;
            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex(ListLastOpen._ID);
            do {
                selectionArgs = new String[] { String.valueOf(c.getString(idColIndex))};
                db.delete(ListLastOpen.TABLE_NAME, selection, selectionArgs);
                count++;
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext() && count < countItems);
        }

        closeDb(DbHelper, db, c);
    }
    private boolean isLastOpen(String path, String fileName, int type, int resource){

        List<HashMap<String, String>> listAllLastRun = getArrayLastOpen();
        for (HashMap<String, String> app: listAllLastRun) {
            if (app.get("secondLine").equals(path) && app.get("firstLine").equals(fileName) && Integer.parseInt(app.get("type")) == type && Integer.parseInt(app.get("resource")) == resource) {
                return true;
            }
        }

        return false;
    }
    public void delLastOpen(String path, String fileName, int type, int resource) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        String selection = ListLastOpen.COLUMN_FULL_PATH + " = ? AND " + ListLastOpen.COLUMN_NAME + " = ? AND " + ListLastOpen.COLUMN_TYPE_RESOURCE + " = ? AND " + ListLastOpen.COLUMN_RESOURCE_LOCATION + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(path), String.valueOf(fileName), String.valueOf(type), String.valueOf(resource)  };
        db.delete(ListLastOpen.TABLE_NAME, selection, selectionArgs);
        closeDb(DbHelper, db, null);
    }
    // ================================================================
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
        db.execSQL("delete from " + ListLastOpen.TABLE_NAME);

        closeDb(DbHelper, db, null);
    }
}
