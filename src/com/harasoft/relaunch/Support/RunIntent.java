package com.harasoft.relaunch.Support;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.harasoft.relaunch.N2DeviceInfo;
import com.harasoft.relaunch.R;
import com.harasoft.relaunch.ReLaunchApp;
import com.harasoft.relaunch.Utils.UtilAppRun;
import com.harasoft.relaunch.Utils.UtilHistory;
import com.harasoft.relaunch.Utils.UtilLastOpen;
import com.harasoft.relaunch.Viewer;

import java.io.File;
import java.util.List;

public class RunIntent {
    private Context context;
    private ReLaunchApp app;
    private boolean askIfAmbiguous;
    private SharedPreferences prefs;

    public RunIntent(Context context){
        this.context = context;
        this.app = (ReLaunchApp) context.getApplicationContext();
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.askIfAmbiguous = prefs.getBoolean("askAmbig", false);
    }

    public void launchReader(String fullFileName){
        if (askIfAmbiguous) {
            List<String> rdrs = app.readerNames(fullFileName);
            if (rdrs.size() == 1) {
                start(launchReader(rdrs.get(0), fullFileName));
            }else if (rdrs.size() > 1){
                final CharSequence[] applications = rdrs.toArray(new CharSequence[rdrs.size()]);
                final String rdr1 = fullFileName;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                // "Select application"
                builder.setTitle(context.getResources().getString( R.string.jv_relaunch_select_application));
                builder.setSingleChoiceItems(applications, -1,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                start(launchReader((String) applications[i],rdr1));
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        } else{
            start(launchReader(app.readerName(fullFileName),fullFileName));
        }
    }
    public void start(Intent i) {
        if (i != null)
            try {
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context,context.getResources().getString(R.string.jv_relaunch_activity_not_found),
                        Toast.LENGTH_LONG).show();
            }
    }

    // common utility - return intent to launch reader by reader name and full
    // file name. Null if not found
    public Intent launchReader(String name, String file) {
        String re[] = name.split(":");
        if (re.length == 2 && re[0].equals("Intent")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            if (file.endsWith(".epub") && N2DeviceInfo.EINK_NOOK){
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | 0x20000000);
            }else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            //intent.setDataAndType(Uri.parse("file:///" + Uri.encode(file.substring(1))), re[1]);
            intent.setDataAndType(Uri.fromFile(new File(file)), re[1]);
            (new UtilLastOpen(context)).addLastOpen(file, ResourceLocation.LOCAL);
            // TODO эти вызовы внешних читалок надо переписывать. Ресурс файла строго только локальная файловая система
            (new UtilHistory(context)).addToHistory(ResourceLocation.LOCAL, file, BookState.READING);
            return intent;
        } else {
            Intent i = getIntentByLabel(name);
            if (i == null) {
                // "Activity \"" + name + "\" not found!"
                app.showToast(context.getResources().getString(R.string.jv_rla_activity)
                        + " \""
                        + name
                        + "\" "
                        + context.getResources().getString(R.string.jv_rla_not_found));
            }else {
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse("file:///" + Uri.encode(file.substring(1))));
                (new UtilLastOpen(context)).addLastOpen(file, ResourceLocation.LOCAL);
                // TODO эти вызовы внешних читалок надо переписывать. Ресурс файла строго только локальная файловая система
                (new UtilHistory(context)).addToHistory(ResourceLocation.LOCAL, file, BookState.READING);
                return i;
            }
        }
        return null;
    }
    // common utility - get intent by label, null if not found
    private Intent getIntentByLabel(String label) {
        String[] labelp = label.split("%");
        if (labelp.length > 1) {
            return createIntent(labelp[0], labelp[1]);
        } else {
            return null;
        }
    }

    private Intent createIntent(String appPackage, String appActivity){
        if(appPackage != null && appActivity != null) {
            Intent intent = new Intent();
            if (appActivity.equals("universe.constellation.orion.viewer.OrionFileManagerActivity")){
                appActivity = "universe.constellation.orion.viewer.OrionViewerActivity";
            }
            intent.setComponent(new ComponentName(appPackage, appActivity));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            return intent;
        }else {
            return null;
        }
    }
    // установка приложения
    public boolean installPackage(Activity a, String full_package_path) {
        // Install application
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + full_package_path), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        a.startActivity(intent);
        return true;
    }
    // запуск внутреннего просмотровщика
    public void runInternalView(Activity a, String full_file_path) {
        Intent intent = new Intent(a, Viewer.class);

        intent.putExtra("filename", full_file_path);
        a.startActivity(intent);
    }

    // === запуск приложения
    public boolean runApp(String nameApp){
        Intent i = getIntentByLabel(nameApp);
        boolean ok;
        if (i == null) {
            // "Activity \"" + item + "\" not found!"
            app.showToast("\" " + nameApp + "\" " + context.getResources().getString(R.string.jv_allapp_not_found));
            ok = false;
        }else {
            ok = true;
            try {
                i.setAction(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                // "Activity \"" + item + "\" not found!"
                app.showToast("\" " + nameApp + "\" " + context.getResources().getString(R.string.jv_allapp_not_found));
                ok = false;
            }
            if (ok) {
                (new UtilAppRun(context)).addAppRun(nameApp);
            }
        }
        return ok;
    }
}
