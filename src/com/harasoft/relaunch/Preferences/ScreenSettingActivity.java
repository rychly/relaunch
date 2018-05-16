package com.harasoft.relaunch.Preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.harasoft.relaunch.R;
import com.harasoft.relaunch.ReLaunchApp;
import com.harasoft.relaunch.Utils.UtilIcons;
import com.harasoft.relaunch.Utils.UtilPanels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ScreenSettingActivity extends Activity {
    private final String TAG = "ScreenSettingActivity";
    private List<HashMap<String, String>> itemsArray;
    private TPAdapter adapter;
    private ReLaunchApp app;
    private SharedPreferences prefs;
    private final static int TYPES_ACT = 1;
    private UtilPanels utilPanels;
    private static ArrayList<Integer> removePanels;
    private static ArrayList<Integer> addPanels;

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
                v = vi.inflate(R.layout.item_screensetting, parent, false);
                if (v == null) {
                    return null;
                }
            }
            final HashMap<String, String> item = itemsArray.get(position);
            if (item != null) {
                // название панели
                //String panelName = item.get("panel");
                String panelName = utilPanels.getNamePanel(item);
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
                if (utilPanels.isPathPanel(item) || itemsArray.size() < 2) {
                    rmBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_delsmall_gray));
                    rmBtn.setEnabled(false);
                }else {
                    rmBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_delsmall));
                    rmBtn.setEnabled(true);
                    rmBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            removePanelFromList(position);
                            itemsArray.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

                // Setting edit  button
                ImageButton edBtn = (ImageButton) v.findViewById(R.id.types_edit);
                if (utilPanels.isPathPanel(item)){
                    edBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_edit_mini_gray));
                    edBtn.setEnabled(false);
                }else{
                    edBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_edit_mini));
                    edBtn.setEnabled(true);
                    edBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent(ScreenSettingActivity.this, PanelSettingsActivity.class);
                            intent.putExtra("panelID", String.valueOf(utilPanels.getNumberPanel (item)));
                            intent.putExtra("namePanel", utilPanels.getNamePanel(item));
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
        app.setOptionsWindowActivity(this);
        setContentView(R.layout.layout_types);
        UtilIcons utilIcons = new UtilIcons(getBaseContext());
        // в заголовке окна меняем:
        // иконку
        ImageView iconPanel = (ImageView) findViewById(R.id.types_icon);
        iconPanel.setImageDrawable(new BitmapDrawable(getResources(), utilIcons.getIcon("PANEL")));
        // название
        TextView panelET = (TextView) findViewById(R.id.types_title);
        panelET.setText(getResources().getString(R.string.jv_setting_screen));
        //============================================================
        utilPanels = new UtilPanels(getBaseContext());
        itemsArray = utilPanels.getListPanelsOfScreen();
        removePanels = new ArrayList<>();
        addPanels = new ArrayList<>();
        //=============================================================================================
        // Fill listview with our info
        ListView lv = (ListView) findViewById(R.id.types_lv);

        adapter = new TPAdapter(this);
        lv.setAdapter(adapter);

        // инициализация массивов

        // OK/Save button
        Button okBtn = (Button) findViewById(R.id.types_ok);
        okBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("OK")), null, null, null);
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                removePanels();
                utilPanels.updatePanels(itemsArray);
                PrefsActivity.baseChange = true;
                // очищаем переменные
                removePanels.clear();
                removePanels.trimToSize();
                addPanels.clear();
                addPanels.trimToSize();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        // Add new button
        Button addBtn = (Button) findViewById(R.id.types_new);
        addBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("ADD")), null, null, null);
        addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // список панелей для выбора
                ArrayList<String> tempvalue = new ArrayList<>();
                Collections.addAll(tempvalue, getResources().getStringArray(R.array.array_panel_names));

                String[] listPanelName = getListAddPanel (tempvalue);

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
                            input.setText(getResources().getString(R.string.pref_i_manualPanel1_title));
                            builder1.setView(input);
                            // "Ok"
                            builder1.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                            addPanel(0, String.valueOf(input.getText()));
                                            adapter.notifyDataSetChanged();
                                            dialog.dismiss();
                                        }
                                    });
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            builder1.show();
                            // выбор номера страницы запущенных программ
                        }else{
                            addPanel(0, finalListPanelName[i]);
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
        // Переделать на удаление всех панелей
        // Cancel button
        Button cancelBtn = (Button) findViewById(R.id.types_cancel);
        cancelBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("DELETE")), null, null, null);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // удаляем добавленные панели
                removeAddsPanels();
                // очищаем переменные
                removePanels.clear();
                removePanels.trimToSize();
                addPanels.clear();
                addPanels.trimToSize();
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        // back btn - work as cancel
        ImageButton backBtn = (ImageButton) findViewById(R.id.back_btn);
        backBtn.setImageBitmap(utilIcons.getIcon("EXIT"));
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // удаляем добавленные панели
                removeAddsPanels();
                // очищаем переменные
                removePanels.clear();
                removePanels.trimToSize();
                addPanels.clear();
                addPanels.trimToSize();
                // возвращаем код операции
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.generalOnResume(TAG);
    }
    // это переделано на новый вариант
    private void addPanel(int typePanel, String namePanel) {
        int numPanel = getUniqNumber();
        // добавляем в специальный массив, чтобы знать , что это новая панель
        addPanels.add(numPanel);
        // добавляем в массив
        int add_panel = getNumberPanel(namePanel);
        if (add_panel == 0) {
            itemsArray.add(utilPanels.getNewPanel(numPanel, namePanel, typePanel));
        }else {
            itemsArray.add(utilPanels.getStandartPanel(add_panel, numPanel, itemsArray.size(), namePanel));
        }

    }
    // получение уникального номера панели
    private int getUniqNumber() {

        int uniqNum;
        for (int count = 0; ; count++) {
            if (removePanels.size() > 0 && removePanels.contains(count)) {
                continue;
            }
            if (addPanels.size() > 0 && addPanels.contains(count)) {
                continue;
            }
            if (isNumberPanel(count)) {
                continue;
            }
            uniqNum = count;
            break;
        }
        return uniqNum;
    }
    // поиск номера панели в массиве
    private boolean isNumberPanel(int num) {
        for (HashMap<String, String> item: itemsArray) {
            if (num == utilPanels.getNumberPanel (item)) {
                return true;
            }
        }
        return false;
    }
    // удаление всех удаленных панелей
    private void removePanels() {
        if (removePanels.size() > 0) {
            for (int id_panel: removePanels) {
                utilPanels.removePanel(id_panel);
            }
        }
    }
    // удаление всех добавленных панелей
    private void removeAddsPanels() {
        if (addPanels.size() > 0) {
            for (int id_panel: addPanels) {
                utilPanels.removePanel(id_panel);
            }
        }
    }
    // удаление панели из списка
    private void removePanelFromList(int position) {
        int id_panel = utilPanels.getNumberPanel(itemsArray.get(position));
        if (addPanels.contains(id_panel)) {
            addPanels.remove(addPanels.indexOf(id_panel));
            utilPanels.removePanel(id_panel);
        }else {
            removePanels.add(id_panel);
        }
    }
    // получение списка панелей
    private String[] getListAddPanel (ArrayList<String> tempvalue) {
        ArrayList<String> temp = new ArrayList<>();
        boolean addPanel;
        for (String namePanel: tempvalue) {
            addPanel = true;
            for (HashMap<String, String> panel: itemsArray) {
                if (namePanel.equals(utilPanels.getNamePanel(panel))) {
                    addPanel = false;
                    break;
                }
            }
            if (addPanel) {
                temp.add(namePanel);
            }
        }
        String[] out = new String[temp.size()];
        out = temp.toArray(out);
        return out;
    }
    // проверяем стандартная панель или нет
    private int getNumberPanel(String name_panel) {
        // список панелей для выбора
        ArrayList<String> tempvalue = new ArrayList<>();
        Collections.addAll(tempvalue, getResources().getStringArray(R.array.array_panel_names));
        int count = 0;
        int num = 0;
        for (String name: tempvalue) {
            if (name.equals(name_panel)) {
                num = count;
            }
            count++;
        }
        return num;
    }
}