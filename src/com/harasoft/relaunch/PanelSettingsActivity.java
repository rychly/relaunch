package com.harasoft.relaunch;

import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class PanelSettingsActivity extends Activity {
    final String TAG = "PanelSettingsActivity";
    static List<HashMap<String, String>> itemsArray;
    TPAdapter adapter;
    ReLaunchApp app;
    SharedPreferences prefs;
    int firstLineIconSizePx = 48;
    CharSequence[] applications;
    CharSequence[] happlications;
    final static public int TYPES_ACT = 1;
    static int idPanel;

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
                if (vi == null) {
                    return null;
                }

                v = vi.inflate(R.layout.panel_layout, parent, false);
                if (v == null) {
                    return null;
                }
            }
            final HashMap<String, String> item = itemsArray.get(position);
            if (item != null) {
                // иконка
                String job = item.get("getClick");
                String namejob = item.get("addGetClick");
                ImageView iv = (ImageView) v.findViewById(R.id.icon_img);
                if ("RUN".equals(job)) {
                    iv.setImageBitmap(BitmapIconForButton(namejob));
                } else {
                    iv.setImageBitmap(app.JobIcon(job));
                }
                // кнопка номер/всего
                TextView button_title = (TextView) v.findViewById(R.id.button_title);
                //
                button_title.setText(getResources().getString(
                        R.string.pref_i_manualButton1_title) + " (" + (position + 1) + "/" + (itemsArray.size()) + ")");
                // действие кнопки
                TextView button_name_title = (TextView) v.findViewById(R.id.button_name_title);
                if ("RUN".equals(job)) {// внешняя программа
                    button_name_title.setText(AppName(namejob));
                } else if ("FAVDOCN".equals(job)) {// страница фаворитов документов
                    button_name_title.setText(getResources().getString(R.string.app_settings_favorites_doc_number) + namejob);
                } else if ("LRUN".equals(job)) {// страница запущенных ДОКУМЕНТОВ
                    button_name_title.setText(getResources().getString(R.string.app_settings_lru_doc_number) + namejob);
                } else if ("HOMEN".equals(job)) {// страница домашних папок
                    button_name_title.setText(getResources().getString(R.string.app_settings_home_number) + namejob);
                } else if ("HOMEMENU".equals(job)) {// всплывающее меню домашних папок
                    button_name_title.setText(getResources().getString(R.string.app_settings_home_menu));
                } else if ("HOMESCREEN".equals(job)) {// экран домашних папок
                    button_name_title.setText(getResources().getString(R.string.app_settings_home));
                } else if ("LRUMENU".equals(job)) {// всплывающее меню запущенных документов
                    button_name_title.setText(getResources().getString(R.string.app_settings_lru_doc_menu));
                } else if ("LRUSCREEN".equals(job)) {// экран запущенных документов
                    button_name_title.setText(getResources().getString(R.string.app_settings_lru_doc));
                } else if ("FAVDOCMENU".equals(job)) {// всплывающее меню фаворитов документов
                    button_name_title.setText(getResources().getString(R.string.app_settings_favorites_doc_menu));
                } else if ("FAVDOCSCREEN".equals(job)) {// экран фаворитов документов
                    button_name_title.setText(getResources().getString(R.string.app_settings_favorites_doc));
                } else if ("ADVANCED".equals(job)) {// расширенные настройки
                    button_name_title.setText(getResources().getString(R.string.app_settings_advanced));
                } else if ("SETTINGS".equals(job)) {// настройки
                    button_name_title.setText(getResources().getString(R.string.app_settings_settings));
                } else if ("APPMANAGER".equals(job)) {// все приложения
                    button_name_title.setText(getResources().getString(R.string.app_settings_app_manager));
                } else if ("BATTERY".equals(job)) {// показ расхода по приложениям
                    button_name_title.setText(getResources().getString(R.string.app_settings_battery));
                } else if ("FAVAPP".equals(job)) {// всплывающее меню фаворитов приложений
                    button_name_title.setText(getResources().getString(R.string.app_settings_favorites_app));
                } else if ("ALLAPP".equals(job)) {//
                    button_name_title.setText(getResources().getString(R.string.app_settings_all_app));
                } else if ("LASTAPP".equals(job)) {
                    button_name_title.setText(getResources().getString(R.string.app_settings_last_app));
                } else if ("SEARCH".equals(job)) {
                    button_name_title.setText(getResources().getString(R.string.app_settings_search));
                } else if ("LOCK".equals(job)) {
                    button_name_title.setText(getResources().getString(R.string.app_settings_lock));
                } else if ("POWEROFF".equals(job)) {
                    button_name_title.setText(getResources().getString(R.string.app_settings_power_off));
                } else if ("REBOOT".equals(job)) {
                    button_name_title.setText(getResources().getString(R.string.app_settings_reboot));
                } else if ("SWITCHWIFI".equals(job)) {
                    button_name_title.setText(getResources().getString(R.string.app_settings_switch_wifi));
                } else if ("DROPBOX".equals(job)) {
                    button_name_title.setText(getResources().getString(R.string.app_settings_dropbox));
                } else if ("OPDS".equals(job)) {
                    button_name_title.setText(getResources().getString(R.string.app_settings_opds));
                } else if ("FTP".equals(job)) {
                    button_name_title.setText(getResources().getString(R.string.app_settings_ftp));
                } else if ("SYSSETTINGS".equals(job)) {
                    button_name_title.setText(getResources().getString(R.string.app_settings_sys_settings));
                } else if ("UPDIR".equals(job)) {// в родительскую папку
                    button_name_title.setText(getResources().getString(R.string.app_settings_up_dir));
                } else if ("UPSCROOL".equals(job)) {// пролистывание на экран вверх
                    button_name_title.setText(getResources().getString(R.string.app_settings_up_scrool));
                } else if ("DOWNSCROOL".equals(job)) {// пролистывае на экран вниз
                    button_name_title.setText(getResources().getString(R.string.app_settings_down_scrool));
                }

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
                        itemsArray.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                });
                // Setting edit  button
                ImageButton edBtn = (ImageButton) v.findViewById(R.id.types_edit);
                edBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_edit_mini));
                edBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(PanelSettingsActivity.this, ButtonSettingActivity.class);
                        intent.putExtra("buttonID", position);
                        startActivityForResult(intent, TYPES_ACT);
                    }
                });

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
        List<String> applicationsArray = app.getAppList();
        applications = applicationsArray.toArray(new CharSequence[applicationsArray.size()]);
        happlications = app.getAppList().toArray(new CharSequence[app.getAppList().size()]);
        // в заголовке окна меняем:
        // иконку
        ImageView iconPanel = (ImageView) findViewById(R.id.types_icon);
        iconPanel.setImageResource(R.drawable.ci_panel);
        // получаем данные
        final Intent data = getIntent();
        if (data.getExtras() == null) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        if (data.getExtras().getString("panelID") != null) {
            idPanel = Integer.valueOf(data.getExtras().getString("panelID"));
            //============================================================
            itemsArray = new ArrayList<HashMap<String, String>>();
            //===========================================================================================
            // наполняем массив с параметрами кнопок из базы
            PanelButonGet (idPanel);
            //=============================================================================================

            // название
            EditText panelET = (EditText) findViewById(R.id.types_title);
            panelET.setText(GetNamePanel(idPanel) + "(id=" + idPanel + ")");

            // Fill listview with our info
            ListView lv = (ListView) findViewById(R.id.types_lv);

            adapter = new TPAdapter(this);
            lv.setAdapter(adapter);

            // OK/Save button
            Button okBtn = (Button) findViewById(R.id.types_ok);
            okBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    UpdateButtonDB(idPanel);
                    PrefsActivity.baseChange = true;
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            });

            // Add new button

            Button addBtn = (Button) findViewById(R.id.types_new);
            addBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(PanelSettingsActivity.this, ButtonSettingActivity.class);
                    intent.putExtra("buttonID", -1);
                    startActivityForResult(intent, TYPES_ACT);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        app.generalOnResume(TAG);
    }
    //===== получение иконки по имени программы =====
    private Bitmap BitmapIconForButton(String nameApp) {
        Drawable d;
        Bitmap temp_icon = null;
        Intent componentSearchIntent = new Intent();
        componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        componentSearchIntent.setAction(Intent.ACTION_MAIN);
        PackageManager pm = getPackageManager();
        if (pm == null) {
            return null;
        }
        List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);
        String pname;
        String aname;
        String hname;
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                pname = ri.activityInfo.packageName;
                aname = ri.activityInfo.name;
                try {
                    if (ri.activityInfo.labelRes != 0) {
                        hname = (String) ri.activityInfo.loadLabel(pm);
                    } else {
                        hname = (String) ri.loadLabel(pm);
                    }
                    if (nameApp.equals(pname + "%" + aname + "%" + hname)) {
                        if (ri.activityInfo.icon != 0) {
                            d = ri.activityInfo.loadIcon(pm);
                        } else {
                            d = ri.loadIcon(pm);
                        }

                        if (d != null) {
                            temp_icon = scaleDrawable(d, firstLineIconSizePx);
                        } else {
                            temp_icon = scaleDrawableById(R.drawable.file_notok, firstLineIconSizePx);
                        }
                    }
                } catch (Exception e) {
                    temp_icon = scaleDrawableById(R.drawable.file_notok, firstLineIconSizePx);
                }
            }
        }
        return temp_icon;
    }
    private Bitmap scaleDrawableById(int id, int size) {
        return Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), id), size, size,
                true);
    }

    private Bitmap scaleDrawable(Drawable d, int size) {
        return Bitmap.createScaledBitmap(((BitmapDrawable) d).getBitmap(),
                size, size, true);
    }

    private String AppName(String nameApp) {
        String[] app = nameApp.split("%");
        return app[2];
    }
    private void PanelButonGet(int id){
        // наполняем массив с параметрами кнопок из базы
        MyDBHelper dbHelper = new MyDBHelper(this, "PANELS");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db == null) {
            return;
        }
        // необходимо проверить наличие записи для кнопки в базе. Если есть, то обновить, иначе добавить.
        Cursor c;
        String selection = "PANEL = ?";
        String[] selectionArgs = new String[] { String.valueOf(id) };

        // находим текущие настройки и удаляем их
        c = db.query("PANELS", null, selection, selectionArgs, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                itemsArray.clear();
                int runjobID = c.getColumnIndex("RUNJOB");
                int namejobColumn = c.getColumnIndex("NAMEJOB");
                int runjobDCID = c.getColumnIndex("RUNJOB_DC");
                int namejobDCColumn = c.getColumnIndex("NAMEJOB_DC");
                int runjobLCID = c.getColumnIndex("RUNJOB_LC");
                int namejobLCColumn = c.getColumnIndex("NAMEJOB_LC");

                do {
                    HashMap<String, String> i = new HashMap<String, String>();
                    i.put("getClick", String.valueOf(c.getString(runjobID)));
                    i.put("addGetClick", String.valueOf(c.getString(namejobColumn)));
                    i.put("getDClick", String.valueOf(c.getString(runjobDCID)));
                    i.put("addGetDClick", String.valueOf(c.getString(namejobDCColumn)));
                    i.put("getLClick", String.valueOf(c.getString(runjobLCID)));
                    i.put("addGetLClick", String.valueOf(c.getString(namejobLCColumn)));
                    itemsArray.add(i);
                } while (c.moveToNext());
            }
            c.close();
        }
        db.close();
        dbHelper.close();

    }
    private void UpdateButtonDB(int id){
        MyDBHelper dbHelper = new MyDBHelper(getApplicationContext(), "PANELS");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (db == null) {
            return;
        }
        // удаляем старые данные из базы
        Cursor c;
        String selection = "PANEL = ?";
        String[] selectionArgs = new String[] { String.valueOf(id) };

        // находим текущие настройки и удаляем их
        c = db.query("PANELS", null, selection, selectionArgs, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                int panelID = c.getColumnIndex("ID");
                do {
                    db.delete("PANELS", "ID = ?" ,new String[]{String.valueOf(c.getInt(panelID))});
                } while (c.moveToNext());
            }
            c.close();
        }
        // заносим новые данные в базу
        for (HashMap<String, String> anItemsArray : itemsArray) {
            cv.put("PANEL", String.valueOf(id));
            cv.put("RUNJOB", anItemsArray.get("getClick"));
            cv.put("NAMEJOB", anItemsArray.get("addGetClick"));
            cv.put("RUNJOB_DC", anItemsArray.get("getDClick"));
            cv.put("NAMEJOB_DC", anItemsArray.get("addGetDClick"));
            cv.put("RUNJOB_LC", anItemsArray.get("getLClick"));
            cv.put("NAMEJOB_LC", anItemsArray.get("addGetLClick"));

            db.insert("PANELS", null, cv);
            cv.clear();
        }
        db.close();
        dbHelper.close();
    }
    private String GetNamePanel(int id){
        MyDBHelper dbHelper = new MyDBHelper(getApplicationContext(), "LIST_PANELS");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String namePanel = "";
        if (db == null) {
            return namePanel;
        }
        // ищем запись в базе
        Cursor c;
        String selection = "NUMBER = ?";
        String[] selectionArgs = new String[] { String.valueOf(id) };

        c = db.query("LIST_PANELS", null, selection, selectionArgs, null, null, null);

        if (c != null) {
            if (c.moveToFirst()) {
                int panelID = c.getColumnIndex("PANEL_NAME");
                namePanel = c.getString(panelID);
            }
            c.close();
        }

        db.close();
        dbHelper.close();

        return namePanel;
    }
}