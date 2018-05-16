package com.harasoft.relaunch.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by anat on 08.10.17.
 * Работа с информацией по установленным программам
 */
public class UtilApp {
    private boolean filterMyself;
    private ArrayList<Info> appInfoArrayList;

    public UtilApp(Context context, PackageManager pm) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        filterMyself = prefs.getBoolean("filterSelf", true);
        appInfoArrayList = createAppList(pm);
    }

    // Icons + Applications
    public  class Info{
        public Drawable appIcon;
        public String appName;
        public String appPackage;
        public String appActivity;
    }

    private ArrayList<Info> createAppList(PackageManager pm) {
        ArrayList<Info> appInfos = new ArrayList<>();
        Info appInfo;

        Intent componentSearchIntent = new Intent(Intent.ACTION_MAIN, null);
        componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        componentSearchIntent.setAction(Intent.ACTION_MAIN);
        List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                appInfo = new Info();
                appInfo.appPackage = ri.activityInfo.packageName;
                appInfo.appActivity = ri.activityInfo.name;
                appInfo.appName = ri.activityInfo.loadLabel(pm).toString();
                if (ri.activityInfo.icon != 0) {
                    appInfo.appIcon = ri.activityInfo.loadIcon(pm);
                } else {
                    appInfo.appIcon = ri.loadIcon(pm);
                }
                if (appInfo.appPackage != null && !(filterMyself && appInfo.appPackage.startsWith("com.harasoft.relaunch"))) {
                    appInfos.add(appInfo);
                }
            }
        }
        Collections.sort(appInfos, new Comparator<Info>() {
            public int compare(Info o1, Info o2) {
                return o1.appName.compareTo(o2.appName);
            }
        });
        return appInfos;
    }

    public ArrayList<String> getAppList(){
        ArrayList<String> itemsArray = new ArrayList<>();
        // получение имен программ
        for (Info anAppsArray : appInfoArrayList) {
            itemsArray.add(anAppsArray.appName);
        }
        return itemsArray;
    }
    public ArrayList<Info> getAppInfoArrayList() {
        return appInfoArrayList;
    }
}
