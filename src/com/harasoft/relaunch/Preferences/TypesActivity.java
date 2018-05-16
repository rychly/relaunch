package com.harasoft.relaunch.Preferences;

import java.util.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.harasoft.relaunch.R;
import com.harasoft.relaunch.ReLaunchApp;
import com.harasoft.relaunch.Utils.UtilIcons;

public class TypesActivity extends Activity {
	private final String TAG = "Types";
	private final String INTENT_PREFIX = "Intent:";
	private String defReader = "Cool Reader";
	private CharSequence[] applications;
	private CharSequence[] happlications;
	private HashMap<String, AppInfo> appList;
	private boolean filterMyself = true;
	private List<HashMap<String, String>> itemsArray;
	private TPAdapter adapter;
	private ReLaunchApp app;


	class TPAdapter extends BaseAdapter {
		final Context cntx;
		private LayoutInflater lInflater;
		UtilIcons utilIcons;

		TPAdapter(Context context) {
			cntx = context;
			this.lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			utilIcons = new UtilIcons(context);
		}

		public int getCount() {
			if (itemsArray == null){
				return 0;
			}else {
				return itemsArray.size();
			}
		}

		public Object getItem(int position) {
			return itemsArray.get(position);
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = lInflater.inflate(R.layout.item_types, parent, false);
			}
			if (v == null) {
				return v;
			}
			final HashMap<String, String> item = itemsArray.get(position);
			if (item != null) {
				String ext_str = item.get("ext");
				String reader_str = item.get("rdr");
				if (ext_str == null || reader_str == null) {
					return v;
				}

				ImageView iv = (ImageView) v.findViewById(R.id.types_img);

				// Setting up button
				ImageButton upBtn = (ImageButton) v.findViewById(R.id.types_up);
				if (position == 0) {
					upBtn.setImageBitmap(null);
					//upBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.checkbox_off_background));
					upBtn.setEnabled(false);
				} else {
					upBtn.setImageBitmap(utilIcons.getIcon("UPENABLE"));
					upBtn.setEnabled(true);
				}
				upBtn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						HashMap<String, String> i = itemsArray.get(position);
						itemsArray.remove(position);
						itemsArray.add(position - 1, i);
						adapter.notifyDataSetChanged();
					}
				});

				// Setting down button
				ImageButton downBtn = (ImageButton) v.findViewById(R.id.types_down);
				if (position == (itemsArray.size() - 1)) {
					downBtn.setImageBitmap(null);
					//downBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.checkbox_off_background));
					downBtn.setEnabled(false);
				} else {
					downBtn.setImageBitmap(utilIcons.getIcon("DOWNENABLE"));
					downBtn.setEnabled(true);
				}
				downBtn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						HashMap<String, String> i = itemsArray.get(position);
						itemsArray.remove(position);
						itemsArray.add(position + 1, i);
						adapter.notifyDataSetChanged();
					}
				});

				// Setting remove button
				ImageButton rmBtn = (ImageButton) v.findViewById(R.id.types_delete);
				rmBtn.setImageBitmap(utilIcons.getIcon("DELETESMALL"));
				rmBtn.setEnabled(itemsArray.size() > 1);
				rmBtn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						itemsArray.remove(position);
						adapter.notifyDataSetChanged();
					}
				});

				// Setting extension title
				TextView ext_title = (TextView) v.findViewById(R.id.types_ext_title);
				// "Suffix (" + (position+1) + "/" + (itemsArray.size()) + ")"
				ext_title.setText(getResources().getString(
						R.string.jv_types_suffix)
						+ " ("
						+ (position + 1)
						+ "/"
						+ (itemsArray.size())
						+ ")");

				// Setting extension
				Button extName = (Button) v.findViewById(R.id.types_ext);
				extName.setText(item.get("ext"));
				extName.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(cntx);
						// "File suffix"
						builder.setTitle(getResources().getString(
								R.string.jv_types_file_suffix));
						final EditText input = new EditText(cntx);
						input.setText(item.get("ext"));
						builder.setView(input);

						// "Ok"
						builder.setPositiveButton(
								getResources().getString(R.string.app_ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										String value = String.valueOf(input.getText());
										if (value.equals(""))
											// "Can't be empty!"
											Toast.makeText(
													cntx,
													getResources()
															.getString(
																	R.string.jv_types_cant_be_empty),
													Toast.LENGTH_LONG).show();
										else {
											itemsArray.get(position).put("ext",value);
											adapter.notifyDataSetChanged();
											dialog.dismiss();
										}
									}
								});

						// "Cancel"
						builder.setNegativeButton(
								getResources().getString(
										R.string.app_cancel),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});

						builder.show();
					}
				});

				// Setting application name
				Button appName = (Button) v.findViewById(R.id.types_app);
				appName.setText(item.get("rdr"));
				String appR = item.get("rdr");
				//ReLaunchApp.AppInfo findApp = app.searchApp(appR);
				if (appR.startsWith("Intent:")) {
					iv.setImageDrawable(getResources().getDrawable(R.drawable.icon_list));
                }else if (appList.get(appR) != null){
					iv.setImageDrawable(appList.get(appR).appIcon);
                }else {
					iv.setImageDrawable(getResources().getDrawable(R.drawable.icon_list));
				}
				appName.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						AlertDialog.Builder builder1 = new AlertDialog.Builder(cntx);
						// "Explicit application or general intent?"
						builder1.setTitle(getResources().getString(
								R.string.jv_types_app_or_int_title));
						// "When you tap on file with specified suffix ReLaunch"
						// " may call explicit application or just generate intent with"
						// " application type you specify (ACTION_VIEW). \n\nWhich method do you want?"
						builder1.setMessage(getResources().getString(
								R.string.jv_types_app_or_int_text));
						// "Explicit application"
						builder1.setPositiveButton(
								getResources().getString(
										R.string.jv_types_explicit_application),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										AlertDialog.Builder builder2 = new AlertDialog.Builder(
												cntx);
										// "Select application"
										builder2.setTitle(getResources()
												.getString(
														R.string.jv_types_select_application));
										builder2.setSingleChoiceItems(
												happlications,
												-1,
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int i) {
														itemsArray.get(position).put("rdr",(String) applications[i]);
														adapter.notifyDataSetChanged();
														dialog.dismiss();
													}
												});
										builder2.show();
									}
								});

						// "General intent"
						builder1.setNeutralButton(
								getResources().getString(R.string.jv_types_general_intent),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										AlertDialog.Builder builder3 = new AlertDialog.Builder(cntx);
										// "Intent type"
										builder3.setTitle(getResources()
												.getString(
														R.string.jv_types_intent_type));
										final EditText input = new EditText(cntx);
										String v = item.get("rdr");
										if (v.startsWith(INTENT_PREFIX))
											v = v.substring(INTENT_PREFIX.length());
										else
											v = "application/";
										input.setText(v);
										builder3.setView(input);
										// "Ok"
										builder3.setPositiveButton(
												getResources().getString(
														R.string.app_ok),
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int whichButton) {
														String value = String.valueOf(input.getText());
														if (value.equals(""))
															// "Can't be empty!"
															Toast.makeText(
																	cntx,
																	getResources()
																			.getString(
																					R.string.jv_types_cant_be_empty),
																	Toast.LENGTH_LONG)
																	.show();
														else {
															itemsArray
																	.get(position)
																	.put("rdr",
																			INTENT_PREFIX
																					+ value);
															adapter.notifyDataSetChanged();
															dialog.dismiss();
														}
													}
												});

										// "Cancel"
										builder3.setNegativeButton(getResources().getString(R.string.app_cancel),
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int whichButton) {
														dialog.dismiss();
													}
												});

										builder3.show();
									}
								});

						// "Cancel"
						builder1.setNegativeButton(getResources().getString(R.string.app_cancel),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
						builder1.show();
					}
				});

			}
			return v;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		// Global storage
		app = ((ReLaunchApp) getApplicationContext());
        if(app == null ) {
            finish();
        }
		app.setOptionsWindowActivity(this);
		setContentView(R.layout.layout_types);
		UtilIcons utilIcons = new UtilIcons(getBaseContext());
		filterMyself = prefs.getBoolean("filterSelf", true);
		// создаём все необходимые списки для отображения программ
		createAppList(getPackageManager());

		itemsArray = getReadersString(appList);

		// Fill listview with our info
		ListView lv = (ListView) findViewById(R.id.types_lv);
		adapter = new TPAdapter(this);
		lv.setAdapter(adapter);

		ImageView iconPanel = (ImageView) findViewById(R.id.types_icon);
		iconPanel.setImageDrawable(new BitmapDrawable(getResources(), utilIcons.getIcon("ASSOCIATIONS")));
		// OK/Save button
		Button okBtn = (Button) findViewById(R.id.types_ok);
		okBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("OK")), null, null, null);
		okBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				List<HashMap<String, String>> readers = new ArrayList<>();
				for (HashMap<String, String> r : itemsArray) {
					HashMap<String, String> a = new HashMap<>();
					a.put(r.get("ext"), getAppString(r.get("rdr")));
					readers.add(a);
				}
				app.setReaders(readers);
				setResult(Activity.RESULT_OK);
				finish();
			}
		});

		// Add new button
		Button addBtn = (Button) findViewById(R.id.types_new);
		addBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("ADD")), null, null, null);
		addBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				HashMap<String, String> i = new HashMap<>();
				i.put("ext", ".");
				if (appList.get(defReader) != null) {
					i.put("rdr", defReader);
				}else {
					i.put("rdr", "application/");
				}
				itemsArray.add(i);
				adapter.notifyDataSetChanged();
			}
		});

		// Cancel button
		Button cancelBtn = (Button) findViewById(R.id.types_cancel);
		cancelBtn.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("DELETE")), null, null, null);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});

		// back btn - work as cancel
		ImageButton backBtn = (ImageButton) findViewById(R.id.back_btn);
		backBtn.setImageBitmap(utilIcons.getIcon("EXIT"));
		backBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		app.generalOnResume(TAG);
	}

	private void createAppList(PackageManager pm) {
		appList = new HashMap<>();
		AppInfo appInfo;
		String appName;
		ArrayList<String> tempNameApp = new ArrayList<>();

		Intent componentSearchIntent = new Intent(Intent.ACTION_MAIN, null);
		componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		componentSearchIntent.setAction(Intent.ACTION_MAIN);
		List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);
		for (ResolveInfo ri : ril) {
			if (ri.activityInfo != null) {
				appInfo = new AppInfo();
				appInfo.appPackage = ri.activityInfo.packageName;
				appInfo.appActivity = ri.activityInfo.name;
				appName = ri.activityInfo.loadLabel(pm).toString();
				if (ri.activityInfo.icon != 0) {
					appInfo.appIcon = ri.activityInfo.loadIcon(pm);
				} else {
					appInfo.appIcon = ri.loadIcon(pm);
				}
				if (!filterMyself || (appInfo.appPackage != null && !"com.harasoft.relaunch".equals(appInfo.appPackage))) {
					appList.put(appName, appInfo);
					tempNameApp.add(appName);
				}
			}
		}
		Collections.sort(tempNameApp);
		applications = tempNameApp.toArray(new CharSequence[tempNameApp.size()]);
		happlications = tempNameApp.toArray(new CharSequence[tempNameApp.size()]);
	}
	class AppInfo{
		String appPackage;
		String appActivity;
		Drawable appIcon;
	}
	private String getAppString(String appName){
		StringBuilder items = new StringBuilder();
		// получение имен программ
		AppInfo appInfo = appList.get(appName);
		if (appInfo != null){
			items.append(appInfo.appPackage);
			items.append("%");
			items.append(appInfo.appActivity);
			items.append("%");
			items.append(appName);
			return items.toString();
		}
		return appName;
	}
	private String getAppName(String app_String){

		Object[] keys = appList.keySet().toArray();
		for (Object key : keys) {
			String appName = getAppString(key.toString());
			if (appName.equals(app_String)) {
				return key.toString();
			}
		}

		return app_String;
	}
	private List<HashMap<String, String>> getReadersString(HashMap<String, AppInfo> appList) {
		List<HashMap<String, String>> rc = app.getReaders(); // получаем массив карт
		ArrayList<HashMap<String, String>> new_array = new ArrayList<>();
		for (HashMap<String, String> aRc : rc) {
			Object[] keys = aRc.keySet().toArray();
			for (Object key : keys) {
				String ext_name = key.toString();
				String ext_prog = aRc.get(key.toString());

				ext_prog = getAppName(ext_prog);
				HashMap<String, String> r = new HashMap<>();
				r.put("ext", ext_name);
				r.put("rdr", ext_prog);
				new_array.add(r);
			}
		}
		return new_array;
	}
}
