package com.harasoft.relaunch.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import com.harasoft.relaunch.DB.ListAppRun;
import com.harasoft.relaunch.DB.RelaunchDBHelper;

import java.util.ArrayList;

/**
 * Created by anat on 08.10.17.
 * Работа с последними запущенными приложениями
 */
public class UtilAppRun {
    private Context context;
    private int appMax;

    public UtilAppRun(Context context) {
        this.context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            appMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
        } catch (NumberFormatException e) {
            appMax = 30;
        }
    }
    // ============== работа с избранными приложениями ================================
    public ArrayList<String> getList(){
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        ArrayList<String> tempList = new ArrayList<>();
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListAppRun.TABLE_NAME, null, null, null, null, null, null);
        // определяем номера столбцов по имени в выборке
        int ureColIndex = c.getColumnIndex(ListAppRun.COLUMN_APP_PACKAGE);
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToLast()) {
            do {
                tempList.add(c.getString(ureColIndex));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToPrevious());
        }

        closeDb(DbHelper, db, c);
        return tempList;
    }
    public void addAppRun(String appPackage){
        // если уже есть, то удаляем и затем вставим новое
        if (isAppRun(appPackage)) {
            delAppFav(appPackage);
        }
        // здесь проверяем на размер списка
        int numAppList = getNumApp();
        if (numAppList >= appMax) {
            delItemsDB ((appMax + 1 ) - numAppList );
        }
        // добавляем запись
        addApp(appPackage);
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
        Cursor c = db.query(ListAppRun.TABLE_NAME, null, null, null, null, null, null);
        // определяем номера столбцов по имени в выборке
        int ureColIndex = c.getColumnIndex(ListAppRun.COLUMN_APP_PACKAGE);
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            do {
                db.delete(ListAppRun.TABLE_NAME, ListAppRun.COLUMN_APP_PACKAGE + " = ?", new String[]{c.getString(ureColIndex)});
                count++;
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext() && count < countItems);
        }

        closeDb(DbHelper, db, c);
    }
    private boolean isAppRun(String appPackage){

        ArrayList<String> listAllApp = getList();

        for (String app: listAllApp) {
            if (app.equals(appPackage)) {
                return true;
            }
        }

        return false;
    }
    private int getNumApp() {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return 0;
        }
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListAppRun.TABLE_NAME, null, null, null, null, null, null);
        int count = c.getCount();

        closeDb(DbHelper, db, c);
        return count;
    }
    public void delAppFav(String appPackage) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();

        db.delete(ListAppRun.TABLE_NAME, ListAppRun.COLUMN_APP_PACKAGE + " = ?", new String[]{appPackage});
        closeDb(DbHelper, db, null);
    }
    public void setListAppRun(ArrayList<UtilApp.Info> listAppFav) {
        resetDb();
        int count = appMax;
        for (UtilApp.Info appFav: listAppFav) {
            addApp(appFav.appPackage);
            count--;
            if (count == 0) {
                break;
            }
        }
    }
    private void addApp(String appPackage){
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
        cv.put(ListAppRun.COLUMN_APP_PACKAGE, appPackage);

        // вставляем запись и получаем ее ID
        db.insert(ListAppRun.TABLE_NAME, null, cv);

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
        db.execSQL("delete from " + ListAppRun.TABLE_NAME);

        closeDb(DbHelper, db, null);
    }
}
