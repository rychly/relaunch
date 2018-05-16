package com.harasoft.relaunch.Preferences;

import android.app.Activity;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.harasoft.relaunch.R;
import com.harasoft.relaunch.ReLaunchApp;
import com.harasoft.relaunch.Utils.UtilIcons;
import com.harasoft.relaunch.Utils.UtilPanels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class PanelSettingsActivity extends Activity {
    final String TAG = "PanelSettingsActivity";
    static List<HashMap<String, String>> itemsArray;
    private TPAdapter adapter;
    private ReLaunchApp app;
    private SharedPreferences prefs;
    private final static int TYPES_ACT = 1;
    private static int idPanel;
    private UtilPanels utilPanels;
    private static ArrayList<Integer> removeButtons;
    private static ArrayList<Integer> addButtons;


    class TPAdapter extends BaseAdapter {
        //private final Context context;
        private LayoutInflater lInflater;

        TPAdapter(Context context) {
            //this.context = context;
            lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                v = lInflater.inflate(R.layout.item_panelsettings, parent, false);
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
                iv.setImageBitmap(getButtonIcon(job, namejob));
                // кнопка номер/всего
                TextView button_title = (TextView) v.findViewById(R.id.button_title);
                //
                button_title.setText(getResources().getString(
                        R.string.pref_i_manualButton1_title) + " (" + (position + 1) + "/" + (itemsArray.size()) + ")");
                // действие кнопки
                TextView button_name_title = (TextView) v.findViewById(R.id.button_name_title);
                button_name_title.setText(getButtonName(job, namejob));

                // Setting up button
                ImageButton upBtn = (ImageButton) v.findViewById(R.id.types_up);
                if (position == 0) {
                    upBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_arrowup_gray));
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
                    downBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_arrowdown_gray));
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
                        removeButtons.add(utilPanels.getButtonPosition(itemsArray.get(position)));
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
                        intent.putExtra("uniqId", utilPanels.getButtonPosition (itemsArray.get(position)));
                        startActivityForResult(intent, TYPES_ACT);
                    }
                });

            }
            return v;
        }
        private String getButtonName(String job, String namejob) {
            String title = "";
            if ("RUN".equals(job)) {// внешняя программа
                title = AppName(namejob);
            } else if ("FAVDOCN".equals(job)) {// страница фаворитов документов
                title = getResources().getString(R.string.app_settings_favorites_doc_number) + namejob;
            } else if ("LRUN".equals(job)) {// страница запущенных ДОКУМЕНТОВ
                title = getResources().getString(R.string.app_settings_lru_doc_number) + namejob;
            } else if ("HOMEN".equals(job)) {// страница домашних папок
                title = getResources().getString(R.string.app_settings_home_number) + namejob;
            } else if ("HOMEMENU".equals(job)) {// всплывающее меню домашних папок
                title = getResources().getString(R.string.app_settings_home_menu);
            } else if ("HOMESCREEN".equals(job)) {// экран домашних папок
                title = getResources().getString(R.string.app_settings_home);
            } else if ("LRUMENU".equals(job)) {// всплывающее меню запущенных документов
                title = getResources().getString(R.string.app_settings_lru_doc_menu);
            } else if ("LRUSCREEN".equals(job)) {// экран запущенных документов
                title = getResources().getString(R.string.app_settings_lru_doc);
            } else if ("FAVDOCMENU".equals(job)) {// всплывающее меню фаворитов документов
                title = getResources().getString(R.string.app_settings_favorites_doc_menu);
            } else if ("FAVDOCSCREEN".equals(job)) {// экран фаворитов документов
                title = getResources().getString(R.string.app_settings_favorites_doc);
            } else if ("ADVANCED".equals(job)) {// расширенные настройки
                title = getResources().getString(R.string.app_settings_advanced);
            } else if ("SETTINGS".equals(job)) {// настройки
                title = getResources().getString(R.string.app_settings_settings);
            } else if ("APPMANAGER".equals(job)) {// все приложения
                title = getResources().getString(R.string.app_settings_app_manager);
            } else if ("BATTERY".equals(job)) {// показ расхода по приложениям
                title = getResources().getString(R.string.app_settings_battery);
            } else if ("FAVAPP".equals(job)) {// всплывающее меню фаворитов приложений
                title = getResources().getString(R.string.app_settings_favorites_app);
            } else if ("ALLAPP".equals(job)) {//
                title = getResources().getString(R.string.app_settings_all_app);
            } else if ("LASTAPP".equals(job)) {
                title = getResources().getString(R.string.app_settings_last_app);
            } else if ("SEARCH".equals(job)) {
                title = getResources().getString(R.string.app_settings_search);
            } else if ("LOCK".equals(job)) {
                title = getResources().getString(R.string.app_settings_lock);
            } else if ("POWEROFF".equals(job)) {
                title = getResources().getString(R.string.app_settings_power_off);
            } else if ("REBOOT".equals(job)) {
                title = getResources().getString(R.string.app_settings_reboot);
            } else if ("SWITCHWIFI".equals(job)) {
                title = getResources().getString(R.string.app_settings_switch_wifi);
            } else if ("DROPBOX".equals(job)) {
                title = getResources().getString(R.string.app_settings_dropbox);
            } else if ("OPDS".equals(job)) {
                title = getResources().getString(R.string.app_settings_opds);
            } else if ("SYSSETTINGS".equals(job)) {
                title = getResources().getString(R.string.app_settings_sys_settings);
            } else if ("UPDIR".equals(job)) {// в родительскую папку
                title = getResources().getString(R.string.app_settings_up_dir);
            } else if ("UPSCROLL".equals(job)) {// пролистывание на экран вверх
                title = getResources().getString(R.string.app_settings_up_scrool);
            } else if ("UPSCROLLPERC".equals(job)) {// пролистывание на экран вверх в процентах
                title = getResources().getString(R.string.app_settings_up_perc);
            } else if ("UPSCROLLBEGIN".equals(job)) {// пролистывание в самое начало
                title = getResources().getString(R.string.app_settings_up_begin);
            } else if ("DOWNSCROLL".equals(job)) {// пролистывае на экран вниз
                title = getResources().getString(R.string.app_settings_down_scrool);
            } else if ("DOWNSCROLLPERC".equals(job)) {// пролистывае на экран вниз в процентах
                title = getResources().getString(R.string.app_settings_down_perc);
            } else if ("DOWNSCROLLEND".equals(job)) {// пролистывае всамый конец
                title = getResources().getString(R.string.app_settings_down_end);
            } else if ("SORTMENU".equals(job)) {// меню выбора сортировки
                title = getResources().getString(R.string.app_settings_sort_menu);
            } else if ("SELNUMCOL".equals(job)) {// выбор числа колонок
                title = getResources().getString(R.string.app_settings_sel_num_col);
            }
            return title;
        }
        private Bitmap getButtonIcon(String job, String namejob) {
            UtilIcons utilIcons = new UtilIcons(getBaseContext());
            if ("RUN".equals(job)) {
                return  utilIcons.getIcon(namejob);
            } else {
                return utilIcons.getIcon(job);
            }
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
        app.setOptionsWindowActivity(this);
        setContentView(R.layout.layout_types);
        UtilIcons utilIcons = new UtilIcons(getBaseContext());
        utilPanels = new UtilPanels(getBaseContext());
        removeButtons = new ArrayList<>();
        addButtons = new ArrayList<>();
        // в заголовке окна меняем:
        // иконку
        ImageView iconPanel = (ImageView) findViewById(R.id.types_icon);
        iconPanel.setImageDrawable(new BitmapDrawable(getResources(), utilIcons.getIcon("PANEL")));
        // получаем данные
        final Intent data = getIntent();
        if (data.getExtras() == null) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        if (data.getExtras().getString("panelID") != null) {
            idPanel = Integer.valueOf(data.getExtras().getString("panelID"));
            String namePanel = data.getExtras().getString("namePanel");
            //============================================================
            // наполняем массив с параметрами кнопок из базы
            itemsArray = utilPanels.getPanelButon(idPanel);
            //=============================================================================================

            // название
            TextView panelET = (TextView) findViewById(R.id.types_title);
            panelET.setText(namePanel + "(id=" + idPanel + ")");

            // Fill listview with our info
            ListView lv = (ListView) findViewById(R.id.types_lv);
            adapter = new TPAdapter(this);
            lv.setAdapter(adapter);

            // OK/Save button
            Button okBtn = (Button) findViewById(R.id.types_ok);
            okBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("OK")), null, null, null);
            okBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    utilPanels.updatePanelButons(idPanel, itemsArray);
                    PrefsActivity.baseChange = true;
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            });

            // Add new button
            Button addBtn = (Button) findViewById(R.id.types_new);
            addBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("ADD")), null, null, null);
            addBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(PanelSettingsActivity.this, ButtonSettingActivity.class);
                    intent.putExtra("buttonID", -1);
                    intent.putExtra("uniqId", getUniqNumber());
                    startActivityForResult(intent, TYPES_ACT);
                }
            });

            // Cancel button
            Button cancelBtn = (Button) findViewById(R.id.types_cancel);
            cancelBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("DELETE")), null, null, null);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            });

            // back btn - work as cancel
            ImageButton backBtn = (ImageButton) findViewById(R.id.back_btn);
            backBtn.setImageBitmap(utilIcons.getIcon("EXIT"));
            backBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.generalOnResume(TAG);
        adapter.notifyDataSetChanged();
    }
    //===== получение иконки по имени программы =====
    private String AppName(String nameApp) {
        String[] app = nameApp.split("%");
        return app[2];
    }
    // получение уникального номера кнопки
    private int getUniqNumber() {

        int uniqNum;
        for (int count = 0; ; count++) {
            if (removeButtons.size() > 0 && removeButtons.contains(count)) {
                continue;
            }
            if (addButtons.size() > 0 && addButtons.contains(count)) {
                continue;
            }
            if (isNumberButton(count)) {
                continue;
            }
            uniqNum = count;
            break;
        }
        return uniqNum;
    }
    // поиск номера панели в массиве
    private boolean isNumberButton(int num) {
        for (HashMap<String, String> item: itemsArray) {
            if (num == utilPanels.getButtonPosition (item)) {
                return true;
            }
        }
        return false;
    }
}