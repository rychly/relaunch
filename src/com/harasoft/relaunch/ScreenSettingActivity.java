package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ScreenSettingActivity extends Activity {
    final String TAG = "ScreenSettingActivity";
    List<HashMap<String, String>> itemsArray;
    TPAdapter adapter;
    ReLaunchApp app;
    SharedPreferences prefs;
    int firstLineIconSizePx = 48;
    final static public int TYPES_ACT = 1;

    class TPAdapter extends BaseAdapter {
        final Context cntx;

        TPAdapter(Context context) {
            cntx = context;
        }

        public int getCount() {
            return itemsArray.size();
        }

        public Object getItem(int position) {
            return itemsArray.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) app.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.panel_view, parent, false);
                if (v == null) {
                    return null;
                }
            }
            final HashMap<String, String> item = itemsArray.get(position);
            if (item != null) {
                // иконка
                String panelName = item.get("panel");
                // кнопка номер/всего
                TextView button_title = (TextView) v.findViewById(R.id.panel_title);
                //
                button_title.setText(getResources().getString(
                        R.string.pref_i_manualPanel1_title) + " (" + (position + 1) + "/" + (itemsArray.size()) + ")");
                // название панели
                TextView button_name_title = (TextView) v.findViewById(R.id.panel_name_title);
                button_name_title.setText(panelName);

                // Setting up button
                ImageButton upBtn = (ImageButton) v.findViewById(R.id.types_up);
                if (position == 0) {
                    upBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.checkbox_off_background));
                    upBtn.setEnabled(false);
                } else {
                    upBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_arrowup));
                    upBtn.setEnabled(true);
                }
                upBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        HashMap<String, String> i = itemsArray.get(position);
                        itemsArray.remove(position);
                        itemsArray.add(position - 1, i);
                        adapter.notifyDataSetChanged();
                    }
                });

                // Setting down button
                ImageButton downBtn = (ImageButton) v.findViewById(R.id.types_down);
                if (position == (itemsArray.size() - 1)) {
                    downBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.checkbox_off_background));
                    downBtn.setEnabled(false);
                } else {
                    downBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_arrowdown));
                    downBtn.setEnabled(true);
                }
                downBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        HashMap<String, String> i = itemsArray.get(position);
                        itemsArray.remove(position);
                        itemsArray.add(position + 1, i);
                        adapter.notifyDataSetChanged();
                    }
                });

                // Setting remove button
                ImageButton rmBtn = (ImageButton) v.findViewById(R.id.types_delete);
                rmBtn.setEnabled(itemsArray.size() > 1);
                rmBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (Integer.valueOf(item.get("id"))>5) {
                            DelPanelDB(Integer.valueOf(item.get("id")));
                        }
                        itemsArray.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                });
                // Setting edit  button
                ImageButton edBtn = (ImageButton) v.findViewById(R.id.types_edit);
                if (Integer.valueOf(item.get("id")) < 6){
                    edBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_edit_mini_gray));
                    edBtn.setEnabled(false);
                }else{
                    edBtn.setEnabled(true);
                    edBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_edit_mini));
                    edBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent(ScreenSettingActivity.this, PanelSettingsActivity.class);
                            intent.putExtra("panelID", item.get("id"));
                            startActivityForResult(intent, TYPES_ACT);
                        }
                    });
                }
            }
            return v;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        // Global storage
        app = ((ReLaunchApp) getApplicationContext());
        if(app == null ) {
            finish();
        }
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.types_view);
        // в заголовке окна меняем:
        // иконку
        ImageView iconPanel = (ImageView) findViewById(R.id.types_icon);
        iconPanel.setImageResource(R.drawable.ci_panel);
        // название
        EditText panelET = (EditText) findViewById(R.id.types_title);
        panelET.setText(getResources().getString(R.string.jv_setting_screen));
//============================================================
        itemsArray = new ArrayList<HashMap<String, String>>();
        // загружаем из базы порядок панелей на экране
        MyDBHelper dbHelper = new MyDBHelper(this, "SCREEN");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (db != null) {
            Cursor c;
            c = db.query("SCREEN", null, null, null, null, null, null);
            if (c != null && c.getCount()>0) {
                c.close();
                dbSCREEN();
            }else{
                GetPanel();
            }
            db.close();
        }else{
            //===========================================================================================
            // наполняем массив с параметрами кнопок из базы
            GetPanel();
        }
        dbHelper.close();
        //=============================================================================================
        // Fill listview with our info
        ListView lv = (ListView) findViewById(R.id.types_lv);

        adapter = new TPAdapter(this);
        lv.setAdapter(adapter);

        // OK/Save button
        Button okBtn = (Button) findViewById(R.id.types_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UpdatePanelDB();
                PrefsActivity.baseChange = true;
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        // Add new button
        Button addBtn = (Button) findViewById(R.id.types_new);
        addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // список панелей для выбора
                ArrayList<String> tempvalue = new ArrayList<String>();
                Collections.addAll(tempvalue, getResources().getStringArray(R.array.array_panel_names));

                String[] listPanelName = new String[tempvalue.size()];
                listPanelName = tempvalue.toArray(listPanelName);

                tempvalue.clear();

                AlertDialog.Builder builder = new AlertDialog.Builder(ScreenSettingActivity.this);
                // "Select application"
                builder.setTitle(getResources().getString(R.string.jv_prefs_select_panels));
                final String[] finalListPanelName = listPanelName;
                builder.setSingleChoiceItems(listPanelName, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int i) {
                        // создание новой панели
                        if (i == 0) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(ScreenSettingActivity.this);
                            // "Select number"
                            builder1.setTitle(getResources().getString( R.string.jv_prefs_select_panel_name));
                            final EditText input = new EditText(ScreenSettingActivity.this);
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            input.setText("Panel");
                            builder1.setView(input);
                            // "Ok"
                            builder1.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                            AddPanelDB(0, String.valueOf(input.getText()));
                                            adapter.notifyDataSetChanged();
                                            dialog.dismiss();
                                        }
                                    });
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            builder1.show();
                            // выбор номера страницы запущенных программ
                        }else{
                            AddPanelDB(i, finalListPanelName[i]);
                            adapter.notifyDataSetChanged();
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });
                builder.show();



            }
        });

        // Cancel button
        Button cancelBtn = (Button) findViewById(R.id.types_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        // back btn - work as cancel
        ImageButton backBtn = (ImageButton) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
        ScreenOrientation.set(this, prefs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.generalOnResume(TAG);
    }
    // это переделано на новый вариант
    private void UpdatePanelDB(){
        MyDBHelper dbHelper = new MyDBHelper(getApplicationContext(), "SCREEN");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (db == null) {
            return;
        }
        // удаляем старые данные из базы
        Cursor c = db.query("SCREEN", null, null, null, null, null, null);
        if (c.getCount()>0){
            dbHelper.resetDb(db);
            c.close();
        }

        // заносим новые данные в базу
        for (HashMap<String, String> anItemsArray : itemsArray) {
            cv.put("ID_PANEL", Integer.valueOf(anItemsArray.get("id")));
            db.insert("SCREEN", null, cv);
            cv.clear();
        }
        db.close();
        dbHelper.close();
    }
    private void dbSCREEN(){
        MyDBHelper dbHelper = new MyDBHelper(getApplicationContext(), "SCREEN");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if(db == null){
            return;
        }
        MyDBHelper dbHelperPanelName = new MyDBHelper(getApplicationContext(), "LIST_PANELS");
        SQLiteDatabase dbPanelName = dbHelperPanelName.getReadableDatabase();
        if(dbPanelName == null){
            return;
        }
        // делаем запрос данных из таблицы , получаем Cursor на таблицу с панелями экрана
        Cursor c = db.query("SCREEN", null, null, null, null, null, null);
        Cursor cListPanels;
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            int panelID = c.getColumnIndex("ID_PANEL");
            String numPanels;
            ArrayList<String> tempvalue = new ArrayList<String>();
            Collections.addAll(tempvalue, getResources().getStringArray(R.array.array_panel_names));
            do {
                HashMap<String, String> item = new HashMap<String, String>();
                numPanels = c.getString(panelID);
                item.put("id", numPanels);
                if ("1".equals(numPanels)){
                    item.put("panel", tempvalue.get(1));
                }else if ("2".equals(numPanels)){
                    item.put("panel", tempvalue.get(2));
                }else if ("3".equals(numPanels)){
                    item.put("panel", tempvalue.get(3));
                }else if ("4".equals(numPanels)){
                    item.put("panel", tempvalue.get(4));
                }else if ("5".equals(numPanels)){
                    item.put("panel", tempvalue.get(5));
                }else{
                    String selection = "NUMBER = ?";
                    String[] selectionArgs = new String[] { String.valueOf(numPanels) };
                    cListPanels = dbPanelName.query("LIST_PANELS", null, selection, selectionArgs, null, null, null);

                    if (cListPanels.getCount()>0){
                        //item.put("panel", "Know");
                        cListPanels.moveToFirst();
                        int panelID2 = cListPanels.getColumnIndex("NUMBER");
                        if (numPanels.equals(String.valueOf(cListPanels.getInt(panelID2)))){
                            int panel = cListPanels.getColumnIndex("PANEL_NAME");
                            item.put("panel", cListPanels.getString(panel));
                        }
                    }else{
                        item.put("panel", "Unknow");
                    }
                    cListPanels.close();
                }
                itemsArray.add(item);
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        dbPanelName.close();
        dbHelper.close();
        dbHelperPanelName.close();
    }
    private void GetPanel(){
        // наполняем массив с параметрами кнопок из базы

        itemsArray.clear();
        ArrayList<String> tempvalue = new ArrayList<String>();
        Collections.addAll(tempvalue, getResources().getStringArray(R.array.array_panel_names));

        HashMap<String, String> i = new HashMap<String, String>();

        i.put("panel", tempvalue.get(1));
        i.put("id", "1");
        itemsArray.add(i);

        i = new HashMap<String, String>();
        i.put("panel", tempvalue.get(2));
        i.put("id", "2");
        itemsArray.add(i);

        i = new HashMap<String, String>();
        i.put("panel", tempvalue.get(3));
        i.put("id", "3");
        itemsArray.add(i);

        i = new HashMap<String, String>();
        i.put("panel", tempvalue.get(4));
        i.put("id", "4");
        itemsArray.add(i);

        i = new HashMap<String, String>();
        i.put("panel", tempvalue.get(5));
        i.put("id", "5");
        itemsArray.add(i);
    }
    private void AddPanelDB(int panel, String namePanel) {
        HashMap<String, String> i = new HashMap<String, String>();
        i.put("panel", namePanel);
        if (panel > 0) {
            i.put("id", String.valueOf(panel));
        }else{
            // новая панель и регистрация в списках панелей
            // id с 0 до 5 заняты стандартными панелями
            int idNewPanel = 6;
            // в цикле ищем первую свободную цифру
            while (!SearchFreeIDPanel(idNewPanel)) {
                idNewPanel++;
            }
            i.put("id", String.valueOf(idNewPanel));
            // надо добавить новую панель в массив
            AddNewPanel(idNewPanel, namePanel);
        }
        // добавляем в массив
        itemsArray.add(i);
        UpdatePanelDB();
    }
    private int[] GetListIDPanel(){
        MyDBHelper dbHelperPanelName = new MyDBHelper(getApplicationContext(), "LIST_PANELS");
        SQLiteDatabase dbPanelName = dbHelperPanelName.getReadableDatabase();
        if(dbPanelName == null){
            return null;
        }
        int[] idPanel = null;
        Cursor cListPanels = dbPanelName.query("LIST_PANELS", null, null, null, null, null, null);
        if (cListPanels.getCount()>0){
            idPanel = new int[cListPanels.getCount()];
            cListPanels.moveToFirst();
            int panelColNum = cListPanels.getColumnIndex("NUMBER");
            int i = 0;
            do {
                idPanel[i] = Integer.valueOf(cListPanels.getString(panelColNum));
                i++;
            }while (cListPanels.moveToNext());
        }
        cListPanels.close();
        dbPanelName.close();
        dbHelperPanelName.close();
        return idPanel;
    }
    private boolean SearchFreeIDPanel(int id){
        boolean flag = true;
        //получаем список номеров панелей
        int[] idListPanel = GetListIDPanel();
        if (idListPanel != null) {
            for (int anIdListPanel : idListPanel) {
                if (anIdListPanel == id) {
                    flag = false;
                }
            }
        }
        return flag;
    }
    private void AddNewPanel(int id, String namePanel){
        MyDBHelper dbHelperPanelName = new MyDBHelper(getApplicationContext(), "LIST_PANELS");
        SQLiteDatabase dbPanelName = dbHelperPanelName.getWritableDatabase();
        if(dbPanelName == null){
            return;
        }
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        cv.put("PANEL_NAME", namePanel);
        cv.put("NUMBER", id);
        // вставляем запись
        //dbPanelName.update("LIST_PANELS", cv, null, null);
        dbPanelName.insert("LIST_PANELS", null, cv);

        dbPanelName.close();
        dbHelperPanelName.close();
    }
    private void DelPanelDB(int panel) {
        // удаление из базы Список панелей
        DelListPanelDB(panel);

        // удаление из базы Кнопки панелей
        DelListButtonDB(panel);

        // удаление из базы Экран
        DelScreenDB(panel);
    }
    private void DelListPanelDB(int id){
        MyDBHelper dbHelperPanelName = new MyDBHelper(getApplicationContext(), "LIST_PANELS");
        SQLiteDatabase dbPanelName = dbHelperPanelName.getWritableDatabase();
        if(dbPanelName == null){
            return;
        }

        dbPanelName.delete("LIST_PANELS", "NUMBER = ?", new String[]{String.valueOf(id)});
        dbPanelName.close();

        dbPanelName.close();
        dbHelperPanelName.close();
    }
    private void DelListButtonDB(int id){
        MyDBHelper dbHelperPanelName = new MyDBHelper(getApplicationContext(), "PANELS");
        SQLiteDatabase dbPanelName = dbHelperPanelName.getWritableDatabase();
        if(dbPanelName == null){
            return;
        }
        // удаляем старые данные из базы
        Cursor c;
        String selection = "PANEL = ?";
        String[] selectionArgs = new String[] { String.valueOf(id) };

        // находим текущие настройки и удаляем их
        c = dbPanelName.query("PANELS", null, selection, selectionArgs, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                int panelID = c.getColumnIndex("ID");
                do {
                    dbPanelName.delete("PANELS", "ID = ?" ,new String[]{String.valueOf(c.getInt(panelID))});
                } while (c.moveToNext());
            }
            c.close();
        }

        dbPanelName.close();
        dbHelperPanelName.close();
    }
    private void DelScreenDB(int id){
        MyDBHelper dbHelperPanelName = new MyDBHelper(getApplicationContext(), "SCREEN");
        SQLiteDatabase dbPanelName = dbHelperPanelName.getWritableDatabase();
        if(dbPanelName == null){
            return;
        }
        // удаляем старые данные из базы
        Cursor c;
        String selection = "ID_PANEL = ?";
        String[] selectionArgs = new String[] { String.valueOf(id) };

        // находим текущие настройки и удаляем их
        c = dbPanelName.query("SCREEN", null, selection, selectionArgs, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                int panelID = c.getColumnIndex("ID_PANEL");
                do {
                    dbPanelName.delete("SCREEN", "ID_PANEL = ?" ,new String[]{String.valueOf(c.getInt(panelID))});
                } while (c.moveToNext());
            }
            c.close();
        }

        dbPanelName.close();
        dbHelperPanelName.close();
    }
}