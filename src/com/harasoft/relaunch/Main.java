package com.harasoft.relaunch;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.Locale;

//import android.app.Application;

public class Main extends Activity {
	final String TAG = "Main";
	ReLaunchApp app;

    private Locale locale;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        SharedPreferences preferences;
        String lang;
        String tmp_startDir;

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        lang = preferences.getString("lang", "default");
        if (lang.equals("default")) {
        	locale = getResources().getConfiguration().locale.getDefault();}
        else {
        	locale = new Locale(lang);}
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);

        if(N2DeviceInfo.EINK_ONYX){
            tmp_startDir = preferences.getString("startDir", "/sdcard,/media/My Files");

            if(tmp_startDir.equals("/sdcard,/media/My Files")){
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("startDir","/mnt/storage");
                editor.commit();
            }

            tmp_startDir = preferences.getString("searchRoot", "/sdcard");
            if(tmp_startDir.equals("/sdcard")){
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("searchRoot","/mnt/storage");
                editor.commit();
            }
        }
        N2EpdController.n2MainActivity = this;
		
		app = ((ReLaunchApp) getApplicationContext());
		app.RestartIntent = PendingIntent.getActivity(this, 0, getIntent(),
				getIntent().getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);

		Intent intent = new Intent(Main.this, ReLaunch.class);
		intent.putExtra("home", false);
		intent.putExtra("home1", false);
		intent.putExtra("shop", false);
		intent.putExtra("library", false);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	}
	@Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);     
    }

}
