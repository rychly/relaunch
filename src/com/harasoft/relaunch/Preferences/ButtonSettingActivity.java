package com.harasoft.relaunch.Preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.harasoft.relaunch.R;
import com.harasoft.relaunch.ReLaunchApp;
import com.harasoft.relaunch.Utils.UtilApp;
import com.harasoft.relaunch.Utils.UtilIcons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ButtonSettingActivity extends Activity {
    private final String TAG = "ButtonSettingActivity";
    private ReLaunchApp app;
    private SharedPreferences prefs;
    private String singleClick = "";
    private String doubleClick = "";
    private String longClick = "";
    private String singleClick2 = "";
    private String doubleClick2 = "";
    private String longClick2 = "";
    private int iconPosition;
    private String[] listNameJob;
    private String[] listNameJobTitle;
    private CharSequence[] applications;
    private CharSequence[] happlications;
    private int idButton = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        // Global storage
        app = ((ReLaunchApp) getApplicationContext());
        if(app == null ) {
            finish();
        }
        app.setOptionsWindowActivity(this);
        setContentView(R.layout.layout_button);
        UtilApp utilApp = new UtilApp(getBaseContext(), getPackageManager());
        List<String> applicationsArray = utilApp.getAppList();
        applications = applicationsArray.toArray(new CharSequence[applicationsArray.size()]);
        happlications = applicationsArray.toArray(new CharSequence[applicationsArray.size()]);
        // получаем данные
        final Intent data = getIntent();
        if(data != null && data.getExtras() != null){
            idButton = data.getExtras().getInt("buttonID");
            iconPosition = data.getExtras().getInt("uniqId");
            if(idButton != -1){
                // здесь необходимо установить текущие действия навешанные на кнопку
                butonGet(idButton);
            }
        }

        // OK/Save button
        Button okBtn = (Button) findViewById(R.id.button_add_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addButtonArray();
                finish();
            }
        });
        // Cancel button
        Button cancelBtn = (Button) findViewById(R.id.button_add_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        ArrayList<String> tempvalue = new ArrayList<>();
        Collections.addAll(tempvalue, getResources().getStringArray(R.array.arr_button_actions_values));
        listNameJob = new String[tempvalue.size()];
        listNameJob = tempvalue.toArray(listNameJob);

        tempvalue.clear();
        Collections.addAll(tempvalue, getResources().getStringArray(R.array.arr_button_actions_names));
        listNameJobTitle = new String[tempvalue.size()];
        listNameJobTitle = tempvalue.toArray(listNameJobTitle);
        // одиночное нажатие
        Button buttonClick = (Button) findViewById(R.id.button_add_click_btn);
        buttonClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialogSelectClick(0);
            }
        });
        // двойное нажатие
        Button buttonDClick = (Button) findViewById(R.id.button_add_double_click_btn);
        buttonDClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialogSelectClick(1);
            }
        });
        // долгое нажатие
        Button buttonLClick = (Button) findViewById(R.id.button_add_long_click_btn);
        buttonLClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialogSelectClick(2);
            }
        });

        getSelect(0, singleClick, singleClick2);
        getSelect(1, doubleClick, doubleClick2);
        getSelect(2, longClick, longClick2);
    }
    @Override
    protected void onResume() {
        super.onResume();
        app.generalOnResume(TAG);
    }

    private void butonGet(int idButton){
            HashMap<String, String> item = PanelSettingsActivity.itemsArray.get(idButton);
            singleClick = item.get("getClick");
            if ("HOMEN".equals(singleClick) && "LRUN".equals(singleClick) && "FAVDOCN".equals(singleClick) && "RUN".equals(singleClick)){
                singleClick2 = item.get("addGetClick");
            }
            //
            doubleClick = item.get("getDClick");
            if ("HOMEN".equals(doubleClick) && "LRUN".equals(doubleClick) && "FAVDOCN".equals(doubleClick) && "RUN".equals(doubleClick)){
                doubleClick2 = item.get("addGetDClick");
            }
            //
            longClick = item.get("getLClick");
            if ("HOMEN".equals(longClick) && "LRUN".equals(longClick) && "FAVDOCN".equals(longClick) && "RUN".equals(longClick)){
                longClick2 = item.get("addGetLClick");
            }
    }

    private void dialogSelectClick(final int id){
        AlertDialog.Builder builder = new AlertDialog.Builder(ButtonSettingActivity.this);
        // "Select application"
        builder.setTitle(getResources().getString(R.string.pref_i_select_application));
        final String[] finalListNameJob = listNameJob;
        builder.setSingleChoiceItems(listNameJobTitle, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                // выбор программы
                switch (finalListNameJob[i]) {
                    case "RUN":
                        AlertDialog.Builder builder = new AlertDialog.Builder(ButtonSettingActivity.this);
                        // "Select application"
                        builder.setTitle(getResources().getString(R.string.pref_i_select_application));
                        builder.setSingleChoiceItems(happlications, -1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                // здесь нужно организовывать сохранение выбранного приложения
                                dialog.dismiss();
                                getSelect(id, "RUN", String.valueOf(applications[i]));
                            }
                        });
                        builder.show();
                        // выбор номера страницы фаворитов документов
                        break;
                    case "FAVDOCN": {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ButtonSettingActivity.this);
                        // "Select number"
                        builder1.setTitle(getResources().getString(R.string.jv_prefs_select_number));
                        final EditText input = new EditText(ButtonSettingActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        input.setText("1");
                        builder1.setView(input);
                        // "Ok"
                        builder1.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                        dialog.dismiss();
                                        getSelect(id, "FAVDOCN", String.valueOf(input.getText()));
                                        //adapter.notifyDataSetChanged();
                                    }
                                });
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        builder1.show();
                        // выбор номера страницы запущенных программ
                        break;
                    }
                    case "LRUN": {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ButtonSettingActivity.this);
                        // "Select number"
                        builder1.setTitle(getResources().getString(R.string.jv_prefs_select_number));
                        final EditText input = new EditText(ButtonSettingActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        input.setText("1");
                        builder1.setView(input);
                        // "Ok"
                        builder1.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                        dialog.dismiss();
                                        getSelect(id, "LRUN", String.valueOf(input.getText()));
                                        //adapter.notifyDataSetChanged();
                                    }
                                });
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        builder1.show();
                        // выбор номера домашней папки
                        break;
                    }
                    case "HOMEN": {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ButtonSettingActivity.this);
                        // "Select number"
                        builder1.setTitle(getResources().getString(R.string.jv_prefs_select_number));
                        final EditText input = new EditText(ButtonSettingActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        input.setText("1");
                        builder1.setView(input);
                        // "Ok"
                        builder1.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                        getSelect(id, "HOMEN", String.valueOf(input.getText()));
                                        //adapter.notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                });
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        builder1.show();
                        break;
                    }
                    default:
                        getSelect(id, finalListNameJob[i], "");
                        //adapter.notifyDataSetChanged();
                        break;
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
    private void getSelect(int id, String job, String namejob) {
        String textToActivity = "";
        if (job == null || job.equals("")){
            return;
        }
        // выбор программы
        switch (job) {
            case "RUN":
                for (int j = 0; j < happlications.length; j++) {
                    if (namejob.equals(applications[j])) {
                        textToActivity = happlications[j].toString();
                        break;
                    }
                }
                // выбор номера страницы фаворитов документов
                break;
            case "FAVDOCN":
                for (int j = 0; j < listNameJob.length; j++) {
                    if ("FAVDOCN".equals(listNameJob[j])) {
                        textToActivity = listNameJobTitle[j] + namejob;
                        break;
                    }
                }
                // выбор номера страницы запущенных программ
                break;
            case "LRUN":
                for (int j = 0; j < listNameJob.length; j++) {
                    if ("LRUN".equals(listNameJob[j])) {
                        textToActivity = listNameJobTitle[j] + namejob;
                        break;
                    }
                }
                // выбор номера домашней папки
                break;
            case "HOMEN":
                for (int j = 0; j < listNameJob.length; j++) {
                    if ("HOMEN".equals(listNameJob[j])) {
                        textToActivity = listNameJobTitle[j] + namejob;
                        break;
                    }
                }
                break;
            default:
                for (int j = 0; j < listNameJob.length; j++) {
                    if (job.equals(listNameJob[j])) {
                        textToActivity = listNameJobTitle[j];
                        break;
                    }
                }
                break;
        }
        TextView textClick = null;
        ImageView imageView = null;
        if (id == 0){
            textClick = (TextView) findViewById(R.id.run_single_click);
            imageView = (ImageView) findViewById(R.id.iV_Click);
            singleClick = job;
            singleClick2 = namejob;
        }
        if (id == 1){
            textClick = (TextView) findViewById(R.id.run_double_click);
            imageView = (ImageView) findViewById(R.id.iV_DClick);
            doubleClick = job;
            doubleClick2 = namejob;
        }
        if (id == 2){
            textClick = (TextView) findViewById(R.id.run_long_click);
            imageView = (ImageView) findViewById(R.id.iV_LClick);
            longClick = job;
            longClick2 = namejob;
        }

        if (textClick != null) {
            textClick.setText(textToActivity);
        }
        if (imageView != null){
            UtilIcons utilIcons = new UtilIcons(getBaseContext());
            imageView.setImageBitmap(utilIcons.getIcon(job));
        }
    }

    private void addButtonArray(){
        HashMap<String, String> item = new HashMap<>();
        item.put("getClick", singleClick);
        item.put("addGetClick", singleClick2);
        item.put("getDClick", doubleClick);
        item.put("addGetDClick", doubleClick2);
        item.put("getLClick", longClick);
        item.put("addGetLClick", longClick2);
        item.put("IDENT_ICON", "0"); // опция для будущего механизма отрисовки иконки. Пока заглушка
        item.put("ICON_POSITION", String.valueOf(iconPosition)); // позиция иконки на панели. Это то, что находится в базе.
        if (idButton == -1) {
            PanelSettingsActivity.itemsArray.add(item);
        }else{
            PanelSettingsActivity.itemsArray.set(idButton, item);
        }
    }
}