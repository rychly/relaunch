package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.util.*;


public class ReLaunchApp extends Application {
	final String TAG = "ReLaunchApp";

	private final String defReaders = ".epub:Intent:application/epub+zip"
			+ "|.fb2:Intent:application/fb2"
			+ "|.fb2.zip:Intent:application/fb2.zip"
			+ "|.jpg,.jpeg:Intent:image/jpeg"
			+ "|.png:Intent:image/png"
			+ "|.pdf:Intent:application/pdf"
			+ "|.djvu:Intent:application/djvu"
			+ "|.djv:Intent:application/djv"
			+ "|.djv,.djvu:Intent:image/vnd.djvu"
			+ "|.doc:Intent:application/msword"
			+ "|.chm,.pdb,.prc,.mobi,.azw:org.coolreader%org.coolreader.CoolReader%Cool Reader"
			+ "|.cbz,.cb7:Intent:application/x-cbz"
			+ "|.cbr:Intent:application/x-cbr";
	// Pending intent to self restart
	public PendingIntent RestartIntent;
	// Miscellaneous public flags/settings
	boolean fullScreen = false;
	boolean hideTitle = true;
	Boolean askIfAmbiguous;
	public boolean customScrollDef = true;
	// Search values
	final String DIR_TAG = ".DIR..";
	// Max file sizes
	public int viewerMax;
	int editorMax;
	// Scrolling related values
	int scrollStep;

	//public String DATA_DIR;

	static private HashMap<String, List<String[]>> m = new HashMap<>();
	private List<HashMap<String, String>> readers;

	// Readers
	public List<HashMap<String, String>> getReaders() {
		return readers;
	}
	public void setReaders(List<HashMap<String, String>> r) {
		readers = r;
	}
	void setReaders() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String typesString = prefs.getString("types", defReaders);
		readers = parseReadersString(typesString);
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
		List<String> rc = new ArrayList<>();
		for (HashMap<String, String> r : readers) {
			for (String key : r.keySet()) {
				if (file.endsWith(key) && !rc.contains(r.get(key)))
					rc.add(r.get(key));
			}
		}
		return rc;
	}

	// set list by name
	public void setDefault(String name) {
		// Set list default
		m.put(name, new ArrayList<String[]>());
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

	void About(final Activity a) {
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



	public void generalOnResume(String name) {
		Log.d(TAG, "--- onResume(" + name + ")");
	}

    public void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
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

	// проверка интернет соединения взята отседова:
	// http://stackoverflow.com/questions/7071578/connectivitymanager-to-verify-internet-connection
	public boolean haveNetworkConnection(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfoMob = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo netInfoWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return ((netInfoMob != null && netInfoMob.isConnectedOrConnecting()) || (netInfoWifi != null) && netInfoWifi.isConnectedOrConnecting());
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
	void downScrollPercent(GridView gvList, int gVsize, boolean disableScrollJump) {
		if (!disableScrollJump) {
			int first = gvList.getFirstVisiblePosition();
			int total = gVsize;
			int last = gvList.getLastVisiblePosition();
			if (total == last + 1)
				return;
			int target = first + (total * scrollStep) / 100;
			if (target <= last)
				target = last + 1; // Special for NOOK, otherwise it
			// won't redraw the listview
			if (target > (total - 1))
				target = total - 1;
			//RepeatedDownScroll ds = new RepeatedDownScroll();
			//ds.doIt(first, target, 0);
			RepeatedDownScroll(gvList, first, target, 0);
		}
	}
	void downScrollEnd(GridView gvList, int gVsize, boolean disableScrollJump) {
		if (!disableScrollJump) {
			int first = gvList.getFirstVisiblePosition();
			int total = gVsize;
			int last = gvList.getLastVisiblePosition();
			if (total == last + 1)
				return;
			int target = total - 1;
			RepeatedDownScroll(gvList, first, target, 0);
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
	void upScrollPercent(GridView gvList, int gVsize, boolean disableScrollJump) {
		if (!disableScrollJump) {
			int first = gvList.getFirstVisiblePosition();
			int total = gVsize;
			first -= (total * scrollStep) / 100;
			if (first < 0)
				first = 0;
			gvList.setSelection(first);
			// some hack workaround against not scrolling in some cases
			if (total > 0) {
				gvList.requestFocusFromTouch();
				gvList.setSelection(first);
			}
		}
	}
	void upScrollBegin(GridView gvList, int gVsize, boolean disableScrollJump) {
		if (!disableScrollJump) {
			int first = gvList.getFirstVisiblePosition();
			int total = gVsize;
			int last = gvList.getLastVisiblePosition();
			if (total == last + 1)
				return;
			int target = total - 1;
			RepeatedDownScroll(gvList, first, target, 0);
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

	private static List<HashMap<String, String>> parseReadersString(String readerList) {
		List<HashMap<String, String>> rc = new ArrayList<>();
		String[] rdrs = readerList.split("\\|");
		for (String rdr : rdrs) {
			String[] re = rdr.split(":");
			switch (re.length) {
				case 2:
					String rName = re[1];
					String[] exts = re[0].split(",");
					for (String ext : exts) {
						HashMap<String, String> r = new HashMap<>();
						r.put(ext, rName);
						rc.add(r);
					}
					break;
				case 3:
					if (re[1].equals("Intent")) {
						String iType = re[2];
						String[] exts1 = re[0].split(",");
						for (String ext1 : exts1) {
							HashMap<String, String> r = new HashMap<>();
							r.put(ext1, "Intent:" + iType);
							rc.add(r);
						}
					}
					break;
			}
		}
		return rc;
	}

	private void setLanguage(SharedPreferences prefs) {
		String lang = prefs.getString("lang", "default");
		if (!lang.equals("default")) {
			Locale locale = new Locale(lang);
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			getBaseContext().getResources().updateConfiguration(config, null);
		}
	}
	private void setFullScreenIfNecessary(Activity a) {
		if (!hideTitle) {
			a.requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		if (fullScreen) {
			a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

	}
	public void setOptionsWindowActivity(Activity activity) {
		setFullScreenIfNecessary(activity);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		setLanguage(prefs);
		ScreenOrientation.set(activity, prefs);
		EinkScreen.setEinkController(prefs);
	}
}
