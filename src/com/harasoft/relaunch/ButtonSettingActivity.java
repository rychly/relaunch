package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ButtonSettingActivity extends Activity {

    ReLaunchApp app;
    SharedPreferences prefs;
    String singleClick = "";
    String doubleClick = "";
    String longClick = "";
    String singleClick2 = "";
    String doubleClick2 = "";
    String longClick2 = "";
    String[] listNameJob;
    String[] listNameJobTitle;
    CharSequence[] applications;
    CharSequence[] happlications;
    int idButton = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        // Global storage
        app = ((ReLaunchApp) getApplicationContext());
        if(app == null ) {
            finish();
        }
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.button_layout_click);
        List<String> applicationsArray = app.getAppList();
        applications = applicationsArray.toArray(new CharSequence[applicationsArray.size()]);
        happlications = app.getAppList().toArray(new CharSequence[app.getAppList().size()]);
        // получаем данные
        final Intent data = getIntent();
        if(data != null && data.getExtras() != null){
            idButton = data.getExtras().getInt("buttonID");
            if(idButton != -1){
                // здесь необходимо установить текущие действия навешанные на кнопку
                ButonGetDB(idButton);
            }
        }

        // OK/Save button
        Button okBtn = (Button) findViewById(R.id.button_add_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AddButtonArray();
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

        ArrayList<String> tempvalue = new ArrayList<String>();
        Collections.addAll(tempvalue, getResources().getStringArray(R.array.arr_button_actions_values));
        listNameJob = new String[tempvalue.size()];
        listNameJob = tempvalue.toArray(listNameJob);

        tempvalue.clear();
        Collections.addAll(tempvalue, getResources().getStringArray(R.array.arr_button_actions_names));
        listNameJobTitle = new String[tempvalue.size()];
        listNameJobTitle = tempvalue.toArray(listNameJobTitle);

        Button buttonClick = (Button) findViewById(R.id.button_add_click_btn);
        buttonClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogSelectClic(0);
            }
        });

        Button buttonDClick = (Button) findViewById(R.id.button_add_double_click_btn);
        buttonDClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogSelectClic(1);
            }
        });

        Button buttonLClick = (Button) findViewById(R.id.button_add_long_click_btn);
        buttonLClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogSelectClic(2);
            }
        });

        GetSelect(0, singleClick, singleClick2);
        GetSelect(1, doubleClick, doubleClick2);
        GetSelect(2, longClick, longClick2);
    }

    private void ButonGetDB(int idButtot){
        HashMap<String, String> item = PanelSettingsActivity.itemsArray.get(idButtot);
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

    private void DialogSelectClic(final int id){
        AlertDialog.Builder builder = new AlertDialog.Builder(ButtonSettingActivity.this);
        // "Select application"
        builder.setTitle(getResources().getString(R.string.pref_i_select_application));
        final String[] finalListNameJob = listNameJob;
        builder.setSingleChoiceItems(listNameJobTitle, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                // выбор программы
                if (finalListNameJob[i].equals("RUN")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ButtonSettingActivity.this);
                    // "Select application"
                    builder.setTitle(getResources().getString(R.string.pref_i_select_application));
                    builder.setSingleChoiceItems(happlications, -1, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int i) {
                            // здесь нужно организовывать сохранение выбранного приложения
                            dialog.dismiss();
                            GetSelect(id, "RUN", String.valueOf(applications[i]));
                        }
                    });
                    builder.show();
                    // выбор номера страницы фаворитов документов
                }else if (finalListNameJob[i].equals("FAVDOCN")) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(ButtonSettingActivity.this);
                    // "Select number"
                    builder1.setTitle(getResources().getString( R.string.jv_prefs_select_number));
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
                                    GetSelect(id, "FAVDOCN", String.valueOf(input.getText()));
                                    //adapter.notifyDataSetChanged();
                                }
                            });
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    builder1.show();
                    // выбор номера страницы запущенных программ
                }else if (finalListNameJob[i].equals("LRUN")) {
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
                                    GetSelect(id, "LRUN", String.valueOf(input.getText()));
                                    //adapter.notifyDataSetChanged();
                                }
                            });
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    builder1.show();
                    // выбор номера домашней папки
                }else if (finalListNameJob[i].equals("HOMEN")) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder( ButtonSettingActivity.this);
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
                                    GetSelect(id, "HOMEN", String.valueOf(input.getText()));
                                    //adapter.notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            });
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    builder1.show();
                }else{
                    GetSelect(id, finalListNameJob[i], "");
                    //adapter.notifyDataSetChanged();
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
    private void GetSelect(int id, String job, String namejob) {
        String textToActivity = "";
        if (job == null || job.equals("")){
            return;
        }
        // выбор программы
        if (job.equals("RUN")) {
            for (int j = 0; j < happlications.length; j++) {
                if (namejob.equals(applications[j])){
                    textToActivity = happlications[j].toString();
                    break;
                }
            }
            // выбор номера страницы фаворитов документов
        }else if (job.equals("FAVDOCN")) {
            for (int j = 0; j < listNameJob.length; j++) {
                if ("FAVDOCN".equals(listNameJob[j])) {
                    textToActivity = listNameJobTitle[j] + namejob;
                    break;
                }
            }
            // выбор номера страницы запущенных программ
        }else if (job.equals("LRUN")) {
            for (int j = 0; j < listNameJob.length; j++) {
                if ("LRUN".equals(listNameJob[j])) {
                    textToActivity = listNameJobTitle[j] + namejob;
                    break;
                }
            }
            // выбор номера домашней папки
        }else if (job.equals("HOMEN")) {
            for (int j = 0; j < listNameJob.length; j++) {
                if ("HOMEN".equals(listNameJob[j])) {
                    textToActivity = listNameJobTitle[j] + namejob;
                    break;
                }
            }
        }else{
            for (int j = 0; j < listNameJob.length; j++) {
                if (job.equals(listNameJob[j])) {
                    textToActivity = listNameJobTitle[j];
                    break;
                }
            }
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
            imageView.setImageBitmap(app.JobIcon(job));
        }
    }

    private void AddButtonArray(){
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("getClick", singleClick);
        item.put("addGetClick", singleClick2);
        item.put("getDClick", doubleClick);
        item.put("addGetDClick", doubleClick2);
        item.put("getLClick", longClick);
        item.put("addGetLClick", longClick2);
        if (idButton == -1) {
            PanelSettingsActivity.itemsArray.add(item);
        }else{
            PanelSettingsActivity.itemsArray.set(idButton, item);
        }
    }
}