package com.harasoft.relaunch;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class NookShadowSettings extends Activity {
    SharedPreferences prefs;
    ReLaunchApp app;
    ArrayList<String> nameSetting = new ArrayList<String>();
    LayoutInflater vi;
    AppAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        app = ((ReLaunchApp) getApplicationContext());
        if(app == null ) {
            finish();
        }
        EinkScreen.setEinkController(prefs);
        app.setFullScreenIfNecessary(this);

        setContentView(R.layout.all_applications);

        vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        (findViewById(R.id.app_btn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        adapter = new AppAdapter(this, R.layout.applications_item, nameSetting);
        GridView lv = (GridView) findViewById(R.id.app_grid);
        lv.setNumColumns(1);
        lv.setAdapter(adapter);

        ReadListSettings();

        final SystemSettings systemSettings = new SystemSettings();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String item = nameSetting.get(position);
                if ("AppWidgetPickActivity".equals(item)){
                    systemSettings.RunAppWidgetPickActivity();
                }
                if ("BandMode".equals(item)){
                    systemSettings.RunBandMode();
                }
                if ("BatteryHistory".equals(item)){
                    systemSettings.RunBatteryHistory();
                }
                if ("BatteryInfo".equals(item)){
                    systemSettings.RunBatteryInfo();
                }
                if ("ChooseLockPin".equals(item)){
                    systemSettings.RunChooseLockPin();
                }
                if ("ChooseLockPinExample".equals(item)){
                    systemSettings.RunChooseLockPinExample();
                }
                if ("ChooseLockPinTutorial".equals(item)){
                    systemSettings.RunChooseLockPinTutorial();
                }
                if ("ConfirmLockPin".equals(item)){
                    systemSettings.RunConfirmLockPin();
                }
                if ("DevelopmentSettings".equals(item)){
                    systemSettings.RunDevelopmentSettings();
                }
                if ("Memory".equals(item)){
                    systemSettings.RunMemory();
                }
                if ("Display".equals(item)){
                    systemSettings.RunDisplay();
                }
                if ("PowerUsageDetail".equals(item)){
                    systemSettings.RunPowerUsageDetail();
                }
                if ("PowerUsageSummary".equals(item)){
                    systemSettings.RunPowerUsageSummary();
                }
                if ("InstalledAppDetails".equals(item)){
                    systemSettings.RunInstalledAppDetails();
                }
                if ("DebugIntentSender".equals(item)){
                    systemSettings.RunDebugIntentSender();
                }
                if ("LauncherAppWidgetBinder".equals(item)){
                    systemSettings.RunLauncherAppWidgetBinder();
                }
                if ("ManageApplications".equals(item)){
                    systemSettings.RunManageApplications();
                }
                if ("MasterClear".equals(item)){
                    systemSettings.RunMasterClear();
                }
                if ("MediaFormat".equals(item)){
                    systemSettings.RunMediaFormat();
                }
                if ("ProxySelector".equals(item)){
                    systemSettings.RunProxySelector();
                }
                if ("RadioInfo".equals(item)){
                    systemSettings.RunRadioInfo();
                }
                if ("RunningServices".equals(item)){
                    systemSettings.RunRunningServices();
                }
                if ("SdCardSettings".equals(item)){
                    systemSettings.RunSdCardSettings();
                }
                if ("Settings_About".equals(item)){
                    systemSettings.RunSettings_About();
                }
                if ("Settings_DateTime".equals(item)){
                    systemSettings.RunSettings_DateTime();
                }
            }
        });
        ScreenOrientation.set(this, prefs);


    }
    private void ReadListSettings() {
        nameSetting.add("AppWidgetPickActivity");
        nameSetting.add("BandMode");
        nameSetting.add("BatteryHistory");
        nameSetting.add("BatteryInfo");
        nameSetting.add("ChooseLockPin");
        nameSetting.add("ChooseLockPinExample");
        nameSetting.add("ChooseLockPinTutorial");
        nameSetting.add("ConfirmLockPin");
        nameSetting.add("DevelopmentSettings");
        nameSetting.add("Memory");
        nameSetting.add("Display");
        nameSetting.add("PowerUsageDetail");
        nameSetting.add("PowerUsageSummary");
        nameSetting.add("InstalledAppDetails");
        nameSetting.add("DebugIntentSender");
        nameSetting.add("LauncherAppWidgetBinder");
        nameSetting.add("ManageApplications");
        nameSetting.add("MasterClear");
        nameSetting.add("MediaFormat");
        nameSetting.add("ProxySelector");
        nameSetting.add("RadioInfo");
        nameSetting.add("RunningServices");
        nameSetting.add("SdCardSettings");
        nameSetting.add("Settings_About");
        nameSetting.add("Settings_DateTime");
        adapter.notifyDataSetChanged();
    }

    class AppAdapter extends ArrayAdapter<String> {
        AppAdapter(Context context, int resource, List<String> data) {
            super(context, resource, data);
        }

        @Override
        public int getCount() {
            return nameSetting.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View v = convertView;
            if (v == null) {
                v = vi.inflate(R.layout.applications_item,  parent, false);
                if(v == null){
                    return null;
                }
                holder = new ViewHolder();
                holder.tv = (TextView) v.findViewById(R.id.app_name);
                v.setTag(holder);
            } else
                holder = (ViewHolder) v.getTag();

            holder.tv.setText(nameSetting.get(position));
            return v;
        }
    }
    static class ViewHolder {
        TextView tv;
        //ImageView iv;
    }
}