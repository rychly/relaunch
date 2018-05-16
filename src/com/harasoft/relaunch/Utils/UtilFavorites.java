package com.harasoft.relaunch.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import com.harasoft.relaunch.DB.ListFavorites;
import com.harasoft.relaunch.DB.RelaunchDBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anat on 11.10.17.
 * Работа с фаворитами
 */
public class UtilFavorites {
    private Context context;
    private int favMax;

    public UtilFavorites(Context context) {
        this.context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            favMax = Integer.parseInt(prefs.getString("favSize", "30"));
        } catch (NumberFormatException e) {
            favMax = 30;
        }
    }
    public List<HashMap<String, String>> getArrayFavorites() {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        List<HashMap<String, String>> outArray = new ArrayList<>();
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListFavorites.TABLE_NAME, null, null, null, null, null, null);
        // смотрим, есть ли данные
        if (c.moveToLast()) { // на последнюю запись
            // определяем номера столбцов по имени в выборке
            int pathColIndex = c.getColumnIndex(ListFavorites.COLUMN_FULL_PATH);
            int nameColIndex = c.getColumnIndex(ListFavorites.COLUMN_NAME);
            int typeColIndex = c.getColumnIndex(ListFavorites.COLUMN_TYPE_RESOURCE);
            int resourceColIndex = c.getColumnIndex(ListFavorites.COLUMN_RESOURCE_LOCATION);

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

    public void addFav(String path, String fileName, int type, int resource){
        // если уже есть, то удаляем и затем вставим новое
        if (isFav(path, fileName, type, resource)) {
            delFav(path, fileName, type, resource);
        }
        // здесь проверяем на размер списка
        int numAppList = getNumFav();
        if (numAppList >= favMax) {
            delItemsDB ((favMax + 1 ) - numAppList );
        }
        // добавляем запись
        addFavDB(path, fileName, type, resource);
    }
    private void addFav(HashMap<String, String> itemFav){
        String path = itemFav.get("secondLine");
        String fileName = itemFav.get("firstLine");
        int type = Integer.parseInt(itemFav.get("type"));
        int resource = Integer.parseInt(itemFav.get("resource"));
        // если уже есть, то удаляем и затем вставим новое
        if (isFav(path, fileName, type, resource)) {
            delFav(path, fileName, type, resource);
        }
        // здесь проверяем на размер списка
        int numAppList = getNumFav();
        if (numAppList >= favMax) {
            delItemsDB ((favMax + 1 ) - numAppList );
        }
        // добавляем запись
        addFavDB(path, fileName, type, resource);
    }
    private void addFavDB(String path, String fileName, int type, int resource){
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
        cv.put(ListFavorites.COLUMN_FULL_PATH, path);
        cv.put(ListFavorites.COLUMN_NAME, fileName);
        cv.put(ListFavorites.COLUMN_TYPE_RESOURCE, type);
        cv.put(ListFavorites.COLUMN_RESOURCE_LOCATION, resource);

        // вставляем запись и получаем ее ID
        db.insert(ListFavorites.TABLE_NAME, null, cv);

        closeDb(DbHelper, db, null);
    }

    private int getNumFav() {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return 0;
        }
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListFavorites.TABLE_NAME, null, null, null, null, null, null);
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
        Cursor c = db.query(ListFavorites.TABLE_NAME, null, null, null, null, null, null);

        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            String selection = ListFavorites._ID + " = ?";
            String[] selectionArgs;
            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex(ListFavorites._ID);
            do {
                selectionArgs = new String[] { String.valueOf(c.getString(idColIndex))};
                db.delete(ListFavorites.TABLE_NAME, selection, selectionArgs);
                count++;
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext() && count < countItems);
        }

        closeDb(DbHelper, db, c);
    }


    public boolean isFav(String path, String fileName){

        if (isFav(path, fileName, 0, 0)){
            return true;
        }

        if (isFav(path, fileName, 1, 0)){
            return true;
        }
        return false;
    }
    private boolean isFav(String path, String fileName, int type, int resource){

        List<HashMap<String, String>> listAllFav = getArrayFavorites();

        for (HashMap<String, String> app: listAllFav) {
            if (app.get("secondLine").equals(path) && app.get("firstLine").equals(fileName) && Integer.parseInt(app.get("type")) == type && Integer.parseInt(app.get("resource")) == resource) {
                return true;
            }
        }

        return false;
    }
    public void delFav(String path, String fileName, int type, int resource) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        String selection = ListFavorites.COLUMN_FULL_PATH + " = ? AND " + ListFavorites.COLUMN_NAME + " = ? AND " + ListFavorites.COLUMN_TYPE_RESOURCE + " = ? AND " + ListFavorites.COLUMN_RESOURCE_LOCATION + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(path), String.valueOf(fileName), String.valueOf(type), String.valueOf(resource)  };
        db.delete(ListFavorites.TABLE_NAME, selection, selectionArgs);
        closeDb(DbHelper, db, null);
    }
    public void setListAppFav(List<HashMap<String, String>> listFav) {
        resetDb();
        int count = favMax;
        for (HashMap<String, String> itemFav: listFav) {
            addFav(itemFav);
            count--;
            if (count == 0) {
                break;
            }
        }
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
        db.execSQL("delete from " + ListFavorites.TABLE_NAME);

        closeDb(DbHelper, db, null);
    }
}
