package com.harasoft.relaunch.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.harasoft.relaunch.Adapter.ViewItem;
import com.harasoft.relaunch.Support.TypeResource;
import com.harasoft.relaunch.LocalFile.LocalFile;
import com.harasoft.relaunch.R;
import com.harasoft.relaunch.ReLaunchApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UtilIcons {
    private Context context;
    private ReLaunchApp app;
    private int iconSizePx;
    private int DEF_ICON_SIZE = 48;
    private int iconCorrectPix;
    private int NOOK_DPI = 160;
    private float coef;
    private ArrayList<imageIcon> arrIcon = new ArrayList<>();
    private ArrayList<imageIcon> arrIconProg = new ArrayList<>();

    public UtilIcons(Context context){
        this.context = context;
        this.app = (ReLaunchApp) context.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.iconSizePx = Integer.parseInt(prefs.getString("firstLineIconSizePx", "48"));
        this.iconCorrectPix = iconSizePx - DEF_ICON_SIZE;
        this.coef = getCorrectCoef();
        loadStandartIcons();
        loadProgramIcons();
    }

    private int numButtonIcon(String button_name){
        int iconID;
        switch (button_name) {
            case "FAVDOCN":// страница фаворитов документов
            case "FAVDOCMENU":// всплывающее меню фаворитов документов
            case "FAVDOCSCREEN": // экран фаворитов документов
                iconID = R.drawable.ci_fav;
            break;
            case "LRUN":// страница запущенных ДОКУМЕНТОВ
            case "LRUMENU":// всплывающее меню запущенных документов
            case "LRUSCREEN":// экран запущенных документов
                iconID = R.drawable.ci_lre;
                break;
            case "HOMEN":// страница домашних папок
            case "HOMEMENU":// всплывающее меню домашних папок
            case "HOMESCREEN":// экран домашних папок
                iconID = R.drawable.ci_home;
                break;
            case "ADVANCED":// расширенные настройки
                iconID = R.drawable.ci_tools;
                break;
            case "SETTINGS":// настройки
                iconID = R.drawable.ci_settings;
                break;
            case "APPMANAGER":// все приложения
                iconID = R.drawable.ci_cpu;
                break;
            case "BATTERY":// показ расхода по приложениям
                iconID = R.drawable.bat1_big;
                break;
            case "FAVAPP":// всплывающее меню фаворитов приложений
                iconID = R.drawable.ci_fava;
                break;
            case "ALLAPP":// все приложения
                iconID = R.drawable.ci_grid;
                break;
            case "LASTAPP":// последние запущенные приложения
                iconID = R.drawable.ci_lrea;
                break;
            case "SEARCH":// поиск
                iconID = R.drawable.ci_search;
                break;
            case "LOCK":// блокировка устройства
                iconID = R.drawable.ci_lock;
                break;
            case "POWEROFF":// выключение устройства
                iconID = R.drawable.ci_power;
                break;
            case "REBOOT":// перезагрузка устройства
                iconID = R.drawable.ci_reboot;
                break;
            case "SWITCHWIFI":// состояние WiFi
                WifiManager wifiManager;
                wifiManager = (WifiManager) (context.getApplicationContext()).getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {
                    iconID = R.drawable.ci_wifi_on;
                }else{
                    iconID = R.drawable.ci_wifi_off;
                }
                break;
            case "WIFION":// WiFi on
                iconID = R.drawable.ci_wifi_on;
                break;
            case "WIFIOFF":// WiFi off
                iconID = R.drawable.ci_wifi_off;
                break;
            case "WIFIONMINI":// WiFi on
                iconID = R.drawable.wifi_on;
                break;
            case "WIFIOFFMINI":// WiFi off
                iconID = R.drawable.wifi_off;
                break;
            case "DROPBOX":// запуск Dropbox
                iconID = R.drawable.ci_dropbox;
                break;
            case "OPDS":// запуск OPDS
                iconID = R.drawable.ci_books;
                break;
            case "SYSSETTINGS":// системные настройки
                iconID = R.drawable.ci_gear2;
                break;
            case "UPDIR":// в родительскую папку
                iconID = R.drawable.ci_levelup_big;
                break;
            case "LEVELUPON":// в родительскую папку
                iconID = R.drawable.ci_levelup;
                break;
            case "LEVELUPOFF":// в родительскую папку
                iconID = R.drawable.ci_levelup_gray;
                break;
            case "UPSCROLL":// пролистывание на экран вверх
                iconID = R.drawable.ci_arrowup_big;
                break;
            case "UPSCROLLPERC":// пролистывание на экран вверх
                iconID = R.drawable.ci_arrowup_big_perc;
                break;
            case "UPSCROLLBEGIN":// пролистывание на экран вверх
                iconID = R.drawable.ci_arrowup_big_begin;
                break;
            case "UPENABLE":// пролистывае на экран вверх. маленькая стрелка
                iconID = R.drawable.ci_arrowup;
                break;
            case "UPDISABLE":// пролистывае на экран вверх. маленькая стрелка, недоступно
                iconID = R.drawable.ci_arrowup_gray;
                break;
            case "DOWNSCROLL":// пролистывае на экран вниз
                iconID = R.drawable.ci_arrowdown_big;
                break;
            case "DOWNSCROLLPERC":// пролистывае на экран вниз
                iconID = R.drawable.ci_arrowdown_big_perc;
                break;
            case "DOWNSCROLLEND":// пролистывае на экран вниз
                iconID = R.drawable.ci_arrowdown_big_end;
                break;
            case "DOWNENABLE":// пролистывае на экран вниз. маленькая стрелка
                iconID = R.drawable.ci_arrowdown;
                break;
            case "DOWNDISABLE":// пролистывае на экран вниз. маленькая стрелка, недоступно
                iconID = R.drawable.ci_arrowdown_gray;
                break;
            case "SORTMENU":// сортировка
                iconID = R.drawable.ci_az;
                break;
            case "SELNUMCOL":// фильтр
                iconID = R.drawable.ci_filter;
                break;
            case "EXIT":// фильтр
                iconID = R.drawable.ci_exit;
                break;
            case "ASSOCIATIONS":// ассоциации
                iconID = R.drawable.ci_associations;
                break;
            case "DELETE":// удаление
                iconID = R.drawable.ci_del;
                break;
            case "DELETESMALL":// удаление
                iconID = R.drawable.ci_delsmall;
                break;
            case "EDIT":// редактирование
                iconID = R.drawable.ci_edit;
                break;
            case "KILL":// редактирование
                iconID = R.drawable.ci_kill;
                break;
            case "OK":// редактирование
                iconID = R.drawable.ci_ok;
                break;
            case "RUN":// редактирование
                iconID = R.drawable.ci_run;
                break;
            case "SAVE":// редактирование
                iconID = R.drawable.ci_save;
                break;
            case "WIFISCANON":// редактирование
                iconID = R.drawable.ci_wifiscan;
                break;
            case "WIFISCANOFF":// редактирование
                iconID = R.drawable.ci_wifiscan_gray;
                break;
            case "WIFISETUP":// редактирование
                iconID = R.drawable.ci_wifisetup;
                break;
            case "CHIP":// редактирование
                iconID = R.drawable.ram;
                break;
            case "PANEL":// редактирование
                iconID = R.drawable.ci_panel;
                break;
            case "ADD":// редактирование
                iconID = R.drawable.ci_add;
                break;
            case "BOOKINFO":// редактирование
                iconID = R.drawable.ci_bookinfo;
                break;
            default:// в остальных случаях стандартная иконка
                iconID = -1;
                //iconID = R.drawable.file_notok;
                break;
        }

        return iconID;
    }

    private Bitmap scaleDrawableById(int id, int icon_corr_size, float coef_corr) {
        Bitmap temp_bitmap = BitmapFactory.decodeResource(context.getResources(), id);
        int x_size = temp_bitmap.getHeight();
        int new_size = (int)(x_size*coef_corr + icon_corr_size);
        return Bitmap.createScaledBitmap(temp_bitmap, new_size, new_size,true);
    }

    private Bitmap scaleDrawable(Drawable d, int icon_corr_size, float coef_corr) {
        Bitmap temp_bitmap = ((BitmapDrawable) d).getBitmap();
        int x_size = temp_bitmap.getHeight();
        int new_size = (int)(x_size*coef_corr + icon_corr_size);
        return Bitmap.createScaledBitmap(temp_bitmap, new_size, new_size,true);
    }

    private void loadStandartIcons() {
        // иконки для файлов заранее заносим в массив========================
        imageIcon temp_icon = new imageIcon();
        temp_icon.icon = scaleDrawableById(R.drawable.dir_ok, iconCorrectPix, coef);
        temp_icon.nameIcon = "dir_ok";
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.icon = scaleDrawableById(R.drawable.file_ok, iconCorrectPix, coef);
        temp_icon.nameIcon = "file_ok";
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.icon = scaleDrawableById(R.drawable.file_notok, iconCorrectPix, coef);
        temp_icon.nameIcon = "file_notok";
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.icon = scaleDrawableById(R.drawable.icon_list, iconCorrectPix, coef);
        temp_icon.nameIcon = "icon";
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.icon = scaleDrawableById(R.drawable.install, iconCorrectPix, coef);
        temp_icon.nameIcon = "install";
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.nameIcon = "opds_catalog";
        temp_icon.icon = scaleDrawableById(R.drawable.opds_catalog, iconCorrectPix, coef);
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.nameIcon = "parent_ok";
        temp_icon.icon = scaleDrawableById(R.drawable.ci_levelup_big, iconCorrectPix, coef);
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.nameIcon = "parent_off";
        temp_icon.icon = scaleDrawableById(R.drawable.ci_levelup_big_gray, iconCorrectPix, coef);
        arrIcon.add(temp_icon);
        //===== иконки всех программ =====
        Drawable d = null;
        Intent componentSearchIntent = new Intent();
        componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        componentSearchIntent.setAction(Intent.ACTION_MAIN);
        PackageManager pm = context.getPackageManager();
        if(pm == null){
            return;
        }
        List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);
        String pname;
        String aname;
        String hname = "";
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                temp_icon = new imageIcon();
                pname = ri.activityInfo.packageName;
                aname = ri.activityInfo.name;
                try {
                    if (ri.activityInfo.labelRes != 0) {
                        hname = (String) ri.activityInfo.loadLabel(pm);
                    } else {
                        hname = (String) ri.loadLabel(pm);
                    }
                    if (ri.activityInfo.icon != 0) {
                        d = ri.activityInfo.loadIcon(pm);
                    } else {
                        d = ri.loadIcon(pm);
                    }
                } catch (Exception e) {
                    // emply
                }
                if (d != null) {
                    temp_icon.icon = scaleDrawable(d,  iconCorrectPix, coef);
                }else{
                    temp_icon.icon = scaleDrawableById(R.drawable.file_notok,  iconCorrectPix, coef);
                }
                temp_icon.nameIcon = pname + "%" + aname + "%" + hname;
                arrIcon.add(temp_icon);
            }
        }
    }
    private void loadProgramIcons() {
        imageIcon temp_icon;
        //===== иконки всех программ =====
        Drawable d = null;
        Intent componentSearchIntent = new Intent();
        componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        componentSearchIntent.setAction(Intent.ACTION_MAIN);
        PackageManager pm = context.getPackageManager();
        if(pm == null){
            return;
        }
        List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);
        String pname;
        String aname;
        String hname = "";
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                temp_icon = new imageIcon();
                pname = ri.activityInfo.packageName;
                aname = ri.activityInfo.name;
                try {
                    if (ri.activityInfo.labelRes != 0) {
                        hname = (String) ri.activityInfo.loadLabel(pm);
                    } else {
                        hname = (String) ri.loadLabel(pm);
                    }
                    if (ri.activityInfo.icon != 0) {
                        d = ri.activityInfo.loadIcon(pm);
                    } else {
                        d = ri.loadIcon(pm);
                    }
                } catch (Exception e) {
                    // emply
                }
                if (d != null) {
                    temp_icon.icon = scaleDrawable(d,  iconCorrectPix, coef);
                }else{
                    temp_icon.icon = scaleDrawableById(R.drawable.file_notok,  iconCorrectPix, coef);
                }
                temp_icon.nameIcon = pname + "%" + aname + "%" + hname;
                arrIconProg.add(temp_icon);
            }
        }
    }
    private class imageIcon {
        String nameIcon;
        Bitmap icon;
    }
    public Bitmap getIcon(String title_button) {
        int icon_id = numButtonIcon(title_button);
        if (icon_id == -1) {
            for (imageIcon anArrIconProg : arrIconProg) {
                if (anArrIconProg.nameIcon.equals(title_button)) {
                    return anArrIconProg.icon;
                }
            }
        }
        // если такой иконки нет, то подставляем стандартную
        if (icon_id == -1) {
            return getIconFile("file_notok");
        }
        return scaleDrawableById(icon_id,  iconCorrectPix, coef);
    }

    public Bitmap getIconFile(String name_icon) {
        int icon_id = getFileIconID(name_icon);
        return arrIcon.get(icon_id).icon;
    }
    private int getFileIconID(String name_icon) {
        int icon_id = -1;
        for (int z = 0, size = arrIcon.size(); z < size; z++) {
            if (arrIcon.get(z).nameIcon.equals(name_icon)) {
                icon_id = z;
                break;
            }
        }
        if (icon_id == -1) {
            for (int z = 0, size = arrIconProg.size(); z < size; z++) {
                if (arrIconProg.get(z).nameIcon.equals(name_icon)) {
                    icon_id = z;
                    break;
                }
            }
        }
        // если такой иконки нет, то подставляем стандартную
        if (icon_id == -1) {
            icon_id = getFileIconID("file_notok");
        }
        return icon_id;
    }

    private int getScreenDPI(){
        DisplayMetrics metrics = new DisplayMetrics();

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        return metrics.densityDpi;
    }

    private float getCorrectCoef(){
        return (float)NOOK_DPI/getScreenDPI();
    }

    public String getNameIcon(HashMap<String, String> item, String list_name) {
        String name_icon;
        if (iconSizePx == 0) {  // если иконки запрещены
            name_icon = "none";
        }else {
            switch (list_name) {
                case "opdslist":
                    name_icon = "opds_catalog";
                    break;
                case "homeList":
                    name_icon = "dir_ok";
                    break;
                default:
                    int type = Integer.parseInt(item.get("type"));
                    if (type == TypeResource.DIR) {
                        name_icon = "dir_ok";
                    }else {
                        String name_file = item.get("firstLine");
                        if (name_file.endsWith(".apk")) { // если установочный пакет
                            name_icon = "install";
                        } else {  // иначе
                            String rdr_name = app.readerName(name_file); // в поле реадера читаем обработчик

                            if (rdr_name.startsWith("Intent:")) { // если ответ начинается с ...
                                name_icon = "icon";
                            } else if (rdr_name.equals("Nope")) { // если не известен
                                String full_file_name;
                                if (item.get("secondLine").equals("/")){
                                    full_file_name = item.get("secondLine") + item.get("firstLine");
                                }else {
                                    full_file_name = item.get("secondLine") + "/" + item.get("firstLine");
                                }

                                long file_size = (new LocalFile(context)).getFileSize(full_file_name);
                                if (file_size > app.viewerMax * 1024) {  // больше определенного размера
                                    name_icon = "file_notok";
                                } else {  // иначе
                                    name_icon = "file_ok";
                                }
                            } else {
                                name_icon = rdr_name;
                            }
                        }
                    }
            }
        }

        return name_icon;
    }

    public String getNameIcon(ViewItem item, String list_name) {
        String name_icon;
        if (iconSizePx == 0) {  // если иконки запрещены
            name_icon = "none";
        }else {
            switch (list_name) {
                case "opdslist":
                    name_icon = "opds_catalog";
                    break;
                case "homeList":
                    name_icon = "dir_ok";
                    break;
                default:
                    int type = item.getFile_type();
                    if (type == TypeResource.DIR) {
                        name_icon = "dir_ok";
                    }else {
                        String name_file = item.getFile_name();
                        if (name_file.endsWith(".apk")) { // если установочный пакет
                            name_icon = "install";
                        } else {  // иначе
                            String rdr_name = app.readerName(name_file); // в поле реадера читаем обработчик

                            if (rdr_name.startsWith("Intent:")) { // если ответ начинается с ...
                                name_icon = "icon";
                            } else if (rdr_name.equals("Nope")) { // если не известен
                                String full_file_name = item.getFile_path() + name_file;

                                long file_size = (new LocalFile(context)).getFileSize(full_file_name);
                                if (file_size > app.viewerMax * 1024) {  // больше определенного размера
                                    name_icon = "file_notok";
                                } else {  // иначе
                                    name_icon = "file_ok";
                                }
                            } else {
                                name_icon = rdr_name;
                            }
                        }
                    }
            }
        }

        return name_icon;
    }

    //===== получение иконки по имени программы =====
    public Bitmap BitmapIconForButton(String nameApp) {
        // все иконки программ уже отмасштабированы и сложены в массив
        // потому просто проверяем нахождение этой иконки там
        int icon_num = getFileIconID(nameApp);
        // если такой иконки нет, то подставляем стандартную
        if (icon_num == -1) {
            icon_num = getFileIconID("file_notok");
        }
        // по номеру в массиве возвращаем иконку
        return arrIcon.get(icon_num).icon;
    }

    public int getIconSize() {
        return (int) (48 * coef + iconCorrectPix);
    }
}
