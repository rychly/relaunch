package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.*;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrefsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	final Context context = this;
	final String TAG = "PreferenceActivity";
	final static public int TYPES_ACT = 1;
	final static public int FILTS_ACT = 2;
	private static String BACKUP_DIR;

	ReLaunchApp app;
	List<String> applicationsArray;
	CharSequence[] applications;
	CharSequence[] happlications;
    static boolean baseChange = false;

	SharedPreferences prefs;
	boolean do_pref_subrequest = true;
	Map<String, ?> oldPrefs;
	Map<String, ?> newPrefs;

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
        app.setFullScreenIfNecessary(this);
		List<String>  applicationsArray = app.getAppList();
		applications = applicationsArray.toArray(new CharSequence[applicationsArray.size()]);
		happlications = app.getAppList().toArray(new CharSequence[app.getAppList().size()]);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		BACKUP_DIR = prefs.getString("backupDir", "/sdcard/.relaunch");
		setContentView(R.layout.prefs_main);
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
										String[] DBlist = databaseList();
										File DBf;
										for (String aDBlist : DBlist) {
											DBf = getDatabasePath(aDBlist);
											if (DBf.getName().equals("BOOKS.db")) {
												DBf.delete();
											}
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
										String[] DBlist = databaseList();
										File DBf;
										for (String aDBlist : DBlist) {
											DBf = getDatabasePath(aDBlist);
											if (DBf.getName().equals("OPDS.db")) {
												DBf.delete();
											}
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
        findPreference("cleanupDatabaseFTP").setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference pref) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                PrefsActivity.this);
                        builder.setTitle(getResources().getString(
                                R.string.jv_prefs_cleanup_database_title));
                        builder.setMessage(getResources().getString(
                                R.string.jv_prefs_cleanup_databaseFTP_text));
                        builder.setPositiveButton(
                                getResources().getString(R.string.app_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
										String[] DBlist = databaseList();
										File DBf;
										for (String aDBlist : DBlist) {
											DBf = getDatabasePath(aDBlist);
											if (DBf.getName().equals("FTP.db")) {
												DBf.delete();
											}
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
										String[] DBlist = databaseList();
										File DBf;
										for (String aDBlist : DBlist) {
											DBf = getDatabasePath(aDBlist);
											if (DBf.getName().equals("PANELS.db") || DBf.getName().equals("SCREEN.db") || DBf.getName().equals("LIST_PANELS.db")) {
												DBf.delete();
											}
										}
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

		findPreference("cleanupLRU").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_clear_lists_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_clear_lru_text));
						builder.setPositiveButton(
								getResources().getString(R.string.app_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										app.setDefault("lastOpened");
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

		findPreference("cleanupFAV").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_clear_lists_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_clear_favorites_text));
						builder.setPositiveButton(
								getResources().getString(R.string.app_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										app.setDefault("favorites");
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
						boolean ret = app.copyPrefs(app.DATA_DIR, BACKUP_DIR);
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
						boolean ret = app.copyPrefs(BACKUP_DIR,	app.DATA_DIR);
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

		// back button - work as cancel
		( findViewById(R.id.back_btn)).setOnClickListener(new View.OnClickListener() {
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

		ScreenOrientation.set(this, prefs);
		// доступность кнопок листания настроек
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
			if (key.equals("startMode")) {
				String value = sharedPreferences.getString(key, "UNKNOWN");
				if (value.equals("LAUNCHER")) {
					do_pref_subrequest = false;
					((CheckBoxPreference) findPreference("fullScreen")).setChecked(true);
					((CheckBoxPreference) findPreference("homeMode")).setChecked(true);
					((CheckBoxPreference) findPreference("libraryMode")).setChecked(true);
					((CheckBoxPreference) findPreference("shopMode")).setChecked(true);
					do_pref_subrequest = false;
				} else if (value.equals("PROGRAM")) {
					do_pref_subrequest = false;
					((CheckBoxPreference) findPreference("fullScreen")).setChecked(false);
					((CheckBoxPreference) findPreference("homeMode")).setChecked(false);
					((CheckBoxPreference) findPreference("libraryMode")).setChecked(false);
					((CheckBoxPreference) findPreference("shopMode")).setChecked(false);
					do_pref_subrequest = true;
				}
			} else if (key.equals("workMode")) {
				String value = sharedPreferences.getString(key, "UNKNOWN");
				if (value.equals("FILES")) {
					do_pref_subrequest = false;
					((CheckBoxPreference) findPreference("showBookTitles")).setChecked(false);
					((CheckBoxPreference) findPreference("rowSeparator")).setChecked(false);
					((CheckBoxPreference) findPreference("useFileManagerFunctions")).setChecked(true);
					((CheckBoxPreference) findPreference("openWith")).setChecked(true);
					((CheckBoxPreference) findPreference("showFullDirPath")).setChecked(true);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("sortMode", 0);
					editor.putBoolean("showBookTitles", false);
					editor.putBoolean("rowSeparator", false);
					editor.putBoolean("useFileManagerFunctions", true);
					editor.putBoolean("openWith", true);
					editor.putBoolean("showFullDirPath", true);
					editor.commit();
					do_pref_subrequest = true;
				} else if (value.equals("BOOKS")) {
					do_pref_subrequest = false;
					((CheckBoxPreference) findPreference("showBookTitles")).setChecked(true);
					((CheckBoxPreference) findPreference("rowSeparator")).setChecked(true);
					((CheckBoxPreference) findPreference("useFileManagerFunctions")).setChecked(false);
					((CheckBoxPreference) findPreference("openWith")).setChecked(false);
					((CheckBoxPreference) findPreference("showFullDirPath")).setChecked(false);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("sortMode", 2);
					editor.putBoolean("showBookTitles", true);
					editor.putBoolean("rowSeparator", true);
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

			if ((key.equals("fullScreen")) || (key.equals("homeMode")) || (key.equals("libraryMode")) || (key.equals("shopMode"))) {
				do_pref_subrequest = false;
				((ListPreference) findPreference("startMode")).setValueIndex(2);
				do_pref_subrequest = true;
			}
			if ((key.equals("showBookTitles")) || (key.equals("useFileManagerFunctions"))) {
				do_pref_subrequest = false;
				((ListPreference) findPreference("workMode")).setValueIndex(2);
				do_pref_subrequest = true;
			}

			// special cases
            //=======================================================================================================
            if (key.equals("dropboxSetButton")) {
                checkSetDropboxAndOpds("dropboxSetButton", sharedPreferences.getString(key, "none"), sharedPreferences);
            }else if (key.equals("opdsSetButton")) {
                checkSetDropboxAndOpds("opdsSetButton", sharedPreferences.getString(key, "none"), sharedPreferences);
            }else if (key.equals("ftpSetButton")) {
                checkSetDropboxAndOpds("ftpSetButton", sharedPreferences.getString(key, "none"), sharedPreferences);
            }else if(key.equals("homeButtonST") || key.equals("homeButtonDT") || key.equals("homeButtonLT") ||
                    key.equals("lruButtonST") || key.equals("lruButtonDT") || key.equals("lruButtonLT") ||
                    key.equals("favButtonST") || key.equals("favButtonDT") || key.equals("favButtonLT") ||
                    key.equals("settingsButtonST") || key.equals("settingsButtonDT") || key.equals("settingsButtonLT") ||
                    key.equals("advancedButtonST") || key.equals("advancedButtonDT") || key.equals("advancedButtonLT") ||
                    key.equals("memButtonST") || key.equals("memButtonDT") || key.equals("memButtonLT") ||
                    key.equals("batButtonST") || key.equals("batButtonDT") || key.equals("batButtonLT") ||
                    key.equals("appFavButtonST") || key.equals("appFavButtonDT") || key.equals("appFavButtonLT") ||
                    key.equals("appAllButtonST") || key.equals("appAllButtonDT") || key.equals("appAllButtonLT") ||
                    key.equals("appLastButtonST") || key.equals("appLastButtonDT") || key.equals("appLastButtonLT") ||
                    key.equals("searchButtonST") || key.equals("searchButtonDT") || key.equals("searchButtonLT"))
                    {
                        if (sharedPreferences.getString(key, "NOTHING").equals("RUN")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PrefsActivity.this);
                            // "Select application"
                            builder.setTitle(getResources().getString(R.string.jv_prefs_select_application));
                            builder.setSingleChoiceItems(happlications, -1, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int i) {
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString(key + "app", (String) applications[i]);
                                            editor.commit();
                                            updatePrefSummary(pref);
                                            dialog.dismiss();
                                        }
                                    });
                            builder.show();
                        }else if (sharedPreferences.getString(key, "NOTHING").equals("FAVN")) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(PrefsActivity.this);
                            // "Select number"
                            builder1.setTitle(getResources().getString( R.string.jv_prefs_select_number));
                            final EditText input = new EditText(PrefsActivity.this);
                            input.setInputType(InputType.TYPE_CLASS_NUMBER);
                            input.setText(sharedPreferences.getString(key + "fav", "1"));
                            builder1.setView(input);
                            // "Ok"
                            builder1.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                            dialog.dismiss();
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString(key + "fav", String.valueOf(input.getText()));
                                            editor.commit();
                                            updatePrefSummary(pref);
                                        }
                                    });
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            builder1.show();
                        }else if (sharedPreferences.getString(key, "NOTHING").equals("LRUN")) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(PrefsActivity.this);
                            // "Select number"
                            builder1.setTitle(getResources().getString(R.string.jv_prefs_select_number));
                            final EditText input = new EditText(PrefsActivity.this);
                            input.setInputType(InputType.TYPE_CLASS_NUMBER);
                            input.setText(sharedPreferences.getString(key + "lru", "1"));
                            builder1.setView(input);
                            // "Ok"
                            builder1.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow( input.getWindowToken(), 0);
                                            dialog.dismiss();
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString(key + "lru", String.valueOf(input.getText()));
                                            editor.commit();
                                            updatePrefSummary(pref);
                                        }
                                    });
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            builder1.show();
                        }else if (sharedPreferences.getString(key, "OPENMENU").equals("HOMEN")) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(
                                    PrefsActivity.this);
                            // "Select number"
                            builder1.setTitle(getResources().getString(R.string.jv_prefs_select_number));
                            final EditText input = new EditText(PrefsActivity.this);
                            input.setInputType(InputType.TYPE_CLASS_NUMBER);
                            input.setText(sharedPreferences.getString(key +  "home", "1"));
                            builder1.setView(input);
                            // "Ok"
                            builder1.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                            dialog.dismiss();
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString(key +  "home", String.valueOf(input.getText()));
                                            editor.commit();
                                            updatePrefSummary(pref);
                                        }
                                    });
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            builder1.show();
                        }
                   checkSetDropboxAndOpds(key, sharedPreferences.getString(key, "none"), sharedPreferences);
            }

		}
	}

	public Preference.OnPreferenceClickListener prefScreenListener = new Preference.OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference pref) {
            final PreferenceScreen prefScreen = (PreferenceScreen) pref;

            prefScreen.getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

			LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View prefView = inflater.inflate(R.layout.prefs_main, null);

			prefScreen.getDialog().setContentView(prefView);

			final ListView prefListView = (ListView) prefView.findViewById(android.R.id.list);
			prefScreen.bind(prefListView);
			// доступность кнопок листания настроек------------
			int total = prefListView.getCount();
			int last = prefListView.getLastVisiblePosition();

			final ImageButton upBtn = (ImageButton) prefView.findViewById(R.id.btn_scrollup);
			final ImageButton downBtn = (ImageButton) prefView.findViewById(R.id.btn_scrolldown);

			upBtn.setEnabled(false);
			upBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_arrowup_gray));
			if (last == total -1){
				downBtn.setEnabled(false);
				downBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_arrowdown_gray));
			}
			//-------------------------------------------------
			EditText tEdit = (EditText) prefView.findViewById(R.id.prefernces_title);
			tEdit.setText(pref.getTitle());

			ImageButton b = (ImageButton) prefView.findViewById(R.id.back_btn);
			b.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					prefScreen.getDialog().dismiss();
				}
			});

			ImageButton bu = (ImageButton) prefView.findViewById(R.id.btn_scrollup);
			bu.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					int first = prefListView.getFirstVisiblePosition();
					int total = prefListView.getCount();
					int last = prefListView.getLastVisiblePosition();
					int visible = last - first + 1;
					first -= visible;
					if (first < 0) {
						first = 0;
					}
					if (total != first + visible + 1) {
						downBtn.setEnabled(true);
						downBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_arrowdown));
					}
					if (first == 0){
						upBtn.setEnabled(false);
						upBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_arrowup_gray));
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

			ImageButton bd = (ImageButton) prefView.findViewById(R.id.btn_scrolldown);
			bd.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					int first = prefListView.getFirstVisiblePosition();
					int total = prefListView.getCount();
					int last = prefListView.getLastVisiblePosition();
					int target = last + 1;
					if (target > (total - 1)){
						target = total - 1;
					}
					if (target + (last - first) > total - 1) {
						downBtn.setEnabled(false);
						downBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_arrowdown_gray));
					}
					if (target > 0){
						upBtn.setEnabled(true);
						upBtn.setImageDrawable(getResources().getDrawable(R.drawable.ci_arrowup));
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

    private void checkSetDropboxAndOpds(String button, String key, SharedPreferences sharedPreferences) {
        String[] buttonName = {"homeButtonDT", "homeButtonLT",
                                "lruButtonDT", "lruButtonLT",
                                "favButtonDT", "favButtonLT",
                                "settingsButtonDT", "settingsButtonLT",
                                "advancedButtonDT", "advancedButtonLT",
                                "memButtonDT", "memButtonLT",
                                "batButtonDT", "batButtonLT",
                                "appFavButtonDT", "appFavButtonLT",
                                "appAllButtonDT", "appAllButtonLT",
                                "appLastButtonDT", "appLastButtonLT",
                                "searchButtonDT", "searchButtonLT",
                                "opdsSetButton", "dropboxSetButton",
                                "ftpSetButton"};
        boolean f_butt = false;
        for (String aButtonName : buttonName) {
            if (aButtonName.equals(button)) {
                f_butt = true;
                break;
            }
        }
        if(!f_butt){
            return;
        }

        boolean newOPDS = false;
        boolean newDropbox = false;
        boolean newFTP = false;
        SharedPreferences.Editor editor = prefs.edit();

        if("OPDS".equals(key)){
            newOPDS = true;
        }
        if("DROPBOX".equals(key)){
            newDropbox = true;
        }
        if("FTP".equals(key)){
            newFTP = true;
        }
        if(!"dropboxSetButton".equals(button) && !"opdsSetButton".equals(button) && !"ftpSetButton".equals(button)){
            // действия при выборе из меню кнопок экрана
            do_pref_subrequest = false;
            String tempValue = ((ListPreference) findPreference("dropboxSetButton")).getValue();
            if(button.equals(tempValue)){
                if(!key.equals("DROPBOX")){
                    ((ListPreference) findPreference("dropboxSetButton")).setValueIndex(0);
                    editor.putString("dropboxSetButton", "NOTHING");
                }
            }
            if(button.equals(((ListPreference) findPreference("opdsSetButton")).getValue())){
                if(!key.equals("OPDS")){
                    ((ListPreference) findPreference("opdsSetButton")).setValueIndex(0);
                    editor.putString("opdsSetButton", "NOTHING");
                }
            }
            if(button.equals(((ListPreference) findPreference("ftpSetButton")).getValue())){
                if(!key.equals("OPDS")){
                    ((ListPreference) findPreference("ftpSetButton")).setValueIndex(0);
                    editor.putString("ftpSetButton", "NOTHING");
                }
            }

            // если новые настройки
            if(newDropbox || newOPDS || newFTP){
                //сбрасываем на всех остальных настройки
                for (int i = 0, j=buttonName.length; i < j; i++) {
                    if(!buttonName[i].equals(button)){
                        // сбрасываем настройки если совпадают с проверяемыми
                        if(sharedPreferences.getString(buttonName[i], "none").equals(key)){
                            ((ListPreference) findPreference(buttonName[i])).setValueIndex(0);
                            editor.putString(buttonName[i], "NOTHING");
                        }
                    }else{ // сохраняем настройки в модулях
                        if(newOPDS){
                            ((ListPreference) findPreference("opdsSetButton")).setValueIndex(i);
                            editor.putString("opdsSetButton",buttonName[i]);
                        }
                        if(newDropbox){
                            ((ListPreference) findPreference("dropboxSetButton")).setValueIndex(i);
                            editor.putString("dropboxSetButton",buttonName[i]);
                        }
                        if(newFTP){
                            ((ListPreference) findPreference("ftpSetButton")).setValueIndex(i);
                            editor.putString("ftpSetButton",buttonName[i]);
                        }
                    }
                }
            }
            editor.commit();
            do_pref_subrequest = true;
        }else {
            // действия при выборе в самих настройках модулей
            do_pref_subrequest = false;
            int indexValue = 0;
            if("dropboxSetButton".equals(button)){// если нажат выбор действия для DROPBOX
                indexValue = ((ListPreference) findPreference(key)).findIndexOfValue("DROPBOX");
                editor.putString(key, "DROPBOX");
                if(sharedPreferences.getString("opdsSetButton", "none").equals(key)){
                    ((ListPreference) findPreference("opdsSetButton")).setValueIndex(0);
                    editor.putString("opdsSetButton", "NOTHING");
                }
                if(sharedPreferences.getString("ftpSetButton", "none").equals(key)){
                    ((ListPreference) findPreference("ftpSetButton")).setValueIndex(0);
                    editor.putString("ftpSetButton", "NOTHING");
                }
                button = key;
                key = "DROPBOX";
            }else if("opdsSetButton".equals(button)){// если нажат выбор действия для OPDS
                indexValue = ((ListPreference) findPreference(key)).findIndexOfValue("OPDS");
                editor.putString(key, "OPDS");
                if(sharedPreferences.getString("dropboxSetButton", "none").equals(key)){
                    ((ListPreference) findPreference("dropboxSetButton")).setValueIndex(0);
                    editor.putString("dropboxSetButton", "NOTHING");
                }
                if(sharedPreferences.getString("ftpSetButton", "none").equals(key)){
                    ((ListPreference) findPreference("ftpSetButton")).setValueIndex(0);
                    editor.putString("ftpSetButton", "NOTHING");
                }
                button = key;
                key = "OPDS";
            }else if("ftpSetButton".equals(button)){// если нажат выбор действия для FTP
                indexValue = ((ListPreference) findPreference(key)).findIndexOfValue("FTP");
                editor.putString(key, "FTP");
                if(sharedPreferences.getString("dropboxSetButton", "none").equals(key)){
                    ((ListPreference) findPreference("dropboxSetButton")).setValueIndex(0);
                    editor.putString("dropboxSetButton", "NOTHING");
                }
                if(sharedPreferences.getString("opdsSetButton", "none").equals(key)){
                    ((ListPreference) findPreference("opdsSetButton")).setValueIndex(0);
                    editor.putString("opdsSetButton", "NOTHING");
                }
                button = key;
                key = "FTP";
            }
            //сбрасываем на всех остальных настройки
            if("OPDS".equals(key) || "DROPBOX".equals(key) || "FTP".equals(key)){
                for (String aButtonName : buttonName) {
                    if (!aButtonName.equals(button)) {
                        // сбрасываем настройки если совпадают с проверяемыми
                        if (sharedPreferences.getString(aButtonName, "NOTHING").equals(key)) {
                            ((ListPreference) findPreference(aButtonName)).setValueIndex(0);
                            editor.putString(aButtonName, "NOTHING");
                        }
                    }
                }
                ((ListPreference) findPreference(button)).setValueIndex(indexValue);
                editor.commit();
            }
            do_pref_subrequest = true;
        }

    }


}
