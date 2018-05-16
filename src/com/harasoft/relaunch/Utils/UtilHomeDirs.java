package com.harasoft.relaunch.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import com.harasoft.relaunch.DB.ListHomeDir;
import com.harasoft.relaunch.DB.RelaunchDBHelper;
import com.harasoft.relaunch.Support.TypeResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UtilHomeDirs {
    private Context context;
    private String nameCurrentHomeDir = "currentHomeDir";
    private String resource_location = "resourceLocation";

    public UtilHomeDirs(Context context) {
        this.context = context;
    }
    public void setCurrentHomeDir(String dir, int resource) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(nameCurrentHomeDir, dir);
        editor.putString(resource_location, String.valueOf(resource));
        editor.commit();
    }
    public String getCurrentHomeDir() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(nameCurrentHomeDir, "/");
    }
    public int getCurrentResourceLocation() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString(resource_location, "0"));
    }

    public void addHomeDir(String fullFileName, int resource){
        String path;
        String fileName;
        if (fullFileName.equals("/")){
            path = "/";
            fileName = "/";
        }else {
            path = fullFileName.substring(0, fullFileName.lastIndexOf("/"));
            fileName = fullFileName.substring(fullFileName.lastIndexOf("/") + 1);
        }
        // если уже есть, то удаляем и затем вставим новое
        if (isHomeDir(path, fileName, resource)) {
            delHomeDir(path, fileName, resource);
        }
        // добавляем запись
        addHomeDirDB(path, fileName, resource);
    }
    private void addHomeDirDB(String path, String fileName, int resource){
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
        cv.put(ListHomeDir.COLUMN_RESOURCE_LOCATION, resource);
        cv.put(ListHomeDir.COLUMN_PATH_HOME_DIR, path);
        cv.put(ListHomeDir.COLUMN_NAME_HOME_DIR, fileName);


        // вставляем запись и получаем ее ID
        db.insert(ListHomeDir.TABLE_NAME, null, cv);

        closeDb(DbHelper, db, null);
    }
    public void setListHomeDir(List<HashMap<String, String>> itemsArray) {
        String file_path;
        String file_name;
        int file_resource;
        for (HashMap<String, String> anItemsArray : itemsArray) {
            file_name = anItemsArray.get("firstLine").trim();
            file_path = anItemsArray.get("secondLine").trim();
            file_resource = Integer.parseInt(anItemsArray.get("resource").trim());

            addHomeDirDB(file_path, file_name, file_resource);
        }
    }

    public void delHomeDir(String path, String fileName, int resource) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        if(db == null){
            return;
        }
        String selection = ListHomeDir.COLUMN_RESOURCE_LOCATION + " = ? AND " + ListHomeDir.COLUMN_PATH_HOME_DIR + " = ? AND " + ListHomeDir.COLUMN_NAME_HOME_DIR + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(resource), String.valueOf(path), String.valueOf(fileName)};
        db.delete(ListHomeDir.TABLE_NAME, selection, selectionArgs);
        closeDb(DbHelper, db, null);
    }
    public boolean isHomeDir(int resource, String file_full_name){
        String path = file_full_name.substring(0, file_full_name.lastIndexOf("/"));
        String fileName = file_full_name.substring(file_full_name.lastIndexOf("/")+1);
        return isHomeDir(path, fileName, resource);
    }
    private boolean isHomeDir(String path, String fileName, int resource){

        List<HashMap<String, String>> listHomeDirs = getArrayHomeDir();
        for (HashMap<String, String> home_dir: listHomeDirs) {
            if (home_dir.get("secondLine").equals(path) && home_dir.get("firstLine").equals(fileName) && Integer.parseInt(home_dir.get("resource")) == resource) {
                return true;
            }
        }

        return false;
    }
    public List<HashMap<String, String>> getArrayHomeDir() {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        List<HashMap<String, String>> outArray = new ArrayList<>();
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListHomeDir.TABLE_NAME, null, null, null, null, null, null);
        // смотрим, есть ли данные
        if (c.moveToFirst()) { // на первую запись
            // определяем номера столбцов по имени в выборке
            int id = c.getColumnIndex(ListHomeDir._ID);
            int resourceColIndex = c.getColumnIndex(ListHomeDir.COLUMN_RESOURCE_LOCATION);
            int pathColIndex = c.getColumnIndex(ListHomeDir.COLUMN_PATH_HOME_DIR);
            int nameColIndex = c.getColumnIndex(ListHomeDir.COLUMN_NAME_HOME_DIR);

            do {
                HashMap<String, String> item = new HashMap<>();
                item.put("id", String.valueOf(c.getInt(id)));
                item.put("firstLine", c.getString(nameColIndex));
                item.put("secondLine", c.getString(pathColIndex));
                item.put("type", String.valueOf(TypeResource.DIR));
                item.put("resource", c.getString(resourceColIndex));

                outArray.add(item);
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }

        closeDb(DbHelper, db, c);
        return outArray;
    }
    public void setHomeDirByID(int id){
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return;
        }
        String selection = ListHomeDir._ID + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(id)};
        String home_dir;
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListHomeDir.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        // смотрим, есть ли данные
        if (c.moveToFirst()) { // на первую запись
            int resourceColIndex = c.getColumnIndex(ListHomeDir.COLUMN_RESOURCE_LOCATION);
            int pathColIndex = c.getColumnIndex(ListHomeDir.COLUMN_PATH_HOME_DIR);
            int nameColIndex = c.getColumnIndex(ListHomeDir.COLUMN_NAME_HOME_DIR);

            String file_path = c.getString(pathColIndex);
            String file_name = c.getString(nameColIndex);
            int resource_location = Integer.parseInt(c.getString(resourceColIndex));

            if (file_path.equals("/")) {
                if (file_name.equals(file_path)) { // когда корень "/"
                    home_dir = "/";
                }else {
                    home_dir = file_path + file_name;
                }
            }else {
                home_dir = file_path + "/" + file_name;
            }
            setCurrentHomeDir(home_dir, resource_location);

        }
        closeDb(DbHelper, db, c);
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
        db.execSQL("delete from " + ListHomeDir.TABLE_NAME);

        closeDb(DbHelper, db, null);
    }
}
