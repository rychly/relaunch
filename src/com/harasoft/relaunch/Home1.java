package com.harasoft.relaunch;

import java.util.Locale;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class Home1 extends Activity {
	final String TAG = "Home";
	ReLaunchApp app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        SharedPreferences preferences;
        Locale locale;
        String lang;

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        lang = preferences.getString("lang", "default");	
        if (lang.equals("default")) {
        	locale=getResources().getConfiguration().locale.getDefault();}
        else {
        	locale = new Locale(lang);}
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
		
        N2EpdController.n2MainActivity = this;

		app = ((ReLaunchApp) getApplicationContext());
		app.RestartIntent = PendingIntent.getActivity(this, 0, getIntent(),
				getIntent().getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);

		Intent intent = new Intent(Home1.this, ReLaunch.class);
		intent.putExtra("home", false);
		intent.putExtra("home1", true);
		intent.putExtra("shop", false);
		intent.putExtra("library", false);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	}
}
