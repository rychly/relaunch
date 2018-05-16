package com.harasoft.relaunch;

import android.app.Activity;
import android.content.*;
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
import com.harasoft.relaunch.Utils.UtilApp;
import com.harasoft.relaunch.Utils.UtilAppFav;
import com.harasoft.relaunch.Utils.UtilAppRun;
import com.harasoft.relaunch.Utils.UtilIcons;

import java.util.ArrayList;

public class AllApplications extends Activity {
	final String TAG = "AllApps";

	private final int UNINSTALL_ACT = 1;

	private final int CNTXT_MENU_RMFAV = 1;
	private final int CNTXT_MENU_ADDFAV = 2;
	private final int CNTXT_MENU_UNINSTALL = 3;
	private final int CNTXT_MENU_CANCEL = 4;
	private final int CNTXT_MENU_MOVEUP = 5;
	private final int CNTXT_MENU_MOVEDOWN = 6;
	private final int CNTXT_MENU_RMLAST = 7;

	ReLaunchApp app;
	private ArrayList<UtilApp.Info> appsArray;
	AppAdapter adapter;
	private String listName;
	private String title;
	SharedPreferences prefs;
	private boolean addSView = true;
	private int gcols = 2;
    LayoutInflater vi;
	private UtilApp utilApp;
	private UtilAppFav utilAppFav;
	private UtilAppRun utilAppRun;
	private UtilIcons utilIcons;


	static class ViewHolder {
		TextView tv;
		ImageView iv;
	}

	class AppAdapter extends ArrayAdapter<String> {
		UtilApp.Info appInfo;
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
				v = vi.inflate(R.layout.item_allapplication,  parent, false);
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

	private void rereadAppList(String listName) {
		// создание списка
		appsArray = utilApp.getAppInfoArrayList();
		if (listName.equals("app_last")) {
			checkListByName(utilAppRun.getList());
		}
		if (listName.equals("app_favorites")) {
			checkListByName(utilAppFav.getList());
		}
		// Collections.sort(itemsArray);
		// формирование заголовка окна
		((TextView) findViewById(R.id.app_title)).setText(title + " (" + appsArray.size() + ")");
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
		app.setOptionsWindowActivity(this);
        setContentView(R.layout.layout_allapplications);

        vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// Create applications list
		final Intent data = getIntent();
		if (data.getExtras() == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		//app.setAppInfoArrayList(app.createAppList(getPackageManager()));
		utilApp = new UtilApp(getBaseContext(), getPackageManager());
		utilIcons = new UtilIcons(getBaseContext());
		listName = data.getExtras().getString("list");
		appsArray = utilApp.getAppInfoArrayList();
		// set app icon
		ImageView app_icon = (ImageView) findViewById(R.id.app_icon);
		if (listName.equals("app_all")) {
            title = getResources().getString(R.string.jv_relaunch_all_a);
            app_icon.setImageBitmap(utilIcons.getIcon("ALLAPP"));
			String cols = prefs.getString("columnsAppAll", "1");
			gcols = Integer.parseInt(cols);
		}
		if (listName.equals("app_last")) {
            title = getResources().getString(R.string.jv_relaunch_lru_a);
			utilAppRun = new UtilAppRun(getBaseContext());
			app_icon.setImageBitmap(utilIcons.getIcon("LASTAPP"));
			String cols = prefs.getString("columnsAppFav", "1");

			gcols = Integer.parseInt(cols);
			checkListByName(utilAppRun.getList());
        }
		if (listName.equals("app_favorites")) {
            title = getResources().getString(R.string.jv_relaunch_fav_a);
            utilAppFav = new UtilAppFav(getBaseContext());
			app_icon.setImageBitmap(utilIcons.getIcon("FAVAPP"));
			String cols = prefs.getString("columnsAppFav", "1");
			gcols = Integer.parseInt(cols);
			checkListByName(utilAppFav.getList());
		}

		ImageView app_exit = (ImageView) findViewById(R.id.app_btn_exit);
		app_exit.setImageBitmap(utilIcons.getIcon("EXIT"));
		app_exit.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});

		adapter = new AppAdapter(this, R.layout.item_allapplication, new ArrayList<String>());
		GridView lv = (GridView) findViewById(R.id.app_grid);
		if (gcols <= 0) {
            gcols = 2;
        }
		lv.setNumColumns(gcols);
		lv.setAdapter(adapter);
		rereadAppList(listName);

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

				UtilApp.Info item = appsArray.get(position);

				Intent i = createIntent(item.appPackage, item.appActivity);
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
						utilAppRun = new UtilAppRun(getBaseContext());
						utilAppRun.addAppRun(item.appPackage);
						utilAppRun = null;
					}
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		rereadAppList(listName);
		app.generalOnResume(TAG);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		int pos = info.position;
		String appPackage = appsArray.get(pos).appPackage;

		switch (listName) {
			case "app_last":
				if (pos > 0) {
					// "Move one position up"
					menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE, getResources().getString(R.string.jv_allapp_move_up));
				}
				if (pos < (appsArray.size() - 1)) {
					// "Move one position down"
					menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE, getResources().getString(R.string.jv_allapp_move_down));
				}
				// "Remove from last"
				menu.add(Menu.NONE, CNTXT_MENU_RMLAST, Menu.NONE, getResources().getString(R.string.jv_allapp_last_remove));
				break;
			case "app_favorites":
				if (pos > 0) {
					// "Move one position up"
					menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE, getResources().getString(R.string.jv_allapp_move_up));
				}
				if (pos < (appsArray.size() - 1)) {
					// "Move one position down"
					menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE, getResources().getString(R.string.jv_allapp_move_down));
				}
				// "Remove from favorites"
				menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, getResources().getString(R.string.jv_allapp_fav_remove));
				break;
			default:
				utilAppFav = new UtilAppFav(getBaseContext());
				if (!utilAppFav.isAppFav(appPackage)) {
					// "Add to favorites"
					menu.add(Menu.NONE, CNTXT_MENU_ADDFAV, Menu.NONE, getResources().getString(R.string.jv_allapp_add));
				}

				break;
		}
		// "Uninstall"
		menu.add(Menu.NONE, CNTXT_MENU_UNINSTALL, Menu.NONE, getResources()
				.getString(R.string.jv_allapp_uninstall));
		// "Cancel"
		menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources()
				.getString(R.string.app_cancel));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
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
			utilAppFav.delAppFav(appsArray.get(pos).appPackage);
			appsArray.remove(pos);
			// обновляем изображение
			adapter.notifyDataSetChanged();
			break;
		case CNTXT_MENU_ADDFAV:
			utilAppFav = new UtilAppFav(getBaseContext());
			utilAppFav.addAppFav(appsArray.get(pos).appPackage);
			utilAppFav = null;
			break;
		case CNTXT_MENU_UNINSTALL:
			PackageManager pm = getPackageManager();

            if(pm == null){
                return false;
            }
			PackageInfo pi;
			UtilApp.Info appInfoTemp = appsArray.get(pos);
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
		case CNTXT_MENU_RMLAST:
			// удаляем его из списка
			utilAppRun.delAppFav(appsArray.get(pos).appPackage);
			appsArray.remove(pos);
			// обновляем изображение
			adapter.notifyDataSetChanged();
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case UNINSTALL_ACT:
			rereadAppList(listName);
			break;
		default:
			//return;
		}
	}
	private void checkListByName(ArrayList<String> listAppPackages) {
		ArrayList<UtilApp.Info> outList = new ArrayList<>();
		String packageName;
		for (String inItem : listAppPackages) {
			for (UtilApp.Info anAppsArray : appsArray) {
				packageName = anAppsArray.appPackage;
				if (packageName.equals(inItem)) {
					outList.add(anAppsArray);
					break;
				}
			}
		}
		appsArray = outList;
	}
	private void saveList(String listName){
		if (listName.equals("app_last")) {
			utilAppRun.setListAppRun(appsArray);
		}
		if (listName.equals("app_favorites")) {
			utilAppFav.setListAppFav(appsArray);
		}
	}
	private void moveUp(int pos){
		if (pos > 0) {
			// сохраняем перемещаемый элемент
			UtilApp.Info appInfoTemp = appsArray.get(pos);
			// удаляем его из списка
			appsArray.remove(pos);
			// вставляем ниже по списку
			appsArray.add(pos - 1, appInfoTemp);
			// сохраняем список
			saveList(listName);
			// обновляем изображение
			EinkScreen.PrepareController(null, false);
			adapter.notifyDataSetChanged();
		}
	}
	private void moveDown(int pos){
		if (pos < (appsArray.size() - 1)) {
			// сохраняем перемещаемый элемент
			UtilApp.Info appInfoTemp = appsArray.get(pos);
			// удаляем его из списка
			appsArray.remove(pos);
			// вставляем ниже по списку
			if (pos + 1 >= appsArray.size() - 1) {
				appsArray.add(appInfoTemp);
			} else {
				appsArray.add(pos + 1, appInfoTemp);
			}
			// сохраняем список
			saveList(listName);
			// обновляем изображение
			EinkScreen.PrepareController(null, false);
			adapter.notifyDataSetChanged();
		}
	}
	private Intent createIntent(String appPackage, String appActivity){
		if(appPackage != null && appActivity != null) {
			Intent i = new Intent();
			i.setComponent(new ComponentName(appPackage, appActivity));
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			return i;
		}else {
			return null;
		}
	}
}
