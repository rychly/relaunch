package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.PendingIntent;
import android.content.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

public class ReLaunchApp extends Application {
	final String TAG = "ReLaunchApp";

	// booting state
	public boolean booted = false;

	// Reading files
	final int FileBufferSize = 1024;

	// Pending intent to self restart
	public PendingIntent RestartIntent;

	// Miscellaneous public flags/settings
	public boolean fullScreen = false;
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

	public final String DATA_DIR = "/data/data/com.harasoft.relaunch";

	public HashMap<String, Integer> history = new HashMap<String, Integer>();
	public HashMap<String, Integer> columns = new HashMap<String, Integer>();
	private HashMap<String, List<String[]>> m = new HashMap<String, List<String[]>>();
	private HashMap<String, Drawable> icons;
	private List<HashMap<String, String>> readers;
	private List<String> apps;

	public BooksBase dataBase;

	// Icons
	public HashMap<String, Drawable> getIcons() {
		return icons;
	}

	public void setIcons(HashMap<String, Drawable> i) {
		icons = i;
	}

	// Applications
	public List<String> getApps() {
		return apps;
	}

	public void setApps(List<String> a) {
		apps = a;
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
	/*
	 * Misc lists management (list is identified by name)
	 * --------------------------------------------------
	 */

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
		List<String[]> nl = new ArrayList<String[]>();
		/*if (name.equals("lastOpened")) {
		} else if (name.equals("favorites")) {
		}  */
		m.put(name, nl);
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
				appLruMax = Integer.parseInt(prefs
						.getString("appLruSize", "30"));
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
				fullName = fullName.substring(0,
						fullName.length() - DIR_TAG.length() - 1);
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
		removeFromList_internal(listName, dr, fn);
	}

	public void removeFromList_internal(String listName, String dr, String fn) {
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
                //emply
			}
		}
		try {
			fos.close();
		} catch (IOException e) {
            //emply
		}
	}

	// Remove file
	public boolean removeFile(String dr, String fn) {
		boolean rc = false;
		String fullName = dr + "/" + fn;
		removeFromList("lastOpened", dr, fn);
		saveList("lastOpened");
		removeFromList("favorites", dr, fn);
		saveList("favorites");
		removeFromList("history", dr, fn);
		history.remove(fullName);
		saveList("history");
		File f = new File(fullName);
		if (f.exists()) {
			try {
				rc = f.delete();
			} catch (SecurityException e) {
                //emply
			}
		}
		return rc;
	}
	public boolean removeDirectory(String dr, String fn) {
		boolean rc = false;
		String dname = dr + "/" + fn;
		File d = new File(dname);
		File[] allEntries = d.listFiles();
        if(allEntries == null){
            return false;
        }
		for (File f : allEntries) {
			if (f.isDirectory()) {
				if (!removeDirectory(dname, f.getName()))
					return false;
			} else {
				if (!removeFile(dname, f.getName()))
					return false;
			}
		}
		try {
			rc = d.delete();
		} catch (SecurityException e) {
            //emply
		}
		return rc;
	}
    public boolean removeDirAndFile(String dir, String file) {
        String dname = dir + "/" + file;
        File d = new File(dname);
        if (d.isDirectory()) {
            if (!removeDirectory(dir, file))
                return false;
        } else {
            if (!removeFile(dir, file))
                return false;
        }
        return true;
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
            if(!toDir.mkdirs()){
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
    public boolean copyDirOrFile(String from, String to, boolean rewrite) {
        File source = new File(from);
        if (source.isFile()) {
            if (!copyFile(from, to, rewrite)){
                return false;
            }
        } else {
            if (!copyDir(from, to)){
                return false;
            }
        }
        return true;
    }
	//Move file src to dst
	public boolean moveFile(String from, String to) {
		boolean ret = false;
		if (from.split("/")[0].equalsIgnoreCase(to.split("/")[0])) {
			File src = new File(from);
			File dst = new File(to);
			ret = src.renameTo(dst);
		} else {
			if (copyFile(from, to, false)) {
                File f = new File(from);
                ret = removeFile(f.getParent(), f.getName());
            }
		}
		return ret;
	}
    //
	public boolean createDir(String dst) {
		File dir = new File(dst);
		return dir.mkdirs();
	}
	
	// common utility - get intent by label, null if not found
	public Intent getIntentByLabel(String label) {
		String[] labelp = label.split("\\%");
        if(labelp.length > 1 && labelp[0] != null && labelp[1] != null) {
            Intent i = new Intent();
            i.setComponent(new ComponentName(labelp[0], labelp[1]));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            return i;
        }
        return null;
	}

	// common utility - return intent to launch reader by reader name and full
	// file name. Null if not found
	public Intent launchReader(String name, String file) {
		String re[] = name.split(":");
		if (re.length == 2 && re[0].equals("Intent")) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); - ALREADY DONE! in
				// getIntentByLabel
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
			if (history.containsKey(fullName))
				return history.get(fullName) != FINISHED;
			else
				return true;
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

	public Drawable specialIcon(String s, boolean isDir) {
		if (isDir)
			return getResources().getDrawable(R.drawable.dir_ok);
		if (s.endsWith(".apk"))
			return getResources().getDrawable(R.drawable.install);
		return null;
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
		try {
			vers = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
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
		if (fullScreen) {
			a.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
		if (!tDir.exists())
			if (!tDir.mkdir())
				return false;
		tDir = new File(toDir.getAbsolutePath() + "/shared_prefs"); 
		if (!tDir.exists())
			if (!tDir.mkdir())
				return false;
		String[] files = {"AppFavorites.txt", "AppLruFile.txt", "Columns.txt", "Filters.txt", "History.txt", "LruFile.txt"};
		for (String f : files) {
			String src = fromDir.getAbsolutePath() + "/files/" + f;
			String dst = toDir.getAbsolutePath() + "/files/" + f;
			copyFile(src, dst, true);
		}
		String src = fromDir.getAbsolutePath() + "/shared_prefs/com.harasoft.relaunch_preferences.xml";
		String dst = toDir.getAbsolutePath() + "/shared_prefs/com.harasoft.relaunch_preferences.xml";
        return copyFile(src, dst, true);
    }

	public boolean isStartDir(String dir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String startDirs = prefs.getString("startDir", "/sdcard,/media/My Files");
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
		String oldStart = prefs.getString("startDir", "/sdcard,/media/My Files");
		editor.putString("startDir", oldStart + "," + dir);
		editor.commit();
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
                tmp.add(anItemsArray.get(polName).length());
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
    private int Percentile(ArrayList<Integer> values, int Quantile)
    // not fully "mathematical proof", but not too difficult and working
    {
        Collections.sort(values);
        int index = (values.size() * Quantile) / 100;
        return values.get(index);
    }
}
