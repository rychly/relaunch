package com.harasoft.relaunch.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.harasoft.relaunch.DB.ListOPDS;
import com.harasoft.relaunch.DB.RelaunchDBHelper;
import com.harasoft.relaunch.Support.TypeResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anat on 08.10.17.
 * Работа со списком OPDS каталогов
 */
public class UtilOPDS {
    private Context context;

    public UtilOPDS(Context context) {
        this.context = context;
    }
    // ============== работа с OPDS ================================
    public List<HashMap<String, String>> getListOPDS(){
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        List<HashMap<String, String>> itemsArray = new ArrayList<>();
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListOPDS.TABLE_NAME, null, null, null, null, null, null);
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        // определяем номера столбцов по имени в выборке
        int id = c.getColumnIndex(ListOPDS._ID);
        int titleColIndex = c.getColumnIndex(ListOPDS.COLUMN_TITLE);
        int ureColIndex = c.getColumnIndex(ListOPDS.COLUMN_LINK);
        if (c.moveToFirst()) { // проверяем есть ли записи в базе

            do {
                HashMap<String, String> item = new HashMap<>();
                item.put("id", c.getString(id));
                item.put("secondLine", c.getString(ureColIndex));
                item.put("firstLine", c.getString(titleColIndex));
                item.put("type", String.valueOf(TypeResource.DIR));
                item.put("resource", "3");

                itemsArray.add(item);
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }else { // если записей нет, то делаем одну и ее же отдаем на экран
            InfoConnectOPDS infoConnectOPDS = new InfoConnectOPDS();
            infoConnectOPDS.setTitle("Либрусек");
            infoConnectOPDS.setLink(String.valueOf("https://lib.rus.ec/opds"));
            infoConnectOPDS.setEnable_pass("false");
            infoConnectOPDS.setLogin("");
            infoConnectOPDS.setPassword("");
            infoConnectOPDS.setEnable_proxy("false");
            infoConnectOPDS.setProxy_type("");
            infoConnectOPDS.setProxy_name("");
            infoConnectOPDS.setProxy_port("");
            addDbOPDS(infoConnectOPDS);

            HashMap<String, String> item = new HashMap<>();
            item.put("id", "0");
            item.put("secondLine", "http://lib.rus.ec/opds");
            item.put("firstLine", "Либрусек");
            item.put("type", String.valueOf(TypeResource.DIR));
            item.put("resource", "3");
            itemsArray.add(item);
        }
        closeDb(DbHelper, db, c);
        return itemsArray;
    }
    public void addDbOPDS(InfoConnectOPDS infoConnectOPDS){
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        if(db == null){
            return;
        }
        cv.put(ListOPDS.COLUMN_TITLE, infoConnectOPDS.getTitle());
        cv.put(ListOPDS.COLUMN_LINK, infoConnectOPDS.getLink());
        cv.put(ListOPDS.COLUMN_ENABLE_PASS, infoConnectOPDS.isEnable_pass());
        cv.put(ListOPDS.COLUMN_LOGIN, infoConnectOPDS.getLogin());
        cv.put(ListOPDS.COLUMN_PASSWORD, infoConnectOPDS.getPassword());
        cv.put(ListOPDS.COLUMN_ENABLE_PROXY, infoConnectOPDS.isEnable_proxy());
        cv.put(ListOPDS.COLUMN_PROXY_TYPE, infoConnectOPDS.getProxy_type());
        cv.put(ListOPDS.COLUMN_PROXY_NAME, infoConnectOPDS.getProxy_name());
        cv.put(ListOPDS.COLUMN_PROXY_PORT, infoConnectOPDS.getProxy_port());

        // вставляем запись и получаем ее ID
        db.insert(ListOPDS.TABLE_NAME, null, cv);

        closeDb(DbHelper, db, null);
    }
    public void updateDbOPDS(String old_title, InfoConnectOPDS infoConnectOPDS){
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // создаем объект для данных
        ContentValues cv = new ContentValues();
        if(db == null){
            return;
        }
        cv.put(ListOPDS.COLUMN_TITLE, infoConnectOPDS.getTitle());
        cv.put(ListOPDS.COLUMN_LINK, infoConnectOPDS.getLink());
        cv.put(ListOPDS.COLUMN_ENABLE_PASS, infoConnectOPDS.isEnable_pass());
        cv.put(ListOPDS.COLUMN_LOGIN, infoConnectOPDS.getLogin());
        cv.put(ListOPDS.COLUMN_PASSWORD, infoConnectOPDS.getPassword());
        cv.put(ListOPDS.COLUMN_ENABLE_PROXY, infoConnectOPDS.isEnable_proxy());
        cv.put(ListOPDS.COLUMN_PROXY_TYPE, infoConnectOPDS.getProxy_type());
        cv.put(ListOPDS.COLUMN_PROXY_NAME, infoConnectOPDS.getProxy_name());
        cv.put(ListOPDS.COLUMN_PROXY_PORT, infoConnectOPDS.getProxy_port());

        // обновляем запись
        db.update(ListOPDS.TABLE_NAME, cv,  ListOPDS.COLUMN_TITLE + " = ?", new String[]{old_title});

        closeDb(DbHelper, db, null);
    }
    public void delDbOPDS(String titleColIndex){
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        if(db == null){
            return;
        }
        db.delete(ListOPDS.TABLE_NAME, ListOPDS._ID + " = ?" ,new String[]{titleColIndex});
        closeDb(DbHelper, db, null);
    }
    public HashMap<String, String> getdbOPDS(int id){
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        HashMap<String, String> item = new HashMap<>();
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query(ListOPDS.TABLE_NAME, null, null, null, null, null, null);
        if (c.moveToPosition(id)) {
            // определяем номера столбцов по имени в выборке
            int titleColIndex = c.getColumnIndex(ListOPDS.COLUMN_TITLE);
            int ureColIndex = c.getColumnIndex(ListOPDS.COLUMN_LINK);
            int enpassColIndex = c.getColumnIndex(ListOPDS.COLUMN_ENABLE_PASS);
            int loginColIndex = c.getColumnIndex(ListOPDS.COLUMN_LOGIN);
            int passColIndex = c.getColumnIndex(ListOPDS.COLUMN_PASSWORD);
            int enproxyColIndex = c.getColumnIndex(ListOPDS.COLUMN_ENABLE_PROXY);
            int typeproxyColIndex = c.getColumnIndex(ListOPDS.COLUMN_PROXY_TYPE);
            int proxynameColIndex = c.getColumnIndex(ListOPDS.COLUMN_PROXY_NAME);
            int proxyportColIndex = c.getColumnIndex(ListOPDS.COLUMN_PROXY_PORT);

            item.put("PATH", c.getString(ureColIndex));
            item.put("SERVER", c.getString(titleColIndex));
            item.put("EN_PASS", c.getString(enpassColIndex));
            item.put("LOGIN", c.getString(loginColIndex));
            item.put("PASSWORD", c.getString(passColIndex));
            item.put("EN_PROXY", c.getString(enproxyColIndex));
            item.put("TYPE_PROXY", c.getString(typeproxyColIndex));
            item.put("PROXY_NAME", c.getString(proxynameColIndex));
            item.put("PROXY_PORT", String.valueOf(c.getInt(proxyportColIndex)));
        }
        closeDb(DbHelper, db, c);
        return item;
    }
    public String getUrlOPDS(int id) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        // ищем запись в базе
        Cursor cursor;
        String selection = ListOPDS._ID + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(id) };
        // делаем запрос данных из таблицы , получаем Cursor
        cursor = db.query(ListOPDS.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        // определяем индексы нужных столбцов
        int path = cursor.getColumnIndex(ListOPDS.COLUMN_LINK);
        // получаем ссылку
        String urlStr = "";
        if (cursor.moveToFirst()) {
                urlStr = cursor.getString(path);
        }
        closeDb(DbHelper, db, cursor);
        return urlStr;
    }
    public InfoConnectOPDS getInfoOPDS(int id) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        // ищем запись в базе
        Cursor cursor;
        String selection = ListOPDS._ID + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(id) };
        // делаем запрос данных из таблицы , получаем Cursor
        cursor = db.query(ListOPDS.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        // определяем индексы нужных столбцов
        int title_opds = cursor.getColumnIndex(ListOPDS.COLUMN_TITLE);
        int path_opds = cursor.getColumnIndex(ListOPDS.COLUMN_LINK);
        int enable_pass = cursor.getColumnIndex(ListOPDS.COLUMN_ENABLE_PASS);
        int login = cursor.getColumnIndex(ListOPDS.COLUMN_LOGIN);
        int password = cursor.getColumnIndex(ListOPDS.COLUMN_PASSWORD);
        int enable_proxy = cursor.getColumnIndex(ListOPDS.COLUMN_ENABLE_PROXY);
        int type_proxy = cursor.getColumnIndex(ListOPDS.COLUMN_PROXY_TYPE);
        int proxy_name = cursor.getColumnIndex(ListOPDS.COLUMN_PROXY_NAME);
        int proxy_port = cursor.getColumnIndex(ListOPDS.COLUMN_PROXY_PORT);
        // получаем ссылку
        InfoConnectOPDS infoConnectOPDS = new InfoConnectOPDS();
        if (cursor.moveToFirst()) {
            infoConnectOPDS.setTitle(cursor.getString(title_opds));
            infoConnectOPDS.setLink(cursor.getString(path_opds));
            infoConnectOPDS.setEnable_pass(cursor.getString(enable_pass));
            infoConnectOPDS.setLogin(cursor.getString(login));
            infoConnectOPDS.setPassword(cursor.getString(password));
            infoConnectOPDS.setEnable_proxy(cursor.getString(enable_proxy));
            infoConnectOPDS.setProxy_type(cursor.getString(type_proxy));
            infoConnectOPDS.setProxy_name(cursor.getString(proxy_name));
            infoConnectOPDS.setProxy_port(cursor.getString(proxy_port));

        }
        closeDb(DbHelper, db, cursor);
        return infoConnectOPDS;
    }
    public void setListOPDS(List<HashMap<String, String>> listOPDS) {
        // считать всю текущую базу
        List<InfoConnectOPDS> old_list = new ArrayList<>();
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return;
        }

        Cursor cursor = db.query(ListOPDS.TABLE_NAME, null, null, null, null, null, null);
        // определяем индексы нужных столбцов
        int id_opds = cursor.getColumnIndex(ListOPDS._ID);
        int title_opds = cursor.getColumnIndex(ListOPDS.COLUMN_TITLE);
        int path_opds = cursor.getColumnIndex(ListOPDS.COLUMN_LINK);
        int enable_pass = cursor.getColumnIndex(ListOPDS.COLUMN_ENABLE_PASS);
        int login = cursor.getColumnIndex(ListOPDS.COLUMN_LOGIN);
        int password = cursor.getColumnIndex(ListOPDS.COLUMN_PASSWORD);
        int enable_proxy = cursor.getColumnIndex(ListOPDS.COLUMN_ENABLE_PROXY);
        int type_proxy = cursor.getColumnIndex(ListOPDS.COLUMN_PROXY_TYPE);
        int proxy_name = cursor.getColumnIndex(ListOPDS.COLUMN_PROXY_NAME);
        int proxy_port = cursor.getColumnIndex(ListOPDS.COLUMN_PROXY_PORT);

        InfoConnectOPDS infoConnectOPDS;
        if (cursor.moveToFirst()) {
            do {
                infoConnectOPDS = new InfoConnectOPDS();
                infoConnectOPDS.setId(cursor.getString(id_opds));
                infoConnectOPDS.setTitle(cursor.getString(title_opds));
                infoConnectOPDS.setLink(cursor.getString(path_opds));
                infoConnectOPDS.setEnable_pass(cursor.getString(enable_pass));
                infoConnectOPDS.setLogin(cursor.getString(login));
                infoConnectOPDS.setPassword(cursor.getString(password));
                infoConnectOPDS.setEnable_proxy(cursor.getString(enable_proxy));
                infoConnectOPDS.setProxy_type(cursor.getString(type_proxy));
                infoConnectOPDS.setProxy_name(cursor.getString(proxy_name));
                infoConnectOPDS.setProxy_port(cursor.getString(proxy_port));

                old_list.add(infoConnectOPDS);
            } while (cursor.moveToNext());
        }
        closeDb(DbHelper, db, cursor);
        // стереть базу
        resetDb();
        // занести в новом порядке
        int old_size = old_list.size();
        for (HashMap<String, String> item: listOPDS) {
            for (int x = old_size; x > 0; x--){
                if (item.get("id").equals(old_list.get(x).getId())){
                    addDbOPDS(old_list.get(x));
                    break;
                }
            }
        }
    }
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
        db.execSQL("delete from " + ListOPDS.TABLE_NAME);

        closeDb(DbHelper, db, null);
    }
}
