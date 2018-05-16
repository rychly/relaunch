package com.harasoft.relaunch.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.harasoft.relaunch.DB.ListHistory;
import com.harasoft.relaunch.DB.RelaunchDBHelper;

import java.util.*;
import java.util.HashMap;

public class UtilHistory {
    private List<HashMap<String, String>> history;
    private Context context;

    public UtilHistory(Context context) {
        this.context = context;
        history = getArrayHistory();
    }

    //=================================================
    private List<HashMap<String, String>> getArrayHistory() {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        List<HashMap<String, String>> outArray = new ArrayList<>();
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListHistory.TABLE_NAME, null, null, null, null, null, null);
        // смотрим, есть ли данные
        if (c.moveToLast()) { // на последнюю запись
            // определяем номера столбцов по имени в выборке
            int resourceIndex = c.getColumnIndex(ListHistory.COLUMN_RESOURCE_LOCATION);
            int pathColIndex = c.getColumnIndex(ListHistory.COLUMN_DIR);
            int nameColIndex = c.getColumnIndex(ListHistory.COLUMN_NAME);
            int stateColIndex = c.getColumnIndex(ListHistory.COLUMN_STATE);

            do {
                HashMap<String, String> item = new HashMap<>();
                item.put("resource", c.getString(resourceIndex));
                item.put("path", c.getString(pathColIndex));
                item.put("name", c.getString(nameColIndex));
                item.put("state", c.getString(stateColIndex));

                outArray.add(item);
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToPrevious());
        }

        closeDb(DbHelper, db, c);
        return outArray;
    }
    public int getState(int resource, String full_path) {

        int ind = full_path.indexOf("/");
        if (ind < 0) {
            return 0; // app.NONE
        }
        ind = full_path.lastIndexOf("/");
        String file_path = full_path.substring(0, ind);
        String file_name = full_path.substring(ind + 1);

        if (file_path.length() == 0) {
            file_path = "/";
        }
        for (HashMap<String, String> item: history) {
            if (Integer.parseInt(item.get("resource")) == resource && item.get("path").equals(file_path) && item.get("name").equals(file_name)) {
                return  Integer.parseInt(item.get("state")); // app.READING || app.FINISHING
            }
        }
        return 0;// app.NONE
    }
    // добавление в базу
    public void addToHistory (int resource, String full_path, int state ) {
        int ind = full_path.indexOf("/");
        if (ind < 0) {
            return;
        }
        ind = full_path.lastIndexOf("/");
        String file_path = full_path.substring(0, ind);
        String file_name = full_path.substring(ind + 1);
        if (file_path.length() == 0) {
            file_path = "/";
        }

        HashMap<String, String> item = new HashMap<>();
        item.put("resource", String.valueOf(resource));
        item.put("path", file_path);
        item.put("name", file_name);
        item.put("state", Integer.toString(state));


        history.add(item);
        addToDB(resource, file_path, file_name, state);
    }
    private void addToDB(int resource, String file_path, String file_name, int state) {
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
        cv.put(ListHistory.COLUMN_RESOURCE_LOCATION, resource);
        cv.put(ListHistory.COLUMN_DIR, file_path);
        cv.put(ListHistory.COLUMN_NAME, file_name);
        cv.put(ListHistory.COLUMN_STATE, state);

        // вставляем запись и получаем ее ID
        db.insert(ListHistory.TABLE_NAME, null, cv);

        closeDb(DbHelper, db, null);
    }
    // удаление из базы
    public void delFromHistory(int resource, String full_path) {
        int ind = full_path.indexOf("/");
        if (ind < 0) {
            return;
        }
        ind = full_path.lastIndexOf("/");
        String file_path = full_path.substring(0, ind);
        String file_name = full_path.substring(ind + 1);
        if (file_path.length() == 0) {
            file_path = "/";
        }
        delFromDB(resource, file_path, file_name);
        delFromArray(resource, file_path, file_name);
    }
    private void delFromDB(int resource, String file_path, String file_name) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        String selection = ListHistory.COLUMN_RESOURCE_LOCATION + " = ? AND " + ListHistory.COLUMN_DIR + " = ? AND " + ListHistory.COLUMN_NAME + " = ?";
        String[] selectionArgs = new String[] {String.valueOf(resource), String.valueOf(file_path), String.valueOf(file_name)  };
        db.delete(ListHistory.TABLE_NAME, selection, selectionArgs);
        closeDb(DbHelper, db, null);
    }
    private void delFromArray(int resource, String file_path, String file_name) {
        for (HashMap<String, String> item: history) {
            if (Integer.parseInt(item.get("resource")) == resource && item.get("path").equals(file_path) && item.get("name").equals(file_name)) {
                history.remove(item);
                break;
            }
        }
    }
    // обновление списка в истории
    public void reloadHistoryArray() {
        history = getArrayHistory();
    }
    // -----------------------------------------
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
        db.execSQL("delete from " + ListHistory.TABLE_NAME);

        closeDb(DbHelper, db, null);
    }
}
