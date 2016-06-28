package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.*;

public class ListActions {
	String TAG = "ListActions";
	List<HashMap<String, String>> itemsArray = new ArrayList<HashMap<String, String>>();
	ReLaunchApp app;
	Activity act;
	SharedPreferences prefs;
	String listName;

	private void createItemsArray() {
		itemsArray = new ArrayList<HashMap<String, String>>();
		for (String[] n : app.getList(listName)) {
			if (!prefs.getBoolean("filterResults", false)
					|| app.filterFile(n[0], n[1])) {
				HashMap<String, String> item = new HashMap<String, String>();
				item.put("dname", n[0]);
				item.put("fname", n[1]);
				if (n[1].equals(app.DIR_TAG)) {
					int ind = n[0].lastIndexOf('/');
					if (ind == -1) {
						item.put("fname", "");
					} else {
						item.put("fname", n[0].substring(ind + 1));
						item.put("dname", n[0].substring(0, ind));
					}
					item.put("type", "dir");
				} else
					item.put("type", "file");
				itemsArray.add(item);
			}
		}
	}

	public ListActions(ReLaunchApp application, Activity activity) {
		app = application;
		act = activity;
		prefs = PreferenceManager.getDefaultSharedPreferences(app
				.getBaseContext());
	}

	public void showMenu(String listName) {
		this.listName = listName;
		createItemsArray();
		if (itemsArray.size() > 0) {
			// exts dirs sorter
			final class ExtsComparator implements java.util.Comparator<String> {
				public int compare(String a, String b) {
					if (a == null && b == null)
						return 0;
					if (a == null && b != null)
						return 1;
					if (a != null && b == null)
						return -1;
					if (a.length() < b.length())
						return 1;
					if (a.length() > b.length())
						return -1;
					return a.compareTo(b);
				}
			}
			// known extensions
			List<HashMap<String, String>> rc;
			ArrayList<String> exts = new ArrayList<String>();
			if (prefs.getBoolean("hideKnownExts", false)) {
				rc = app.getReaders();
				Set<String> tkeys = new HashSet<String>();
				for (int i = 0; i < rc.size(); i++) {
					Object[] keys = rc.get(i).keySet().toArray();
					for (int j = 0; j < keys.length; j++) {
						tkeys.add(keys[j].toString());
					}
				}
				exts = new ArrayList<String>(tkeys);
				Collections.sort(exts, new ExtsComparator());
			}

			final CharSequence[] lnames = new CharSequence[itemsArray.size()];
			for (int i = 0; i < itemsArray.size(); i++) {
				HashMap<String, String> item = itemsArray.get(i);
				String fname = item.get("fname");
				String sname = item.get("fname");
				String dname = item.get("dname");
				// clean extension, if needed
				if (prefs.getBoolean("hideKnownExts", false)) {
					for (int j = 0; j < exts.size(); j++) {
						if (sname.endsWith(exts.get(j))) {
							sname = sname.substring(0, sname.length()
									- exts.get(j).length());
						}
					}
				}
				if (dname.equals("")) {
					//dname = "/";
					if (fname.equals("")) {
						//fname = "/";
						sname = "/";
						//dname = "";
					}
				}
				lnames[i] = sname;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			// "Select home directory"
			final String flistName = listName;
			builder.setTitle(app.getResources().getString(R.string.jv_results_menu_title));
			builder.setSingleChoiceItems(lnames, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
							// action
							runItem(flistName, i);
							dialog.dismiss();
						}
					});
			builder.setNegativeButton(app.getResources().getString(R.string.app_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});
			builder.show();

		}
	}

	public void runItem(String listName, int position) {

		this.listName = listName;
		createItemsArray();

		if (position < 0 || position > itemsArray.size() - 1)
			return;

		HashMap<String, String> item = itemsArray.get(position);

		String fullName = item.get("dname") + "/" + item.get("fname");
		if (item.get("type").equals("dir")) {
			Intent intent = new Intent(act, ReLaunch.class);
			intent.putExtra("start_dir", fullName);
			//intent.putExtra("home", ReLaunch.useHome);
			act.startActivityForResult(intent, ReLaunch.DIR_ACT);
		} else {
			String fileName = item.get("fname");
			if (!app.specialAction(act, fullName)) {
				if (app.readerName(fileName).equals("Nope"))
					app.defaultAction(act, fullName);
				else {
					// Launch reader
					app.LaunchReader(fullName);
				}
			}
		}
	}
}
