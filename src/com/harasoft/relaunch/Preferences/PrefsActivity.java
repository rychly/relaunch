package com.harasoft.relaunch.Preferences;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.*;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import com.harasoft.relaunch.*;
import com.harasoft.relaunch.LocalFile.LocalFile;
import com.harasoft.relaunch.Utils.*;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class PrefsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	private final Context context = this;
	private final String TAG = "PreferenceActivity";
	private final static int TYPES_ACT = 1;
	private final static int FILTS_ACT = 2;
	private static String BACKUP_DIR;

	private ReLaunchApp app;
    public static boolean baseChange = false;
	private String DATA_DIR;

	private SharedPreferences prefs;
	private boolean do_pref_subrequest = true;
	private Map<String, ?> oldPrefs;
	private Map<String, ?> newPrefs;

	//Clear shared preferences
	private void resetPreferences() {
		do_pref_subrequest = false;
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		editor.commit();
		do_pref_subrequest = true;
	}

	//Undoes the preference changes
	private void cancel() {
		do_pref_subrequest = false;
		SharedPreferences.Editor editor = prefs.edit();
		Set<String> keys = oldPrefs.keySet();
		for (String key : keys) {
			Object value = oldPrefs.get(key);
			if (value instanceof Boolean) {
				editor.putBoolean(key, (Boolean) value);
			} else if (value instanceof Float) {
				editor.putFloat(key, (Float) value);
			} else if (value instanceof Integer) {
				editor.putInt(key, (Integer) value);
			} else if (value instanceof Long) {
				editor.putLong(key, (Long) value);
			} else if (value instanceof String) {
				editor.putString(key, (String) value);
			}
		}
		editor.commit();
		do_pref_subrequest = true;
	}

	private void updatePrefSummary(Preference p) {
		if (p instanceof ListPreference) {
			ListPreference listPref = (ListPreference) p;
            if(listPref.getValue().equals("OPENN")){
                p.setSummary(listPref.getEntry() + prefs.getString(p.getKey()+"openN", "1"));
            }else if(listPref.getValue().equals("RUN")){

                String[] appa = prefs.getString(p.getKey()+"app", "%%").split("%");

                if(appa.length > 2 && appa[2] != null && appa[2].length() > 0){
                    p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
                }

            }else{
                p.setSummary(listPref.getEntry());
            }

		}
		// For future - even more intellectual Settings
		// if (p instanceof EditTextPreference) {
		// EditTextPreference editTextPref = (EditTextPreference) p;
		// p.setSummary(editTextPref.getText());
		// }
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory pCat = (PreferenceCategory) p;
			for (int i = 0; i < pCat.getPreferenceCount(); i++) {
				initSummary(pCat.getPreference(i));
			}
		} else if (p instanceof PreferenceScreen) {
			PreferenceScreen pScr = (PreferenceScreen) p;
			for (int i = 0; i < pScr.getPreferenceCount(); i++) {
				initSummary(pScr.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		app = ((ReLaunchApp) getApplicationContext());
        if(app == null) {
            finish();
        }
		app.setOptionsWindowActivity(this);
		File filesDir = app.getFilesDir();
		if(filesDir != null){
			DATA_DIR = filesDir.getParent();
		}else{
			DATA_DIR = "/data/data/com.harasoft.relaunch";
		}
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		BACKUP_DIR = prefs.getString("backupDir", "/sdcard/.relaunch");
		setContentView(R.layout.layout_prefs);
		PreferenceScreen prefAdvancedScreen = (PreferenceScreen) findPreference("screenAdvanced");
        if(prefAdvancedScreen == null){
            finish();
        }
		prefAdvancedScreen.setOnPreferenceClickListener(prefScreenListener);

		for (int i = 0; i < prefAdvancedScreen.getPreferenceCount(); i++) {
			Preference p = prefAdvancedScreen.getPreference(i);
			if (p instanceof PreferenceScreen){
				p.setOnPreferenceClickListener(prefScreenListener);
            }
		}

        //---------------------

		// Save items value
//		setDefaults();

		oldPrefs = prefs.getAll();

		findViewById(R.id.LLbuttons).setVisibility(View.GONE);

		findPreference("cleanupDatabaseBooks").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_cleanup_database_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_cleanup_database_text));
						builder.setPositiveButton(
								getResources().getString(R.string.app_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										(new UtilHistory(getBaseContext())).resetDb();
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(
								getResources().getString(
										R.string.app_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});
        findPreference("cleanupDatabaseOPDS").setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference pref) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                PrefsActivity.this);
                        builder.setTitle(getResources().getString(
                                R.string.jv_prefs_cleanup_database_title));
                        builder.setMessage(getResources().getString(
                                R.string.jv_prefs_cleanup_databaseOPDS_text));
                        builder.setPositiveButton(
                                getResources().getString(R.string.app_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
										(new UtilOPDS(getBaseContext())).resetDb();
                                        dialog.dismiss();
                                    }
                                });
                        builder.setNegativeButton(
                                getResources().getString(
                                        R.string.app_no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();
                        return true;
                    }
                });
		findPreference("cleanupDatabasePANELS").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_cleanup_database_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_cleanup_databasePANELS_text));
						builder.setPositiveButton(
								getResources().getString(R.string.app_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										(new UtilPanels(getBaseContext())).resetDb();
										PrefsActivity.baseChange = true;
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(
								getResources().getString(
										R.string.app_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});
		findPreference("cleanupDatabaseAppFavorites").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_cleanup_database_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_cleanup_databaseAppFavorites_text));
						builder.setPositiveButton(
								getResources().getString(R.string.app_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										(new UtilAppFav(getBaseContext())).resetDb();
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(
								getResources().getString(
										R.string.app_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});
		findPreference("cleanupDatabaseAppRun").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_cleanup_database_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_cleanup_databaseAppLast_text));
						builder.setPositiveButton(
								getResources().getString(R.string.app_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										(new UtilAppRun(getBaseContext())).resetDb();
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(
								getResources().getString(
										R.string.app_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});
		findPreference("cleanupDatabaseColumns").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_cleanup_database_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_cleanup_databaseColumns_text));
						builder.setPositiveButton(
								getResources().getString(R.string.app_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										(new UtilColumns(getBaseContext())).resetDb();
										PrefsActivity.baseChange = true;
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(
								getResources().getString(
										R.string.app_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});
		findPreference("cleanupDatabaseFavorites").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_cleanup_database_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_cleanup_databaseFavorites_text));
						builder.setPositiveButton(
								getResources().getString(R.string.app_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										(new UtilFavorites(getBaseContext())).resetDb();
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(
								getResources().getString(
										R.string.app_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});
		findPreference("cleanupDatabaseLastRun").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_cleanup_database_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_cleanup_databaseLastRun_text));
						builder.setPositiveButton(
								getResources().getString(R.string.app_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										(new UtilFavorites(getBaseContext())).resetDb();
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(
								getResources().getString(
										R.string.app_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});
        findPreference("deleteDatabaseAll").setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference pref) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                PrefsActivity.this);
                        builder.setTitle(getResources().getString(
                                R.string.jv_prefs_cleanup_database_title));
                        builder.setMessage(getResources().getString(
                                R.string.jv_prefs_delete_databaseALL_text));
                        builder.setPositiveButton(
                                getResources().getString(R.string.app_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        String[] DBlist = databaseList();
                                        File DBf;
										for (String aDBlist : DBlist) {
											DBf = getDatabasePath(aDBlist);
											DBf.delete();
										}
                                        dialog.dismiss();
                                    }
                                });
                        builder.setNegativeButton(
                                getResources().getString(
                                        R.string.app_no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();
                        return true;
                    }
                });

		findPreference("fileFilter").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						Intent intent = new Intent(PrefsActivity.this, FiltersActivity.class);
						startActivityForResult(intent, FILTS_ACT);
						return true;
					}
				});

		findPreference("fileAssociations").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						Intent intent = new Intent(PrefsActivity.this, TypesActivity.class);
						startActivityForResult(intent, TYPES_ACT);
						return true;
					}
				});
        findPreference("screenManualPanel").setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference pref) {
                        Intent intent = new Intent(PrefsActivity.this, ScreenSettingActivity.class);
                        startActivityForResult(intent, TYPES_ACT);
                        return true;
                    }
                });

		findPreference("infoDevice").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						// "Default settings warning"
						builder.setTitle(getResources().getString(R.string.jv_prefs_info_device_title));
						// "Are you sure to restore default settings?"
						// организуем текст для вывода
						String str = "";
						str += "MANUFACTURER: ";
						str += N2DeviceInfo.MANUFACTURER;
						str += "\nMODEL: ";
						str += N2DeviceInfo.MODEL;
						str += "\nDEVICE: ";
						str += N2DeviceInfo.DEVICE;
						str += "\nPRODUCT: ";
						str += N2DeviceInfo.PRODUCT;
						str += "\nVERSION: ";
						str += N2DeviceInfo.getName();
						// screen size in pixels
						DisplayMetrics dm = new DisplayMetrics();
						getWindowManager().getDefaultDisplay().getMetrics(dm);
						str += "\nSCREEN: " + dm.widthPixels +" x " + dm.heightPixels;
						str += "\nDPI: ";
						str += dm.densityDpi;
						builder.setMessage(str);
						// "Yes"
						builder.setPositiveButton(
								getResources().getString(R.string.app_ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});

		findPreference("resetSettings").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						// "Default settings warning"
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_default_settings_title));
						// "Are you sure to restore default settings?"
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_default_settings_text));
						// "Yes"
						builder.setPositiveButton(
								getResources().getString(R.string.app_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										resetPreferences();
										AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
										mgr.set(AlarmManager.RTC,
												System.currentTimeMillis() + 500,
												app.RestartIntent);
										System.exit(0);
									}
								});
						// "No"
						builder.setNegativeButton(
								getResources().getString(R.string.app_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});
		findPreference("saveSettings").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						boolean ret = (new LocalFile(context)).copyPrefs(DATA_DIR, BACKUP_DIR);
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						if (ret) {
							builder.setTitle(getResources().getString(
									R.string.jv_prefs_rsr_ok_title));
							builder.setMessage(getResources().getString(
									R.string.jv_prefs_rsr_ok_text));
						} else {
							builder.setTitle(getResources().getString(
									R.string.jv_prefs_rsr_fail_title));
							builder.setMessage(getResources().getString(
									R.string.jv_prefs_rsr_fail_text));
						}
						builder.setNeutralButton(
								getResources().getString(R.string.app_ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});

		findPreference("loadSettings").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						boolean ret = (new LocalFile(context)).copyPrefs(BACKUP_DIR,	DATA_DIR);
						AlertDialog.Builder builder = new AlertDialog.Builder(
								context);
						if (ret) {
							builder.setTitle(getResources().getString(
									R.string.jv_prefs_rsr_ok_title));
							builder.setMessage(getResources().getString(
									R.string.jv_prefs_rsr_okrst_text));
						} else {
							builder.setTitle(getResources().getString(
									R.string.jv_prefs_rsr_fail_title));
							builder.setMessage(getResources().getString(
									R.string.jv_prefs_rsr_fail_text));
						}
						builder.setNeutralButton(
								getResources().getString(R.string.app_ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
										AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
										mgr.set(AlarmManager.RTC,
												System.currentTimeMillis() + 500,
												app.RestartIntent);
										System.exit(0);
									}
								});
						builder.show();
						return true;
					}
				});

		findPreference("restart").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
						mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, app.RestartIntent);
						System.exit(0);
						return true;
					}
				});

		// final Activity pact = this;
		UtilIcons utilIcons = new UtilIcons(getBaseContext());
		// Icon
		ImageView prefs_icon = (ImageView) findViewById(R.id.prefs_icon);
		prefs_icon.setImageBitmap(utilIcons.getIcon("SETTINGS"));
		// back button - work as cancel
		ImageButton back_btn = (ImageButton) findViewById(R.id.pref_back_btn);
		back_btn.setImageBitmap(utilIcons.getIcon("EXIT"));
		back_btn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (isPreferencesChanged()) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									context);
							// "Decline changes warning"
							builder.setTitle(getResources().getString(
									R.string.jv_prefs_decline_changes_title));
							// "Are you sure to decline changes?"
							builder.setMessage(getResources().getString(
									R.string.jv_prefs_decline_changes_text));
							// "Yes"
							builder.setPositiveButton(
									getResources().getString(
											R.string.app_yes),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
											mgr.set(AlarmManager.RTC,
													System.currentTimeMillis() + 500,
													app.RestartIntent);
											System.exit(0);
										}
									});
							// "No"
							builder.setNegativeButton(
									getResources().getString(
											R.string.app_cancel),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											dialog.dismiss();
										}
									});
							// "Cancel"
							builder.setNeutralButton(
									getResources().getString(
											R.string.app_no),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											dialog.dismiss();
											cancel();
											finish();
										}
									});
							builder.show();
						} else {
							finish();
						}
					}
				});

		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		app.generalOnResume(TAG);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if (resultCode == Activity.RESULT_OK) {
		// settings_changed = true;
		// }
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode) {
		case TYPES_ACT:
			String newTypes = ReLaunch.createReadersString(app.getReaders());
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("types", newTypes);
			editor.commit();
			break;
		default:
        }
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,final String key) {

		final Preference pref = findPreference(key);
		// update summary
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}

		if (do_pref_subrequest) {
			if (key.equals("workMode")) {
				String value = sharedPreferences.getString(key, "UNKNOWN");
				if (value.equals("FILES")) {
					do_pref_subrequest = false;
					((CheckBoxPreference) findPreference("showBookTitles")).setChecked(false);
					((CheckBoxPreference) findPreference("showRowSeparator")).setChecked(false);
					((CheckBoxPreference) findPreference("useFileManagerFunctions")).setChecked(true);
					((CheckBoxPreference) findPreference("openWith")).setChecked(true);
					((CheckBoxPreference) findPreference("showFullDirPath")).setChecked(true);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("sortMode", 0);
					editor.putBoolean("showBookTitles", false);
					editor.putBoolean("showRowSeparator", false);
					editor.putBoolean("useFileManagerFunctions", true);
					editor.putBoolean("openWith", true);
					editor.putBoolean("showFullDirPath", true);
					editor.commit();
					do_pref_subrequest = true;
				} else if (value.equals("BOOKS")) {
					do_pref_subrequest = false;
					((CheckBoxPreference) findPreference("showBookTitles")).setChecked(true);
					((CheckBoxPreference) findPreference("showRowSeparator")).setChecked(true);
					((CheckBoxPreference) findPreference("useFileManagerFunctions")).setChecked(false);
					((CheckBoxPreference) findPreference("openWith")).setChecked(false);
					((CheckBoxPreference) findPreference("showFullDirPath")).setChecked(false);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("sortMode", 2);
					editor.putBoolean("showBookTitles", true);
					editor.putBoolean("showRowSeparator", true);
					editor.putBoolean("useFileManagerFunctions", false);
					editor.putBoolean("openWith", false);
					editor.putBoolean("showFullDirPath", false);
					editor.commit();
					do_pref_subrequest = true;
				}
			} else if (key.equals("screenUpdateMode")) {
				boolean value = sharedPreferences.getBoolean(key, true);
				if (value) {
					((ListPreference) findPreference("einkUpdateMode")).setValueIndex(2);
				} else {
					((ListPreference) findPreference("einkUpdateMode")).setValueIndex(1);
				}
			} else if (key.equals("fileFontSize")) {
				do_pref_subrequest = false;
				String sValue = sharedPreferences.getString("fileFontSize", "20");
				int f1Size = Integer.valueOf(sValue);
				int f2Size = f1Size*4/5;
				((EditTextPreference) findPreference("firstLineFontSizePx")).setText(((Integer) f1Size).toString());
				((EditTextPreference) findPreference("secondLineFontSizePx")).setText(((Integer) f2Size).toString());
				do_pref_subrequest = true;
			}
			if ((key.equals("showBookTitles")) || (key.equals("useFileManagerFunctions"))) {
				do_pref_subrequest = false;
				((ListPreference) findPreference("workMode")).setValueIndex(2);
				do_pref_subrequest = true;
			}
		}
	}

	private Preference.OnPreferenceClickListener prefScreenListener = new Preference.OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference pref) {
            final PreferenceScreen prefScreen = (PreferenceScreen) pref;

            prefScreen.getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

			LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View prefView = inflater.inflate(R.layout.layout_prefs, null);

			prefScreen.getDialog().setContentView(prefView);

			final ListView prefListView = (ListView) prefView.findViewById(android.R.id.list);
			prefScreen.bind(prefListView);
			UtilIcons utilIcons = new UtilIcons(getBaseContext());
			// Icon
			ImageView prefs_icon = (ImageView) findViewById(R.id.prefs_icon);
			prefs_icon.setImageBitmap(utilIcons.getIcon("SETTINGS"));
			//-------------------------------------------------
			TextView tEdit = (TextView) prefView.findViewById(R.id.prefernces_title);
			tEdit.setText(pref.getTitle());

			ImageButton back_btn = (ImageButton) prefView.findViewById(R.id.pref_back_btn);
			back_btn.setImageBitmap(utilIcons.getIcon("EXIT"));
			back_btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					prefScreen.getDialog().dismiss();
				}
			});

			ImageButton btn_scrollup = (ImageButton) prefView.findViewById(R.id.btn_scrollup);
			btn_scrollup.setImageBitmap(utilIcons.getIcon("UPENABLE"));
			btn_scrollup.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					int first = prefListView.getFirstVisiblePosition();
					int last = prefListView.getLastVisiblePosition();
					int visible = last - first + 1;
					first -= visible;
					if (first < 0) {
						first = 0;
					}
					if (N2DeviceInfo.EINK_NOOK) {
						MotionEvent ev;
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis(),
								MotionEvent.ACTION_DOWN, 200, 100, 0);
						prefListView.dispatchTouchEvent(ev);
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis() + 100,
								MotionEvent.ACTION_MOVE, 200, 200, 0);
						prefListView.dispatchTouchEvent(ev);
						SystemClock.sleep(100);
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis(),
								MotionEvent.ACTION_UP, 200, 200, 0);
						prefListView.dispatchTouchEvent(ev);
					} else {
						final int finfirst = first;
						prefListView.clearFocus();
						prefListView.post(new Runnable() {

							public void run() {
								prefListView.setSelection(finfirst);
							}
						});
					}



				}
			});

			ImageButton btn_scrolldown = (ImageButton) prefView.findViewById(R.id.btn_scrolldown);
			btn_scrolldown.setImageBitmap(utilIcons.getIcon("DOWNENABLE"));
			btn_scrolldown.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					int total = prefListView.getCount();
					int last = prefListView.getLastVisiblePosition();
					int target = last + 1;
					if (target > (total - 1)){
						target = total - 1;
					}
					if (N2DeviceInfo.EINK_NOOK) {
						MotionEvent ev;
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis(),
								MotionEvent.ACTION_DOWN, 200, 200, 0);
						prefListView.dispatchTouchEvent(ev);
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis() + 100,
								MotionEvent.ACTION_MOVE, 200, 100, 0);
						prefListView.dispatchTouchEvent(ev);
						SystemClock.sleep(100);
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis(),
								MotionEvent.ACTION_UP, 200, 100, 0);
						prefListView.dispatchTouchEvent(ev);
					} else {
						if (total != last + 1) {
							final int ftarget = target;
							prefListView.clearFocus();
							prefListView.post(new Runnable() {
								public void run() {
									prefListView.setSelection(ftarget);
								}
							});
						}
					}

				}
			});

			return false;
		}
	};

	// Check changed preferences or not
	private boolean isPreferencesChanged() {
		boolean changed = false;
		newPrefs = prefs.getAll();
		Set<String> keys = newPrefs.keySet();
		for (String key : keys) {
			Object newObject = newPrefs.get(key);
			Object oldObject = oldPrefs.get(key);
			if (!isEqual(newObject, oldObject)) {
				changed = true;
				break;
			}
		}
        if (baseChange){
            changed = true;
        }
		return changed;
	}

	// Compares two preference values
	private boolean isEqual(Object o1, Object o2) {
		boolean equal = true;
		if ((o1 instanceof Boolean) && (o2 instanceof Boolean)) {
			boolean v1 = (Boolean) o1;
			boolean v2 = (Boolean) o2;
			if (v1 != v2)
				equal = false;
		} else if ((o1 instanceof String) && (o2 instanceof String)) {
			String v1 = (String) o1;
			String v2 = (String) o2;
			if (!v1.equalsIgnoreCase(v2))
				equal = false;
		} else if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
			int v1 = (Integer) o1;
			int v2 = (Integer) o2;
			if (v1 != v2)
				equal = false;
		} else if ((o1 instanceof Float) && (o2 instanceof Float)) {
			float v1 = (Float) o1;
			float v2 = (Float) o2;
			if (v1 != v2)
				equal = false;
		} else if ((o1 instanceof Long) && (o2 instanceof Long)) {
			long v1 = (Long) o1;
			long v2 = (Long) o2;
			if (v1 != v2)
				equal = false;
		}
		return equal;
	}



}
