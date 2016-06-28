package com.harasoft.relaunch;

import android.app.*;
import android.app.ActivityManager.MemoryInfo;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextUtils.TruncateAt;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.*;
import com.dropbox.client2.DropboxAPI;
import com.stericson.RootTools.RootTools;
import ebook.EBook;
import ebook.parser.InstantParser;
import ebook.parser.Parser;
import it.sauronsoftware.ftp4j.*;

import java.io.*;
import java.lang.Process;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReLaunch extends Activity {

	final static String TAG = "ReLaunch";
	static public final String APP_LRU_FILE = "AppLruFile.txt";
	static public final String APP_FAV_FILE = "AppFavorites.txt";
	static public final String LRU_FILE = "LruFile.txt";
	static public final String FAV_FILE = "Favorites.txt";
	static public final String HIST_FILE = "History.txt";
	static public final String FILT_FILE = "Filters.txt";
	static public final String COLS_FILE = "Columns.txt";
	final String defReaders = ".epub:Intent:application/epub+zip"
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
	final static public String defReader = "org.coolreader%org.coolreader.CoolReader%Cool Reader";
	final static public int TYPES_ACT = 1;
	final static public int DIR_ACT = 2;
	final static int CNTXT_MENU_DELETE_F = 1;
	final static int CNTXT_MENU_DELETE_D_EMPTY = 2;
	final static int CNTXT_MENU_DELETE_D_NON_EMPTY = 3;
	final static int CNTXT_MENU_ADD = 4;
	final static int CNTXT_MENU_CANCEL = 5;
	final static int CNTXT_MENU_MARK_READING = 6;
	final static int CNTXT_MENU_MARK_FINISHED = 7;
	final static int CNTXT_MENU_MARK_FORGET = 8;
	final static int CNTXT_MENU_INTENT = 9;
	final static int CNTXT_MENU_OPENWITH = 10;
	final static int CNTXT_MENU_COPY_FILE = 11;
	final static int CNTXT_MENU_MOVE_FILE = 12;
	final static int CNTXT_MENU_PASTE = 13;
	final static int CNTXT_MENU_RENAME = 14;
	final static int CNTXT_MENU_CREATE_DIR = 15;
	final static int CNTXT_MENU_SWITCH_TITLES = 16;
	final static int CNTXT_MENU_TAGS_RENAME = 17;
	final static int CNTXT_MENU_ADD_STARTDIR = 18;
	final static int CNTXT_MENU_SHOW_BOOKINFO = 19;
	final static int CNTXT_MENU_FILE_INFO = 20;
	final static int CNTXT_MENU_SET_STARTDIR = 21;
    final static int CNTXT_MENU_COPY_DROPBOX = 22;
    final static int CNTXT_MENU_COPY_DIR_DROPBOX = 23;
    final static int CNTXT_MENU_SETTINGS = 24;
    final static int CNTXT_MENU_SELECTE = 25;
    final static int CNTXT_MENU_FILEUNZIP = 26;
    final static int CNTXT_MENU_PERMISSION = 27;

	final static int SORT_FILES_ASC = 0;
	final static int SORT_FILES_DESC = 1;
    final static int SORT_DATES_ASC = 2;
    final static int SORT_DATES_DESC = 3;
    final static int SORT_SIZES_ASC = 4;
    final static int SORT_SIZES_DESC = 5;
	final static int SORT_TITLES_ASC = 6;
	final static int SORT_TITLES_DESC = 7;


	static String currentRoot;
	static int currentPosition = -1;
	List<HashMap<String, String>> itemsArray;
	Stack<Integer> positions = new Stack<Integer>();
	SimpleAdapter adapter;
	static SharedPreferences prefs;
    static SharedPreferences.Editor prefsEditor;
	ReLaunchApp app;

	static public boolean filterMyself = true;
	String[] allowedModels;
	String[] allowedDevices;
	String[] allowedManufacts;
	String[] allowedProducts;
	boolean addSView = true;
    static boolean disableScrollJump;

	// multicolumns per directory configuration
	int currentColsNum = -1;

	// Bottom info panel
	boolean mountReceiverRegistered = false;
	boolean powerReceiverRegistered = false;
	boolean wifiReceiverRegistered = false;
	TextView memTitle;
	TextView memLevel;
	TextView battTitle = null;
	TextView battLevel;
    static ImageButton battLevelSec = null;
    static ImageButton wifiOp = null;

	static String fileOpFile[];
	static String fileOpDir[];
    static int ftpID;
	static int fileOp;
	static String sortType = "sname";
	static boolean sortOrder = true;
    static ArrayList<imageIcon> arrIcon = new ArrayList<imageIcon>();
    ArrayList<Integer> arrSelItem = new ArrayList<Integer>();

    private static Locale locale;
    static boolean fileExtendedData;
    static String fileExtendedDataFormat;
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
    static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    // ====== FLSimpleAdapter==================================================================
    boolean useFaces;
    int firstLineIconSizePx;
    int firstLineFontSizePx;
    int secondLineFontSizePx;
    boolean doNotHyph;
    boolean hideKnownExts;
    boolean showBookTitles;
    boolean rowSeparator;
    static ArrayList<String> exts;
    String currentHomeDir = null;
	boolean f_onCreate = false;
    //=================================================================================
    // ====== GetDateAndMewory()==================================================================
    boolean dateUS;
    //=================================================================================
    // ====== getAutoColsNum==================================================================
    static String columnsAlgIntensity;
    //=================================================================================
    // ====== redrawList==================================================================
    boolean filterResults;
    //=================================================================================
    // ====== setUpButton==================================================================
    boolean notLeaveStartDir;
    static String[] startDir;
    Button upButton;
    String upDir = "";
    //=================================================================================
    // ====== drawDirectory==================================================================
    boolean showFullDirPath;
    boolean showHidden;
    boolean showOnlyKnownExts;
    boolean showButtonParenFolder;
    String bookTitleFormat;
    Button tv_title;
    GridView gvList;
    Button upScroll;
    Button downScroll;
    //=================================================================================
    // for dropbox
    static MyDropboxClient dropboxClient;
    //===================================================
    // for FTP
    static FTPConnector connectorFTP;
    static int ID_FTP;
    static String FTPTempDir;
    //===================================================
    static boolean blockExitLauncher = true;
	static boolean selectRootNavigation = false;
    static boolean accessRoot = false;
	static boolean showFileOperation;

	private void actionSwitchWiFi() {
		WifiManager wifiManager;
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			// "WiFi is off"
			Toast.makeText(
					ReLaunch.this,
					getResources().getString(
							R.string.jv_relaunch_turning_wifi_off),
					Toast.LENGTH_SHORT).show();
			wifiManager.setWifiEnabled(false);
		} else {
			// "WiFi is ON"
			Toast.makeText(
					ReLaunch.this,
					getResources().getString(
							R.string.jv_relaunch_turning_wifi_on),
					Toast.LENGTH_SHORT).show();
			wifiManager.setWifiEnabled(true);
		}
	}

	private void actionLock() {
		PowerFunctions.actionLock(ReLaunch.this, accessRoot);
	}

	private void actionPowerOff() {
		PowerFunctions.actionPowerOff(ReLaunch.this, accessRoot);
	}

	private void actionReboot() {
		PowerFunctions.actionReboot(ReLaunch.this, accessRoot);
	}

	private void saveLast() {
		int appLruMax = 30;
		try {
			appLruMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
		} catch (NumberFormatException e) {
            // emply
		}
		app.writeFile("app_last", ReLaunch.APP_LRU_FILE, appLruMax, ":");
	}

	private boolean checkField(String[] a, String f) {
        for (String anA : a) {
            if (anA.equals("*") || anA.equals(f))
                return true;
        }
		return false;
	}

	private void checkDevice(String dev, String man, String model,String product) {
		if (checkField(allowedModels, model))
			return;
		if (checkField(allowedDevices, dev))
			return;
		if (checkField(allowedManufacts, man))
			return;
		if (checkField(allowedProducts, product))
			return;

		if (!prefs.getBoolean("allowDevice", false)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			WebView wv = new WebView(this);
			wv.loadDataWithBaseURL(null,
					getResources().getString(R.string.model_warning),
					"text/html", "utf-8", null);
			// "Wrong model !"
			builder.setTitle(getResources().getString(R.string.jv_relaunch_wrong_model));
			builder.setView(wv);
			// "YES"
			builder.setPositiveButton(
					getResources().getString(R.string.app_yes),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
                            prefsEditor.putBoolean("allowDevice", true);
                            prefsEditor.commit();
							dialog.dismiss();
						}
					});
			// "NO"
			builder.setNegativeButton(
					getResources().getString(R.string.app_no),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							finish();
						}
					});

			builder.show();
		}
	}

	static class ViewHolder {
		TextView tv;
		TextView tv2;
		ImageView iv;
		ImageView is;
		LinearLayout tvHolder;
        int position;
	}

	private Bitmap scaleDrawableById(int id, int size) {
		return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), id), size, size,true);
	}

	private Bitmap scaleDrawable(Drawable d, int size) {
		return Bitmap.createScaledBitmap(((BitmapDrawable) d).getBitmap(),size, size, true);
	}

	class FLSimpleAdapter1 extends SimpleAdapter {
		LayoutInflater vi;
		FLSimpleAdapter1(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
			vi = (LayoutInflater) app.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
            if(itemsArray == null){
                return 0;
            }else{
			    return itemsArray.size();
            }
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder; // перечень элементов на создаваемом объекте
			View v = convertView; // передаваемый тэг ??????
            HashMap<String, String> item = itemsArray.get(position); // получаем карту для указанной позиции
            if (v == null) {  // если нет объекта то создаем его
				v = vi.inflate(R.layout.flist_layout, parent, false);
                if(v == null){
                    return null;
                }
				holder = new ViewHolder();
				holder.tv = (TextView) v.findViewById(R.id.fl_text);
				holder.tv2 = (TextView) v.findViewById(R.id.fl_text2);
				holder.iv = (ImageView) v.findViewById(R.id.fl_icon);
				holder.is = (ImageView) v.findViewById(R.id.fl_separator);
				holder.tvHolder = (LinearLayout) v.findViewById(R.id.grid_cell);
                holder.position = position;
                // проверяем на однострочный режим
                if (doNotHyph) {
                    holder.tv.setLines(1); // первой - только одна строка
                    holder.tv.setHorizontallyScrolling(true); // разрешить прокрутку по горизонтали
                    holder.tv.setEllipsize(TruncateAt.END); // многоточие на конце видимой части
                    holder.tv2.setLines(1);  // только одна строка
                    holder.tv2.setHorizontallyScrolling(true); // прокрутка
                    holder.tv2.setEllipsize(TruncateAt.END);   // многоточие
                }
                // если разделитель запрещен
                if (!rowSeparator){
                    holder.is.setVisibility(View.GONE);// выключаем его
                }

				v.setTag(holder);
			} else{
				holder = (ViewHolder) v.getTag();
            }
            if (firstLineIconSizePx == 0) { // если отключены картинки
                holder.iv.setVisibility(View.GONE); // скрываем поле с ними
            }else {
                String temp_nameIcon = item.get("nameIcon");
                for (imageIcon anArrIcon : arrIcon) {
                    if (anArrIcon.nameIcon.equals(temp_nameIcon)) {
                        holder.iv.setImageBitmap(anArrIcon.icon);
                        break;
                    }
                }
            }
            // проверяем на существование
			if (item != null) {
                // ============================================
                // получаем полное имя файла с путем
				String fname = item.get("fname");
                // выделение жирым
				boolean setBold = false;

                // устанавливаем размеры шрифтов
                holder.tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, firstLineFontSizePx);
                holder.tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, secondLineFontSizePx);

                // определяем папка это или файл
                int color_txt = getResources().getColor(R.color.file_new_fg);
                int backgroung_txt = getResources().getColor(R.color.file_new_bg);

				if (item.get("type").equals("dir")) {
                    holder.tv2.setVisibility(View.GONE); // скрываем вторую строку
				} else {
					if (useFaces) {  // прочитанные/новые
						if (app.history.containsKey(fname)) { // смотрим историю по файлу
                            int baseHistory = app.history.get(fname);
							if (baseHistory == app.READING) { // если в базе - читается
                                color_txt = getResources().getColor(R.color.file_reading_fg);
                                backgroung_txt = getResources().getColor(R.color.file_reading_bg);
 							} else if (baseHistory == app.FINISHED) { // если в базе прочитано
                                color_txt = getResources().getColor(R.color.file_finished_fg);
                                backgroung_txt = getResources().getColor(R.color.file_finished_bg);
							} else { // при других раскладах
                                color_txt = getResources().getColor(R.color.file_unknown_fg);
                                backgroung_txt = getResources().getColor(R.color.file_unknown_bg);
							}
						} else { // если файл определен как новый
                            color_txt = getResources().getColor(R.color.file_new_fg);
                            backgroung_txt = getResources().getColor(R.color.file_new_bg);
							//if (getResources().getBoolean(R.bool.show_new_as_bold))
							setBold = true;
						}
					}

				}
                if(arrSelItem.contains(position)){
                    holder.tv.setTextColor(backgroung_txt);
                    holder.tv2.setTextColor(backgroung_txt);
                    holder.tvHolder.setBackgroundColor(getResources().getColor(R.color.file_finished_bg));
                }else{
                    holder.tv.setTextColor(color_txt);
                    holder.tv2.setTextColor(color_txt);
                    holder.tvHolder.setBackgroundColor(backgroung_txt);
                }

                String sname = item.get("sname"); // получаем имя
                // делаем копию в первую строку
				String sname1 = sname;
                // пустая вторая
				String sname2 = "";
                // находим номер места с символом перевода каретки ??? накуя?
				int newLinePos = sname.indexOf('\n');

				if (newLinePos > 0) {// если есть такой, то разбиваем на подстроки
					sname1 = sname.substring(0, newLinePos).trim();
					sname2 = sname.substring(newLinePos, sname.length()).trim();
				}
                // если идет отображение прочитанных/новых
				if (useFaces) {
					SpannableString s1 = new SpannableString(sname1);
					s1.setSpan(new StyleSpan(setBold ? Typeface.BOLD : Typeface.NORMAL), 0, sname1.length(), 0);
                    holder.tv.setText(s1);
					if (!sname2.equalsIgnoreCase("")) {
						SpannableString s2 = new SpannableString(sname2);
						s2.setSpan(new StyleSpan(setBold ? Typeface.BOLD : Typeface.NORMAL), 0, sname2.length(), 0);
                        holder.tv2.setText(s2);
					}
				} else {
                    holder.tv.setText(sname1);
                    holder.tv2.setText(sname2);
				}
                // если есть вторая строка, то вытаскиваем ее??? иначе скрываем
				if (sname2.equalsIgnoreCase("")) {
                    holder.tv2.setVisibility(View.GONE);
				} else {
                    holder.tv2.setVisibility(View.VISIBLE);
				}

			}
			// fixes on rows height in grid
            // если у грида не одна колонка, то выравниваем ячейки по высоте в одной строке
			if (currentColsNum != 1) {
				int colw = (gvList.getWidth()) / currentColsNum; // получаем ширину колонки
				int recalc_num = position; // номер позиции
				int recalc_height = 0;
                View temp_v;
				while (recalc_num % currentColsNum != 0) {  // находим последний элемент в строке
					recalc_num = recalc_num - 1;
					temp_v = getView(recalc_num, null, parent);
                    if(temp_v != null){
                        temp_v.measure(MeasureSpec.EXACTLY | colw, MeasureSpec.UNSPECIFIED);
                        int p_height = temp_v.getMeasuredHeight();
                        if (p_height > 0)
                            recalc_height = p_height;
                    }
				}
				if (recalc_height > 0) {
					v.setMinimumHeight(recalc_height);
				}
			}

			return v;
		}


	}

	private static List<HashMap<String, String>> parseReadersString(String readerList) {
		List<HashMap<String, String>> rc = new ArrayList<HashMap<String, String>>();
		String[] rdrs = readerList.split("\\|");
        for (String rdr : rdrs) {
            String[] re = rdr.split(":");
            switch (re.length) {
                case 2:
                    String rName = re[1];
                    String[] exts = re[0].split(",");
                    for (String ext : exts) {
                        HashMap<String, String> r = new HashMap<String, String>();
                        r.put(ext, rName);
                        rc.add(r);
                    }
                    break;
                case 3:
                    if (re[1].equals("Intent")) {
                        String iType = re[2];
                        String[] exts1 = re[0].split(",");
                        for (String ext1 : exts1) {
                            HashMap<String, String> r = new HashMap<String, String>();
                            r.put(ext1, "Intent:" + iType);
                            rc.add(r);
                        }
                    }
                    break;
            }
        }
		return rc;
	}

	public static String createReadersString(List<HashMap<String, String>> rdrs) {
		String rc = "";
		for (HashMap<String, String> r : rdrs) {
			for (String key : r.keySet()) {
				if (!rc.equals(""))
					rc += "|";
				rc += key + ":" + r.get(key);
			}
		}
		return rc;
	}

	private void pushCurrentPos(AdapterView<?> parent, boolean push_to_stack) {
		Integer p1 = parent.getFirstVisiblePosition();
		if (push_to_stack)
			positions.push(p1);
		currentPosition = p1;
	}

	private void setUpButton() {
		if (upButton != null) {
			if (CheckUpDir()) {
				upButton.setEnabled(true);
				upButton.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ci_levelup, 0, 0, 0);
			}else{
				upButton.setEnabled(false);
				upButton.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ci_levelup_gray, 0, 0, 0);
			}
		}
	}

    private void drawDirectory(String root, Integer startPosition) {
		// организуем проверку на выход из домашней папки
		if(notLeaveStartDir && !root.startsWith(currentHomeDir) ){
			root = currentHomeDir;
		}
		// организуем два массива для хранения имен папок и имен файлов
		List<String> files = new ArrayList<String>();
		List<Long> filesDate = new ArrayList<Long>();
		List<Long> filesSize = new ArrayList<Long>();
		List<String> dirs = new ArrayList<String>();
		// очищаем массив выделений
		arrSelItem.clear();
		// текущая позиция неизвестна (-1) или задана при вызове
		currentPosition = (startPosition == -1) ? 0 : startPosition;
		// создаем массив элементов списка
		itemsArray = new ArrayList<HashMap<String, String>>();
		upDir = "";
		if (showButtonParenFolder) {
			itemsArray.add(null); // помещаем элемент в массив
		}
		// выясняем что обрабатываем - локальную папку или аккаунт Dropbox
		if (root.startsWith("FTP| ")) {
			if (root.equals("FTP| ")) {
				root = "/";
			} else {
				root = root.substring("FTP| ".length());
			}

			if (root.equals("/")) {
				currentRoot = "FTP| ";
			}
			//connectorFTP.changeDirectory(root);
			FTPFile[] FTPlist = connectorFTP.ftpFilesList(root);
			if (FTPlist == null) {
				return;
			}
			// заполняем заголовок
			if (tv_title != null) {
				if (showFullDirPath) { // в зависимости от настроек
					// полный путь к текущей папки + ( число элементов в ней)
					tv_title.setText("FTP| " + root + " (" + FTPlist.length + ")");
				} else {
					// имя папки
					tv_title.setText("FTP| " + root.substring(root.lastIndexOf("/")));
				}
			}
			if (!(root.lastIndexOf("/") > 0)) {
				upDir = "FTP| /";
			} else {
				upDir = "FTP| " + root.substring(0, root.lastIndexOf("/"));
			}
			Date d;
			long milliseconds;
			for (FTPFile aFTPlist : FTPlist) {
				if (aFTPlist.getType() == FTPFile.TYPE_DIRECTORY) {
					dirs.add(aFTPlist.getName());
				} else if (!filterResults || app.filterFile(root, aFTPlist.getName())) {
					d = aFTPlist.getModifiedDate();
					milliseconds = d.getTime();
					filesDate.add(milliseconds);
					filesSize.add(aFTPlist.getSize());
					files.add(aFTPlist.getName());
				}
			}
		} else if (root.startsWith("Dropbox| ")) {
			root = root.substring("Dropbox| ".length());
			if (root.equals("/")) {
				currentRoot = "Dropbox| ";
			}
			// список файлов и папок
			DropboxAPI.Entry entries;
			entries = dropboxClient.metadata(root);// может быть ошибка при получении коллекции
			if (entries != null) {
				SimpleDateFormat f = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
				Date d;
				long milliseconds;
				long sysMillisec = System.currentTimeMillis();
				for (DropboxAPI.Entry e : entries.contents) {
					if (!e.isDeleted) {
						if (e.isDir) {
							dirs.add(e.fileName());
						} else if (!filterResults || app.filterFile(e.path, e.fileName())) {
							try {
								d = f.parse(e.modified);
								milliseconds = d.getTime();
							} catch (ParseException e1) {
								milliseconds = sysMillisec;
							}
							filesDate.add(milliseconds);
							filesSize.add(e.bytes);
							files.add(e.fileName());
						}
					}
				}
				// заполняем заголовок
				if (tv_title != null) {
					if (showFullDirPath) { // в зависимости от настроек
						// полный путь к текущей папки + ( число элементов в ней)
						tv_title.setText("Dropbox| " + entries.fileName() + " (" + entries.contents.size() + ")");
					} else {
						// имя папки
						tv_title.setText("Dropbox| " + entries.path);
					}
				}
				if (entries.parentPath() != null) {
					upDir = "Dropbox| " + entries.parentPath();
				}
			} else {
				drawDirectory(startDir[0], -1);
				return;
			}
		} else {
			if (root.equals("")) {
				root = "/";
				upDir = "";
			} else {
				upDir = root.substring(0, root.lastIndexOf("/"));
				if (upDir.equals("") && !root.equals("/")) {
					upDir = "/";
				}
			}
			// устанавливаем текущую папку корневой

			currentRoot = root;
			// получаем папку как объект
			File dir = new File(root);
			// получаем список подпапок и файлов
			File[] allEntries = dir.listFiles();
			// заполняем заголовок
			if (tv_title != null) {
				if (showFullDirPath) { // в зависимости от настроек
					// полный путь к текущей папки + ( число элементов в ней)
					tv_title.setText(currentRoot + " (" + ((allEntries == null) ? 0 : allEntries.length) + ")");
				} else {
					// имя папки
					tv_title.setText(dir.getName());
				}
			}
			// полученный массив элементов разбиваем на папки и файлы
			if (allEntries != null) {
				for (File allEntry : allEntries) {
					if (allEntry.isDirectory()) {
						dirs.add(allEntry.getName());
					} else if (!filterResults || app.filterFile(dir.getAbsolutePath(), allEntry.getName())) {
						filesDate.add(allEntry.lastModified());
						filesSize.add(allEntry.length());
						files.add(allEntry.getName());
					}
				}
			} else if (selectRootNavigation) {
				ArrayList<String[]> dirContent = RootCommands.listFiles(root, true);
				if (dirContent != null) {
					for (String[] element : dirContent) {
						if (element != null && element.length > 0) {
							if (element[0].startsWith("d")) {
								dirs.add(element[5]);
							} else {
								filesDate.add((long) 0);
								filesSize.add(Long.valueOf(element[3]));
								files.add(element[6]);
							}
						}
					}
				}
			}
		}
		if (showButtonParenFolder) {
			HashMap<String, String> item = new HashMap<String, String>(); //создаем карту и начинаем ее заполнять
			item.put("name", ".."); // имя папки
			item.put("sname", "..");// имя папки
			item.put("dname", currentRoot);  // полный путь к папке
			item.put("fname", "..");
			item.put("type", "dir");  // тип - папка
			item.put("reader", "Nope"); // программа обработчик не назначена
			if (firstLineIconSizePx != 0) {
				if (CheckUpDir()) {
					item.put("nameIcon", "parent_ok");
				} else {
					item.put("nameIcon", "parent_off");
				}
			}
			itemsArray.set(0, item); // помещаем элемент в массив
		}
		//сохраняем имя папки
		// lastdir = currentRoot;
		// сортируем папки
		Collections.sort(dirs);
		// перебираем папки
		for (String f : dirs) {
			if ((f.startsWith(".")) && (!showHidden)) { // если скрытая и указано не показывать - пропускаем
				continue;
			}
			HashMap<String, String> item = new HashMap<String, String>(); //создаем карту и начинаем ее заполнять
			item.put("name", f); // имя папки
			item.put("sname", f);// имя папки
			item.put("dname", currentRoot);  // полный путь к папке
			if (currentRoot.equals("/")) {// полный путь папки
				item.put("fname", currentRoot + f);
			} else {
				item.put("fname", currentRoot + File.separator + f);
			}
			item.put("type", "dir");  // тип - папка
			item.put("reader", "Nope"); // программа обработчик не назначена
			if (firstLineIconSizePx != 0) {
				item.put("nameIcon", "dir_ok");
			}
			itemsArray.add(item); // помещаем элемент в массив
		}
		// создаем массив карт для файлов
		List<HashMap<String, String>> fileItemsArray = new ArrayList<HashMap<String, String>>();
		// перебираем файлы
		String sname;
		String f;
		long date;
		long size;

		String tt;
		for (int i = 0, j = files.size(); i < j; i++) {
			f = files.get(i);
			if ((f.startsWith(".")) && (!showHidden)) { // если скрытый и указано не показывать - пропускаем
				continue;
			}
			if (showOnlyKnownExts) {  // пропускаем если расширение не зарегистрировано
				boolean hide = true;
				for (String ext : exts) { // прогоняем все расширения через имя файла
					if (f.endsWith(ext)) {
						hide = false; // зарегистрировано
					}
				}
				if (hide) {// ели не зарегистрировано, то пропускаем
					continue;
				}
			}
			HashMap<String, String> item = new HashMap<String, String>();//создаем карту и начинаем ее заполнять
			sname = f;
			if (showBookTitles) { // показывать имена книг
				sname = app.dataBase.getEbookName(currentRoot, f, bookTitleFormat); // bvz bp ,fps
			} else if (hideKnownExts) {  // скрываем расширение
				for (String ext : exts) { // прогоняем все расширения через имя файла
					if (sname.endsWith(ext)) {
						sname = sname.substring(0, sname.length() - ext.length());// удаляем если нашли совпадение
					}
				}
			}
			if (fileExtendedData) {
				date = filesDate.get(i);
				size = filesSize.get(i);
				tt = fileExtendedDataFormat;
				tt = tt.toLowerCase();
				String temp;
				if (tt.contains("%d")) {
					tt = tt.replace("%d", String.valueOf(dateFormat.format(date)));
				}
				if (tt.contains("%t")) {
					tt = tt.replace("%t", String.valueOf(timeFormat.format(date)));
				}
				if (tt.contains("%sb")) {
					tt = tt.replace("%sb", String.valueOf(size));
				}
				if (tt.contains("%skb")) {
					temp = String.valueOf((int) (size / 102.4));
					if (temp.length() > 1) {
						temp = temp.substring(0, temp.length() - 1) + "," + temp.substring(temp.length() - 1);
					} else if (temp.length() == 1) {
						temp = "0," + temp;
					}
					tt = tt.replace("%skb", temp);
				}
				if (tt.contains("%smb")) {
					temp = String.valueOf((int) (size / 104857.6));
					if (temp.length() > 1) {
						temp = temp.substring(0, temp.length() - 1) + "," + temp.substring(temp.length() - 1);
					} else if (temp.length() == 1) {
						temp = "0," + temp;
					}
					tt = tt.replace("%smb", temp);
				}
				sname += tt;
			}
			item.put("sname", sname);// имя файла
			item.put("name", f); // имя файла
			item.put("dname", currentRoot); // путь к файлу
			if (currentRoot.equals("/")) {// полный путь папки
				item.put("fname", currentRoot + f);
			} else {
				item.put("fname", currentRoot + File.separator + f);
			}
			item.put("type", "file"); // тип - файл
			item.put("size", String.valueOf(filesSize.get(i) + 1000000000000000L));
			item.put("date", String.valueOf(filesDate.get(i)));
			item.put("reader", app.readerName(f)); // программа обработчик
			if (firstLineIconSizePx != 0) {
				String nameIcon;

				if (f.endsWith(".apk")) { // если удалось
					nameIcon = "install";
				} else {  // иначе
					String rdrName = app.readerName(f); // в поле реадера читаем обработчик

					if (rdrName.startsWith("Intent:")) { // если ответ начинается с ...
						nameIcon = "icon";
					} else if (rdrName.equals("Nope")) { // если не известен
						File fil = new File(currentRoot + "/" + f); // получаем файл
						if (fil.length() > app.viewerMax * 1024) {  // больше определенного размера
							nameIcon = "file_notok";
						} else {  // иначе
							nameIcon = "file_ok";
						}
					} else {
						nameIcon = rdrName;
					}
				}
				item.put("nameIcon", nameIcon); // тип - файл
			}
			fileItemsArray.add(item);  // добавляем в массив
		}
		fileItemsArray = sortFiles(fileItemsArray, sortType, sortOrder);
		// добавляем в массив карт предназначенный для отображения
		itemsArray.addAll(fileItemsArray);
		// скрываем или показываем кнопку переход в родительскую папку
		setUpButton();
		// определяем число колонок для отображения
		Integer colsNum = getDirectoryColumns(currentRoot);
		if (colsNum == 0) {  // проверяем не назначено ли пользователем отображение определенного числа колонок для этой папки
			colsNum = Integer.parseInt(prefs.getString("columnsDirsFiles", "-1"));
		}
		// override auto (not working fine in adnroid) судя по всему работает дерьмого автоколлумнилование
		if (colsNum == -1) {
			colsNum = app.getAutoColsNum(itemsArray, "sname", columnsAlgIntensity);
		}
		// устанавливаем число колонок для отображения
		currentColsNum = colsNum;
		// устанавливаем число колонок у View
		gvList.setNumColumns(colsNum);
		// устанавливаем стартовый элемент
		if (startPosition != -1) {
			gvList.setSelection(currentPosition);
			if(N2DeviceInfo.EINK_ONYX || N2DeviceInfo.EINK_GMINI || N2DeviceInfo.EINK_BOEYE){
				DownScroll( currentPosition);
			}
		}

		reDraw();
	}

	private void start(Intent i) {
		if (i != null)
			try {
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(ReLaunch.this,getResources().getString(R.string.jv_relaunch_activity_not_found),
						Toast.LENGTH_LONG).show();
			}
	}

	private BroadcastReceiver SDCardChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Code to react to SD mounted goes here
			Intent i = new Intent(context, ReLaunch.class);
			startActivity(i);
		}
	};

	private BroadcastReceiver PowerChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Battery
			try {
				int batDraw;
				int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int scale = intent.getIntExtra( BatteryManager.EXTRA_SCALE, -1);
				int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
				int level = -1;
				if (rawlevel >= 0 && scale > 0) {
					level = (rawlevel * 100) / scale;
				}
				if (battLevel != null) {
					String add_text = "";
					if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
						add_text = " AC";
					} else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
						add_text = " USB";
					}
					battLevel.setText(level + "%" + add_text);
					if(level < 25){
						if (plugged == BatteryManager.BATTERY_PLUGGED_AC){
							batDraw = R.drawable.bat1_outlet;
						}else if (plugged == BatteryManager.BATTERY_PLUGGED_USB){
							batDraw = R.drawable.bat1_usb;
						}else{
							batDraw = R.drawable.bat1;
						}
					}else if (level < 50){
						if (plugged == BatteryManager.BATTERY_PLUGGED_AC){
							batDraw = R.drawable.bat2_outlet;
						}else if (plugged == BatteryManager.BATTERY_PLUGGED_USB){
							batDraw = R.drawable.bat2_usb;
						}else{
							batDraw = R.drawable.bat2;
						}
					}else if (level < 75){
						if (plugged == BatteryManager.BATTERY_PLUGGED_AC){
							batDraw = R.drawable.bat3_outlet;
						}else if (plugged == BatteryManager.BATTERY_PLUGGED_USB){
							batDraw = R.drawable.bat3_usb;
						}else{
							batDraw = R.drawable.bat3;
						}
					}else{
						if (plugged == BatteryManager.BATTERY_PLUGGED_AC){
							batDraw = R.drawable.bat4_outlet;
						}else if (plugged == BatteryManager.BATTERY_PLUGGED_USB){
							batDraw = R.drawable.bat4_usb;
						}else{
							batDraw = R.drawable.bat4;
						}
					}
					battLevel
							.setCompoundDrawablesWithIntrinsicBounds(
									getResources().getDrawable(batDraw), null, null, null);
				}
				if (battLevelSec != null) {
					if(level < 25){
						if (plugged == BatteryManager.BATTERY_PLUGGED_AC){
							batDraw = R.drawable.bat1_big_outlet;
						}else if (plugged == BatteryManager.BATTERY_PLUGGED_USB){
							batDraw = R.drawable.bat1_big_usb;
						}else{
							batDraw = R.drawable.bat1_big;
						}
					}else if (level < 50){
						if (plugged == BatteryManager.BATTERY_PLUGGED_AC){
							batDraw = R.drawable.bat2_big_outlet;
						}else if (plugged == BatteryManager.BATTERY_PLUGGED_USB){
							batDraw = R.drawable.bat2_big_usb;
						}else{
							batDraw = R.drawable.bat2_big;
						}
					}else if (level < 75){
						if (plugged == BatteryManager.BATTERY_PLUGGED_AC){
							batDraw = R.drawable.bat3_big_outlet;
						}else if (plugged == BatteryManager.BATTERY_PLUGGED_USB){
							batDraw = R.drawable.bat3_big_usb;
						}else{
							batDraw = R.drawable.bat3_big;
						}
					}else{
						if (plugged == BatteryManager.BATTERY_PLUGGED_AC){
							batDraw = R.drawable.bat4_big_outlet;
						}else if (plugged == BatteryManager.BATTERY_PLUGGED_USB){
							batDraw = R.drawable.bat4_big_usb;
						}else{
							batDraw = R.drawable.bat4_big;
						}
					}
					battLevelSec.setImageBitmap(BitmapFactory.decodeResource(getResources(), batDraw));
				}
			} catch (IllegalArgumentException e) {
				//Log.v("ReLaunch", "Battery intent illegal arguments");
			}
			GetDateAndMewory();
		}
	};

	private BroadcastReceiver WiFiChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Wifi status
            WiFiReceiver();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // ------ загружаем глобальные массивы и переменные ---------------------
        initGlobalVariable();
        // ------ загружаем настройки программы в переменные -----------------------
        initPrefsVariable();
        // ------ загружаем стандартные иконки для отображения в менеджере ----------
        if (firstLineIconSizePx != 0) {
            loadStandartIcons();
        }

        // If we called from Home launcher?
        final Intent data = getIntent();
		if(data != null && data.getExtras() != null){
			if (data.getExtras().getString("ftplist") != null) {
				ID_FTP = data.getExtras().getInt("id");
				currentHomeDir = data.getExtras().getString("path");
			}else{
				currentHomeDir = data.getStringExtra("start_dir");
			}
			// полученный путь сохраняем
			prefsEditor.putString("intentStartDir", currentHomeDir);
			prefsEditor.commit();
			currentRoot = currentHomeDir;
			f_onCreate = true;
		}
        filterMyself = prefs.getBoolean("filterSelf", true);

        //=================================================================================
		app.fullScreen = prefs.getBoolean("fullScreen", true);
		app.hideTitle = prefs.getBoolean("hideTitle", true);
		app.setFullScreenIfNecessary(this);


		// Create application icons map
		app.setAppInfoArrayList(app.createAppList(getPackageManager()));

		// Create applications label list
		//app.setApps(createAppList(getPackageManager()));

		// Miscellaneous lists list
		app.readFile("filters", FILT_FILE, ":");
		app.filters_and = prefs.getBoolean("filtersAnd", true);
		app.readFile("columns", COLS_FILE, ":");
		app.columns.clear();
		for (String[] r : app.getList("columns")) {
			app.columns.put(r[0], Integer.parseInt(r[1]));
		}
		app.readFile("history", HIST_FILE, ":");
		app.history.clear();
		for (String[] r : app.getList("history")) {
			if (r[1].equals("READING")){
				app.history.put(r[0], app.READING);
            }else if (r[1].equals("FINISHED")){
				app.history.put(r[0], app.FINISHED);
            }
		}
		setSortMode(prefs.getInt("sortMode", 0));

        // Main layout
        setContentView(R.layout.main);

		ArrayList<String> listPanels = dbSCREEN();
		LayoutInflater ltInflater = getLayoutInflater();
		LinearLayout ll_container = (LinearLayout) findViewById(R.id.conteiner);
		if (listPanels != null && listPanels.size() > 0){
			for (String listPanel : listPanels) {
				if (listPanel.equals("1")) {
					drawingFirstPanel(ltInflater, ll_container);
				} else if (listPanel.equals("2")) {
					drawingSecondPanel(ltInflater, ll_container);
				} else if (listPanel.equals("3")) {
					drawingThirdPanel(ltInflater, ll_container);
				} else if (listPanel.equals("4")) {
					drawingFourthPanel(ltInflater, ll_container);
				} else if (listPanel.equals("5")) {
					drawingFifthPanel(ltInflater, ll_container);
				} else {
					drawingSixPanel(ltInflater, ll_container, listPanel);
				}
			}
		}else{
			drawingFirstPanel(ltInflater, ll_container);
			drawingSecondPanel(ltInflater, ll_container);
			drawingThirdPanel(ltInflater, ll_container);
			drawingFourthPanel(ltInflater, ll_container);
			drawingFifthPanel(ltInflater, ll_container);
		}

        //=================================================================
        // What's new processing
        int latestVersion = prefs.getInt("latestVersion", 0);
        int tCurrentVersion = 0;
        PackageManager ttt = getPackageManager();
        if (ttt != null) {
            try {
                tCurrentVersion = ttt.getPackageInfo(getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                tCurrentVersion = 0;
            }
        }
        if (tCurrentVersion > latestVersion) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            WebView wv = new WebView(this);
            wv.loadDataWithBaseURL(null,
                    getResources().getString(R.string.about_help)
                            + getResources().getString(R.string.about_appr)
                            + getResources().getString(R.string.whats_new),
                    "text/html", "utf-8", null);
            // "What's new"
            builder.setTitle(getResources().getString(R.string.jv_relaunch_whats_new));
            builder.setView(wv);
            builder.setPositiveButton(
                    getResources().getString(R.string.app_ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            try {
                                int tCurrentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                                prefsEditor.putInt("latestVersion", tCurrentVersion);
                                prefsEditor.commit();
                            } catch (Exception e) {
                                //emply
                            }

                            dialog.dismiss();
                        }
                    });
            builder.show();
        }

        // incorrect device warning
        checkDevice(Build.DEVICE, Build.MANUFACTURER, Build.MODEL, Build.PRODUCT);

		ScreenOrientation.set(this, prefs);
	}
	@Override
	protected void onStart() {
		super.onStart();

        N2EpdController.n2MainActivity = this;
        // установка начальной папки
        // загрузка сохранённой папки если сохранение последней разрешено
        if (!f_onCreate && prefs.getBoolean("saveDir", true)){
            if(N2DeviceInfo.EINK_ONYX){
				currentRoot = prefs.getString("lastdir", "/mnt/storage");
            }else{
				currentRoot = prefs.getString("lastdir", "/sdcard");
            }
			f_onCreate = false;
        }
        // если текущая пустая то грузим в неё домашнюю
        if((currentRoot == null || currentRoot.equals("")) && currentHomeDir != null && currentHomeDir.length() > 0){
			currentRoot = currentHomeDir;
        }
        // при запуске учитывать работу с дропбоксом
        if(currentRoot.startsWith("Dropbox| ")){
            if(dropboxClient == null) {
                dropboxClient = new MyDropboxClient(prefs, ReLaunch.this);
            }
            if (!dropboxClient.getSession()) {
                dropboxClient.logIn();
            }
        }
        if (currentRoot.startsWith("FTP| ")){
            connectorFTP = new FTPConnector(ID_FTP, this);
        }

		// Reread preferences
		String typesString = prefs.getString("types", defReaders);
		// Recreate readers list
		app.setReaders(parseReadersString(typesString));
        // known extensions
        List<HashMap<String, String>> rc;   // массив карт расширений
        exts = new ArrayList<String>();  // массив строк
        // скрываем известные расширения
        if (hideKnownExts) {
            rc = app.getReaders(); // получаем массив карт
            Set<String> tkeys = new HashSet<String>();
            for (HashMap<String, String> aRc : rc) {
                Object[] keys = aRc.keySet().toArray();
                for (Object key : keys) {
                    tkeys.add(key.toString());
                }
            }
            // получаем массив расширений
            exts = new ArrayList<String>(tkeys);
            // сортируем массив расширений
            Collections.sort(exts, new ExtsComparator());
        }

        drawDirectory(currentRoot, currentPosition);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			menuSearch();
			return true;
		case R.id.mime_types:
			menuTypes();
			return true;
		case R.id.about:
			menuAbout();
			return true;
		case R.id.setting:
			menuSettings();
			return true;
		case R.id.lastopened:
			menuLastopened();
			return true;
		case R.id.favorites:
			menuFavorites();
			return true;
		default:
			return true;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode) {
		case TYPES_ACT:
			String newTypes = createReadersString(app.getReaders());
            prefsEditor.putString("types", newTypes);
            prefsEditor.commit();
			drawDirectory(currentRoot, currentPosition);
			break;
		default:
			//return;
		}
	}
    @Override
    protected void onResume() {
        // register Receiver
		IntentFilter filter;
        if (!mountReceiverRegistered) {
            filter = new IntentFilter();
			filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
			filter.addAction(Intent.ACTION_MEDIA_SHARED);
			filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
			filter.addAction(Intent.ACTION_MEDIA_REMOVED);
			filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
			filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addDataScheme("file");
            registerReceiver(this.SDCardChangeReceiver, new IntentFilter(filter));
            mountReceiverRegistered = true;
        }

        if (!powerReceiverRegistered) {
            filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
			registerReceiver(this.PowerChangeReceiver, filter);
            powerReceiverRegistered = true;
        }

        if (!wifiReceiverRegistered) {
            filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(this.WiFiChangeReceiver, new IntentFilter(filter));
            wifiReceiverRegistered = true;
        }
        WiFiReceiver();
        //=====================================================================
        if(currentRoot.startsWith("Dropbox| ")){
            dropboxClient.logFinish();
        }
        EinkScreen.setEinkController(prefs);
        super.onResume();
        app.generalOnResume(TAG);
    }
    @Override
    protected void onPause() {
        // unregister Receiver
        unregisterReceiver(this.SDCardChangeReceiver);
        unregisterReceiver(this.PowerChangeReceiver);
        unregisterReceiver(this.WiFiChangeReceiver);
        wifiReceiverRegistered = false;
        powerReceiverRegistered = false;
        mountReceiverRegistered = false;

		int lruMax = 30;
		int favMax = 30;
		int appLruMax = 30;
		int appFavMax = 30;
		try {
			lruMax = Integer.parseInt(prefs.getString("lruSize", "30"));
			favMax = Integer.parseInt(prefs.getString("favSize", "30"));
			appLruMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
			appFavMax = Integer.parseInt(prefs.getString("appFavSize", "30"));
		} catch (NumberFormatException e) {
			//emply
		}
		app.writeFile("lastOpened", LRU_FILE, lruMax, "/");
		app.writeFile("favorites", FAV_FILE, favMax, "/");
		app.writeFile("app_last", APP_LRU_FILE, appLruMax, ":");
		app.writeFile("app_favorites", APP_FAV_FILE, appFavMax, ":");
		List<String[]> h = new ArrayList<String[]>();
		for (String k : app.history.keySet()) {
			if (app.history.get(k) == app.READING)
				h.add(new String[] { k, "READING" });
			else if (app.history.get(k) == app.FINISHED)
				h.add(new String[] { k, "FINISHED" });
		}
		app.setList("history", h);
		app.writeFile("history", HIST_FILE, 0, ":");
		List<String[]> c = new ArrayList<String[]>();
		for (String k : app.columns.keySet()) {
			c.add(new String[] { k, Integer.toString(app.columns.get(k)) });
		}
		app.setList("columns", c);
		app.writeFile("columns", ReLaunch.COLS_FILE, 0, ":");

		prefsEditor.putString("intentStartDir", currentHomeDir);
		if (prefs.getBoolean("saveDir", true)) {
			prefsEditor.putString("lastdir", currentRoot);
		}
		prefsEditor.commit();

        super.onPause();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
		String lang;
		lang = prefs.getString("lang", "default");
		if (!lang.equals("default")) {
			locale = new Locale(lang);
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			getBaseContext().getResources().updateConfiguration(config, null);
		}
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME)
            return true;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
			boolean isRoot = !CheckUpDir();
			if (prefs.getBoolean("useBackButton", true)) {
                if (!isRoot) {
					TapUpDir();
                }
            }
            if (((isRoot) || (!prefs.getBoolean("useBackButton", true))) && blockExitLauncher) {// выход из программы
                // Ask the user if they want to quit
                new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                        // "This is a launcher!"
                        .setTitle(getResources().getString(R.string.jv_relaunch_launcher))
                                // "Are you sure you want to quit ?"
                        .setMessage(getResources().getString(R.string.jv_relaunch_launcher_text))
                                // "YES"
                        .setPositiveButton(getResources().getString(R.string.app_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int which) {
                                        finish();
                                    }
                                })
                                // "NO"
                        .setNegativeButton(getResources().getString(R.string.app_no),null).show();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (N2DeviceInfo.EINK_SONY) {
            int prevCode = 0x0069;
            int nextCode = 0x006a;
            if ((event.getScanCode() == prevCode) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                int first = gvList.getFirstVisiblePosition();
                int visible = gvList.getLastVisiblePosition() - first + 1;
                int total = itemsArray.size();
                first -= visible;
                if (first < 0)
                    first = 0;
                gvList.setSelection(first);
                // some hack workaround against not scrolling in some cases
                if (total > 0) {
                    //gvList.requestFocusFromTouch();
                    gvList.setSelection(first);
                }
            }
            if ((event.getScanCode() == nextCode) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                int first = gvList.getFirstVisiblePosition();
                int total = itemsArray.size();
                int last = gvList.getLastVisiblePosition();
                if (total == last + 1)
                    return true;
                int target = last + 1;
                if (target > (total - 1))
                    target = total - 1;
				app.RepeatedDownScroll(gvList, first, target, 0);
            }
        }
        return super.dispatchKeyEvent(event);
    }

	public boolean onContextMenuSelected(int itemId, int mPos) {
		if (itemId == CNTXT_MENU_CANCEL){
			return true;
        }
		HashMap<String, String> i;
		int tpos = 0;

		if (mPos == -1) {
			i = new HashMap<String, String>();
			i.put("dname", currentRoot);
			i.put("name", "");
		} else {
			tpos = mPos;
			i = itemsArray.get(tpos);
		}
		final int pos = tpos;
		final String fname = i.get("name");
		final String dname = i.get("dname");
		final String fullName = i.get("fname");
		final String type = i.get("type");

		switch (itemId) {
		case CNTXT_MENU_SET_STARTDIR:// FTP не работает
            if(dname.startsWith("FTP| ")) {
                return true;
            }
			app.setStartDir(fullName);
			drawDirectory(fullName, -1);
			break;
		case CNTXT_MENU_ADD_STARTDIR:// FTP не работает
            if(dname.startsWith("FTP| ")) {
                return true;
            }
			app.addStartDir(fullName);
			startDir = app.loadStartDirs();
			break;
		case CNTXT_MENU_ADD:// FTP не работает
            if(dname.startsWith("FTP| ")) {
                return true;
            }
			if (type.equals("file"))
				app.addToList("favorites", dname, fname, false);
			else
				app.addToList("favorites", fullName, app.DIR_TAG, false);
			break;
		case CNTXT_MENU_MARK_READING:
			app.history.put(fullName, app.READING);
			app.saveList("history");
            reDraw();
			break;
		case CNTXT_MENU_MARK_FINISHED:
			app.history.put(fullName, app.FINISHED);
			app.saveList("history");
            reDraw();
			break;
		case CNTXT_MENU_MARK_FORGET:
			app.history.remove(fullName);
			app.saveList("history");
            reDraw();
			break;
		case CNTXT_MENU_OPENWITH: {
			final ArrayList<String> listApp2 = app.getAppList();
			CharSequence[] happlications = app.getAppList().toArray(new CharSequence[app.getAppList().size()]);
			AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
			// "Select application"
			builder.setTitle(getResources().getString(R.string.jv_relaunch_select_application));
			builder.setSingleChoiceItems(happlications, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
                            String temp = fullName;
                            if(temp.startsWith("FTP| ")) {
                                downloadFTP(connectorFTP, dname, fname, "");
                                temp = FTPTempDir + File.separator + temp.substring(temp.lastIndexOf("/")+1);
                            }
                            if(temp.startsWith("Dropbox| ")) {
                                temp = loadFileDB(temp, fname);
                            }
							start(app.launchReader(listApp2.get(i), temp));
							dialog.dismiss();
						}
					});
			builder.setNegativeButton(getResources().getString(R.string.app_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});
			builder.show();
			break;
		}
		case CNTXT_MENU_INTENT: {
			String re[] = fname.split("\\.");
			List<String> ilist = new ArrayList<String>();
			for (int j = 1; j < re.length; j++) {
				String act = "application/";
				String typ = re[j];
				if (typ.equals("jpg"))
					typ = "jpeg";
				if (typ.equals("jpeg") || typ.equals("png"))
					act = "image/";
				ilist.add(act + typ);
				if (re.length > 2) {
					for (int k = j + 1; k < re.length; k++) {
						String x = "";
						for (int l = k; l < re.length; l++)
							x += "+" + re[l];
						ilist.add(act + typ + x);
					}
				}
			}
			final CharSequence[] intents = ilist.toArray(new CharSequence[ilist.size()]);
			AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
			// "Select intent type"
			builder.setTitle(getResources().getString(R.string.jv_relaunch_select_intent_type));
			builder.setSingleChoiceItems(intents, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
                            String temp = fullName;
                            if(temp.startsWith("FTP| ")) {
                                downloadFTP(connectorFTP, dname, fname, "");
                                temp = FTPTempDir + File.separator + temp.substring(temp.lastIndexOf("/")+1);
                            }
                            if(temp.startsWith("Dropbox| ")) {
                                temp = loadFileDB(temp, fname);
                            }
							Intent in = new Intent();
							in.setAction(Intent.ACTION_VIEW);
							in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
							in.setDataAndType(Uri.parse("file://" + temp),(String) intents[i]);
							dialog.dismiss();
							try {
								startActivity(in);
							} catch (ActivityNotFoundException e) {
								AlertDialog.Builder builder1 = new AlertDialog.Builder(ReLaunch.this);
								// "Activity not found"
								builder1.setTitle(getResources()
										.getString(R.string.jv_relaunch_activity_not_found_title));
								// "Activity for file \"" + fullName +
								// "\" with type \"" + intents[i] +
								// "\" not found"
								builder1.setMessage(getResources()
										.getString(
												R.string.jv_relaunch_activity_not_found_text1)
										+ " \""
										+ temp
										+ "\" "
										+ getResources()
												.getString(
														R.string.jv_relaunch_activity_not_found_text2)
										+ " \""
										+ intents[i]
										+ "\" "
										+ getResources()
												.getString(
														R.string.jv_relaunch_activity_not_found_text3));
								builder1.setPositiveButton(getResources()
										.getString(R.string.app_ok),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
											}
										});
								builder1.show();
							}
						}
					});
			// "Other"
			builder.setPositiveButton(
					getResources().getString(R.string.jv_relaunch_other),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							AlertDialog.Builder builder1 = new AlertDialog.Builder(ReLaunch.this);
							// "Intent type"
							builder1.setTitle(getResources().getString(R.string.jv_relaunch_intent_type));
							final EditText input = new EditText(ReLaunch.this);
							input.setText("application/");
							builder1.setView(input);
							// "Ok"
							builder1.setPositiveButton(getResources()
									.getString(R.string.app_ok),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
                                            String temp = fullName;
                                            if(temp.startsWith("FTP| ")) {
                                                downloadFTP(connectorFTP, dname, fname, "");
                                                temp = FTPTempDir + File.separator + temp.substring(temp.lastIndexOf("/")+1);
                                            }
                                            if(temp.startsWith("Dropbox| ")) {
                                                temp = loadFileDB(temp, fname);
                                            }
											Intent in = new Intent();
											in.setAction(Intent.ACTION_VIEW);
											in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
													| Intent.FLAG_ACTIVITY_CLEAR_TOP);
											in.setDataAndType(
													Uri.parse("file://"
															+ temp), String.valueOf(input.getText())
                                            );
											dialog.dismiss();
											try {
												startActivity(in);
											} catch (ActivityNotFoundException e) {
												AlertDialog.Builder builder2 = new AlertDialog.Builder(
														ReLaunch.this);
												// "Activity not found"
												builder2.setTitle(getResources()
														.getString(
																R.string.jv_relaunch_activity_not_found_title));
												// "Activity for file \"" +
												// fullName + "\" with type \""
												// + input.getText() +
												// "\" not found"
												builder2.setMessage(getResources()
														.getString(
																R.string.jv_relaunch_activity_not_found_text1)
														+ " \""
														+ temp
														+ "\" "
														+ getResources()
																.getString(
																		R.string.jv_relaunch_activity_not_found_text2)
														+ " \""
														+ input.getText()
														+ "\" "
														+ getResources()
																.getString(
																		R.string.jv_relaunch_activity_not_found_text3));
												// "OK"
												builder2.setPositiveButton(
														getResources()
																.getString(
																		R.string.app_ok),
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int whichButton) {
															}
														});
												builder2.show();
											}
										}
									});
							builder1.show();
						}
					});
			// "Cancel"
			builder.setNegativeButton(
					getResources().getString(R.string.app_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});

			builder.show();
			break;
		}
		case CNTXT_MENU_DELETE_F:
            if(arrSelItem.size() < 1) {
                if (prefs.getBoolean("confirmFileDelete", true)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    // "Delete file warning"
                    builder.setTitle(getResources().getString(R.string.jv_relaunch_del_file_title));
                    // "Are you sure to delete file \"" + fname + "\" ?"
                    builder.setMessage(getResources().getString(
                            R.string.jv_relaunch_del_file_text1)
                            + " \""
                            + fname
                            + "\" "
                            + getResources().getString(R.string.jv_relaunch_del_file_text2));
                    // "Yes"
                    builder.setPositiveButton(getResources().getString(R.string.app_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.dismiss();
                                    delAll(fullName, dname, fname, pos);
                                }
                            }
                    );
                    // "No"
                    builder.setNegativeButton(getResources().getString(R.string.app_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    );
                    builder.show();
                } else {
                    delAll(fullName, dname, fname, pos);
                }
            }else{
                delSelect();
            }
			break;
		case CNTXT_MENU_DELETE_D_EMPTY:
            if(arrSelItem.size() < 1) {
                if (prefs.getBoolean("confirmDirDelete", true)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    // "Delete empty directory warning"
                    builder.setTitle(getResources().getString(R.string.jv_relaunch_del_em_dir_title));
                    // "Are you sure to delete empty directory \"" + fname + "\" ?"
                    builder.setMessage(getResources().getString(
                            R.string.jv_relaunch_del_em_dir_text1)
                            + " \""
                            + fname
                            + "\" "
                            + getResources().getString(R.string.jv_relaunch_del_em_dir_text2));
                    // "Yes"
                    builder.setPositiveButton(getResources().getString(R.string.app_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    dialog.dismiss();
                                    delAll(fullName, dname, fname, pos);
                                }
                            });
                    // "No"
                    builder.setNegativeButton(getResources().getString(R.string.app_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                } else {
                    delAll(fullName, dname, fname, pos);
                }
            }else{
                delSelect();
            }
			break;
		case CNTXT_MENU_DELETE_D_NON_EMPTY:
            if(arrSelItem.size() < 1) {
                if (prefs.getBoolean("confirmNonEmptyDirDelete", true)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    // "Delete non empty directory warning"
                    builder.setTitle(getResources().getString(R.string.jv_relaunch_del_ne_dir_title));
                    // "Are you sure to delete non-empty directory \"" + fname +
                    // "\" (dangerous) ?"
                    builder.setMessage(getResources().getString(
                            R.string.jv_relaunch_del_ne_dir_text1)
                            + " \""
                            + fname
                            + "\" "
                            + getResources().getString(R.string.jv_relaunch_del_ne_dir_text2));
                    // "Yes"
                    builder.setPositiveButton(
                            getResources().getString(R.string.app_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    dialog.dismiss();
                                    delAll(fullName, dname, fname, pos);
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
                } else{
                    delAll(fullName, dname, fname, pos);
                }
            }else{
                delSelect();
            }
			break;
		case CNTXT_MENU_COPY_FILE:
            if (dname.startsWith("FTP| ")){
                ftpID = ID_FTP;
            }else{
                ftpID = -1;
            }
            if(arrSelItem.size() < 1) {
                fileOpFile = new String[1];
                fileOpDir = new String[1];
                fileOpFile[0] = fname;
                fileOpDir[0] = dname;

            }else{
                HashMap<String, String> i2;
                int k = arrSelItem.size();
                fileOpFile = new String[k];
                fileOpDir = new String[k];
                for(int i1=0; i1 < k; i1++){
                    i2 = itemsArray.get(arrSelItem.get(i1));
                    fileOpDir[i1] = i2.get("dname");
                    fileOpFile[i1] = i2.get("name");
                }
            }
            fileOp = CNTXT_MENU_COPY_FILE;
			break;

		case CNTXT_MENU_MOVE_FILE:
            if (dname.startsWith("FTP| ")){
                ftpID = ID_FTP;
            }else{
                ftpID = -1;
            }
            if(arrSelItem.size() < 1) {
                fileOpFile = new String[1];
                fileOpDir = new String[1];
                fileOpFile[0] =  fname;
                fileOpDir[0] = dname;
            }else{
                HashMap<String, String> i2;
                int k = arrSelItem.size();
                fileOpFile = new String[k];
                fileOpDir = new String[k];
                for(int i1=0; i1 < k; i1++){
                    i2 = itemsArray.get(arrSelItem.get(i1));
                    fileOpDir[i1] = i2.get("dname");
                    fileOpFile[i1] = i2.get("name");
                }
            }
            fileOp = CNTXT_MENU_MOVE_FILE;
			break;

		case CNTXT_MENU_PASTE:
            String src;
            String dst;
            FTPConnector connectorFTPdown = null;
            if (fileOpDir[0].startsWith("FTP| ")){
                connectorFTPdown = new FTPConnector(ftpID, this);
            }

            for(int l=0; l < fileOpDir.length; l++) {
                if (fileOpDir[l].equalsIgnoreCase("/"))
                    src = fileOpDir[l] + fileOpFile[l];
                else
                    src = fileOpDir[l] + "/" + fileOpFile[l];
                dst = dname + "/" + fileOpFile[l];

                boolean dropSrc = src.startsWith("Dropbox| ");// источник в дропе?
                boolean dropDst = dst.startsWith("Dropbox| ");// приемник в дропе?
                boolean ftpSrc = src.startsWith("FTP| ");// источник в FTP?
                boolean ftpDst = dst.startsWith("FTP| ");// приемник в FTP?

                boolean retCode = false;
                // если источник и приемник в дропе.
                if (dropSrc && dropDst && !ftpSrc && !ftpDst) {
                    if (fileOp == CNTXT_MENU_COPY_FILE) {
                        retCode = DBmoveORcopy(src, dst, true);
                    } else if (fileOp == CNTXT_MENU_MOVE_FILE) {
                        retCode = DBmoveORcopy(src, dst, false);
                    }
                }
                // если источник локально, а приемник в дропе.
                if (!dropSrc && dropDst && !ftpSrc && !ftpDst) {
                    retCode = DBupload(src, dname);
                    if (fileOp == CNTXT_MENU_MOVE_FILE) {
                        //app.removeFile(fileOpDir[l], fileOpFile[l]);
                        if(app.fileRemove(new File(fileOpDir[l] + "/" + fileOpFile[l]))) {
                            app.fileRemoveAllList(fileOpDir[l], fileOpFile[l]);
                        }
                    }
                }
                // если источник в дропе, а приемник локально.
                if (dropSrc && !dropDst && !ftpSrc && !ftpDst) {
                    retCode = DBdownload(src, dname);
                    if (fileOp == CNTXT_MENU_MOVE_FILE) {
                        DBdelete(src);
                    }
                }
                // если файлы находяться локально
                if (!dropDst && !dropSrc && !ftpSrc && !ftpDst) {
                    if (fileOp == CNTXT_MENU_COPY_FILE) {
                        retCode = app.copyAll(src, dst, false);
                    } else if (fileOp == CNTXT_MENU_MOVE_FILE) {
                        retCode = app.moveFile(src, dst);
                    }
                }
                // если источник в дропе, а приемник FTP.
                if (dropSrc && !dropDst && !ftpSrc && ftpDst) {
                    DBdownload(src, FTPTempDir);
                    retCode = uploadFTP(connectorFTP, fileOpDir[l], fileOpFile[l], dname);
                    if (fileOp == CNTXT_MENU_MOVE_FILE) {
                        DBdelete(src);
                    }
                }
                // если источник локально, а приемник FTP.
                if (!dropSrc && !dropDst && !ftpSrc && ftpDst) {
                    retCode = uploadFTP(connectorFTP, fileOpDir[l], fileOpFile[l], dname);
                    if (fileOp == CNTXT_MENU_MOVE_FILE) {
                        //app.removeFile(fileOpDir[l], fileOpFile[l]);
                        if(app.fileRemove(new File(fileOpDir[l] + "/" + fileOpFile[l]))) {
                            app.fileRemoveAllList(fileOpDir[l], fileOpFile[l]);
                        }
                    }
                }
                // если источник FTP, а приемник локально.
                if (!dropSrc && !dropDst && ftpSrc && !ftpDst) {
                    retCode = downloadFTP(connectorFTPdown, fileOpDir[l], fileOpFile[l], dname);
                    if (fileOp == CNTXT_MENU_MOVE_FILE) {
                        delAll(src, fileOpDir[l], fileOpFile[l], -1);
                    }
                }
                // если источник FTP, а приемник в дропе.
                if (!dropSrc && dropDst && ftpSrc && !ftpDst) {
                    downloadFTP(connectorFTPdown, fileOpDir[l], fileOpFile[l], FTPTempDir);
                    String temp_dname = FTPTempDir + File.separator + fileOpFile[l];
                    retCode = DBupload(temp_dname, dname);
                    if (fileOp == CNTXT_MENU_MOVE_FILE) {
                        delAll(src, fileOpDir[l], fileOpFile[l], -1);
                    }
                    delAll(temp_dname, FTPTempDir, fileOpFile[l], -1);
                }
                // если источник FTP, а приемник в FTP.
                if (!dropSrc && !dropDst && ftpSrc && ftpDst) {
                    downloadFTP(connectorFTPdown, fileOpDir[l], fileOpFile[l], FTPTempDir);
                    String temp_dname = FTPTempDir + File.separator + fileOpFile[l];
                    retCode = uploadFTP(connectorFTP, FTPTempDir, fileOpFile[l], dname);
                    if (fileOp == CNTXT_MENU_MOVE_FILE) {
                        delAll(src, fileOpDir[l], fileOpFile[l], -1);
                    }
                    delAll(temp_dname, FTPTempDir, fileOpFile[l], -1);
                }
                if (!retCode) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getResources().getString(R.string.jv_relaunch_error_title));
                    builder.setMessage(getResources().getString(R.string.jv_relaunch_paste_fail_text) + " " + fileOpFile[l]);
                    builder.setNeutralButton(getResources().getString(R.string.app_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    );
                    builder.show();
                }

            }
            fileOp = 0;
            if (fileOpDir[0].startsWith("FTP| ")){
                ftpID = -1;
            }
            drawDirectory(dname, currentPosition);
			break;

		case CNTXT_MENU_TAGS_RENAME: {
			final Context mThis = this;
			String oldFullName = dname + "/" + fname;
            String temp_dname = dname;
            if(oldFullName.startsWith("FTP| ")) {
                downloadFTP(connectorFTP, dname, fname, "");
                temp_dname = FTPTempDir;
            }
            if(oldFullName.startsWith("Dropbox| ")) {
                oldFullName = loadFileDB(fullName, fname);
                temp_dname = oldFullName.substring(0, oldFullName.length() - (fname.length()+1));
            }
			String newName = app.dataBase.getEbookName(temp_dname, fname, bookTitleFormat);
			newName = newName.replaceAll("[\n\r]", ". ");
			if (fname.endsWith("fb2"))
				newName = newName.concat(".fb2");
			else if (fname.endsWith("fb2.zip"))
				newName = newName.concat(".fb2.zip");
			else if (fname.endsWith("epub"))
				newName = newName.concat(".epub");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			input.setText(newName);
			builder.setView(input);
			builder.setTitle(getResources().getString(
					R.string.jv_relaunch_rename_title));
			// "OK"
            final String finalOldFullName = oldFullName;
            final String finalTemp_dname = temp_dname;
            builder.setPositiveButton(
					getResources().getString(R.string.app_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
							String newName = String.valueOf(input.getText()).trim();
							String newFullName = finalTemp_dname + "/" + newName;
                            if(dname.startsWith("FTP| ")) {
                                connectorFTP.rename(dname + "/" + fname, dname + "/" + newName);
                            }
                            if(dname.startsWith("Dropbox| ")) {
                                DBmoveORcopy(dname + "/" + fname, dname + "/" + newName, false);
                            }
							if (app.moveFile(finalOldFullName, newFullName)) {
								drawDirectory(dname, currentPosition);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
								builder.setTitle(getResources().getString(R.string.jv_relaunch_error_title));
								builder.setMessage(getResources().getString(R.string.jv_relaunch_rename_fail_text)
										                                    + " " + fname);
								builder.setNeutralButton(
										getResources().getString(R.string.app_ok),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int whichButton) {
												dialog.dismiss();
											}
										});
								builder.show();
							}
						}
					});
			// "Cancel"
			builder.setNegativeButton(
					getResources().getString(R.string.app_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});

			builder.show();
			}
			break;

		case CNTXT_MENU_RENAME: {
			final Context mThis = this;
			final String oldFullName = dname + "/" + fname;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			input.setText(fname);
			input.selectAll();
			builder.setView(input);
			builder.setTitle(getResources().getString(R.string.jv_relaunch_rename_title));
			// "OK"
			builder.setPositiveButton(
					getResources().getString(R.string.app_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
							String newName = String.valueOf(input.getText()).trim();
							String newFullName = dname + "/" + newName;
                            if(dname.startsWith("FTP| ")) {
                                connectorFTP.rename(oldFullName, newFullName);
                                drawDirectory(dname, currentPosition);
                            }else if(dname.startsWith("Dropbox| ")) {
                                DBmoveORcopy(oldFullName, newFullName, false);
                                drawDirectory(dname, currentPosition);
                            }else if (app.moveFile(oldFullName, newFullName)) {
								drawDirectory(dname, currentPosition);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
								builder.setTitle(getResources().getString(R.string.jv_relaunch_error_title));
								builder.setMessage(getResources().getString(
										R.string.jv_relaunch_rename_fail_text)+ " " + fname);
								builder.setNeutralButton(
										getResources().getString(R.string.app_ok),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int whichButton) {
												dialog.dismiss();
											}
										});
								builder.show();
							}
						}
					});
			// "Cancel"
			builder.setNegativeButton(
					getResources().getString(R.string.app_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});

			builder.show();
			}
			break;

		case CNTXT_MENU_CREATE_DIR: {
			final Context mThis = this;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			builder.setView(input);
			builder.setTitle(getResources().getString(R.string.jv_relaunch_create_folder_title));
			// "OK"
			builder.setPositiveButton(
					getResources().getString(R.string.app_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
							String dname = currentRoot;
							String newName = String.valueOf(input.getText()).trim();
							if (newName.equalsIgnoreCase("")) {
								return;
							}
							String newFullName = dname + "/" + newName;
                            boolean f;
                            if(dname.startsWith("FTP| ")) {
                                dname = dname.substring("FTP| ".length());
                                f = connectorFTP.createDir(dname + "/" + newName);
                            }else if(dname.startsWith("Dropbox| ")) {
                                dname = dname.substring("Dropbox| ".length());
                                f = dropboxClient.createFolder(dname + "/" + newName);
                            }else {
                                f = app.createDir(newFullName);
                            }
                            if (f) {
                                drawDirectory(currentRoot, currentPosition);
                            }else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
                                builder.setTitle(getResources().getString(R.string.jv_relaunch_error_title));
                                builder.setMessage(getResources().getString(
                                        R.string.jv_relaunch_create_folder_fail_text)+ " " + newFullName);
                                builder.setNeutralButton(
                                        getResources().getString(R.string.app_ok),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) {
                                                dialog.dismiss();
                                            }
                                        });
                                builder.show();
                            }

						}
					});
			// "Cancel"
			builder.setNegativeButton(
					getResources().getString(R.string.app_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});

			builder.show();
			}
			break;

		case CNTXT_MENU_SWITCH_TITLES:
            prefsEditor.putBoolean("showBookTitles", !showBookTitles);
            prefsEditor.commit();
            showBookTitles = !showBookTitles;
			drawDirectory(dname, currentPosition);
			break;

		case CNTXT_MENU_SHOW_BOOKINFO:
            String temp = fullName;
            if(fullName.startsWith("FTP| ")) {
                downloadFTP(connectorFTP, dname, fname, "");
                temp = FTPTempDir + File.separator + temp.substring(temp.lastIndexOf("/")+1);
            }
            if(temp.startsWith("Dropbox| ")) {
                temp = loadFileDB(fullName, fname);
            }
			showBookInfo(temp);
			break;

		case CNTXT_MENU_FILE_INFO:
		    showFileInfo(fullName);
			break;

        case CNTXT_MENU_COPY_DROPBOX:
            copyFileToDropbox(dname, fname);
            break;

        case CNTXT_MENU_COPY_DIR_DROPBOX:
            copyFileToLocalDirDropbox(dname, fname);
            break;
        case CNTXT_MENU_SETTINGS:
            menuSettings();
            break;
        case CNTXT_MENU_SELECTE:
            if(arrSelItem.contains(mPos)){
                // пункт уже отмечен. надо снять отметку
                arrSelItem.remove(arrSelItem.indexOf(mPos));
            }else{
                // добавить отмеченный новый пункт в массив
                arrSelItem.add(mPos);
            }
            reDraw();
            break;
        case CNTXT_MENU_FILEUNZIP:
            ZipUtil zipUtil = new ZipUtil();
            if (!zipUtil.unzip(fullName)){
                app.showToast(getResources().getString(R.string.jv_relaunch_fileunzip_error));
            }
            drawDirectory(dname, currentPosition);
            break;
        case CNTXT_MENU_PERMISSION:
            SetPermission(fullName);
            break;
		}

		return true;
	}

	private void menuSearch() {
		Intent intent = new Intent(ReLaunch.this, SearchActivity.class);
		startActivity(intent);
	}

	private void menuTypes() {
		Intent intent = new Intent(ReLaunch.this, TypesActivity.class);
		startActivityForResult(intent, TYPES_ACT);
	}

	private void menuSettings() {
		Intent intent = new Intent(ReLaunch.this, PrefsActivity.class);
		startActivity(intent);
	}

	private void menuLastopened() {
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "lastOpened");
		// "Last opened"
		intent.putExtra("title",getResources().getString(R.string.jv_relaunch_lru));
		intent.putExtra("rereadOnStart", true);
		startActivity(intent);
	}

	private void menuFavorites() {
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "favorites");
		// "Favorites"
		intent.putExtra("title",getResources().getString(R.string.jv_relaunch_fav));
		intent.putExtra("rereadOnStart", true);
		startActivity(intent);
	}

	private void openHome() {
		drawDirectory(currentHomeDir, 0);
	}

	private void menuHome() {
		final CharSequence[] hhomes = new CharSequence[startDir.length];
		for (int j = 0; j < startDir.length; j++) {
			int ind = startDir[j].lastIndexOf('/');
			if (ind == -1 && !startDir[j].equals("Dropbox| ")) {
				hhomes[j] = "";
			} else if (startDir[j].length() == 1){
					hhomes[j] = "/";
			} else {
				hhomes[j] = startDir[j].substring(ind + 1).trim();
				if (hhomes[j].length() == 0) {
					hhomes[j] = "/";
				}
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
		// "Select home directory"
		builder.setTitle(R.string.jv_relaunch_home);
		builder.setSingleChoiceItems(hhomes, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int i) {
                        String curDirTemp;
                        if(startDir[i].equals("Dropbox| ")){
                            curDirTemp = "Dropbox| /";
                        }else{
                            curDirTemp = startDir[i];
                        }
                        currentHomeDir = curDirTemp;
                        drawDirectory(curDirTemp, -1);
                        dialog.dismiss();
					}
				});
		builder.setNegativeButton(
				getResources().getString(R.string.app_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	private void screenHome() {
		// make home list
		List<String[]> homeList = new ArrayList<String[]>();
        for (String aStartDir : startDir) {
            String[] homeEl = new String[2];
            homeEl[0] = aStartDir;
            homeEl[1] = app.DIR_TAG;
            homeList.add(homeEl);
        }
		app.setList("homeList", homeList);
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "homeList");
		intent.putExtra("title", getResources().getString(R.string.jv_relaunch_home));
		intent.putExtra("rereadOnStart", true);
		startActivity(intent);
	}

    private void screenDropbox() {
        Intent intent = new Intent(ReLaunch.this, DropBoxActivity.class);
        startActivity(intent);
    }

    private void screenOPDS() {
        //
        Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
        intent.putExtra("list", "opdslist");
        intent.putExtra("title", getResources().getString(R.string.jv_relaunch_OPDS_catalogs));
        intent.putExtra("rereadOnStart", true);
        startActivity(intent);

    }

    private void screenFTP() {
        //
        Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
        intent.putExtra("list", "ftplist");
        intent.putExtra("title", getResources().getString(R.string.jv_relaunch_FTP_catalogs));
        intent.putExtra("rereadOnStart", true);
        startActivity(intent);

    }

	private void menuAbout() {
		app.About(this);
	}

	private Integer getDirectoryColumns(String dir) {
		Integer columns = 0;
		if (app.columns.containsKey(dir)) {
			columns = app.columns.get(dir);
		}
		return columns;
	}

	@SuppressWarnings("unchecked")
	private List<HashMap<String, String>> sortFiles(List<HashMap<String, String>> array, String field, boolean order)  {
		class ArrayComparator implements Comparator<Object> {
			String key;

			ArrayComparator(String key) {
				this.key = key;
			}

			public int compare(Object lhs, Object rhs) {
				return ((HashMap<String, String>) lhs).get(key)
						.compareToIgnoreCase(
								((HashMap<String, String>) rhs).get(key));
			}
		}
		Object[] arr = array.toArray();
		ArrayComparator comparator = new ArrayComparator(field);
		Arrays.sort(arr, comparator);
		List<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
		if (order) {
            for (Object anArr : arr) {
                ret.add((HashMap<String, String>) anArr);
            }
		} else {
			for (int i = arr.length - 1; i >=0; i--)
				ret.add((HashMap<String, String>) arr[i]);
		}
		return ret;
	}

	private void menuSort() {
		final String[] orderList;
		if (showBookTitles) {
			orderList = new String[8];
			orderList[0] = getString(R.string.jv_relaunch_sort_file_dir);
			orderList[1] = getString(R.string.jv_relaunch_sort_file_rev);
            orderList[2] = getString(R.string.jv_relaunch_sort_date_dir);
            orderList[3] = getString(R.string.jv_relaunch_sort_date_rev);
            orderList[4] = getString(R.string.jv_relaunch_sort_size_dir);
            orderList[5] = getString(R.string.jv_relaunch_sort_size_rev);
            orderList[6] = getString(R.string.jv_relaunch_sort_title_dir);
            orderList[7] = getString(R.string.jv_relaunch_sort_title_rev);
		} else {
			orderList = new String[6];
			orderList[0] = getString(R.string.jv_relaunch_sort_file_dir);
			orderList[1] = getString(R.string.jv_relaunch_sort_file_rev);
            orderList[2] = getString(R.string.jv_relaunch_sort_date_dir);
            orderList[3] = getString(R.string.jv_relaunch_sort_date_rev);
            orderList[4] = getString(R.string.jv_relaunch_sort_size_dir);
            orderList[5] = getString(R.string.jv_relaunch_sort_size_rev);
		}
		int sortMode = prefs.getInt("sortMode", 0);
		if (sortMode > orderList.length - 1)
			sortMode = 0;
		AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
		builder.setTitle(R.string.jv_relaunch_sort_header);
		builder.setSingleChoiceItems(orderList, sortMode,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int i) {
                        prefsEditor.putInt("sortMode", i);
                        prefsEditor.commit();
						setSortMode(i);
						dialog.dismiss();
						drawDirectory(currentRoot, -1);
					}
				});
		builder.setNegativeButton(
				getResources().getString(R.string.app_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	private void setSortMode(int i) {
		if ((!showBookTitles) && (i > 5))
			i = 0;
		if (i == SORT_FILES_ASC) {
			sortType = "name";
			sortOrder = true;
		} else if (i == SORT_FILES_DESC) {
			sortType = "name";
			sortOrder = false;
		} else if (i == SORT_DATES_ASC) {
            sortType = "date";
            sortOrder = true;
        } else if (i == SORT_DATES_DESC) {
            sortType = "date";
            sortOrder = false;
        } else if (i == SORT_SIZES_ASC) {
            sortType = "size";
            sortOrder = false;
        } else if (i == SORT_SIZES_DESC) {
            sortType = "size";
            sortOrder = true;
        }else if (i == SORT_TITLES_ASC) {
			sortType = "sname";
			sortOrder = true;
		} else if (i == SORT_TITLES_DESC) {
			sortType = "sname";
			sortOrder = false;
		}
	}

	private void showBookInfo(final String file) {
		final int COVER_MAX_W = 280;
		Bitmap cover = null;
		final Dialog dialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.bookinfo);
        if(file.endsWith(".fb2") || file.endsWith(".fb2.zip")){
            // добавляем кнопку дополнительной информации о файле
            Button btnMore = new Button(this);
            btnMore.setText(getString(R.string.srt_btn_more_info_book));//"More");
            btnMore.setTextSize(24);
            btnMore.setBackgroundResource(R.drawable.main_button);
            btnMore.setPadding(20,10,20,10);
            LinearLayout ll = (LinearLayout)dialog.findViewById(R.id.linearLayout4);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT;
            ll.addView(btnMore, lp);
            btnMore.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(app, ExtendedInfoBook.class);
                    intent.putExtra("filename", file);
                    startActivity(intent);
                }
            });

            //=============================================================================
        }
		Parser parser = new InstantParser();
		EBook eBook = parser.parse(file, true);
		if (eBook.cover != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(eBook.cover, 0,
					eBook.cover.length);
			if (bitmap != null) {
				int width = Math.min(COVER_MAX_W, bitmap.getWidth());
				int height = (width * bitmap.getHeight())/bitmap.getWidth();
				 cover = Bitmap.createScaledBitmap(bitmap, width, height, true);
			}
		}

		if (eBook.isOk) {
			ImageView img = (ImageView) dialog.findViewById(R.id.cover);
			if (cover != null) {
                img.setImageBitmap(cover);
            }else {
                img.setImageResource(R.drawable.icon_book_list);
            }
			TextView tv = (TextView) dialog.findViewById(R.id.tvTitle);
			tv.setText(eBook.title);
			tv = (TextView) dialog.findViewById(R.id.tvAnnotation);
			if (eBook.annotation != null) {
				eBook.annotation = eBook.annotation.trim()
					.replace("<p>", "")
					.replace("</p>", "\n");
				tv.setText(eBook.annotation);
			} else
				tv.setVisibility(View.GONE);
			ListView lv = (ListView) dialog.findViewById(R.id.authors);
			lv.setDivider(null);
			if (eBook.authors.size() > 0) {
				final String[] authors = new String[eBook.authors.size()];
				for (int i = 0; i < eBook.authors.size(); i++) {
					String author = "";
					if (eBook.authors.get(i).firstName != null)
						if (eBook.authors.get(i).firstName.length() > 0)
							author += eBook.authors.get(i).firstName.substring(0,1) + ".";
					if (eBook.authors.get(i).middleName != null)
						if (eBook.authors.get(i).middleName.length() > 0)
							author += eBook.authors.get(i).middleName.substring(0,1) + ".";
					if (eBook.authors.get(i).lastName != null)
						author += " " + eBook.authors.get(i).lastName;
					authors[i] = author;
				}
				final ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1, authors);
				lv.setAdapter(lvAdapter);
			}
			tv = (TextView) dialog.findViewById(R.id.tvSeries);
			if (eBook.sequenceName != null) {
				tv.setText(eBook.sequenceName);
			}

			((TextView) dialog.findViewById(R.id.book_title)).setText(file.substring(file.lastIndexOf("/") + 1));
		}

		ImageButton btn = (ImageButton) dialog.findViewById(R.id.btnExit);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void showFileInfo(String filename) {
        File file;
        String fileName = "";
        String fileSize = "";
        String fileTime = "";
        String filePerm = "";
        String fileOwn = "";

        final Dialog dialog = new Dialog(this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fileinfo);

        LinearLayout llSize = (LinearLayout) dialog.findViewById(R.id.llSize);

        if(filename.startsWith("FTP| ")) {
            Date d = connectorFTP.getModifiedDate(filename);
            fileTime = d.toLocaleString();
            fileSize = String.valueOf(connectorFTP.getFileSize(filename));
            fileOwn = connectorFTP.addressServer;
        }else if(filename.startsWith("Dropbox| ")) {
            String temp;
            DropboxAPI.Entry entries;
            temp = filename.substring("Dropbox| ".length());
            entries = dropboxClient.metadata(temp);
            if (entries != null) {
                fileName = entries.fileName();
                fileSize = entries.size;
                fileTime = entries.modified;
                fileOwn = "Dropbox.com";
            }
        }else{
            file = new File(filename);
            fileName = file.getName();
            fileSize = String.valueOf(file.length());
            fileTime = (new Date(file.lastModified())).toLocaleString();

            if (file.isDirectory())
                llSize.setVisibility(View.GONE);

            String fileAttr = null;
            try {
                Runtime rt = Runtime.getRuntime();
                String[] args;
                if (file.isDirectory()){
                    args = new String[]{"ls", "-l", file.getParent(), "|grep", filename};
                }else {
                    args = new String[]{"ls", "-l", filename};
                }
                Process proc = rt.exec(args);
                //String str = filename.replace(" ", "\\ ");
                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                int read;
                char[] buffer = new char[4096];
                StringBuilder output = new StringBuilder();
                while ((read = br.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                br.close();
                proc.waitFor();
                fileAttr = output.toString();
            } catch (Throwable t) {
                //emply
            }
            if(fileAttr != null && fileAttr.length()>0) {
                fileAttr = fileAttr.replaceAll(" +", " ");
                int iPerm = fileAttr.indexOf(" ");
                int iOwner = fileAttr.indexOf(" ", iPerm + 1);
                int iGroup = fileAttr.indexOf(" ", iOwner + 1);
                filePerm = fileAttr.substring(1, iPerm);
                fileOwn = fileAttr.substring(iPerm + 1, iOwner) + "/" + fileAttr.substring(iOwner + 1, iGroup);
            }
        }



		TextView tv = (TextView) dialog.findViewById(R.id.tvName);
		tv.setText(fileName);
		tv = (TextView) dialog.findViewById(R.id.tvSize);
		tv.setText(fileSize + " bytes");
		tv = (TextView) dialog.findViewById(R.id.tvTime);
		tv.setText(fileTime);

		tv = (TextView) dialog.findViewById(R.id.tvPerm);
		tv.setText(filePerm);
		tv = (TextView) dialog.findViewById(R.id.tvOwner);
		tv.setText(fileOwn);

		Button btn = (Button) dialog.findViewById(R.id.btnOk);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

    private void tapButton(String buttonName) {
        // battery button
        //prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String putSettingButton = prefs.getString(buttonName, "NOTHING");
        // если действие не назначено
        // проверяеим стандартные кнопки
        if(putSettingButton.equals("NOTHING")){
            if(buttonName.equals("homeButtonST")){
                putSettingButton = "HOMEN";
            }else if(buttonName.equals("homeButtonDT")){
                putSettingButton = "HOMEMENU";
            }else if(buttonName.equals("homeButtonLT")){
                putSettingButton = "HOMESCREEN";
            }else if(buttonName.equals("lruButtonST")){
                putSettingButton = "LRUSCREEN";
            }else if(buttonName.equals("favButtonST")){
                putSettingButton = "FAVDOCSCREEN";
            }else if(buttonName.equals("settingsButtonST") || buttonName.equals("settingsButtonDT") || buttonName.equals("settingsButtonLT")){
                putSettingButton = "SETTINGS";
            }else if(buttonName.equals("advancedButtonST")){
                putSettingButton = "ADVANCED";
            }else if(buttonName.equals("memButtonST")){
                putSettingButton = "APPMANAGER";
            }else if(buttonName.equals("batButtonST")){
                putSettingButton = "SWITCHWIFI";
            }else if(buttonName.equals("appFavButtonST")){
                putSettingButton = "FAVAPP";
            }else if(buttonName.equals("appAllButtonST")){
                putSettingButton = "ALLAPP";
            }else if(buttonName.equals("appLastButtonST")){
                putSettingButton = "LASTAPP";
            }else if(buttonName.equals("searchButtonST")){
                putSettingButton = "SEARCH";
            }
        }
        if(putSettingButton.equals("NOTHING")){
            return;
        }
        if (putSettingButton.equals("LOCK")) {
            actionLock();
        } else if (putSettingButton.equals("POWEROFF")) {
            actionPowerOff();
		} else if (putSettingButton.equals("REBOOT")) {
			actionReboot();
        } else if (putSettingButton.equals("SWITCHWIFI")) {
            actionSwitchWiFi();
        } else if (putSettingButton.equals("RUN")) {
            if(prefs.getString(buttonName + "app", "%%").length() > 0) {
				RunApp(prefs.getString(buttonName + "app", "%%"));
            }
        } else if (putSettingButton.equals("DROPBOX")) {
            screenDropbox();
        } else if (putSettingButton.equals("OPDS")) {
            screenOPDS();
        } else if (putSettingButton.equals("FTP")) {
            screenFTP();
        } else if (putSettingButton.equals("BATTERY")) {
            Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
            startActivity(intent);
        } else if (putSettingButton.equals("APPMANAGER")) {
            Intent intent = new Intent(ReLaunch.this, TaskManager.class);
            startActivity(intent);
        } else if (putSettingButton.equals("FAVDOCN")) {
            ListActions la = new ListActions(app, ReLaunch.this);
            la.runItem("favorites", Integer.parseInt(prefs.getString(buttonName + "fav", "1")) - 1);
        } else if (putSettingButton.equals("FAVDOCMENU")) {
            ListActions la = new ListActions(app, ReLaunch.this);
            la.showMenu("favorites");
        } else if (putSettingButton.equals("FAVDOCSCREEN")) {
            menuFavorites();
        } else if (putSettingButton.equals("LRUN")) {
            ListActions la = new ListActions(app,ReLaunch.this);
            la.runItem("lastOpened", Integer.parseInt(prefs.getString(buttonName + "lru", "1")) - 1);
        } else if (putSettingButton.equals("LRUMENU")) {
            ListActions la = new ListActions(app,ReLaunch.this);
            la.showMenu("lastOpened");
        } else if (putSettingButton.equals("LRUSCREEN")) {
            menuLastopened();
        } else if (putSettingButton.equals("HOMEN")) {
            openHome();
        } else if (putSettingButton.equals("HOMEMENU")) {
            menuHome();
        } else if (putSettingButton.equals("HOMESCREEN")) {
            screenHome();
        } else if (putSettingButton.equals("SETTINGS")) {
            menuSettings();
        } else if (putSettingButton.equals("ADVANCED")) {
            Intent i = new Intent(ReLaunch.this, Advanced.class);
            startActivity(i);
        } else if (putSettingButton.equals("FAVAPP")) {
            Intent intent = new Intent(ReLaunch.this, AllApplications.class);
            intent.putExtra("list", "app_favorites");
            startActivity(intent);
        } else if (putSettingButton.equals("ALLAPP")) {
            Intent intent = new Intent(ReLaunch.this,AllApplications.class);
            intent.putExtra("list", "app_all");
            startActivity(intent);
        } else if (putSettingButton.equals("LASTAPP")) {
            Intent intent = new Intent(ReLaunch.this,AllApplications.class);
            intent.putExtra("list", "app_last");
            startActivity(intent);
        } else if (putSettingButton.equals("SEARCH")) {
            menuSearch();
        } else if (putSettingButton.equals("SYSSETTINGS")) {
            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        } else if (putSettingButton.equals("UPDIR")) {
            TapUpDir();
        } else if (putSettingButton.equals("UPSCROOL")) {
			app.TapUpScrool(gvList, itemsArray.size());
        } else if (putSettingButton.equals("DOWNSCROOL")) {
			app.TapDownScrool(gvList, itemsArray.size());
        }

    }

    class ExtsComparator implements java.util.Comparator<String> {
        public int compare(String a, String b) {
            if (a == null) {
                if (b == null) {
                    return 0;
                }else {
                    return 1;
                }
            }else if (b == null) {
                return -1;
            }
            if (a.length() < b.length())
                return 1;
            if (a.length() > b.length())
                return -1;
            return a.compareTo(b);
        }
    }

    public class imageIcon {
        String nameIcon;
        Bitmap icon;
    }

    private void reDraw(){
        // режим обновления экрана
        EinkScreen.PrepareController(null, false);
        // говорим что данные изменились и нужно перерисовать данные
        adapter.notifyDataSetChanged();
    }
    //============= dropbox ==========================
    private void copyFileToLocalDirDropbox(String dirname, String filename) {
        String src, str_f, locDirDrop;

        locDirDrop = prefs.getString("LocalfolderDropbox", "none");
        if(!"none".equals(locDirDrop)){
            str_f = locDirDrop.trim();
            if(str_f.lastIndexOf("/") == str_f.length()-1) {
                locDirDrop = str_f.substring(0, str_f.length()-1);
            }else{
                locDirDrop = str_f;
            }
            String dst = locDirDrop + "/" + filename;
            src = dirname + "/" + filename;
            if(src.startsWith("FTP| ")) {
                downloadFTP(connectorFTP, dirname, filename, locDirDrop);
            }else {
                app.copyAll(src, dst, false);
            }
        }
    }
    private void copyFileToDropbox(final String dirname, String filename) {
        String tempDirName = dirname;
        if(dirname.startsWith("FTP| ")) {
            downloadFTP(connectorFTP, dirname, filename, "");
            tempDirName = FTPTempDir;
        }
        // получаем имя папки Dropbox, куда будем закидывать выделенное
        String str_f;
        String DBPath;
        final Activity activity = this;
        DBPath = prefs.getString("DropBoxfolder", "none");
        str_f = DBPath.trim();
        if(str_f.lastIndexOf("/") == str_f.length()-1){
            DBPath = str_f.substring(0, str_f.length()-1);
        }else{
            DBPath = str_f;
        }
        //---------------------------------------------------------------------
        // проверяем наличие подключения к сервису Dropbox
        if(dropboxClient == null){
            if(haveNetworkConnection(app)) {
                dropboxClient = new MyDropboxClient(prefs, ReLaunch.this);
                dropboxClient.logIn();
            }else{
                return;
            }
        }
        //------------------------------------------------------------------------
        // проверяем наличие на сервере и если такое уже есть - выдаем диалог с вариантами
        int checkRez = checkFileDB(DBPath, filename);
        if(checkRez == 1){

            // выбор действия
            String[] list_url = new String[2];
            final String[] tempNameFile = {filename};
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(getString(R.string.jv_relaunch_activity_not_found_text1));//"На сервере уже есть такой файл.");//"Скачать файлы");
            list_url[0] = getString(R.string.jv_relaunch_rename);//"Переименовать";
            list_url[1] = getString(R.string.jv_relaunch_rewrite);//"Заменить";
            final String finalDBPath = DBPath;
            final String finalTempDirName = tempDirName;
            builder.setItems(list_url, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {

                    if(item == 0){
                        final AlertDialog.Builder builder2 = new AlertDialog.Builder(activity);
                        builder2.setTitle(getString(R.string.jv_relaunch_rename_title));//"Введите новое имя");
                        final EditText input = new EditText(activity);
                        builder2.setView(input);
                        builder2.setCancelable(true);
                        builder2.setNegativeButton(getResources().getString(R.string.app_cancel), new DialogInterface.OnClickListener() { // Кнопка Отмена
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss(); // Отпускает диалоговое окно
                            }
                        });
                        builder2.setPositiveButton(getResources().getString(R.string.app_ok), new DialogInterface.OnClickListener() { // Кнопка Отмена
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newName = String.valueOf(input.getText()).trim();

                                if (newName.length() > 0) {
                                    uploadFilesOrDir(finalTempDirName, tempNameFile[0], finalDBPath, newName);
                                }else{
                                    uploadFilesOrDir(finalTempDirName, tempNameFile[0], finalDBPath, "");
                                }
                                dialog.dismiss(); // Отпускает диалоговое окно
                            }
                        });
                        builder2.show();
                    }
                    if(item == 1){
                        if (dropboxClient.delete(finalDBPath + "/" +tempNameFile[0])){
                            app.showToast(getString(R.string.srt_dbactivity_err_upl_dropbox));//"Ошибка при перезаписи.");
                        }

                        uploadFilesOrDir(dirname, tempNameFile[0], finalDBPath, "");
                    }
                    dialog.dismiss(); // Отпускает диалоговое окно
                }
            });
            builder.setCancelable(true);
            builder.setNegativeButton(getResources().getString(R.string.app_cancel), new DialogInterface.OnClickListener() { // Кнопка Отмена
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss(); // Отпускает диалоговое окно
                }
            });
            builder.show();

        }else if(checkRez == 0){
            uploadFilesOrDir(dirname, filename, DBPath, "");
        }
    }
    private int checkFileDB(String PathDB, String filenameOrDirname){
        DropboxAPI.Entry entries;

        entries = dropboxClient.metadata(PathDB);
        if (entries == null){
            return -1;
        }
        for (DropboxAPI.Entry e : entries.contents) {
            if (!e.isDeleted) {
                if(e.fileName().equals(filenameOrDirname)){
                    return 1;
                }
            }
        }
        return 0;
    }
    private boolean uploadFilesOrDir(String dirname, String filename, String DBPath, String newfilename) {
        //------------------------------------------------------------------------
        // выясняем чего копируем - папку или файл
        FileInputStream mFos;
        if(newfilename.equals("")){
            newfilename = filename;
        }
        String src = dirname + "/" + filename;
        File toDir = new File(src);
        if(toDir.isDirectory()){
            if (!uploadFiles(dirname + "/" + filename, (dirname + "/" ).length(), DBPath)) {
                return false;
            }
        }else{ // копирование одиночного файла
            File f1 = new File(src);
            try {
                mFos = new FileInputStream(src);
            } catch (FileNotFoundException e) {
                return false;
            }
            if(dropboxClient.putFile(DBPath + "/" + newfilename, mFos, f1.length())){
                app.showToast(getString(R.string.srt_dbactivity_err_upl_dropbox));//" Ошибка при записи файла на сервер");
                return false;
            }
        }
        //-----------------------------------------------------------------------
        app.showToast(getString(R.string.jv_prefs_rsr_ok_text));//" Успешно выполнено");
        return true;
    }
    private boolean uploadFiles(String Path, int start_pos_path, String DBPath) {
        String[] strDirList = (new File(Path)).list();
        String strPath = Path.substring(start_pos_path);
        FileInputStream mFos;

        for (String aStrDirList : strDirList) {

            File f1 = new File(Path+ File.separator + aStrDirList);
            if (f1.isFile()) {
                try {
                    mFos = new FileInputStream(f1);
                } catch (FileNotFoundException e) {
                    app.showToast(getString(R.string.jv_editor_openerr_text1));//" Ошибка при открытии файла");
                    return false;
                }
                if(!dropboxClient.putFile(DBPath + "/" + strPath + File.separator + f1.getName(), mFos, f1.length())){
                    app.showToast(getString(R.string.srt_dbactivity_err_upl_dropbox));//" Ошибка при записи на сервер");
                    return false;
                }
            } else {
                if (!uploadFiles(Path + File.separator + aStrDirList , start_pos_path, DBPath)) {
                    return false;
                }
            }
        }
        return true;
    }
    // проверка интернет соединения взята отседова:
    // http://stackoverflow.com/questions/7071578/connectivitymanager-to-verify-internet-connection
    public static boolean haveNetworkConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfoMob = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo netInfoWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return ((netInfoMob != null && netInfoMob.isConnectedOrConnecting()) || (netInfoWifi != null) && netInfoWifi.isConnectedOrConnecting());
    }
    //----------------------------------------------------------------------------------------------
    private String loadFileDB(String DropBox_Path_File, String nameFile) {

        SharedPreferences prefs2 =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String DBLocalPath = prefs2.getString("LocalfolderDropbox", "none");
        String str_f = DBLocalPath.trim();

        if(str_f.lastIndexOf("/") == str_f.length()-1){
            DBLocalPath = str_f.substring(0, str_f.length()-1);
        }else{
            DBLocalPath = str_f;
        }
        if(nameFile.startsWith("Dropbox| ")){
            nameFile = nameFile.substring("Dropbox| ".length());
        }
        File checkDir = new File(DBLocalPath);
        if(!checkDir.exists()){
            if(!checkDir.mkdirs()){
                return "";
            }
        }
        if(!DBdownload(DropBox_Path_File, DBLocalPath)){
            return "";
        }
        return (DBLocalPath + File.separator + nameFile);
    }
    private boolean DBdelete(String fullName) {
        DropboxAPI.Entry entries;

        String temp;
        temp = fullName.substring("Dropbox| ".length());

        entries = dropboxClient.metadata(temp);// может быть ошибка при получении коллекции
        if (entries != null) {
            if (entries.isDir) {
                dropboxClient.delete(entries.path + "/");
            } else {
                dropboxClient.delete(entries.path);
            }
        }else {
            return false;
        }

        return true;
    }
    private boolean DBdownload(String fullName, String localPath) {
        DropboxAPI.Entry entries;
        FileOutputStream mFos;
        File file;
        String temp;
        temp = fullName.substring("Dropbox| ".length());

        entries = dropboxClient.metadata(temp);// может быть ошибка при получении коллекции
        if (entries == null) {
            return false;
        }
        if(entries.isDir){
            file=new File(localPath + File.separator + entries.fileName());
            if (!file.exists()) {
                if(!file.mkdirs()){
                    return false;
                }
            }
            localPath +=File.separator + entries.fileName();
            for (DropboxAPI.Entry e : entries.contents) {
                if (!e.isDeleted) {
                    file=new File(localPath + File.separator + e.fileName());
                    if(e.isDir){
                        if (!file.exists()) {
                            if(!file.mkdirs()){
                                return false;
                            }
                        }
                        if(!DBdownload(fullName + File.separator + e.fileName(), localPath + File.separator + e.fileName())){
                            return false;
                        }
                    }else{
                        try {
                            mFos = new FileOutputStream(file);
                        } catch (FileNotFoundException e1) {
                            return false;
                        }
                        if (!dropboxClient.getFile(e.path, mFos)){
                            return false;
                        }
                        try {
                            mFos.close();
                        } catch (IOException e1) {
                            return false;
                        }
                    }
                }
            }
        }else{
            file=new File(localPath + File.separator + entries.fileName());
            try {
                mFos = new FileOutputStream(file);
            } catch (FileNotFoundException e1) {
                return false;
            }
            if (!dropboxClient.getFile(entries.path, mFos)){
                return false;
            }
            try {
                mFos.close();
            } catch (IOException e1) {
                return false;
            }
        }

        return true;
    }
    private boolean DBupload(String fullNameL, String DBPath) {
        File file = new File(fullNameL);
        FileInputStream mFos;
        String temp;
        temp = DBPath.substring("Dropbox| ".length());
        if(file.isDirectory()){
            dropboxClient.createFolder(temp + File.separator + file.getName());
            String[] strDirList = file.list();
            for (String aStrDirList : strDirList) {
                File f1 = new File(aStrDirList);
                if (f1.isFile()) {
                    try {
                        mFos = new FileInputStream(f1);
                    } catch (FileNotFoundException e) {
                        return false;
                    }
                    if(!dropboxClient.putFile(temp + File.separator + f1.getName(), mFos, f1.length())){
                        return false;
                    }
                } else {
                    dropboxClient.createFolder(temp + File.separator + f1.getName());
                    if (!DBupload(fullNameL + File.separator + f1.getName(), DBPath + File.separator + f1.getName())) {
                        return false;
                    }
                }
            }
        }else{
            try {
                mFos = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                return false;
            }
            if(!dropboxClient.putFile(temp + File.separator + file.getName(), mFos, file.length())){
                return false;
            }
        }
        return true;
    }
    private boolean DBmoveORcopy(String DBPathSRC, String DBPathDst, boolean f_copy) {

        String tempSrc, tempDst;
        if(DBPathSRC.startsWith("Dropbox| ")){
            tempSrc = DBPathSRC.substring("Dropbox| ".length());
        }else{
            tempSrc = DBPathSRC;
        }
        if(DBPathDst.startsWith("Dropbox| ")){
            tempDst = DBPathDst.substring("Dropbox| ".length());
        }else{
            tempDst = DBPathDst;
        }

        if(f_copy){
            dropboxClient.copy(tempSrc, tempDst);
        }else{
            dropboxClient.move(tempSrc, tempDst);
        }
        return true;
    }
    //===============================================
    // ========= load settings ======================
    private void loadStandartIcons() {
        // иконки для файлов заранее заносим в массив========================
        imageIcon temp_icon = new imageIcon();
        temp_icon.icon = scaleDrawableById(R.drawable.dir_ok, firstLineIconSizePx);
        temp_icon.nameIcon = "dir_ok";
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.icon = scaleDrawableById(R.drawable.file_ok, firstLineIconSizePx);
        temp_icon.nameIcon = "file_ok";
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.icon = scaleDrawableById(R.drawable.file_notok, firstLineIconSizePx);
        temp_icon.nameIcon = "file_notok";
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.icon = scaleDrawableById(R.drawable.icon_list, firstLineIconSizePx);
        temp_icon.nameIcon = "icon";
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.icon = scaleDrawableById(R.drawable.install, firstLineIconSizePx);
        temp_icon.nameIcon = "install";
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.nameIcon = "opdslist";
        temp_icon.icon = scaleDrawableById(R.drawable.ci_books, firstLineIconSizePx);
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.nameIcon = "parent_ok";
        temp_icon.icon = scaleDrawableById(R.drawable.ci_levelup_big, firstLineIconSizePx);
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.nameIcon = "parent_off";
        temp_icon.icon = scaleDrawableById(R.drawable.ci_levelup_big_gray, firstLineIconSizePx);
        arrIcon.add(temp_icon);
        //===== иконки всех программ =====
        Drawable d = null;
        Intent componentSearchIntent = new Intent();
        componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        componentSearchIntent.setAction(Intent.ACTION_MAIN);
        PackageManager pm = getPackageManager();
        if(pm == null){
            return;
        }
        List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);
        String pname;
        String aname;
        String hname = "";
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                temp_icon = new imageIcon();
                pname = ri.activityInfo.packageName;
                aname = ri.activityInfo.name;
                try {
                    if (ri.activityInfo.labelRes != 0) {
                        hname = (String) ri.activityInfo.loadLabel(pm);
                    } else {
                        hname = (String) ri.loadLabel(pm);
                    }
                    if (ri.activityInfo.icon != 0) {
                        d = ri.activityInfo.loadIcon(pm);
                    } else {
                        d = ri.loadIcon(pm);
                    }
                } catch (Exception e) {
                    // emply
                }
                if (d != null) {
                    temp_icon.icon = scaleDrawable(d, firstLineIconSizePx);
                }else{
                    temp_icon.icon = scaleDrawableById(R.drawable.file_notok, firstLineIconSizePx);
                }
                temp_icon.nameIcon = pname + "%" + aname + "%" + hname;
                arrIcon.add(temp_icon);
            }
        }
    }
    private void initGlobalVariable() {
        // Global arrays
        allowedModels = getResources().getStringArray(R.array.allowed_models);
        allowedDevices = getResources().getStringArray(R.array.allowed_devices);
        allowedManufacts = getResources().getStringArray(R.array.allowed_manufacturers);
        allowedProducts = getResources().getStringArray(R.array.allowed_products);

        // Create global storage with values
        app = (ReLaunchApp) getApplicationContext();

        if (app != null) {
            app.FLT_SELECT = getResources().getInteger(R.integer.FLT_SELECT);
            app.FLT_STARTS = getResources().getInteger(R.integer.FLT_STARTS);
            app.FLT_ENDS = getResources().getInteger(R.integer.FLT_ENDS);
            app.FLT_CONTAINS = getResources().getInteger(R.integer.FLT_CONTAINS);
            app.FLT_MATCHES = getResources().getInteger(R.integer.FLT_MATCHES);
            app.FLT_NEW = getResources().getInteger(R.integer.FLT_NEW);
            app.FLT_NEW_AND_READING = getResources().getInteger(R.integer.FLT_NEW_AND_READING);
            File filesDir = app.getFilesDir();
            if(filesDir != null){
                app.DATA_DIR = filesDir.getParent();
            }else{
                app.DATA_DIR = "/data/data/com.harasoft.relaunch";
            }
        }else{
            finish();
        }
    }
    private void initPrefsVariable() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefsEditor = prefs.edit();
        // установка языка приложения
        String lang;
        EinkScreen.setEinkController(prefs);
        lang = prefs.getString("lang", "default");
        if (!lang.equals("default")) {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, null);
        }
        // ====== FLSimpleAdapter==================================================================
        useFaces = prefs.getBoolean("showNew", true);
        firstLineIconSizePx = Integer.parseInt(prefs.getString("firstLineIconSizePx", "48"));  // по возможности вынести из адаптера
        firstLineFontSizePx = Integer.parseInt(prefs.getString("firstLineFontSizePx", "20"));
        secondLineFontSizePx = Integer.parseInt(prefs.getString("secondLineFontSizePx", "16"));
        doNotHyph = prefs.getBoolean("doNotHyph", false);
        hideKnownExts = prefs.getBoolean("hideKnownExts", false);
        showBookTitles = prefs.getBoolean("showBookTitles", false);
        if (app.dataBase == null) {
            app.dataBase = new BooksBase(this);
        }
        rowSeparator = prefs.getBoolean("rowSeparator", false);
        disableScrollJump = prefs.getBoolean("disableScrollJump", true);
        //=================================================================================
        // ====== GetDateAndMewory()==================================================================
        dateUS = prefs.getBoolean("dateUS", false);
        //=================================================================================
        // ====== getAutoColsNum==================================================================
        columnsAlgIntensity = prefs.getString("columnsAlgIntensity", "70 3:5 7:4 15:3 48:2");
        //=================================================================================
        // ====== redrawList==================================================================
        filterResults = prefs.getBoolean("filterResults", false);
        //=================================================================================
        // ====== setUpButton==================================================================
        notLeaveStartDir = prefs.getBoolean("notLeaveStartDir", false);

        startDir = app.loadStartDirs();
        // ====== drawDirectory==================================================================
        showFullDirPath = prefs.getBoolean("showFullDirPath", true);
        showHidden = prefs.getBoolean("showHidden", false);
        showOnlyKnownExts = prefs.getBoolean("showOnlyKnownExts", false);
        showButtonParenFolder = prefs.getBoolean("showButtonParenFolder", false);
        bookTitleFormat = prefs.getString("bookTitleFormat", "%t[\n%a][. %s][-%n]");
		showFileOperation = prefs.getBoolean("showFileOperation", true);
		//===================================================================
        // параметры прокрутки экрана
        try {
            app.scrollStep = Integer.parseInt(prefs.getString("scrollPerc","10"));
            app.viewerMax = Integer.parseInt(prefs.getString("viewerMaxSize","1024"));
            app.editorMax = Integer.parseInt(prefs.getString("editorMaxSize","256"));
        } catch (NumberFormatException e) {
            app.scrollStep = 10;
            app.viewerMax = 1024;
            app.editorMax = 256;
        }
        if (app.scrollStep < 1){
            app.scrollStep = 1;
        }
        if (app.scrollStep > 100){
            app.scrollStep = 100;
        }
        blockExitLauncher = prefs.getBoolean("blockExit", true);
        //---------------------------------------------------------------------
        // определяем есть ли возможность работать от root
		selectRootNavigation = prefs.getBoolean("selectRootNavigation", false);
        accessRoot = RootTools.isAccessGiven();
        if (selectRootNavigation){
            selectRootNavigation = accessRoot;
        }
        //---------------------------------------------------------------------
        FTPTempDir = prefs.getString("ftpTempDir", "/sdcard/.FTP_temp");
        //=======================================================================
        fileExtendedData = prefs.getBoolean("fileExtendedFormat", false);
        if(fileExtendedData){
            fileExtendedDataFormat = prefs.getString("fileExtendedDataFormat", "");
        }

		currentHomeDir = prefs.getString("intentStartDir", "/");

        app.readFile("app_last", APP_LRU_FILE, ":");
        app.readFile("app_favorites", APP_FAV_FILE, ":");
        app.readFile("lastOpened", LRU_FILE, "/");
        app.readFile("favorites", FAV_FILE, "/");

        app.askIfAmbiguous = prefs.getBoolean("askAmbig", false);
    }
    // ================ =============================
    // небольшой модуль для удалени файла или папки
    private void delAll(String fullName, String dname, String fname, int pos) {
        if(fullName.startsWith("FTP| ")) {
            boolean dirBool = false;
            if (itemsArray.get(pos).get("type").equals("dir")){
                dirBool = true;
            }
            if(connectorFTP.delete(fullName,dirBool)){
                itemsArray.remove(pos);
            }
        }else if(fullName.startsWith("Dropbox| ")) {
            if(DBdelete(fullName)){
                itemsArray.remove(pos);
            }
        }else{
            if(app.fileRemove(new File(dname + "/" + fname))) {
                app.fileRemoveAllList(dname, fname);
                if (pos != -1) {
                    itemsArray.remove(pos);
                }
            }

        }
        drawDirectory(currentRoot, currentPosition);
    }
    private void delSelect (){
        HashMap<String, String> i;
        int j = arrSelItem.size();
        final String arrFullName[] = new String[j];
        final String arrDName[] = new String[j];
        final String arrFName[] = new String[j];
        final int arrPos[] = new int[j];

        String listFiles = "";

        for(int i1=0; i1 < j; i1++){
            i = itemsArray.get(arrSelItem.get(i1));
            arrFullName[i1] = i.get("fname");
            arrDName[i1] = i.get("dname");
            arrFName[i1] = i.get("name");
            arrPos[i1] = arrSelItem.get(i1);
            listFiles = listFiles + "\n" + arrFName[i1];
        }

        if (prefs.getBoolean("confirmFileDelete", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // "Delete file warning"
            builder.setTitle(getResources().getString(R.string.jv_relaunch_del_sel_file_title));
            // "Are you sure to delete file \"" + fname + "\" ?"
            builder.setMessage(getResources().getString(
                    R.string.jv_relaunch_del_sel_file_text1)
                    + listFiles
                    + getResources().getString(R.string.jv_relaunch_del_file_text2));
            // "Yes"
            builder.setPositiveButton(getResources().getString(R.string.app_yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            for(int h = arrFullName.length - 1, k = -1; h > k; h--) {
                                delAll(arrFullName[h], arrDName[h], arrFName[h], arrPos[h]);
                            }
                        }
                    }
            );
            // "No"
            builder.setNegativeButton(getResources().getString(R.string.app_no),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    }
            );
            builder.show();
        } else {
            for(int h = 0, k = arrFullName.length; h < k; h++) {
                delAll(arrFullName[h], arrDName[h], arrFName[h], arrPos[h]);
            }
        } }
    //========== Панели =============================
    private void drawingFirstPanel (LayoutInflater ltInflater, LinearLayout ll_container){
        // = Первая панель с документами
        ltInflater.inflate(R.layout.ll_doc, ll_container, true);

        // Home button
        final ImageButton home_button = (ImageButton) findViewById(R.id.home_btn);
        class HomeSimpleOnGestureListener extends
                SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                tapButton("homeButtonST");
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                tapButton("homeButtonDT");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (home_button.hasWindowFocus()) {
                    tapButton("homeButtonLT");
                }
            }
        }

        HomeSimpleOnGestureListener home_gl = new HomeSimpleOnGestureListener();
        final GestureDetector home_gd = new GestureDetector(home_gl);
        home_button.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                home_gd.onTouchEvent(event);
                return false;
            }
        });
        //  Settings button
        final ImageButton settings_button = (ImageButton) findViewById(R.id.settings_btn);
        class SettingsSimpleOnGestureListener extends
                SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                tapButton("settingsButtonST");
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                tapButton("settingsButtonDT");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (settings_button.hasWindowFocus()) {
                    tapButton("settingsButtonLT");
                }
            }
        }

        SettingsSimpleOnGestureListener settings_gl = new SettingsSimpleOnGestureListener();
        final GestureDetector settings_gd = new GestureDetector(
                settings_gl);
        settings_button.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                settings_gd.onTouchEvent(event);
                return false;
            }
        });
        // Search button
        final ImageButton search_button = (ImageButton) findViewById(R.id.search_btn);
        class SearchSimpleOnGestureListener extends
                SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                tapButton("searchButtonST");
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                tapButton("searchButtonDT");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (settings_button.hasWindowFocus()) {
                    tapButton("searchButtonLT");
                }
            }
        }

        SearchSimpleOnGestureListener search_gl = new SearchSimpleOnGestureListener();
        final GestureDetector search_gd = new GestureDetector(search_gl);
        search_button.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                search_gd.onTouchEvent(event);
                return false;
            }
        });
        // Last open book
        final ImageButton lru_button = (ImageButton) findViewById(R.id.lru_btn);
        class LruSimpleOnGestureListener extends
                SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                tapButton("lruButtonST");
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                tapButton("lruButtonDT");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (lru_button.hasWindowFocus()) {
                    tapButton("lruButtonLT");
                }
            }
        }

        LruSimpleOnGestureListener lru_gl = new LruSimpleOnGestureListener();
        final GestureDetector lru_gd = new GestureDetector(lru_gl);
        lru_button.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                lru_gd.onTouchEvent(event);
                return false;
            }
        });
        // Favorites book
        final ImageButton fav_button = (ImageButton) findViewById(R.id.favor_btn);
        class FavSimpleOnGestureListener extends
                SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                tapButton("favButtonST");
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                tapButton("favButtonDT");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (fav_button.hasWindowFocus()) {
                    tapButton("favButtonLT");
                }
            }
        }

        FavSimpleOnGestureListener fav_gl = new FavSimpleOnGestureListener();
        final GestureDetector fav_gd = new GestureDetector(fav_gl);
        fav_button.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                fav_gd.onTouchEvent(event);
                return false;
            }
        });
    }
    private void drawingSecondPanel (LayoutInflater ltInflater, LinearLayout ll_container) {
        // = Вторая панель с кнопко оттображения пути
        ltInflater.inflate(R.layout.ll_button, ll_container, true);
        // кнопка заголовка на которой отображается текущая папка
        tv_title = (Button) findViewById(R.id.title_txt);
        class TvSimpleOnGestureListener extends SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                final String[] columns = getResources().getStringArray(R.array.output_columns_names);
                final CharSequence[] columnsmode = new CharSequence[columns.length + 1];
                columnsmode[0] = getResources().getString(
                        R.string.jv_relaunch_default);
                System.arraycopy(columns, 0, columnsmode, 1, columns.length);
                Integer checked;
                if (app.columns.containsKey(currentRoot)) {
                    if (app.columns.get(currentRoot) == -1) {
                        checked = 1;
                    } else {
                        checked = app.columns.get(currentRoot) + 1;
                    }
                } else {
                    checked = 0;
                }
                // get checked
                AlertDialog.Builder builder = new AlertDialog.Builder( ReLaunch.this);
                // "Select application"
                builder.setTitle(getResources().getString(R.string.jv_relaunch_select_columns));
                builder.setSingleChoiceItems(columnsmode, checked,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    app.columns.remove(currentRoot);
                                } else {
                                    if (i == 1) {
                                        app.columns.put(currentRoot, -1);
                                    } else {
                                        app.columns.put(currentRoot, i - 1);
                                    }
                                }
                                app.saveList("columns");
                                drawDirectory(currentRoot, currentPosition);
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                menuSort();
            }
        }

        TvSimpleOnGestureListener tv_gl = new TvSimpleOnGestureListener();
        final GestureDetector tv_gd = new GestureDetector(tv_gl);
        tv_title.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                tv_gd.onTouchEvent(event);
                return false;
            }
        });
    }
    private void drawingThirdPanel (LayoutInflater ltInflater, LinearLayout ll_container){
        // = Третья панель навигации
        ltInflater.inflate(R.layout.ll_navigate, ll_container, true);
        // Advanced button
        final ImageButton adv = (ImageButton) findViewById(R.id.advanced_btn);
        if (adv != null) {
            class advSimpleOnGestureListener extends SimpleOnGestureListener {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    tapButton("advancedButtonST");
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    tapButton("advancedButtonDT");
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    if (adv.hasWindowFocus()) {
                        tapButton("advancedButtonLT");
                    }
                }
            }

            advSimpleOnGestureListener adv_gl = new advSimpleOnGestureListener();
            final GestureDetector adv_gd = new GestureDetector(adv_gl);
            adv.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    adv_gd.onTouchEvent(event);
                    return false;
                }
            });
        }

        // кнопка перехода в родительскую папку
        upButton = (Button) findViewById(R.id.goup_btn);
        // gesture listener
        class UpSimpleOnGestureListener extends SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                TapUpDir();
                return true;
            }
        }

        UpSimpleOnGestureListener up_gl = new UpSimpleOnGestureListener();
        final GestureDetector up_gd = new GestureDetector(up_gl);
        upButton.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                up_gd.onTouchEvent(event);
                return false;
            }
        });

        // up scroll button
        upScroll = (Button) findViewById(R.id.upscroll_btn);
        if (disableScrollJump) {
            upScroll.setText(getResources().getString(R.string.jv_relaunch_prev));
        } else {
            upScroll.setText(app.scrollStep + "%");
        }
        class upScrlSimpleOnGestureListener extends SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                app.TapUpScrool(gvList, itemsArray.size());
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!disableScrollJump) {
                    int first = gvList.getFirstVisiblePosition();
                    int total = itemsArray.size();
                    first -= (total * app.scrollStep) / 100;
                    if (first < 0)
                        first = 0;
                    gvList.setSelection(first);
                    // some hack workaround against not scrolling in some cases
                    if (total > 0) {
                        gvList.requestFocusFromTouch();
                        gvList.setSelection(first);
                    }
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (upScroll.hasWindowFocus()) {
                    if (!disableScrollJump) {
                        int total = itemsArray.size();
                        gvList.setSelection(0);
                        // some hack workaround against not scrolling in some
                        // cases
                        if (total > 0) {
                            gvList.requestFocusFromTouch();
                            gvList.setSelection(0);
                        }
                    }
                }
            }
        }

        upScrlSimpleOnGestureListener upscrl_gl = new upScrlSimpleOnGestureListener();
        final GestureDetector upscrl_gd = new GestureDetector(upscrl_gl);
        upScroll.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                upscrl_gd.onTouchEvent(event);
                return false;
            }
        });

        // down scroll button
        downScroll = (Button) findViewById(R.id.downscroll_btn);
        if (disableScrollJump) {
            downScroll.setText(getResources().getString(R.string.jv_relaunch_next));
        } else {
            downScroll.setText(app.scrollStep + "%");
        }
        class dnScrlSimpleOnGestureListener extends SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
				app.TapDownScrool(gvList, itemsArray.size());
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!disableScrollJump) {
                    int first = gvList.getFirstVisiblePosition();
                    int total = itemsArray.size();
                    int last = gvList.getLastVisiblePosition();
                    if (total == last + 1)
                        return true;
                    int target = first + (total * app.scrollStep) / 100;
                    if (target <= last)
                        target = last + 1; // Special for NOOK, otherwise it
                    // won't redraw the listview
                    if (target > (total - 1))
                        target = total - 1;
					//RepeatedDownScroll ds = new RepeatedDownScroll();
					//ds.doIt(first, target, 0);
					app.RepeatedDownScroll(gvList, first, target, 0);
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (downScroll.hasWindowFocus()) {
                    if (!disableScrollJump) {
                        int first = gvList.getFirstVisiblePosition();
                        int total = itemsArray.size();
                        int last = gvList.getLastVisiblePosition();
                        if (total == last + 1)
                            return;
                        int target = total - 1;
						app.RepeatedDownScroll(gvList, first, target, 0);
                    }
                }
            }
        }

        dnScrlSimpleOnGestureListener dnscrl_gl = new dnScrlSimpleOnGestureListener();
        final GestureDetector dnscrl_gd = new GestureDetector(dnscrl_gl);
        downScroll.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                dnscrl_gd.onTouchEvent(event);
                return false;
            }
        });
    }
    private void drawingFourthPanel (LayoutInflater ltInflater, LinearLayout ll_container){
        // = Четвертая панель со списком файла
        ltInflater.inflate(R.layout.ll_filelist, ll_container, true);

        //=============================================================================
        // список файлов и папок
        String[] from = new String[] { "name" };
        int[] to = new int[] { R.id.fl_text };
        gvList = (GridView) findViewById(R.id.gl_list);
        adapter = new FLSimpleAdapter1(this, itemsArray, R.layout.flist_layout, from, to);
        gvList.setAdapter(adapter);
        gvList.setHorizontalSpacing(0);
        if (prefs.getBoolean("customScroll", app.customScrollDef)) {
            if (addSView) {
                int scrollW;
                try {
                    scrollW = Integer.parseInt(prefs.getString("scrollWidth", "25"));
                } catch (NumberFormatException e) {
                    scrollW = 25;
                }
                LinearLayout ll = (LinearLayout) findViewById(R.id.gl_layout);
                final SView sv = new SView(getBaseContext());
                LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(scrollW, ViewGroup.LayoutParams.FILL_PARENT, 1f);
                sv.setLayoutParams(pars);
                ll.addView(sv);
                gvList.setOnScrollListener(new AbsListView.OnScrollListener() {
                    public void onScroll(AbsListView view,
                                         int firstVisibleItem, int visibleItemCount,
                                         int totalItemCount) {
                        sv.total = totalItemCount;
                        sv.count = visibleItemCount;
                        sv.first = firstVisibleItem;
                        EinkScreen.PrepareController(null, false);
                        sv.invalidate();
                    }

                    public void onScrollStateChanged(AbsListView view,
                                                     int scrollState) {
                    }
                });
                addSView = false;
            }
        } else {
            gvList.setOnScrollListener(new AbsListView.OnScrollListener() {
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    EinkScreen.PrepareController(null, false);
                }

                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }
            });
        }
        class GlSimpleOnGestureListener extends SimpleOnGestureListener {
            Context context;
			String finalFullName;
			String finalDr;
			String finalFn;
			int menuType;
			int pos;
			String[] list;

            public GlSimpleOnGestureListener(Context context) {
                super();
                this.context = context;
            }
            public int findViewByXY(MotionEvent e) {
                int location[] = new int[2];
                float x = e.getRawX();
                float y = e.getRawY();
                int first = gvList.getFirstVisiblePosition();
                int last = gvList.getLastVisiblePosition();
                int count = last -first + 1;
                for (int i = 0; i<count; i++) {
                    View v = gvList.getChildAt(i);
                    if(v == null){
                        return -1;
                    }
                    v.getLocationOnScreen(location);
                    int viewX = location[0];
                    int viewY = location[1];

                    if(( x > viewX && x < (viewX + v.getWidth())) && ( y > viewY && y < (viewY + v.getHeight()))){
                        int ost = (int) (x - viewX);
                        if(prefs.getBoolean("selectFileTapIcon", true) && (ost < (firstLineIconSizePx + 1))){
                            if (showButtonParenFolder && (first + i) == 0){
                                return 0;
                            }
                            if(arrSelItem.contains(first + i)){
                                // пункт уже отмечен. надо снять отметку
                                arrSelItem.remove(arrSelItem.indexOf(first + i));
                            }else{
                                // добавить отмеченный новый пункт в массив
                                arrSelItem.add(first + i);
                            }
                            reDraw();
                            return -1;
                        }
                        return first + i;
                    }
                }
                return -1;
            }
            private void MenuSelect(String s, int pos){
                if (s.equalsIgnoreCase(getString(R.string.app_cancel)))
                    onContextMenuSelected(CNTXT_MENU_CANCEL, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_delete)))
                    onContextMenuSelected(CNTXT_MENU_DELETE_F, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_delete_emp_dir)))
                    onContextMenuSelected(CNTXT_MENU_DELETE_D_EMPTY, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_delete_non_emp_dir)))
                    onContextMenuSelected(CNTXT_MENU_DELETE_D_NON_EMPTY, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_add)))
                    onContextMenuSelected(CNTXT_MENU_ADD, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_mark)))
                    onContextMenuSelected(CNTXT_MENU_MARK_FINISHED, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_unmark)))
                    onContextMenuSelected(CNTXT_MENU_MARK_READING, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_unmarkall)))
                    onContextMenuSelected(CNTXT_MENU_MARK_FORGET, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_createintent)))
                    onContextMenuSelected(CNTXT_MENU_INTENT, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_openwith)))
                    onContextMenuSelected(CNTXT_MENU_OPENWITH, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_copy)))
                    onContextMenuSelected(CNTXT_MENU_COPY_FILE, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_move)))
                    onContextMenuSelected(CNTXT_MENU_MOVE_FILE, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_paste)))
                    onContextMenuSelected(CNTXT_MENU_PASTE, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_rename)))
                    onContextMenuSelected(CNTXT_MENU_RENAME, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_create_folder)))
                    onContextMenuSelected(CNTXT_MENU_CREATE_DIR, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_tags_rename)))
                    onContextMenuSelected(CNTXT_MENU_TAGS_RENAME, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_set_startdir)))
                    onContextMenuSelected(CNTXT_MENU_SET_STARTDIR, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_add_startdir)))
                    onContextMenuSelected(CNTXT_MENU_ADD_STARTDIR, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_bookinfo)))
                    onContextMenuSelected(CNTXT_MENU_SHOW_BOOKINFO, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_fileinfo)))
                    onContextMenuSelected(CNTXT_MENU_FILE_INFO, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_add_Dropbox)))
                    onContextMenuSelected(CNTXT_MENU_COPY_DROPBOX, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_add_dir_Dropbox)))
                    onContextMenuSelected(CNTXT_MENU_COPY_DIR_DROPBOX, pos);
                else if (s.equalsIgnoreCase(getString(R.string.app_settings_settings)))
                    onContextMenuSelected(CNTXT_MENU_SETTINGS, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_selecte)) || s.equalsIgnoreCase(getString(R.string.jv_relaunch_unselecte)))
                    onContextMenuSelected(CNTXT_MENU_SELECTE, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_fileunzip)))
                    onContextMenuSelected(CNTXT_MENU_FILEUNZIP, pos);
                else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_permission)))
                    onContextMenuSelected(CNTXT_MENU_PERMISSION, pos);
				else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_return)))
					FirstDialog(list);
            }

            private ArrayList<String> CreateArrangeMenu(int menuType, String fullName, String dr, String fn){
                ArrayList<String> arrangeList = new ArrayList<String>(10);
                switch (menuType) {
                    case 1:
                        if (!app.contains("favorites", fullName, app.DIR_TAG)){
                            if (!dr.startsWith("FTP| ")) {
                                arrangeList.add(getString(R.string.jv_relaunch_add));
                            }
                        }
                        // Set as Start folders & Add to Start folders
                        if ((!app.isStartDir(fullName)) && (prefs.getBoolean("showAddStartDir", false))) {
                            if (!dr.startsWith("FTP| ")) {
                                arrangeList.add(getString(R.string.jv_relaunch_set_startdir));
                                arrangeList.add(getString(R.string.jv_relaunch_add_startdir));
                            }
                        }
                        break;
                    case 2:
                        if (!app.contains("favorites", dr, fn)) {
                            if (!dr.startsWith("FTP| ")) {
                                arrangeList.add(getString(R.string.jv_relaunch_add));
                            }
                        }
                        // Mark as read & unread
                        if (app.history.containsKey(fullName)) {
                            if (app.history.get(fullName) == app.READING) {
                                arrangeList.add(getString(R.string.jv_relaunch_mark));
                            } else if (app.history.get(fullName) == app.FINISHED) {
                                arrangeList.add(getString(R.string.jv_relaunch_unmark));
                            }

                            arrangeList.add(getString(R.string.jv_relaunch_unmarkall));
                        } else {
                            arrangeList.add(getString(R.string.jv_relaunch_mark));
                        }
                        // Open with
                        if (prefs.getBoolean("openWith", true)) {
                            arrangeList.add(getString(R.string.jv_relaunch_openwith));
                        }
                        // Unzip
                        if (fn.toLowerCase().endsWith("zip")){
                            arrangeList.add(getString(R.string.jv_relaunch_fileunzip));
                        }
                        break;
                    case 3:
                        if (!app.contains("favorites", dr, fn)) {
                            arrangeList.add(getString(R.string.jv_relaunch_add));
                        }
                        // Mark & unmark && all
                        if (app.history.containsKey(fullName)) {
                            if (app.history.get(fullName) == app.READING) {
                                arrangeList.add(getString(R.string.jv_relaunch_mark));
                            } else if (app.history.get(fullName) == app.FINISHED) {
                                arrangeList.add(getString(R.string.jv_relaunch_unmark));
                            }
                            arrangeList.add(getString(R.string.jv_relaunch_unmarkall));
                        } else {
                            arrangeList.add(getString(R.string.jv_relaunch_mark));
                        }
                        // Open with...
                        if (prefs.getBoolean("openWith", true)) {
                            arrangeList.add(getString(R.string.jv_relaunch_openwith));
                        }
                        // Unzip
                        if (fn.toLowerCase().endsWith("zip")){
                            arrangeList.add(getString(R.string.jv_relaunch_fileunzip));
                        }

                        break;

                }
                return arrangeList;
            }
            private ArrayList<String> CreateSystemMenu(int menuType, int position){
                ArrayList<String> arrangeList = new ArrayList<String>(10);
                switch (menuType) {
                    case 1:
                        // Permission
                        if (selectRootNavigation){
                            arrangeList.add(getString(R.string.jv_relaunch_permission));
                        }
                        // Properties
                        arrangeList.add(getString(R.string.jv_relaunch_fileinfo));
                        // Open settings windows
                        arrangeList.add(getString(R.string.app_settings_settings));
                        // Select
                        if (prefs.getBoolean("useFileManagerFunctions", true)) {
                            if (arrSelItem.contains(position)) {
                                arrangeList.add(getString(R.string.jv_relaunch_unselecte));
                            } else {
                                arrangeList.add(getString(R.string.jv_relaunch_selecte));
                            }
                        }
                        break;
                    case 2:
                        // Permission
                        if (selectRootNavigation){
                            arrangeList.add(getString(R.string.jv_relaunch_permission));
                        }
                        // Properties
                        arrangeList.add(getString(R.string.jv_relaunch_fileinfo));
                        // Open settings windows
                        arrangeList.add(getString(R.string.app_settings_settings));
                        // Create Intent
                        if (prefs.getBoolean("createIntent", false)) {
                            arrangeList.add(getString(R.string.jv_relaunch_createintent));
                        }
                        // Select
                        if (arrSelItem.contains(position)) {
                            arrangeList.add(getString(R.string.jv_relaunch_unselecte));
                        } else {
                            arrangeList.add(getString(R.string.jv_relaunch_selecte));
                        }
                        break;
                    case 3:
                        // Permission
                        if (selectRootNavigation){
                            arrangeList.add(getString(R.string.jv_relaunch_permission));
                        }
                        // Properties
                        arrangeList.add(getString(R.string.jv_relaunch_fileinfo));
                        //Open settings windows
                        arrangeList.add(getString(R.string.app_settings_settings));
                        // Create Intent
                        if (prefs.getBoolean("createIntent", false)) {
                            arrangeList.add(getString(R.string.jv_relaunch_createintent));
                        }
                        // Select
                        if (arrSelItem.contains(position)) {
                            arrangeList.add(getString(R.string.jv_relaunch_unselecte));
                        } else {
                            arrangeList.add(getString(R.string.jv_relaunch_selecte));
                        }

                        break;

                }
                return arrangeList;
            }
			private ArrayList<String> FileOperationsMenu(String fullName){
				ArrayList<String> arrangeList = new ArrayList<String>(10);

				if (prefs.getBoolean("useFileManagerFunctions", true)) {
					// Create folder
					arrangeList.add(getString(R.string.jv_relaunch_create_folder));
					// Rename file with tag
					if (!showBookTitles) {
						arrangeList.add(getString(R.string.jv_relaunch_tags_rename));
					}
					// Rename
					if (!showBookTitles) {
						arrangeList.add(getString(R.string.jv_relaunch_rename));
					}
					// Paste
					if (fileOp != 0) {
						arrangeList.add(getString(R.string.jv_relaunch_paste));
					}
					// Move
					arrangeList.add(getString(R.string.jv_relaunch_move));
					// Copy
					arrangeList.add(getString(R.string.jv_relaunch_copy));
					// Delete
					arrangeList.add(getString(R.string.jv_relaunch_delete));
				}
				// Dropbox
				if(!fullName.startsWith("Dropbox| ")){
					arrangeList.add(getString(R.string.jv_relaunch_add_Dropbox));
					arrangeList.add(getString(R.string.jv_relaunch_add_dir_Dropbox));
				}
				return arrangeList;
			}

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                int position = findViewByXY(e);
                if (position == -1){
                    return true;
                }

                HashMap<String, String> item = itemsArray.get(position);
                if (item.get("type").equals("dir")) {
                    // Goto directory
                    if (item.get("name").equals("..")){
                        TapUpDir();
                    }else {
                        pushCurrentPos(gvList, true);
                        drawDirectory(item.get("fname"), -1);
                    }
                }else{
                    String tempName = item.get("fname");
                    String fname = tempName;
                    if(tempName.startsWith("FTP| ")){
                        if (downloadFTP(connectorFTP, tempName.substring(0, tempName.lastIndexOf("/")), tempName.substring(tempName.lastIndexOf("/")+1) , "")){
                            fname = FTPTempDir + "/" + File.separator + tempName.substring(tempName.lastIndexOf("/")+1);
                        }
                    }
                    if(tempName.startsWith("Dropbox| ")){
                        fname = loadFileDB(tempName, item.get("name"));
                    }
                    if (app.specialAction(ReLaunch.this, fname)){
                        pushCurrentPos(gvList, false);
                    }else {
                        prefsEditor.putInt("posInFolder", gvList.getFirstVisiblePosition());
                        prefsEditor.commit();
                        pushCurrentPos(gvList, false);
                        if (item.get("reader").equals("Nope")){
                            app.defaultAction(ReLaunch.this, fname);
                        }else {
                            // Launch reader
							app.LaunchReader(fname);
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                int position = findViewByXY(e);
                if (position != -1) {
                    HashMap<String, String> item = itemsArray.get(position);
                    String file = item.get("dname") + "/" + item.get("name");
                    if (file.endsWith("fb2") || file.endsWith("fb2.zip") || file.endsWith("epub")) {
                        prefsEditor.putInt("posInFolder", gvList.getFirstVisiblePosition());
                        prefsEditor.commit();
                        pushCurrentPos(gvList, false);
                        String temp = file;
                        if(temp.startsWith("Dropbox| ")) {
                            temp = loadFileDB(temp, item.get("name"));
                        }
						if(temp.startsWith("FTP| ")){
							if (downloadFTP(connectorFTP, temp.substring(0, temp.lastIndexOf("/")), temp.substring(temp.lastIndexOf("/")+1) , "")){
								temp = FTPTempDir + "/" + File.separator + temp.substring(temp.lastIndexOf("/")+1);
							}
						}
                        showBookInfo(temp);
                    }
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (!ReLaunch.this.hasWindowFocus())
                    return;
                //final int menuType;
                int position = findViewByXY(e);
                HashMap<String, String> item;
                String fn = null;
                String dr = null;
                String tp;
                String fullName = null;
                ArrayList<String> aList = new ArrayList<String>(10);
                if (position == -1)
                    menuType = 0;
                else {
                    item = itemsArray.get(position);
                    fn = item.get("name");
                    dr = item.get("dname");
                    tp = item.get("type");
                    fullName = dr + "/" + fn;
                    if (tp.equals("dir")) {
                        menuType = 1;
                        if (fn.equals("..")){
                            return;
                        }
                    }else if (fn.endsWith("fb2") || fn.endsWith("fb2.zip") || fn.endsWith("epub"))
                        menuType = 2;
                    else
                        menuType = 3;
                }

                switch (menuType) {
                    case 0:
                        if (prefs.getBoolean("useFileManagerFunctions", true)) {
                            aList.add(getString(R.string.jv_relaunch_create_folder));
                            if (fileOp != 0) {
                                aList.add(getString(R.string.jv_relaunch_paste));
                            }
                        }
                        // Open settings windows
                        aList.add(getString(R.string.app_settings_settings));
                        break;
                    case 1:

							if (prefs.getBoolean("useFileManagerFunctions", true)) {
								// Rename
								aList.add(getString(R.string.jv_relaunch_rename));
								// Move
								aList.add(getString(R.string.jv_relaunch_move));
								// Copy
								aList.add(getString(R.string.jv_relaunch_copy));
								// Paste
								int countList = 0;
								if (fullName.startsWith("FTP| ")) {
									if (fullName.equals("FTP| ")) {
										fullName = "/";
									} else {
										fullName = fullName.substring("FTP| ".length());
									}

									FTPFile[] FTPlist = connectorFTP.ftpFilesList(fullName);
									if (FTPlist != null) {
										countList = FTPlist.length;
									}
								} else if (fullName.startsWith("Dropbox| ")) {
									fullName = fullName.substring("Dropbox| ".length());
									// список файлов и папок
									DropboxAPI.Entry entries;
									entries = dropboxClient.metadata(fullName);// может быть ошибка при получении коллекции
									if (entries != null) {
										countList = entries.contents.size();
									}
								} else {
									File d = new File(fullName);
									String[] allEntries = d.list();
									if (allEntries == null) {
										countList = 0;
									} else {
										countList = allEntries.length;
									}

									if (selectRootNavigation) {
										aList.add(getString(R.string.jv_relaunch_permission));
									}
								}
								if (fileOp != 0) {
									aList.add(getString(R.string.jv_relaunch_paste));
								}
								// Delete
								if (countList > 0) {
									aList.add(getString(R.string.jv_relaunch_delete_non_emp_dir));
								} else {
									aList.add(getString(R.string.jv_relaunch_delete_emp_dir));
								}
								// Create folder
								aList.add(getString(R.string.jv_relaunch_create_folder));
							}
							// Add to Favorites
							aList.add(getString(R.string.jv_relaunch_arrange));
							// Dropbox
							if (!fullName.startsWith("Dropbox| ")) {
								aList.add(getString(R.string.jv_relaunch_Dropbox));
							}
							//System
							aList.add(getString(R.string.jv_relaunch_system));
                        break;
                    case 2:
						if (showFileOperation) {
							// Annotation
							aList.add(getString(R.string.jv_relaunch_bookinfo));
							if (prefs.getBoolean("useFileManagerFunctions", true)) {
								// Rename
								if (!showBookTitles) {
									aList.add(getString(R.string.jv_relaunch_rename));
								}
								// Move
								aList.add(getString(R.string.jv_relaunch_move));
								// Copy
								aList.add(getString(R.string.jv_relaunch_copy));
								// Paste
								if (fileOp != 0) {
									aList.add(getString(R.string.jv_relaunch_paste));
								}
								// Delete
								aList.add(getString(R.string.jv_relaunch_delete));
								// Create folder
								aList.add(getString(R.string.jv_relaunch_create_folder));
								// Rename file with tag
								if (!showBookTitles) {
									aList.add(getString(R.string.jv_relaunch_tags_rename));
								}
							}
							// Add favorites
							aList.add(getString(R.string.jv_relaunch_arrange));
							// Dropbox
							if (!fullName.startsWith("Dropbox| ")) {
								aList.add(getString(R.string.jv_relaunch_Dropbox));
							}
							//System
							aList.add(getString(R.string.jv_relaunch_system));
						}else{
							// Annotation
							aList.add(getString(R.string.jv_relaunch_bookinfo));
							// Add favorites
							aList.add(getString(R.string.jv_relaunch_arrange));
							// Mark & unmark && all
							if (app.history.containsKey(fullName)) {
								if (app.history.get(fullName) == app.READING) {
									aList.add(getString(R.string.jv_relaunch_mark));
								} else if (app.history.get(fullName) == app.FINISHED) {
									aList.add(getString(R.string.jv_relaunch_unmark));
								}
								aList.add(getString(R.string.jv_relaunch_unmarkall));
							} else {
								aList.add(getString(R.string.jv_relaunch_mark));
							}
							// Select
							if (prefs.getBoolean("useFileManagerFunctions", true)) {
								if (arrSelItem.contains(position)) {
									aList.add(getString(R.string.jv_relaunch_unselecte));
								} else {
									aList.add(getString(R.string.jv_relaunch_selecte));
								}
							}
							// Open with...
							if (prefs.getBoolean("openWith", true)) {
								aList.add(getString(R.string.jv_relaunch_openwith));
							}
							// Create Intent
							if (prefs.getBoolean("createIntent", false)) {
								aList.add(getString(R.string.jv_relaunch_createintent));
							}
							// Properties
							aList.add(getString(R.string.jv_relaunch_fileinfo));
							// Properties
							aList.add(getString(R.string.jv_relaunch_file_operations));
						}
                        break;
                    case 3:
						if (showFileOperation) {
							if (prefs.getBoolean("useFileManagerFunctions", true)) {
								// Rename
								if (!showBookTitles)
									aList.add(getString(R.string.jv_relaunch_rename));
								//Move
								aList.add(getString(R.string.jv_relaunch_move));
								// Copy
								aList.add(getString(R.string.jv_relaunch_copy));
								// Paste
								if (fileOp != 0)
									aList.add(getString(R.string.jv_relaunch_paste));
								// Delete
								aList.add(getString(R.string.jv_relaunch_delete));
								// Create folder
								aList.add(getString(R.string.jv_relaunch_create_folder));
							}
							// Add favorites
							aList.add(getString(R.string.jv_relaunch_arrange));
							// Dropbox
							if(!fullName.startsWith("Dropbox| ")){
								aList.add(getString(R.string.jv_relaunch_Dropbox));
							}
							//System
							aList.add(getString(R.string.jv_relaunch_system));
						}else{
							// Annotation
							aList.add(getString(R.string.jv_relaunch_bookinfo));
							// Add favorites
							if (!app.contains("favorites", dr, fn)) {
								if (!dr.startsWith("FTP| ")) {
									aList.add(getString(R.string.jv_relaunch_add));
								}
							}
							// Mark & unmark && all
							if (app.history.containsKey(fullName)) {
								if (app.history.get(fullName) == app.READING) {
									aList.add(getString(R.string.jv_relaunch_mark));
								} else if (app.history.get(fullName) == app.FINISHED) {
									aList.add(getString(R.string.jv_relaunch_unmark));
								}
								aList.add(getString(R.string.jv_relaunch_unmarkall));
							} else {
								aList.add(getString(R.string.jv_relaunch_mark));
							}
							// Select
							if (prefs.getBoolean("useFileManagerFunctions", true)) {
								if (arrSelItem.contains(position)) {
									aList.add(getString(R.string.jv_relaunch_unselecte));
								} else {
									aList.add(getString(R.string.jv_relaunch_selecte));
								}
							}
							// Open with...
							if (prefs.getBoolean("openWith", true)) {
								aList.add(getString(R.string.jv_relaunch_openwith));
							}
							// Create Intent
							if (prefs.getBoolean("createIntent", false)) {
								aList.add(getString(R.string.jv_relaunch_createintent));
							}
							// Properties
							aList.add(getString(R.string.jv_relaunch_fileinfo));
							// Properties
							aList.add(getString(R.string.jv_relaunch_file_operations));
						}
                        break;
                }
                aList.add(getString(R.string.app_cancel));
                pos = position;
                list = aList.toArray(new String[aList.size()]);

                finalFullName = fullName;
                finalDr = dr;
                finalFn = fn;
				FirstDialog(list);
            }

			private void FirstDialog(final String[] list){
				ListAdapter cmAdapter = new ArrayAdapter<String>(app, R.layout.cmenu_list_item, list);

				final AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setCancelable(false);
				builder.setAdapter(cmAdapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						String s = list[item];
						if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_arrange)) ||
								s.equalsIgnoreCase(getString(R.string.jv_relaunch_Dropbox)) ||
								s.equalsIgnoreCase(getString(R.string.jv_relaunch_system)) ||
								s.equalsIgnoreCase(getString(R.string.jv_relaunch_file_operations))){
							ArrayList<String> arrangeList = new ArrayList<String>(10);
							if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_arrange))){
								arrangeList = CreateArrangeMenu(menuType, finalFullName, finalDr, finalFn);
							}else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_Dropbox))){
								arrangeList.add(getString(R.string.jv_relaunch_add_Dropbox));
								arrangeList.add(getString(R.string.jv_relaunch_add_dir_Dropbox));
							}else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_system))){
								arrangeList = CreateSystemMenu(menuType, pos);
							}else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_file_operations))){
								arrangeList = FileOperationsMenu(finalFullName);
							}
							arrangeList.add(getString(R.string.jv_relaunch_return));

							final String[] zList = arrangeList.toArray(new String[arrangeList.size()]);

							AddedDialog1(zList, pos);
						}else{
							MenuSelect(s, pos);
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
				alert.show();
			}

			private void AddedDialog1(final String[] zList, final int pos){
				ListAdapter adapterSubMenu = new ArrayAdapter<String>(app, R.layout.cmenu_list_item, zList);
				final AlertDialog.Builder dialogArrange = new AlertDialog.Builder(context);

				dialogArrange.setAdapter(adapterSubMenu, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						String s = zList[item];
						MenuSelect(s, pos);
						dialog.dismiss();
					}
				});
				AlertDialog aAlert = dialogArrange.create();
				aAlert.requestWindowFeature(Window.FEATURE_NO_TITLE);
				aAlert.show();
			}
        }

        GlSimpleOnGestureListener gv_gl = new GlSimpleOnGestureListener(this);
        final GestureDetector gv_gd = new GestureDetector(gv_gl);
        gvList.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                gv_gd.onTouchEvent(event);
                return false;
            }
        });
    }
    private void drawingFifthPanel (LayoutInflater ltInflater, LinearLayout ll_container){
        // = Пятая панель с программами
        ltInflater.inflate(R.layout.ll_applist, ll_container, true);

        // last applications button
        final ImageButton lrua_button = ((ImageButton) findViewById(R.id.app_last));
        class LruaSimpleOnGestureListener extends
                SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                tapButton("appLastButtonST");
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                tapButton("appLastButtonDT");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (ReLaunch.this.hasWindowFocus())
                    tapButton("appLastButtonLT");
            }
        }

        LruaSimpleOnGestureListener lrua_gl = new LruaSimpleOnGestureListener();
        final GestureDetector lrua_gd = new GestureDetector(lrua_gl);
        lrua_button.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                lrua_gd.onTouchEvent(event);
                return false;
            }
        });
        // all applications button
        final ImageButton alla_button = ((ImageButton) findViewById(R.id.all_applications_btn));
        class AllaSimpleOnGestureListener extends
                SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                tapButton("appAllButtonST");
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                tapButton("appAllButtonDT");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (ReLaunch.this.hasWindowFocus())
                    tapButton("appAllButtonLT");
            }

        }

        AllaSimpleOnGestureListener alla_gl = new AllaSimpleOnGestureListener();
        final GestureDetector alla_gd = new GestureDetector(alla_gl);
        alla_button.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                alla_gd.onTouchEvent(event);
                return false;
            }
        });
        // applications favorites button
        final ImageButton fava_button = ((ImageButton) findViewById(R.id.app_favorites));
        class FavaSimpleOnGestureListener extends
                SimpleOnGestureListener {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                tapButton("appFavButtonST");
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                tapButton("appFavButtonDT");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (ReLaunch.this.hasWindowFocus())
                    tapButton("appFavButtonLT");
            }
        }
        FavaSimpleOnGestureListener fava_gl = new FavaSimpleOnGestureListener();
        final GestureDetector fava_gd = new GestureDetector(this, fava_gl);
        fava_button.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                fava_gd.onTouchEvent(event);
                return false;
            }
        });
        // Memory buttons (task manager activity)
        final LinearLayout mem_l = (LinearLayout) findViewById(R.id.mem_layout);
        if (mem_l != null) {
            class MemlSimpleOnGestureListener extends
                    SimpleOnGestureListener {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    tapButton("memButtonST");
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    tapButton("memButtonDT");
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    if (mem_l.hasWindowFocus()) {
                        tapButton("memButtonLT");
                    }
                }
            }

            MemlSimpleOnGestureListener meml_gl = new MemlSimpleOnGestureListener();
            final GestureDetector meml_gd = new GestureDetector(meml_gl);
            mem_l.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    meml_gd.onTouchEvent(event);
                    return false;
                }
            });
        }
        memLevel = (TextView) findViewById(R.id.mem_level);
        memTitle = (TextView) findViewById(R.id.mem_title);

        // Battery Layout
        final LinearLayout bat_l = (LinearLayout) findViewById(R.id.bat_layout);
        if (bat_l != null) {
            class BatlSimpleOnGestureListener extends
                    SimpleOnGestureListener {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    tapButton("batButtonST");
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    tapButton("batButtonDT");
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    if (mem_l != null && mem_l.hasWindowFocus()) {
                        tapButton("batButtonLT");
                    }
                }
            }

            BatlSimpleOnGestureListener batl_gl = new BatlSimpleOnGestureListener();
            final GestureDetector batl_gd = new GestureDetector(batl_gl);
            bat_l.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    batl_gd.onTouchEvent(event);
                    return false;
                }
            });
        }
        // Battery buttons
        battLevel = (TextView) findViewById(R.id.bat_level);
        battTitle = (TextView) findViewById(R.id.bat_title);
    }
    private void drawingSixPanel (LayoutInflater ltInflater, LinearLayout ll_container, String id){
        // своя панель
        // загрузили пустую панель
		View testPanel = ltInflater.inflate(R.layout.ll_panel_main, null, false);
		// вылавливаем лайоут для вставки кнопок
		LinearLayout ll_panel_main = (LinearLayout) testPanel.findViewById(R.id.linearLayout_panel_main);
        // контекст приложения
        Context context = getBaseContext();
        // настройка отступов и расположения кнопки
        LinearLayout.LayoutParams Lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Lp.leftMargin = 2;
        Lp.rightMargin = 2;
        Lp.weight = 1;

        final float scale = getResources().getDisplayMetrics().density;
        int padding_4dp = (int) (4 * scale + 0.5f);
        int padding_10dp = (int) (20 * scale + 0.5f);
		Lp.height = (int) (54 * scale + 0.5f);
        // создаем нужное колличество кнопок
        // надо обратиться в базу
        MyDBHelper dbHelper = new MyDBHelper(this, "PANELS");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c;
        String selection = "PANEL = ?";
        String[] selectionArgs = new String[] { id };
        c = db.query("PANELS", null, selection, selectionArgs, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                int runjobColumn = c.getColumnIndex("RUNJOB");
                int namejobColumColIndex = c.getColumnIndex("NAMEJOB");
				int runjobDCColumn = c.getColumnIndex("RUNJOB_DC");
				int namejobColumDCColIndex = c.getColumnIndex("NAMEJOB_DC");
				int runjobLCColumn = c.getColumnIndex("RUNJOB_LC");
				int namejobColumLCColIndex = c.getColumnIndex("NAMEJOB_LC");
                do {
                    if (!c.getString(namejobColumColIndex).equals("NOTHING")) {
                        // ------- параметры общие для всех кнопок -----
                        final ImageButton imageButton = new ImageButton(context);
                        imageButton.setBackgroundResource(R.drawable.main_button);

                        imageButton.setPadding(padding_10dp, padding_4dp, padding_10dp, padding_4dp);
                        imageButton.setLayoutParams(Lp);
                        // --------------------------------------------
						// --- получение данных по кнопке
						// одиночное нажатие
						String jobClick = c.getString(runjobColumn);
						String namejobClick = c.getString(namejobColumColIndex);
						// двойное нажатие
						String jobDClick = c.getString(runjobDCColumn);
						String namejobDClick = c.getString(namejobColumDCColIndex);
						// долгое нажатие
						String jobLClick = c.getString(runjobLCColumn);
						String namejobLClick = c.getString(namejobColumLCColIndex);
                        // персональные настройки каждой кнопки
                        // --------- загрузка иконки ------------------
                        if (jobClick.equals("RUN")) {
                            imageButton.setImageBitmap(BitmapIconForButton(c.getString(namejobColumColIndex)));
                        } else {
                            imageButton.setImageBitmap(app.JobIcon(jobClick));
                        }
                        //-----------------------------------------------------

                        // --------- действие на нажатие -------------------------
						// одиночное нажатие
						final String finalClick1 = jobClick;
						final String finalClick2 = namejobClick;
						// двойное нажатие
						final String finalDClick1 = jobDClick;
						final String finalDClick2 = namejobDClick;
						// долгое нажатие
						final String finalLClick1 = jobLClick;
						final String finalLClick2 = namejobLClick;
//----------------------------------------------------------------
						// новая обработка тапов по кнопке
						class ButtonSimpleOnGestureListener extends SimpleOnGestureListener {
							@Override
							public boolean onSingleTapConfirmed(MotionEvent e) {
								SetButtonClick(finalClick1, finalClick2, imageButton);
								return true;
							}

							@Override
							public boolean onDoubleTap(MotionEvent e) {
								SetButtonClick(finalDClick1, finalDClick2, imageButton);
								return true;
							}

							@Override
							public void onLongPress(MotionEvent e) {
								if (ReLaunch.this.hasWindowFocus())
									SetButtonClick(finalLClick1, finalLClick2, imageButton);
							}
						}
						ButtonSimpleOnGestureListener button_gl = new ButtonSimpleOnGestureListener();
						final GestureDetector button_gd = new GestureDetector(button_gl);
						imageButton.setOnTouchListener(new OnTouchListener() {
							public boolean onTouch(View v, MotionEvent event) {
								button_gd.onTouchEvent(event);
								return false;
							}
						});
                        // -------------------------------------------------------
						if ("BATTERY".equals(finalClick1)) {// показ расхода по приложениям
							battLevelSec = imageButton;
						}                        // добавляем кнопку на панель
						ll_panel_main.addView(imageButton);
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
        db.close();
        dbHelper.close();
		ll_container.addView(testPanel);
    }
    //========== FTP ================================
    private boolean downloadFTP(FTPConnector connectorFTP, String dirFile, String nameFile, String localPath){
        if (dirFile.startsWith("FTP| ")){
            dirFile = dirFile.substring("FTP| ".length());
        }
        if (localPath.equals("")) {
            File checkDir = new File(FTPTempDir);
            if (!checkDir.exists()) {
                if (!checkDir.mkdirs()) {
                    return false;
                }
            } else {
                app.fileRemove(new File(FTPTempDir));
                if (!checkDir.mkdirs()) {
                    return false;
                }
            }
        }
        return connectorFTP.download(dirFile, nameFile, localPath);
    }
    private boolean uploadFTP(FTPConnector connectorFTP, String dirFile, String nameFile, String FTPPath){
        return connectorFTP.upload(dirFile, nameFile, FTPPath);
    }

    // === запуск приложения
    private void RunApp(String nameApp){
        Intent i = app.getIntentByLabel(nameApp);
        if (i == null)
            // "Activity \"" + item + "\" not found!"
            app.showToast("\" " + nameApp + "\" " + getResources().getString(R.string.jv_allapp_not_found));
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
                app.showToast("\" " + nameApp + "\" " + getResources().getString(R.string.jv_allapp_not_found));
				ok = false;
            }
			if (ok) {
				app.addToList("app_last", nameApp, "X", false);
				saveLast();
			}
        }

    }
    //===== получение иконки по имени программы =====
    private Bitmap BitmapIconForButton(String nameApp) {
        Drawable d;
        Bitmap temp_icon = null;
        Intent componentSearchIntent = new Intent();
        componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        componentSearchIntent.setAction(Intent.ACTION_MAIN);
        PackageManager pm = getPackageManager();
        if (pm == null) {
            return null;
        }
        List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);
        String pname;
        String aname;
        String hname;
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                pname = ri.activityInfo.packageName;
                aname = ri.activityInfo.name;
                try {
                    if (ri.activityInfo.labelRes != 0) {
                        hname = (String) ri.activityInfo.loadLabel(pm);
                    } else {
                        hname = (String) ri.loadLabel(pm);
                    }
                    if (nameApp.equals(pname + "%" + aname + "%" + hname)) {
                        if (ri.activityInfo.icon != 0) {
                            d = ri.activityInfo.loadIcon(pm);
                        } else {
                            d = ri.loadIcon(pm);
                        }

                        if (d != null) {
                            temp_icon = scaleDrawable(d, firstLineIconSizePx);
                        } else {
                            temp_icon = scaleDrawableById(R.drawable.file_notok, firstLineIconSizePx);
                        }
                    }
                } catch (Exception e) {
                    temp_icon = scaleDrawableById(R.drawable.file_notok, firstLineIconSizePx);
                }
            }
        }
        return temp_icon;
    }
	//===== Проверка выхода из родительской папки =====
	private boolean CheckUpDir(){
		String tempDB = currentRoot;
		boolean enabled = !upDir.equals("");
		if(currentRoot.startsWith("Dropbox| ") && notLeaveStartDir){
			if("Dropbox| ".length() == upDir.length()){
				enabled = false;
			}
		}else if(currentRoot.startsWith("FTP| ") && notLeaveStartDir){
			tempDB = currentRoot.substring("FTP| ".length());
			enabled = (tempDB.startsWith(connectorFTP.rootPath) && tempDB.length() > connectorFTP.rootPath.length());
		}else if (enabled && !tempDB.equals("/") && notLeaveStartDir) {
			enabled = ((currentHomeDir.length() < currentRoot.length()) && currentRoot.startsWith(currentHomeDir));
		}
		return enabled;
	}
    //===== Переход в родительскую папку =====
    private void TapUpDir() {
		if (CheckUpDir()) {
			Integer p = -1;
			if (!positions.empty()){
				p = positions.pop();
			}
			if(N2DeviceInfo.EINK_ONYX || N2DeviceInfo.EINK_GMINI || N2DeviceInfo.EINK_BOEYE){
				p++;
			}
			drawDirectory(upDir, p);
		}
    }
    private void GetDateAndMewory() {
        // Date
        String d;
        Calendar c = Calendar.getInstance();
        if (dateUS)
            d = String.format("%02d:%02d%s %02d/%02d/%02d",
                    c.get(Calendar.HOUR), c.get(Calendar.MINUTE),
                    ((c.get(Calendar.AM_PM) == 0) ? "AM" : "PM"),
                    c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
                    (c.get(Calendar.YEAR) - 2000));
        else
            d = String.format("%02d:%02d %02d/%02d/%02d",
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
                    c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1,
                    (c.get(Calendar.YEAR) - 2000));
        if (memTitle != null) //  && useHome)
            memTitle.setText(d);

        // Memory
        MemoryInfo mi = new MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        if (memLevel != null) {// && useHome) {
            // "M free"
            memLevel.setText(mi.availMem / 1048576L
                    + getResources().getString(R.string.jv_relaunch_m_free));
            memLevel.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.ram), null);
        }
    }

    private void WiFiReceiver(){
        if (battTitle != null || wifiOp != null) {
            WifiManager wfm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wfm.isWifiEnabled()) {
                String nowConnected = wfm.getConnectionInfo().getSSID();
                if (battTitle != null) {
                    if (nowConnected != null && !nowConnected.equals("")) {
                        battTitle.setText(nowConnected);
                    } else {
                        battTitle.setText(getResources().getString(R.string.jv_relaunch_wifi_is_on));
                    }
                    battTitle.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(R.drawable.wifi_on), null,
                            null, null);
                }
                if (wifiOp != null){
                    wifiOp.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ci_wifi_on), firstLineIconSizePx, firstLineIconSizePx,true));
                }
            } else {
                // "WiFi is off"
                if (battTitle != null) {
                    battTitle.setText(getResources().getString(R.string.jv_relaunch_wifi_is_off));
                    battTitle.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(R.drawable.wifi_off), null,
                            null, null);
                }
                if (wifiOp != null){
                    wifiOp.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ci_wifi_off), firstLineIconSizePx, firstLineIconSizePx, true));
                }
            }
        }
        GetDateAndMewory();
    }

	private ArrayList<String> dbSCREEN(){
		ArrayList<String> listPanels = new ArrayList<String>();
		MyDBHelper dbHelper = new MyDBHelper(getApplicationContext(), "SCREEN");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if(db == null){
			return null;
		}
		// делаем запрос данных из таблицы , получаем Cursor на таблицу с панелями экрана
		Cursor c = db.query("SCREEN", null, null, null, null, null, null);
		// ставим позицию курсора на первую строку выборки
		// если в выборке нет строк, вернется false
		if (c.moveToFirst()) {
			// определяем номера столбцов по имени в выборке
			int panelID = c.getColumnIndex("ID_PANEL");
			do {
				listPanels.add(c.getString(panelID));
				// переход на следующую строку
				// а если следующей нет (текущая - последняя), то false - выходим из цикла
			} while (c.moveToNext());
		}
		c.close();
		db.close();
		dbHelper.close();
		return listPanels;
	}

    private void SetPermission(String filename){
        final File file = new File(filename);
        String filePerm;
        String fileOwn;
        String fileAttr = null;
        try {
            Runtime rt = Runtime.getRuntime();

            String[] args;
            if (file.isDirectory()){
                args = new String[]{"ls", "-l", file.getParent(), "|grep", filename};
            }else {
                args = new String[]{"ls", "-l", filename};
            }
            Process proc = rt.exec(args);
            //String str = filename.replace(" ", "\\ ");
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = br.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            br.close();
            proc.waitFor();
            fileAttr = output.toString();
        } catch (Throwable t) {
            //emply
        }
        if(fileAttr != null && fileAttr.length()>0) {
            filePerm = fileAttr.substring(0, 10);
            fileAttr = fileAttr.replaceAll(" +", " ");
            int iPerm = fileAttr.indexOf(" ");
            int iOwner = fileAttr.indexOf(" ", iPerm + 1);
            int iGroup = fileAttr.indexOf(" ", iOwner + 1);
            fileOwn = "Owner: " + fileAttr.substring(iPerm + 1, iOwner) + "  Group: " + fileAttr.substring(iOwner + 1, iGroup);
        }else{
            return;
        }
        Permissions permissions = new Permissions(filePerm);

        final Dialog dialog = new Dialog(this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.permission);
        // имя файла
        TextView textView = (TextView) dialog.findViewById(R.id.tVFileName);
        textView.setText(file.getName());
        // владелец
        textView = (TextView) dialog.findViewById(R.id.tvOwner);
        textView.setText(fileOwn);
        // для владельца
        CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.cBur);
        checkBox.setChecked(permissions.ur);
        checkBox = (CheckBox) dialog.findViewById(R.id.cBuw);
        checkBox.setChecked(permissions.uw);
        checkBox = (CheckBox) dialog.findViewById(R.id.cBux);
        checkBox.setChecked(permissions.ux);
        // для группы
        checkBox = (CheckBox) dialog.findViewById(R.id.cBgr);
        checkBox.setChecked(permissions.gr);
        checkBox = (CheckBox) dialog.findViewById(R.id.cBgw);
        checkBox.setChecked(permissions.gw);
        checkBox = (CheckBox) dialog.findViewById(R.id.cBgx);
        checkBox.setChecked(permissions.gx);
        // для остальных
        checkBox = (CheckBox) dialog.findViewById(R.id.cBor);
        checkBox.setChecked(permissions.or);
        checkBox = (CheckBox) dialog.findViewById(R.id.cBow);
        checkBox.setChecked(permissions.ow);
        checkBox = (CheckBox) dialog.findViewById(R.id.cBox);
        checkBox.setChecked(permissions.ox);
        // рекурсивно для папок
        textView = (TextView) dialog.findViewById(R.id.textView50);
        checkBox = (CheckBox) dialog.findViewById(R.id.cBdr);
        if (file.isDirectory()){
            textView.setVisibility(View.VISIBLE);
            checkBox.setVisibility(View.VISIBLE);
        }else{
            textView.setVisibility(View.INVISIBLE);
            checkBox.setVisibility(View.INVISIBLE);
        }

        Button btn = (Button) dialog.findViewById(R.id.btnCancel);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn = (Button) dialog.findViewById(R.id.btnOk);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Permissions permissionsEnd = new Permissions(
                        ((CheckBox) dialog.findViewById(R.id.cBur)).isChecked(),
                        ((CheckBox) dialog.findViewById(R.id.cBuw)).isChecked(),
                        ((CheckBox) dialog.findViewById(R.id.cBux)).isChecked(),
                        ((CheckBox) dialog.findViewById(R.id.cBgr)).isChecked(),
                        ((CheckBox) dialog.findViewById(R.id.cBgw)).isChecked(),
                        ((CheckBox) dialog.findViewById(R.id.cBgx)).isChecked(),
                        ((CheckBox) dialog.findViewById(R.id.cBor)).isChecked(),
                        ((CheckBox) dialog.findViewById(R.id.cBow)).isChecked(),
                        ((CheckBox) dialog.findViewById(R.id.cBox)).isChecked()
                );
                RootCommands.applyPermissions(file, permissionsEnd);
                dialog.dismiss();
            }
        });


        dialog.show();


    }

	private void SetButtonClick(String final1, String final2, ImageButton imageButton){
		//;
		if ("RUN".equals(final1)) {// внешняя программа
			RunApp(final2);
		} else if ("FAVDOCN".equals(final1)) {// страница фаворитов документов
			ListActions la = new ListActions(app, ReLaunch.this);
			la.runItem("favorites", Integer.parseInt(final2) - 1);
		} else if ("LRUN".equals(final1)) {// страница запущенных ДОКУМЕНТОВ
			ListActions la = new ListActions(app, ReLaunch.this);
			la.runItem("lastOpened", Integer.parseInt(final2) - 1);
		} else if ("HOMEN".equals(final1)) {// страница домашних папок
			openHome();
		} else if ("HOMEMENU".equals(final1)) {// всплывающее меню домашних папок
			menuHome();
		} else if ("HOMESCREEN".equals(final1)) {// экран домашних папок
			screenHome();
		} else if ("LRUMENU".equals(final1)) {// всплывающее меню запущенных документов
			ListActions la = new ListActions(app, ReLaunch.this);
			la.showMenu("lastOpened");
		} else if ("LRUSCREEN".equals(final1)) {// экран запущенных документов
			menuLastopened();
		} else if ("FAVDOCMENU".equals(final1)) {// всплывающее меню фаворитов документов
			ListActions la = new ListActions(app, ReLaunch.this);
			la.showMenu("favorites");
		} else if ("FAVDOCSCREEN".equals(final1)) {// экран фаворитов документов
			menuFavorites();
		} else if ("ADVANCED".equals(final1)) {// расширенные настройки
			Intent i = new Intent(ReLaunch.this, Advanced.class);
			startActivity(i);
		} else if ("SETTINGS".equals(final1)) {// настройки
			menuSettings();
		} else if ("APPMANAGER".equals(final1)) {// все приложения
			Intent intent = new Intent(ReLaunch.this, TaskManager.class);
			startActivity(intent);
		} else if ("BATTERY".equals(final1)) {// показ расхода по приложениям
			battLevelSec = imageButton;
			Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
			startActivity(intent);
		} else if ("FAVAPP".equals(final1)) {// всплывающее меню фаворитов приложений
			Intent intent = new Intent(ReLaunch.this, AllApplications.class);
			intent.putExtra("list", "app_favorites");
			intent.putExtra("title", getResources().getString(R.string.jv_relaunch_fav_a));
			startActivity(intent);
		} else if ("ALLAPP".equals(final1)) {//
			Intent intent = new Intent(ReLaunch.this, AllApplications.class);
			intent.putExtra("list", "app_all");
			// "All applications"
			intent.putExtra("title", getResources().getString(R.string.jv_relaunch_all_a));
			startActivity(intent);
		} else if ("LASTAPP".equals(final1)) {
			Intent intent = new Intent(ReLaunch.this, AllApplications.class);
			intent.putExtra("list", "app_last");
			// "Last recently used applications"
			intent.putExtra("title", getResources().getString(R.string.jv_relaunch_lru_a));
			startActivity(intent);
		} else if ("SEARCH".equals(final1)) {
			menuSearch();
		} else if ("LOCK".equals(final1)) {
			actionLock();
		} else if ("POWEROFF".equals(final1)) {
			actionPowerOff();
		} else if ("REBOOT".equals(final1)) {
			actionReboot();
		} else if ("SWITCHWIFI".equals(final1)) {
			wifiOp = imageButton;
			actionSwitchWiFi();
		} else if ("DROPBOX".equals(final1)) {
			screenDropbox();
		} else if ("OPDS".equals(final1)) {
			screenOPDS();
		} else if ("FTP".equals(final1)) {
			screenFTP();
		} else if ("SYSSETTINGS".equals(final1)) {
			startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
		} else if ("UPDIR".equals(final1)) {
			TapUpDir();
		} else if ("UPSCROOL".equals(final1)) {
			app.TapUpScrool(gvList, itemsArray.size());
		} else if ("DOWNSCROOL".equals(final1)) {
			app.TapDownScrool(gvList, itemsArray.size());
		}
	}

	private void DownScroll( int target) {
		int first = gvList.getFirstVisiblePosition();
		final int ftarget = target;
		gvList.clearFocus();
		gvList.post(new Runnable() {
			public void run() {
				gvList.setSelection(ftarget);
			}
		});
		final int ffirst = first;
		gvList.postDelayed(new Runnable() {
			public void run() {
				int nfirst = gvList.getFirstVisiblePosition();
				if (nfirst == ffirst) {
					DownScroll(ftarget);
				}
			}
		}, 150);
	}
}
