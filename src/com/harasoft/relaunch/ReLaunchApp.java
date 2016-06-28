package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;


public class ReLaunchApp extends Application {
	final String TAG = "ReLaunchApp";


	// Reading files
	final int FileBufferSize = 1024;

	// Pending intent to self restart
	public PendingIntent RestartIntent;

	// Miscellaneous public flags/settings
	public boolean fullScreen = false;
	public boolean hideTitle = true;
	public Boolean askIfAmbiguous;
	public boolean customScrollDef = true;

	// Search values
	final String DIR_TAG = ".DIR..";

	// Book status
	final int READING = 1;
	final int FINISHED = 2;

	// Max file sizes
	int viewerMax;
	int editorMax;

	// Scrolling related values
	public int scrollStep;

	// Filter values
	public int FLT_SELECT;
	public int FLT_STARTS;
	public int FLT_ENDS;
	public int FLT_CONTAINS;
	public int FLT_MATCHES;
	public int FLT_NEW;
	public int FLT_NEW_AND_READING;
	public boolean filters_and;

	public String DATA_DIR;

	public HashMap<String, Integer> history = new HashMap<String, Integer>();
	public HashMap<String, Integer> columns = new HashMap<String, Integer>();
	static private HashMap<String, List<String[]>> m = new HashMap<String, List<String[]>>();
	private List<HashMap<String, String>> readers;

	public BooksBase dataBase;
	private ArrayList<AppInfo> appInfoArrayList;

	// Icons + Applications
	public  class AppInfo{
		String appName;
		String appPackage;
		String appActivity;
		Drawable appIcon;
	}
	public ArrayList<AppInfo> createAppList(PackageManager pm) {
		ArrayList<AppInfo> appInfos = new ArrayList<AppInfo>();
		AppInfo appInfo;

		Intent componentSearchIntent = new Intent(Intent.ACTION_MAIN, null);
		componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		componentSearchIntent.setAction(Intent.ACTION_MAIN);
		List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);
		for (ResolveInfo ri : ril) {
			if (ri.activityInfo != null) {
				appInfo = new AppInfo();
				appInfo.appPackage = ri.activityInfo.packageName;
				appInfo.appActivity = ri.activityInfo.name;
				appInfo.appName = ri.activityInfo.loadLabel(pm).toString();

				if (ri.activityInfo.icon != 0) {
					appInfo.appIcon = ri.activityInfo.loadIcon(pm);
				} else {
					appInfo.appIcon = ri.loadIcon(pm);
				}
				if (!ReLaunch.filterMyself || (appInfo.appPackage != null && !"com.harasoft.relaunch.Main".equals(appInfo.appPackage))) {
					appInfos.add(appInfo);
				}
			}
		}
		Collections.sort(appInfos, new Comparator<ReLaunchApp.AppInfo>() {
			public int compare(ReLaunchApp.AppInfo o1, ReLaunchApp.AppInfo o2) {
				return o1.appName.compareTo(o2.appName);
			}
		});
		return appInfos;
	}
	public ArrayList<AppInfo> getAppInfoArrayList(){
		return appInfoArrayList;
	}
	public void setAppInfoArrayList(ArrayList<AppInfo> appList){
		appInfoArrayList = appList;
	}
	public ArrayList<String> getAppList(){
		ArrayList<String> itemsArray = new ArrayList<String>();
		// получение имен программ
		for (ReLaunchApp.AppInfo anAppsArray : appInfoArrayList) {
			itemsArray.add(anAppsArray.appName);
		}
		return itemsArray;
	}

	// Readers
	public List<HashMap<String, String>> getReaders() {
		return readers;
	}
	public void setReaders(List<HashMap<String, String>> r) {
		readers = r;
	}
	public String readerName(String file) {
		for (HashMap<String, String> r : readers) {
			for (String key : r.keySet()) {
				if (file.endsWith(key))
					return r.get(key);
			}
		}
		return "Nope";
	}
	public List<String> readerNames(String file) {
		List<String> rc = new ArrayList<String>();
		for (HashMap<String, String> r : readers) {
			for (String key : r.keySet()) {
				if (file.endsWith(key) && !rc.contains(r.get(key)))
					rc.add(r.get(key));
			}
		}
		return rc;
	}

	// get list by name
	public List<String[]> getList(String name) {
		if (m.containsKey(name))
			return m.get(name);
		else
			return new ArrayList<String[]>();
	}
	// set list by name
	public void setList(String name, List<String[]> l) {
		m.put(name, l);
	}
	public void setDefault(String name) {
		// Set list default
		//List<String[]> nl = new ArrayList<String[]>();
		/*if (name.equals("lastOpened")) {
		} else if (name.equals("favorites")) {
		}  */
		m.put(name, new ArrayList<String[]>());
	}

	// save list
	public void saveList(String listName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		if (listName.equals("favorites")) {
			int favMax = 30;
			try {
				favMax = Integer.parseInt(prefs.getString("favSize", "30"));
			} catch (NumberFormatException e) {
                //emply
			}
			this.writeFile("favorites", ReLaunch.FAV_FILE, favMax, "/");
		}
		if (listName.equals("lastOpened")) {
			int lruMax = 30;
			try {
				lruMax = Integer.parseInt(prefs.getString("lruSize", "30"));
			} catch (NumberFormatException e) {
                //emply
			}
			this.writeFile("lastOpened", ReLaunch.LRU_FILE, lruMax, "/");
		}
		if (listName.equals("app_last")) {
			int appLruMax = 30;
			try {
				appLruMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
			} catch (NumberFormatException e) {
                //emply
			}
			this.writeFile("app_last", ReLaunch.APP_LRU_FILE, appLruMax, ":");
		}
		if (listName.equals("app_favorites")) {
			int appFavMax = 30;
			try {
				appFavMax = Integer.parseInt(prefs
						.getString("appFavSize", "30"));
			} catch (NumberFormatException e) {
                //emply
			}
			this.writeFile("app_favorites", ReLaunch.APP_FAV_FILE, appFavMax, ":");
		}
		if (listName.equals("history")) {
			List<String[]> h = new ArrayList<String[]>();
			for (String k : this.history.keySet()) {
				if (this.history.get(k) == this.READING)
					h.add(new String[] { k, "READING" });
				else if (this.history.get(k) == this.FINISHED)
					h.add(new String[] { k, "FINISHED" });
			}
			this.setList("history", h);
			this.writeFile("history", ReLaunch.HIST_FILE, 0, ":");
		}
		if (listName.equals("columns")) {
			List<String[]> c = new ArrayList<String[]>();
			for (String k : this.columns.keySet()) {
				c.add(new String[] { k, Integer.toString(this.columns.get(k)) });
			}
			this.setList("columns", c);
			this.writeFile("columns", ReLaunch.COLS_FILE, 0, ":");
		}
	}
	// Add to list
	public void addToList(String listName, String dr, String fn, Boolean addToEnd) {
		addToList_internal(listName, dr, fn, addToEnd);
	}
	public void addToList(String listName, String fullName, Boolean addToEnd) {
		addToList(listName, fullName, addToEnd, "/");
	}
	public void addToList(String listName, String fullName, Boolean addToEnd, String delimiter) {

		if (delimiter.equals("/")) {
			if (fullName.endsWith("/" + DIR_TAG)) {
				fullName = fullName.substring(0, fullName.length() - DIR_TAG.length() - 1);
				File f = new File(fullName);
				if (!f.exists())
					return;
				addToList_internal(listName, fullName, DIR_TAG, addToEnd);
			} else {
				File f = new File(fullName);
				if (!f.exists())
					return;
				addToList_internal(listName, f.getParent(), f.getName(),
						addToEnd);
			}
		} else {
			int ind = fullName.indexOf(delimiter);
			if (ind < 0)
				return;
			if (ind + delimiter.length() >= fullName.length())
				return;
			String dname = fullName.substring(0, ind);
			String fname = fullName.substring(ind + delimiter.length());
			if (listName.equals("history")) {
				// Special case - delimiter is not "/" but we need to check file
				// existence anyway.
				// directory name is a full file name in such case. filename is
				// a READ/NEW mark
				// File f = new File(dname);
				// if (!f.exists())
				// return;
				addToList_internal(listName, dname, fname, addToEnd);
			} else
				addToList_internal(listName, dname, fname, addToEnd);
		}

	}
	public void addToList_internal(String listName, String dr, String fn, Boolean addToEnd) {
		if (!m.containsKey(listName))
			m.put(listName, new ArrayList<String[]>());
		List<String[]> resultList = m.get(listName);

		String[] entry = new String[] { dr, fn };
		for (int i = 0; i < resultList.size(); i++) {
			if (resultList.get(i)[0].equals(dr) && resultList.get(i)[1].equals(fn)) {
				resultList.remove(i);
				break;
			}
		}
		if (addToEnd)
			resultList.add(entry);
		else
			resultList.add(0, entry);
	}
	// Remove from list
	public void removeFromList(String listName, String dr, String fn) {
        if (!m.containsKey(listName))
            return;
        List<String[]> resultList = m.get(listName);
        for (int i = 0; i < resultList.size(); i++) {
            if (resultList.get(i)[0].equals(dr) && resultList.get(i)[1].equals(fn)) {
                resultList.remove(i);
                saveList(listName);
                return;
            }
        }
	}

	// If list contains
	public boolean contains(String listName, String dr, String fn) {
		if (!m.containsKey(listName))
			return false;
		List<String[]> resultList = m.get(listName);

        for (String[] aResultList : resultList) {
            if (aResultList[0].equals(dr) && aResultList[1].equals(fn))
                return true;
        }
		return false;
	}

	// Read misc. lists
	public boolean readFile(String listName, String fileName, String delimiter) {
		FileInputStream fis = null;
		try {
			fis = openFileInput(fileName);
		} catch (FileNotFoundException e) {
            //emply
		}
		if (fis == null)
			return false;
		else {
			InputStreamReader insr;
			try {
				insr = new InputStreamReader(fis, "utf8");
			} catch (UnsupportedEncodingException e) {
				return false;
			}
			BufferedReader bufr = new BufferedReader(insr, FileBufferSize);
			String l;
			while (true) {
				try {
					l = bufr.readLine();
				} catch (IOException e) {
					return false;
				}
				if (l == null)
					break;
				if (!l.equals("")) {
					addToList(listName, l, true, delimiter);
				}
			}
			try {
				bufr.close();
				insr.close();
				fis.close();
			} catch (IOException e) {
                //emply
			}
		}
		return true;
	}

	// Save to file miscellaneous lists
	public void writeFile(String listName, String fileName, int maxEntries, String delimiter) {
        //Log.i("========= writeFile ===================", "--------------------------------start listName=" + listName);
		if (!m.containsKey(listName))
			return;

		List<String[]> resultList = m.get(listName);
		FileOutputStream fos;
		try {
			fos = openFileOutput(fileName, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			return;
		}
		for (int i = 0; i < resultList.size(); i++) {
			if (maxEntries != 0 && i >= maxEntries)
				break;
			String line = resultList.get(i)[0] + delimiter + resultList.get(i)[1] + "\n";
			try {
				fos.write(line.getBytes());
			} catch (IOException e) {
                //Log.i("========= writeFile ===================", "--------------------------------error listName=" + listName);
			}
		}
		try {
			fos.close();
		} catch (IOException e) {
            //Log.i("========= writeFile ===================", "--------------------------------error listName=" + listName);
		}
        //Log.i("========= writeFile ===================", "--------------------------------end listName=" + listName);
	}

	// Remove file
    public boolean fileRemove(File nameFile) {

        if (!nameFile.exists()) {
            return false;
        }
        if (nameFile.isDirectory()) {
            File[] allEntries = nameFile.listFiles();
            if (allEntries == null) {
                return false;
            }
            for (File allEntry : allEntries) {
                if (allEntry.isDirectory()) {
                    if (!fileRemove(allEntry)) {
                        if (ReLaunch.selectRootNavigation){
                            if (!RootCommands.DeleteFileRoot(allEntry.getAbsolutePath())){
                                return false;
                            }
                        }else {
                            return false;
                        }
                    }
                } else {
                    if (!allEntry.delete()) {
                        if (ReLaunch.selectRootNavigation){
                            if (!RootCommands.DeleteFileRoot(allEntry.getAbsolutePath())){
                                return false;
                            }
                        }else {
                            return false;
                        }
                    }
                }
            }
        }
        if (nameFile.delete()){
            return true;
        }else if (ReLaunch.selectRootNavigation){
            if (!RootCommands.DeleteFileRoot(nameFile.getAbsolutePath())){
                return false;
            }
        }else {
            return false;
        }

        return true;
    }
    public void fileRemoveAllList(String dr, String fn) {
        removeFromList("lastOpened", dr, fn);
        saveList("lastOpened");
        removeFromList("favorites", dr, fn);
        saveList("favorites");
        removeFromList("history", dr, fn);
        history.remove(dr + "/" + fn);
        saveList("history");
    }

	//Copy file src to dst
	public boolean copyFile(String from, String to, boolean rewrite) {
		File srcFile = new File(from);
		File dstFile = new File(to);
		FileChannel src;
		FileChannel dst;

		if ((!srcFile.canRead()) || ((dstFile.exists()) && (!rewrite)))
			return false;
		try {
			if(dstFile.createNewFile()) {
                src = new FileInputStream(srcFile).getChannel();
                dst = new FileOutputStream(dstFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }else if (ReLaunch.selectRootNavigation){
                if (!RootCommands.CopyFileRoot(from, to)){
                    return false;
                }
            }else {
                return false;
            }
		} catch (IOException e) {
			return false;
		}
		return true;
	}
    public boolean copyDir(String from, String to) {
        File toDir = new File(to);
        String[] strDirList = (new File(from)).list();

        if(!toDir.exists()){
            if(!createDir(to)){
                return false;
            }
        }
        for (String aStrDirList1 : strDirList) {
            File f1 = new File(from +"/" + aStrDirList1);
            if (f1.isFile()) {
                if (!copyFile(from +"/" + aStrDirList1, to +"/" + aStrDirList1, false)){
                    return false;
                }
            } else {
                if (!copyDir(from +"/" + aStrDirList1, to + "/" +aStrDirList1)){
                    return false;
                }
            }
        }
        return true;
    }
    public boolean copyAll(String from, String to, boolean rewrite) {
        File source = new File(from);
        if (source.isFile()) {
            if (!copyFile(from, to, rewrite)) {
                return false;
            }
        } else {
            if (!copyDir(from, to)) {
                return false;
            }
        }
        return true;
    }
	//Move file src to dst
	public boolean moveFile(String from, String to) {

		boolean ret;
		if (from.split("/")[0].equalsIgnoreCase(to.split("/")[0])) {
			File src = new File(from);
			File dst = new File(to);
			ret = src.renameTo(dst);
		} else {
            File file = new File(from);
            ret = file.renameTo(new File(to));
		}
        if (!ret && ReLaunch.selectRootNavigation){
            ret = RootCommands.MoveFileRoot(from, to);
        }
		return ret;
	}
	public boolean createDir(String dst) {
        File dir = new File(dst);
        if (!dir.mkdirs()){
            return RootCommands.CreateDirRoot(dst);
        }else{
            return true;
        }

	}
	
	// common utility - get intent by label, null if not found
	public Intent getIntentByLabel(String label) {
		String[] labelp = label.split("%");
		if (labelp.length > 1) {
			return createIntent(labelp[0], labelp[1]);
		} else {
			return null;
		}
	}
	public Intent getIntentByLabel(AppInfo appInfo) {
		if (appInfo != null) {
			return createIntent(appInfo.appPackage, appInfo.appActivity);
		}else {
			return null;
		}
	}
	private Intent createIntent(String appPackage, String appActivity){
		if(appPackage != null && appActivity != null) {
			Intent i = new Intent();
			i.setComponent(new ComponentName(appPackage, appActivity));
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | 0x20000000);
			return i;
		}else {
			return null;
		}
	}

	// common utility - return intent to launch reader by reader name and full
	// file name. Null if not found
	public Intent launchReader(String name, String file) {
		String re[] = name.split(":");
		if (re.length == 2 && re[0].equals("Intent")) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | 0x20000000);
			i.setDataAndType(Uri.parse("file:///" + Uri.encode(file.substring(1))), re[1]);
			addToList("lastOpened", file, false);
			saveList("lastOpened");
			if (!history.containsKey(file) || history.get(file) == FINISHED) {
				history.put(file, READING);
				saveList("history");
			}
			return i;
		} else {
			Intent i = getIntentByLabel(name);
			if (i == null)
				// "Activity \"" + name + "\" not found!"
				Toast.makeText(this,getResources().getString(R.string.jv_rla_activity)
								+ " \""
								+ name
								+ "\" "
								+ getResources().getString(R.string.jv_rla_not_found),
						Toast.LENGTH_SHORT).show();
			else {
				i.setAction(Intent.ACTION_VIEW);
				i.setData(Uri.parse("file:///" + Uri.encode(file.substring(1))));
				addToList("lastOpened", file, false);
				saveList("lastOpened");
				if (!history.containsKey(file) || history.get(file) == FINISHED) {
					history.put(file, READING);
					saveList("history");
				}
				return i;
			}
		}
		return null;
	}
	public AppInfo searchApp(String appName){
		AppInfo appFinded = new AppInfo();
		for (AppInfo anAppsArray : appInfoArrayList) {
			if (anAppsArray.appName.equals(appName)){
				appFinded = anAppsArray;
				break;
			}
		}
		return appFinded;
	}

	// FILTER
	public boolean filterFile1(String dname, String fname, Integer method, String value) {
		if (method == FLT_STARTS)
			return fname.startsWith(value);
		else if (method == FLT_ENDS)
			return fname.endsWith(value);
		else if (method == FLT_CONTAINS)
			return fname.contains(value);
		else if (method == FLT_MATCHES)
			return fname.matches(value);
		else if (method == FLT_NEW)
			return !history.containsKey(dname + "/" + fname);
		else if (method == FLT_NEW_AND_READING) {
			String fullName = dname + "/" + fname;
			return  !(history.containsKey(fullName)&& history.get(fullName) == FINISHED);
		} else
			return false;
	}

	public boolean filterFile(String dname, String fname) {
		List<String[]> filters = getList("filters");
		if (filters.size() > 0) {
			for (String[] f : filters) {
				Integer filtMethod = 0;
				try {
					filtMethod = Integer.parseInt(f[0]);
				} catch (NumberFormatException e) {
                    //emply
				}
				if (filters_and) {
					// AND all filters
					if (!filterFile1(dname, fname, filtMethod, f[1]))
						return false;
				} else {
					// OR all filters
					if (filterFile1(dname, fname, filtMethod, f[1]))
						return true;
				}
			}
			return filters_and;
		} else
			return true;
	}

	public boolean specialAction(Activity a, String s) {
		if (s.endsWith(".apk")) {
			// Install application
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + s), "application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			a.startActivity(intent);
			return true;
		}
		return false;
	}

	public void defaultAction(Activity a, String fname) {
		Intent intent = new Intent(a, Viewer.class);
		intent.putExtra("filename", fname);
		a.startActivity(intent);
	}

	public void About(final Activity a) {
		String vers = "<version>";
        PackageInfo temp;
        PackageManager temp2;
		try {
            temp2 = getPackageManager();
            if (temp2 != null) {
                temp = temp2.getPackageInfo(getPackageName(), 0);
                vers = temp.versionName;
            }
		} catch (Exception e) {
            //emply
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(a);
		WebView wv = new WebView(a);

		builder.setTitle("ReLaunch");
		// String str = "<h1><center>ReLaunch</center></h1>"
		// + "<center><b>Reader launcher for Nook Simple Touch</b></center><br>"
		// + "<center>Version: <b>" + vers + "</b></center><br>"
		// +
		// "<center>Source code: <a href=\"https://github.com/yiselieren/ReLaunch\">git://github.com/yiselieren/ReLaunch.git</a></center>";
		String str = getResources().getString(R.string.jv_rla_about_prev)
				+ vers + getResources().getString(R.string.jv_rla_about_post);
		wv.loadDataWithBaseURL(null, str, "text/html", "utf-8", null);
		builder.setView(wv);
		// "Ok"
		builder.setPositiveButton(getResources().getString(R.string.app_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				});
		// "See changelog"
		builder.setNegativeButton(
				getResources().getString(R.string.jv_rla_see_changelog),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						AlertDialog.Builder builder1 = new AlertDialog.Builder(a);
						WebView wv = new WebView(a);
						wv.loadDataWithBaseURL(
								null,
								getResources().getString(R.string.about_help)
										+ getResources().getString(R.string.about_appr)
										+ getResources().getString(R.string.whats_new),
								"text/html", "utf-8", null);
						// "What's new"
						builder1.setTitle(getResources().getString(R.string.jv_rla_whats_new_title));
						builder1.setView(wv);
						// "Ok"
						builder1.setPositiveButton(
								getResources().getString(R.string.app_ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});
						builder1.show();
					}
				});
		builder.show();
	}

	public void setFullScreenIfNecessary(Activity a) {
		if (!hideTitle) {
			a.requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		if (fullScreen) {
			a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

	}

	public void generalOnResume(String name) {
		Log.d(TAG, "--- onResume(" + name + ")");
	}

	public boolean copyPrefs(String from, String to) {
		File fromDir = new File(from);
		File toDir = new File(to);
		if (!fromDir.exists())
			return false;
		if (!toDir.exists())
			if (!toDir.mkdir())
				return false;
		File tDir = new File(toDir.getAbsolutePath() + "/files"); 
		if (!tDir.exists()) {
            if (!tDir.mkdir())
                return false;
        }else{
            fileRemove(new File(toDir.getAbsolutePath() + "/files"));
            if (!tDir.mkdir())
                return false;
        }
        tDir = new File(toDir.getAbsolutePath() + "/databases");
        if (!tDir.exists()) {
            if (!tDir.mkdir())
                return false;
        }else{
            fileRemove(new File(toDir.getAbsolutePath() + "/databases"));
            if (!tDir.mkdir())
                return false;
        }
		tDir = new File(toDir.getAbsolutePath() + "/shared_prefs"); 
		if (!tDir.exists()){
			if (!tDir.mkdir())
				return false;
        }else{
            fileRemove(new File(toDir.getAbsolutePath() + "/shared_prefs"));
            if (!tDir.mkdir())
                return false;
        }
		String[] files = {"AppFavorites.txt", "AppLruFile.txt", "Columns.txt", "Filters.txt", "History.txt", "LruFile.txt"};
		for (String f : files) {
			String src = fromDir.getAbsolutePath() + "/files/" + f;
			String dst = toDir.getAbsolutePath() + "/files/" + f;
            File file = new File(src);
            if(file.exists()) {
                copyAll(src, dst, true);
            }
		}

        File dirName = new File(fromDir.getAbsolutePath() + "/databases");
        String[] DBlist = dirName.list();
        for (String aDBlist : DBlist) {
            String src = fromDir.getAbsolutePath() + "/databases/" + aDBlist;
            String dst = toDir.getAbsolutePath() + "/databases/" + aDBlist;
            copyAll(src, dst, true);
        }

		String src = fromDir.getAbsolutePath() + "/shared_prefs/com.harasoft.relaunch_preferences.xml";
		String dst = toDir.getAbsolutePath() + "/shared_prefs/com.harasoft.relaunch_preferences.xml";

        return copyAll(src, dst, true);
    }

	public boolean isStartDir(String dir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String startDirs = prefs.getString("startDir", "");
        return startDirs.contains(dir);
    }

	public void setStartDir(String dir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("startDir", dir);
		editor.putBoolean("showAddStartDir", false);
		editor.commit();
	}

	public void addStartDir(String dir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = prefs.edit();
		String oldStart = prefs.getString("startDir", "");
		editor.putString("startDir", oldStart + "," + dir);
		editor.commit();
	}

	public String[] loadStartDirs(){
		// для доступа к настройкам получаем объект
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		// получаем список домашних папок
		ArrayList<String> arrayListDir = new ArrayList<String>(Arrays.asList(prefs.getString("startDir", "").split(",")));
		// из списка выкидываем папку с дропбоксом, чтобы понимать сколько остальных
		if (arrayListDir.size() > 0){
			String delDir = "Dropbox| ";
			for (int i = 0, k = arrayListDir.size(); i < k; i++) {
				if (delDir.equals(arrayListDir.get(i))) {
					arrayListDir.remove(i);
				}
			}
		}
		if (arrayListDir.get(0).trim().length() == 0){
			arrayListDir.remove(0);
		}
		// если папок ноль, то ищем возможные корневые иначе добавляем самый корень
		if (arrayListDir.size() == 0){
			String findDir = getLocalCard();
			if (findDir.length() == 0){
				arrayListDir.add("/");
			}else {
				arrayListDir.addAll(Arrays.asList(findDir.split(",")));
			}
		}
		// если стоит флаг возвращаем папку дропа
		if(prefs.getBoolean("showDropbox", false)){
			addStartDir("Dropbox| ");
			arrayListDir.add("Dropbox| ");
		}
		// массив превращаем в строку для сохранения
		StringBuilder tempStartDirs = new StringBuilder();
		for (int i = 0, k = arrayListDir.size(); i < k; i++) {
			tempStartDirs.append(arrayListDir.get(i));
			if ((i + 1) < k ){
				tempStartDirs.append(",");
			}
		}

		// теперь необходимо сохранить всё, что наворотили
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("startDir", tempStartDirs.toString());
		editor.commit();
		// возвращаем полученный массив
		return arrayListDir.toArray(new String[arrayListDir.size()]);
	}

    public void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    // автоматический подбор числа колонок
    public int getAutoColsNum(List<HashMap<String, String>> itemsArray, String polName, String columnsAlgIntensity) {
        // implementation - via percentiles len
        int auto_cols_num = 1;
        ArrayList<Integer> tmp = new ArrayList<Integer>();

        if (itemsArray.size() > 0) {
            int factor ;
            for (HashMap<String, String> anItemsArray : itemsArray) {
                if (anItemsArray != null) {
                    tmp.add(anItemsArray.get(polName).length());
                }
            }
            String[] spat = columnsAlgIntensity.split("[\\s\\:]+");
            int quantile = Integer.parseInt(spat[0]);
            factor = Percentile(tmp, quantile);
            for (int i = 1; i < spat.length; i = i + 2) {
                try {
                    double fval = Double.parseDouble(spat[i]);
                    int cval = Integer.parseInt(spat[i + 1]);
                    if (factor <= fval) {
                        auto_cols_num = cval;
                        break;
                    }
                } catch (Exception e) {
                    // emply
                }
            }
        }
        if (auto_cols_num > itemsArray.size())
            auto_cols_num = itemsArray.size();
        return auto_cols_num;
    }
    private int Percentile(ArrayList<Integer> values, int Quantile){
    // not fully "mathematical proof", but not too difficult and working
        Collections.sort(values);
        int index = (values.size() * Quantile) / 100;
        return values.get(index);
    }

    public String getLocalCard(){
        BufferedReader buf_reader = null;
        StringBuilder listDrive = new StringBuilder();

        try {
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;

            while ((line = buf_reader.readLine()) != null) {
                if (line.contains("vfat") || line.contains("/mnt")) {
                    if (line.contains("/dev/block/vold") || line.contains("/dev/block//vold")) {
                        if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs")) {
                            StringTokenizer tokens = new StringTokenizer(line, " ");
                            tokens.nextToken(); //device
                            String mount_point = tokens.nextToken(); //mount point
                            if (listDrive.length() > 1){
                                listDrive.append(",");
                            }
							if (mount_point.trim().length() > 0) {
								listDrive.append(mount_point);
							}
                        }
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {
                    //emply
                }
            }
        }

        return listDrive.toString();
    }

    public Bitmap JobIcon(String job){
        int iconID;
        if ("FAVDOCN".equals(job)) {// страница фаворитов документов
            iconID = R.drawable.ci_fav;
        } else if ("LRUN".equals(job)) {// страница запущенных ДОКУМЕНТОВ
            iconID = R.drawable.ci_lre;
        } else if ("HOMEN".equals(job)) {// страница домашних папок
            iconID = R.drawable.ci_home;
        } else if ("HOMEMENU".equals(job)) {// всплывающее меню домашних папок
            iconID = R.drawable.ci_home;
        } else if ("HOMESCREEN".equals(job)) {// экран домашних папок
            iconID = R.drawable.ci_home;
        } else if ("LRUMENU".equals(job)) {// всплывающее меню запущенных документов
            iconID = R.drawable.ci_lre;
        } else if ("LRUSCREEN".equals(job)) {// экран запущенных документов
            iconID = R.drawable.ci_lre;
        } else if ("FAVDOCMENU".equals(job)) {// всплывающее меню фаворитов документов
            iconID = R.drawable.ci_fav;
        } else if ("FAVDOCSCREEN".equals(job)) {// экран фаворитов документов
            iconID = R.drawable.ci_fav;
        } else if ("ADVANCED".equals(job)) {// расширенные настройки
            iconID = R.drawable.ci_tools;
        } else if ("SETTINGS".equals(job)) {// настройки
            iconID = R.drawable.relaunch_settings;
        } else if ("APPMANAGER".equals(job)) {// все приложения
            iconID = R.drawable.ci_cpu;
        } else if ("BATTERY".equals(job)) {// показ расхода по приложениям
            iconID = R.drawable.bat1_big;
        } else if ("FAVAPP".equals(job)) {// всплывающее меню фаворитов приложений
            iconID = R.drawable.ci_fava;
        } else if ("ALLAPP".equals(job)) {// все приложения
            iconID = R.drawable.ci_grid;
        } else if ("LASTAPP".equals(job)) {// последние запущенные приложения
            iconID = R.drawable.ci_lrea;
        } else if ("SEARCH".equals(job)) {// поиск
            iconID = R.drawable.ci_search;
        } else if ("LOCK".equals(job)) {// блокировка устройства
            iconID = R.drawable.ci_lock;
        } else if ("POWEROFF".equals(job)) { // выключение устройства
            iconID = R.drawable.ci_power;
		} else if ("REBOOT".equals(job)) { // выключение устройства
			iconID = R.drawable.ci_reboot;
        } else if ("SWITCHWIFI".equals(job)) {// выключение WiFi
            WifiManager wifiManager;
            wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                iconID = R.drawable.ci_wifi_on;
            }else{
                iconID = R.drawable.ci_wifi_off;
            }
        } else if ("DROPBOX".equals(job)) {// запуск Dropbox
            iconID = R.drawable.ci_dropbox;
        } else if ("OPDS".equals(job)) {// запуск OPDS
            iconID = R.drawable.ci_books;
        } else if ("FTP".equals(job)) {// запуск FTP
            iconID = R.drawable.ci_ftp;
        } else if ("SYSSETTINGS".equals(job)) {// системные настройки
            iconID = R.drawable.ci_gear2;
        } else if ("UPDIR".equals(job)) {// в родительскую папку
            iconID = R.drawable.ci_levelup_big;
        } else if ("UPSCROOL".equals(job)) {// пролистывание на экран вверх
            iconID = R.drawable.ci_arrowup_big;
        } else if ("DOWNSCROOL".equals(job)) {// пролистывае на экран вниз
            iconID = R.drawable.ci_arrowdown_big;
        } else{
			iconID = R.drawable.file_notok;
		}

        return BitmapFactory.decodeResource(getResources(), iconID);
    }

	//===== Прокрутка списка на один экран вниз =====
	public void TapDownScrool(GridView gridView, int gVsize) {
		if (N2DeviceInfo.EINK_NOOK) { // nook special
			NOOKScrool(gridView, true);
		} else { // other devices
			int first = gridView.getFirstVisiblePosition();
			int last = gridView.getLastVisiblePosition();
			int shift = 0;
			// для ониксов уменьшаем число пролистываемых элементов
			if (N2DeviceInfo.EINK_ONYX || N2DeviceInfo.EINK_GMINI || N2DeviceInfo.EINK_BOEYE){
				shift = -1;
			}
			if (gVsize != last + 1) {
				int target = last + 1;
				if (target > (gVsize - 1)) {
					target = gVsize - 1;
				}
				RepeatedDownScroll(gridView, first, target, shift);
			}
		}
	}
	//===== Прокрутка списка на один экран вверх =====
	public void TapUpScrool(GridView gridView, int gVsize) {
		if (N2DeviceInfo.EINK_NOOK) { // nook
			NOOKScrool(gridView, false);
		} else { // other devices
			int first = gridView.getFirstVisiblePosition();
			int visible = gridView.getLastVisiblePosition() - first + 1;
			first -= visible;
			// для ониксов уменьшаем число пролистываемых элементов
			if (N2DeviceInfo.EINK_ONYX || N2DeviceInfo.EINK_GMINI || N2DeviceInfo.EINK_BOEYE){
				first++;
			}
			if (first < 0) {
				first = 0;
			}
			gridView.setSelection(first);
			// some hack workaround against not scrolling in some cases
			if (gVsize > 0) {
				gridView.requestFocusFromTouch();
				gridView.setSelection(first);
			}
		}
	}
	private void NOOKScrool(GridView gridView, boolean downScrool) {// nook special
			MotionEvent ev;
			int actionUp, actionMove, actionDown;
		if (downScrool){
			actionDown = 200;
			actionMove = 100;
			actionUp = 100;
		}else{
			actionDown = 100;
			actionMove = 200;
			actionUp = 200;
		}
			ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
					SystemClock.uptimeMillis(),
					MotionEvent.ACTION_DOWN, 200, actionDown, 0);
			if (ev != null) {
				gridView.dispatchTouchEvent(ev);
			}
			ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
					SystemClock.uptimeMillis() + 100,
					MotionEvent.ACTION_MOVE, 200, actionMove, 0);
			if (ev != null) {
				gridView.dispatchTouchEvent(ev);
			}
			SystemClock.sleep(100);
			ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
					SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
					200, actionUp, 0);
			if (ev != null) {
				gridView.dispatchTouchEvent(ev);
			}
	}
	public void RepeatedDownScroll(GridView gridView, int first, int target, int shift) {
		int total = gridView.getCount();
		int last = gridView.getLastVisiblePosition();
		if (total == last + 1)
			return;
		final int ftarget = target + shift;
		gridView.clearFocus();
		final GridView finalTempGV = gridView;
		gridView.post(new Runnable() {
			public void run() {
				finalTempGV.setSelection(ftarget);
			}
		});
		final int ffirst = first;
		final int fshift = shift;
		final GridView finalTempGV1 = gridView;
		gridView.postDelayed(new Runnable() {
			public void run() {
				int nfirst = finalTempGV1.getFirstVisiblePosition();
				if (nfirst == ffirst) {
					RepeatedDownScroll(finalTempGV1, ffirst, ftarget, fshift + 1);
				}
			}
		}, 150);
	}

	//===============
	public void LaunchReader(String fullFileName){
		if (askIfAmbiguous) {
			List<String> rdrs = readerNames(fullFileName);
			if (rdrs.size() == 1) {
				start(launchReader(rdrs.get(0), fullFileName));
			}else if (rdrs.size() > 1){
				final CharSequence[] applications = rdrs.toArray(new CharSequence[rdrs.size()]);
				final String rdr1 = fullFileName;
				AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunchApp.this);
				// "Select application"
				builder.setTitle(getResources().getString( R.string.jv_relaunch_select_application));
				builder.setSingleChoiceItems(applications, -1,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int i) {
								start(launchReader((String) applications[i],rdr1));
								dialog.dismiss();
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
		} else{
			start(launchReader(readerName(fullFileName),fullFileName));
		}
	}
	private void start(Intent i) {
		if (i != null)
			try {
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(ReLaunchApp.this,getResources().getString(R.string.jv_relaunch_activity_not_found),
						Toast.LENGTH_LONG).show();
			}
	}
}
