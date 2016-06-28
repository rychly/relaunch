package com.harasoft.relaunch;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AllApplications extends Activity {
	final String TAG = "AllApps";

	final int UNINSTALL_ACT = 1;

	final int CNTXT_MENU_RMFAV = 1;
	final int CNTXT_MENU_ADDFAV = 2;
	final int CNTXT_MENU_UNINSTALL = 3;
	final int CNTXT_MENU_CANCEL = 4;
	final int CNTXT_MENU_MOVEUP = 5;
	final int CNTXT_MENU_MOVEDOWN = 6;

	ReLaunchApp app;
	ArrayList<String> itemsArray = new ArrayList<String>();
	ArrayList<ReLaunchApp.AppInfo> appsArray = new ArrayList<ReLaunchApp.AppInfo>();
	AppAdapter adapter;
	GridView lv;
	String listName;
	String title;
	SharedPreferences prefs;
	boolean addSView = true;
	int gcols = 2;
    LayoutInflater vi;


	static class ViewHolder {
		TextView tv;
		ImageView iv;
	}

	class AppAdapter extends ArrayAdapter<String> {
		ReLaunchApp.AppInfo appInfo;
		AppAdapter(Context context, int resource, ArrayList<String> data) {
			super(context, resource, data);

		}

		@Override
		public int getCount() {
			return appsArray.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v = convertView;
			appInfo = appsArray.get(position);
			if (v == null) {
				v = vi.inflate(R.layout.applications_item,  parent, false);
                if(v == null){
                    return null;
                }
				holder = new ViewHolder();
				holder.tv = (TextView) v.findViewById(R.id.app_name);
				holder.iv = (ImageView) v.findViewById(R.id.app_icon);
				v.setTag(holder);
			} else {
				holder = (ViewHolder) v.getTag();
			}

			if (appInfo != null) {
				holder.tv.setText(appInfo.appName);
				holder.iv.setImageDrawable(appInfo.appIcon);
			}
			return v;
		}
	}

	private void rereadAppList() {
		// создание списка
		if (listName.equals("app_all")){
			appsArray = app.getAppInfoArrayList();
		}else {
			checkListByName();
			appsArray = createArray();
		}
		// сортировка
		if (!listName.equals("app_favorites")) {
			Collections.sort(appsArray, new Comparator<ReLaunchApp.AppInfo>() {
				public int compare(ReLaunchApp.AppInfo o1, ReLaunchApp.AppInfo o2) {
					return o1.appName.compareTo(o2.appName);
				}
			});
		}
		// получение имен программ
		for (ReLaunchApp.AppInfo anAppsArray : appsArray) {
			itemsArray.add(anAppsArray.appName);
		}
		// формирование заголовка окна
		((TextView) findViewById(R.id.app_title)).setText(title + " (" + itemsArray.size() + ")");
		// обновление экрана
		EinkScreen.PrepareController(null, false);
		adapter.notifyDataSetChanged();
	}

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
		// Create applications list
		final Intent data = getIntent();
		if (data.getExtras() == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		app.setAppInfoArrayList(app.createAppList(getPackageManager()));
		listName = data.getExtras().getString("list");

		// set app icon
		ImageView app_icon = (ImageView) findViewById(R.id.app_icon);
		if (listName.equals("app_all")) {
            title = getResources().getString(R.string.jv_relaunch_all_a);
            app_icon.setImageDrawable(getResources().getDrawable(R.drawable.ci_grid));
			String cols = prefs.getString("columnsAppAll", "-1");
			gcols = Integer.parseInt(cols);
			appsArray = app.getAppInfoArrayList();
		}
		if (listName.equals("app_last")) {
            title = getResources().getString(R.string.jv_relaunch_lru_a);
            app_icon.setImageDrawable(getResources().getDrawable(R.drawable.ci_lrea));
            gcols = 2;
			checkListByName();
			appsArray = createArray();
        }
		if (listName.equals("app_favorites")) {
            title = getResources().getString(R.string.jv_relaunch_fav_a);
			app_icon.setImageDrawable(getResources().getDrawable(R.drawable.ci_fava));
			String cols = prefs.getString("columnsAppFav", "-1");
			gcols = Integer.parseInt(cols);
			checkListByName();
			appsArray = createArray();
		}

		(findViewById(R.id.app_btn)).setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});

		adapter = new AppAdapter(this, R.layout.applications_item, itemsArray);
		lv = (GridView) findViewById(R.id.app_grid);
		if (gcols <= 0) {
            gcols = 2;
        }
		lv.setNumColumns(gcols);
		lv.setAdapter(adapter);
		rereadAppList();

		registerForContextMenu(lv);
		if (prefs.getBoolean("customScroll", app.customScrollDef)) {
			if (addSView) {
				int scrollW;
				try {
					scrollW = Integer.parseInt(prefs.getString("scrollWidth","25"));
				} catch (NumberFormatException e) {
					scrollW = 25;
				}

				LinearLayout ll = (LinearLayout) findViewById(R.id.app_grid_layout);
				final SView sv = new SView(getBaseContext());
				LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(scrollW, ViewGroup.LayoutParams.FILL_PARENT, 1f);
				sv.setLayoutParams(pars);
				ll.addView(sv);
				lv.setOnScrollListener(new AbsListView.OnScrollListener() {
					public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
						sv.total = totalItemCount;
						sv.count = visibleItemCount;
						sv.first = firstVisibleItem;
                        EinkScreen.PrepareController(null, false);
                        sv.invalidate();
					}

					public void onScrollStateChanged(AbsListView view, int scrollState) {
					}
				});
				addSView = false;
			}
		} else {
			lv.setOnScrollListener(new AbsListView.OnScrollListener() {
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    EinkScreen.PrepareController(null, false);
				}

				public void onScrollStateChanged(AbsListView view, int scrollState) {
				}
			});
		}
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ReLaunchApp.AppInfo item = appsArray.get(position);

				Intent i = app.getIntentByLabel(item);
				if (i == null)
					// "Activity \"" + item + "\" not found!"
					app.showToast("\" " + item.appName + "\" " + getResources().getString(R.string.jv_allapp_not_found));
				else {
					boolean ok = true;
					try {
						i.setAction(Intent.ACTION_MAIN);
						i.addCategory(Intent.CATEGORY_LAUNCHER);
						startActivity(i);
						if (prefs.getBoolean("returnToMain", false)) {
                            finish();
                        }
					} catch (ActivityNotFoundException e) {
						// "Activity \"" + item + "\" not found!"
						app.showToast("\" " + item.appName + "\" " + getResources().getString(R.string.jv_allapp_not_found));
						ok = false;
					}
					if (ok) {
						app.addToList("app_last", item.appName, ":", false);
						saveList("app_last");
					}
				}
			}
		});
		ScreenOrientation.set(this, prefs);
	}

	@Override
	protected void onResume() {
		super.onResume();
		rereadAppList();
		app.generalOnResume(TAG);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		int pos = info.position;
		String i = itemsArray.get(pos);

		if (listName.equals("app_favorites")) {
			if (pos > 0) {
                // "Move one position up"
                menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE, getResources().getString(R.string.jv_allapp_move_up));
            }
			if (pos < (itemsArray.size() - 1)) {
                // "Move one position down"
                menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE, getResources().getString(R.string.jv_allapp_move_down));
            }
			// "Remove from favorites"
			menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, getResources().getString(R.string.jv_allapp_remove));
		} else {
			List<String[]> lit = app.getList("app_favorites");
			boolean in_fav = false;
			for (String[] r : lit) {
				if (r[0].equals(i)) {
					in_fav = true;
					break;
				}
			}
			if (!in_fav) {
                // "Add to favorites"
                menu.add(Menu.NONE, CNTXT_MENU_ADDFAV, Menu.NONE, getResources().getString(R.string.jv_allapp_add));
            }

		}
		// "Uninstall"
		menu.add(Menu.NONE, CNTXT_MENU_UNINSTALL, Menu.NONE, getResources()
				.getString(R.string.jv_allapp_uninstall));
		// "Cancel"
		menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources()
				.getString(R.string.app_cancel));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)                                                                                                                                     {
		if (item.getItemId() == CNTXT_MENU_CANCEL) {
            return true;
        }
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if(info == null){
            return false;
        }
		final int pos = info.position;

		switch (item.getItemId()) {
		case CNTXT_MENU_MOVEUP:
			moveUp(pos);
			break;
		case CNTXT_MENU_MOVEDOWN:
			moveDown(pos);
			break;
		case CNTXT_MENU_RMFAV:
			// удаляем его из списка
			itemsArray.remove(pos);
			appsArray.remove(pos);
			// сохраняем список
			app.setList(listName, convArrToStr(itemsArray));
			saveList(listName);
			// обновляем изображение
			EinkScreen.PrepareController(null, false);
			adapter.notifyDataSetChanged();
			break;
		case CNTXT_MENU_ADDFAV:
			app.addToList("app_favorites", itemsArray.get(pos), ":", true);
			saveList("app_favorites");
			break;
		case CNTXT_MENU_UNINSTALL:
			PackageManager pm = getPackageManager();

            if(pm == null){
                return false;
            }
			PackageInfo pi;
			ReLaunchApp.AppInfo appInfoTemp = appsArray.get(pos);
			try {
				pi = pm.getPackageInfo(appInfoTemp.appPackage, 0);
			} catch (Exception e) {
				return false;
			}
			if (pi == null) {
                // "PackageInfo not found for label \"" + it + "\""
                Toast.makeText(
                        AllApplications.this,
                        getResources().getString(
                                R.string.jv_allapp_package_info_not_found)
                                + " \"" + appInfoTemp.appName + "\"", Toast.LENGTH_LONG)
                        .show();
            }else {
				// "Package name is \"" + pi.packageName + "\" for label \"" +
				// it + "\""
				Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts(
						"package", pi.packageName, null));
				try {
					startActivityForResult(intent, UNINSTALL_ACT);
				} catch (ActivityNotFoundException e) {
					// "Activity \"" + pi.packageName + "\" not found"
					Toast.makeText(
							AllApplications.this,
							getResources().getString(
									R.string.jv_allapp_activity)
									+ " \""
									+ pi.packageName
									+ "\" "
									+ getResources().getString(
											R.string.jv_allapp_not_found),
							Toast.LENGTH_LONG).show();
					return true;
				}
			}
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case UNINSTALL_ACT:
			rereadAppList();
			break;
		default:
			//return;
		}
	}

	private ArrayList<ReLaunchApp.AppInfo> createArray(){
		ArrayList<ReLaunchApp.AppInfo> tempArray = new ArrayList<ReLaunchApp.AppInfo>();
		List<String[]> tempAppList = app.getList(listName);
		for (String[] tempApp : tempAppList) {
			for (ReLaunchApp.AppInfo anAppsArray : appsArray) {
				if (anAppsArray.appPackage.equals(tempApp[0])) {
					tempArray.add(anAppsArray);
					break;
				}
			}
		}
		return tempArray;
	}

	private void checkListByName() {
		List<String[]> rc = app.getList(listName);
		ArrayList<String> rc1 = new ArrayList<String>();
		String packageName;
		for (String[] r : rc) {
			for (ReLaunchApp.AppInfo anAppsArray : appsArray) {
				packageName = anAppsArray.appPackage;
				if (packageName.equals(r[0])) {
					rc1.add(packageName);
					break;
				}
			}
		}
		if (rc.size() != rc1.size()){
			saveList(listName);
			app.setList(listName, convArrToStr(rc1));
		}
	}
	private void saveList(String listN){
		int appMax;
		if (listN.equals("app_last")) {
			try {
				appMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
			} catch (NumberFormatException e) {
				appMax = 30;
			}
		}else if (listN.equals("app_favorites")) {
			try {
				appMax = Integer.parseInt(prefs.getString("appFavSize", "30"));
			} catch (NumberFormatException e) {
				appMax = 30;
			}
		}else {
			return;
		}
		app.writeFile(listN, ReLaunch.APP_LRU_FILE, appMax, ":");
	}
	private void moveUp(int pos){
		if (pos > 0) {
			// сохраняем перемещаемый элемент
			String itemTemp = itemsArray.get(pos);
			ReLaunchApp.AppInfo appInfoTemp = appsArray.get(pos);
			// удаляем его из списка
			itemsArray.remove(pos);
			appsArray.remove(pos);
			// вставляем ниже по списку
			itemsArray.add(pos - 1, itemTemp);
			appsArray.add(pos - 1, appInfoTemp);
			// сохраняем список
			app.setList(listName, convArrToStr(itemsArray));
			saveList(listName);
			// обновляем изображение
			EinkScreen.PrepareController(null, false);
			adapter.notifyDataSetChanged();
		}
	}
	private void moveDown(int pos){
		if (pos < (itemsArray.size() - 1)) {
			// сохраняем перемещаемый элемент
			String itemTemp = itemsArray.get(pos);
			ReLaunchApp.AppInfo appInfoTemp = appsArray.get(pos);
			// удаляем его из списка
			itemsArray.remove(pos);
			appsArray.remove(pos);
			// вставляем ниже по списку
			if (pos + 1 >= itemsArray.size() - 1) {
				itemsArray.add(itemTemp);
				appsArray.add(appInfoTemp);
			} else {
				itemsArray.add(pos + 1, itemTemp);
				appsArray.add(pos + 1, appInfoTemp);
			}
			itemsArray.add(pos + 1, itemTemp);
			appsArray.add(pos + 1, appInfoTemp);
			// сохраняем список
			app.setList(listName, convArrToStr(itemsArray));
			saveList(listName);
			// обновляем изображение
			EinkScreen.PrepareController(null, false);
			adapter.notifyDataSetChanged();
		}
	}
	private List<String[]> convArrToStr(ArrayList<String> arrayList){
		List<String[]> temp = new ArrayList<String[]>();
		for (String anArrayList : arrayList) {
			temp.add(new String[]{anArrayList, ""});
		}
		return temp;
	}
}
