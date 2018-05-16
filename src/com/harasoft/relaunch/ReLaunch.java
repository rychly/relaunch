package com.harasoft.relaunch;

import android.app.*;
import android.app.ActivityManager.MemoryInfo;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.*;
import com.harasoft.relaunch.Adapter.AdapterRL;
import com.harasoft.relaunch.Adapter.ViewItem;
import com.harasoft.relaunch.Preferences.PrefsActivity;
import com.harasoft.relaunch.Preferences.TypesActivity;
import com.harasoft.relaunch.Support.*;
import com.harasoft.relaunch.LocalFile.LocalFile;
import com.harasoft.relaunch.Utils.*;

import java.text.DateFormat;
import java.util.*;

public class ReLaunch extends Activity {

	final static String TAG = "ReLaunch";

	final static int TYPES_ACT = 1;
	final static int DIR_ACT = 2;
	private final static int HOME_DIR_ACT = 3;
	private final static int CURRENT_DIR_ACT = 4;
	private final static int FAVORITE_ACT = 5;
	private final static int SEARCH_ACT = 6;
	private final static int CNTXT_MENU_DELETE_F = 1;
	private final static int CNTXT_MENU_DELETE_D_EMPTY = 2;
	private final static int CNTXT_MENU_DELETE_D_NON_EMPTY = 3;
	private final static int CNTXT_MENU_ADD = 4;
	private final static int CNTXT_MENU_CANCEL = 5;
	private final static int CNTXT_MENU_MARK_READING = 6;
	private final static int CNTXT_MENU_MARK_FINISHED = 7;
	private final static int CNTXT_MENU_MARK_FORGET = 8;
	private final static int CNTXT_MENU_INTENT = 9;
	private final static int CNTXT_MENU_OPENWITH = 10;
	private final static int CNTXT_MENU_COPY_FILE = 11;
	private final static int CNTXT_MENU_MOVE_FILE = 12;
	private final static int CNTXT_MENU_PASTE = 13;
	private final static int CNTXT_MENU_RENAME = 14;
	private final static int CNTXT_MENU_CREATE_DIR = 15;
	private final static int CNTXT_MENU_SWITCH_TITLES = 16;
	private final static int CNTXT_MENU_TAGS_RENAME = 17;
	private final static int CNTXT_MENU_ADD_STARTDIR = 18;
	private final static int CNTXT_MENU_SHOW_BOOKINFO = 19;
	private final static int CNTXT_MENU_FILE_INFO = 20;
	private final static int CNTXT_MENU_SET_STARTDIR = 21;
	private final static int CNTXT_MENU_SETTINGS = 22;
	private final static int CNTXT_MENU_SELECTE = 23;
	private final static int CNTXT_MENU_FILEUNZIP = 24;
	private final static int CNTXT_MENU_PERMISSION = 25;

	private final static int SORT_FILES_ASC = 0;
	private final static int SORT_FILES_DESC = 1;
	private final static int SORT_DATES_ASC = 2;
	private final static int SORT_DATES_DESC = 3;
	private final static int SORT_SIZES_ASC = 4;
	private final static int SORT_SIZES_DESC = 5;
	private final static int SORT_TITLES_ASC = 6;
	private final static int SORT_TITLES_DESC = 7;

	private List<ViewItem> all_view_items;
	private ArrayList<String> select_files = new ArrayList<>();

	private AdapterRL adapter;
	private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefsEditor;
	private ReLaunchApp app;

    public static boolean disableScrollJump;
	// Bottom info panel
	private boolean mountReceiverRegistered = false;
	private boolean powerReceiverRegistered = false;
	private boolean wifiReceiverRegistered = false;
	private TextView memTitle;
	private TextView memLevel;
	private TextView battTitle = null;
	private TextView battLevel;
	private static ImageButton battLevelSec = null;
	private static ImageButton wifiOp = null;

	private static int fileOp;
	private static String sortType = "sname";
	private static boolean sortOrder = true;

    //private static Locale locale;
	private static boolean fileExtendedData;
	private static String fileExtendedDataFormat;

    // ====== drawDirectory==================================================================
	private DateFormat dateFormat;
	private DateFormat timeFormat;
	private int firstLineIconSizePx;
	private boolean hideKnownExts;
    private boolean hideKnownDirs = false;
	private boolean showBookTitles;
	private static ArrayList<String> exts;
	private boolean showFullDirPath;
	private boolean showButtonParenFolder;
	private String bookTitleFormat;
	private Button tv_title;
	private GridView gvList;
    //===================================================
	private static boolean blockExitLauncher = true;
	private static boolean showFileOperation;
	// ========= Home Dir ===========================
	private String currentFolder;
	private Stack<Integer> positions = new Stack<>();
	private static int currentPosition = 0;
	private String currentHomeDir = null;
	private int resource_location = ResourceLocation.LOCAL;
	// ====== setUpButton==================================================================
	private boolean notLeaveStartDir;
	private Button upButton;
	//=================================================================================
	private UtilBooks utilBooks;
	private UtilColumns utilColumns;
	private UtilApp utilApp;
	private UtilHistory utilHistory;
	private UtilHomeDirs utilHomeDirs;
	private LocalFile localFile;
	private UtilIcons utilIcons;


	private void actionSwitchWiFi() {
		WifiManager wifiManager;
		wifiManager = (WifiManager) (getApplicationContext()).getSystemService(Context.WIFI_SERVICE);
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
		PowerFunctions.actionLock(ReLaunch.this);
	}

	private void actionPowerOff() {
		PowerFunctions.actionPowerOff(ReLaunch.this);
	}

	private void actionReboot() {
		PowerFunctions.actionReboot(ReLaunch.this);
	}

	public static String createReadersString(List<HashMap<String, String>> rdrs) {
		StringBuilder rc = new StringBuilder();
		for (HashMap<String, String> r : rdrs) {
			for (String key : r.keySet()) {
				if (rc.length() > 0) {
					rc.append("|");
				}
				rc.append(key);
				rc.append(":");
				rc.append(r.get(key));
			}
		}
		return rc.toString();
	}

	private void pushCurrentPos(AdapterView<?> parent) {
		Integer p1 = parent.getFirstVisiblePosition();
		positions.push(p1);
	}

	private void setUpButton(String curDir) {
		if (upButton != null) {
			if (CheckUpDir(curDir)) {
				upButton.setEnabled(true);
				upButton.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("LEVELUPON")), null, null, null);
			}else{
				upButton.setEnabled(false);
				upButton.setCompoundDrawablesWithIntrinsicBounds( new BitmapDrawable(getResources(), utilIcons.getIcon("LEVELUPOFF")), null, null, null);
			}
		}
	}

    private void drawDirectory(String root, Integer startPosition){
		// организуем проверку на выход из домашней папки
		if(notLeaveStartDir && !root.startsWith(currentHomeDir) ){
			root = currentHomeDir;
		}
		// запоминаем текущую папку
		currentFolder = root;
		// текущая позиция (-1) или задана при вызове
		currentPosition = (startPosition == -1) ? currentPosition : startPosition;

		all_view_items = localFile.getListItems(root);
		List<ViewItem> folders = new ArrayList<>();
		List<ViewItem> files = new ArrayList<>();

		// для нормальной работы необходимо путь привести путь к виду "/........./"
		String current_path;
		if (root.endsWith("/")) {
			current_path = root;
		}else {
			current_path = root + "/";
		}
		// установка кнопки с отображение текущей папки
		setTitleButton(root, all_view_items.size());
		// разбивка массива по типу item
		for (ViewItem item: all_view_items) {
			item.setFile_path(current_path);
			if (item.getFile_type() == TypeResource.DIR) {
				folders.add(item);
			}else {
				files.add(item);
			}
		}
		// === сортировка ====
		// сортируем папки
		SortViewItems sortViewItems = new SortViewItems(folders, sortType, sortOrder);
		sortViewItems.sortViewItem();
		// сортируем файлы
		sortViewItems = new SortViewItems(files, sortType, sortOrder);
		sortViewItems.sortViewItem();
		// ===================
		// очищаем основной массив для заполнения
		all_view_items.clear();
		// добавляем в список кнопку выхода в родительский каталог
		if (showButtonParenFolder) {
			ViewItem view_item = new ViewItem();
			view_item.setFirst_string("..");
			view_item.setFile_type(TypeResource.DIR);
			view_item.setFile_status_read(BookState.NEW);
			view_item.setSelected(false);
			if (firstLineIconSizePx != 0) {
				if (CheckUpDir(root)) {
					view_item.setIcon_name("parent_ok");
				} else {
					view_item.setIcon_name("parent_off");
				}
			}
			all_view_items.add(view_item);
		}
		// пербираем массив с папками
		for (ViewItem folder : folders) {
			folder.setSelected(false);
			folder.setIcon_name("dir_ok");
			folder.setFile_status_read(BookState.NEW);
			folder.setFirst_string(folder.getFile_name());
		}
		// перебираем массив с файлами

		for (ViewItem file : files) {
			if (file == null) {
				continue;
			}
			String file_name = file.getFile_name();


			// --- формируем первую строку
			// если показываем имена книг
			if (showBookTitles) { // показывать имена книг
				if (file.getBook_name_string() != null && file.getBook_name_string().length() != 0){
					file.setFirst_string(file.getBook_name_string());
				}
			}
			// стоит флаг скрывать расширение
			if (hideKnownExts && file.getFirst_string() != null) {  // скрываем расширение
				for (String ext : exts) { // прогоняем все расширения через имя файла
					if (file_name.endsWith(ext)) {
						file.setFirst_string(file_name.substring(0, file_name.length() - ext.length()));// удаляем если нашли совпадение
					}
				}
			}
			// если в первой строке нет названия книги или имени файла без расширения
			if (file.getFirst_string() == null) {
				file.setFirst_string(file_name);
			}
			//-------------------------
			// здесь определяем состояние прочтения файла
			file.setFile_status_read(utilHistory.getState(resource_location, current_path + file_name));

			// вывод дополнительных данных во вторую строку
			if (fileExtendedData) {
				long date = file.getFile_time();
				long size = file.getFile_size();
				String second_string = fileExtendedDataFormat.toLowerCase();
				String temp;
				if (second_string.contains("%d")) {
					second_string = second_string.replace("%d", dateFormat.format(date));
				}
				if (second_string.contains("%t")) {
					second_string = second_string.replace("%t", timeFormat.format(date));
				}
				if (second_string.contains("%sb")) {
					second_string = second_string.replace("%sb", String.valueOf(size));
				}
				if (second_string.contains("%skb")) {
					temp = String.valueOf((int) (size / 102.4));
					if (temp.length() > 1) {
						temp = temp.substring(0, temp.length() - 1) + "," + temp.substring(temp.length() - 1);
					} else if (temp.length() == 1) {
						temp = "0," + temp;
					}
					second_string = second_string.replace("%skb", temp);
				}
				if (second_string.contains("%smb")) {
					temp = String.valueOf((int) (size / 104857.6));
					if (temp.length() > 1) {
						temp = temp.substring(0, temp.length() - 1) + "," + temp.substring(temp.length() - 1);
					} else if (temp.length() == 1) {
						temp = "0," + temp;
					}
					second_string = second_string.replace("%smb", temp);
				}
				file.setSecond_string(second_string);
			}

			// имя иконки
			if (firstLineIconSizePx != 0) {
				file.setIcon_name(utilIcons.getNameIcon(file, "file_navigator"));
			}
		}
		// в начало списка закидываем папки
		all_view_items.addAll(folders);
		// потом добавляем файлы
		all_view_items.addAll(files);

		// скрываем или показываем кнопку переход в родительскую папку
		setUpButton(root);
		// устанавливаем число колонок для отображения
		int columns = getDirectoryColumns(all_view_items, root);
		gvList.setNumColumns(columns);
		gvList.invalidate();
		if (adapter != null) {
			adapter.setColumn(columns, gvList.getWidth());
		}else {
			// создание адаптера
			adapter = new AdapterRL(this, all_view_items, columns, gvList.getWidth() / columns);
			gvList.setAdapter(adapter);
		}

		// устанавливаем стартовый элемент
		if (startPosition != -1) {
			gvList.setSelection(currentPosition);
			if(N2DeviceInfo.EINK_ONYX || N2DeviceInfo.EINK_GMINI || N2DeviceInfo.EINK_BOEYE){
				DownScroll( currentPosition);
			}
		}
		reDraw();
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
					if (plugged == BatteryManager.BATTERY_PLUGGED_AC) { // зарядка от блока питания
						add_text = " AC";
						if(level < 25){
							batDraw = R.drawable.bat1_outlet;
						}else if (level < 50){
							batDraw = R.drawable.bat2_outlet;
						}else if (level < 75){
							batDraw = R.drawable.bat3_outlet;
						}else{
							batDraw = R.drawable.bat4_outlet;
						}
					} else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) { // зарядка от USB порта
						add_text = " USB";
						if(level < 25){
							batDraw = R.drawable.bat1_usb;
						}else if (level < 50){
							batDraw = R.drawable.bat2_usb;
						}else if (level < 75){
							batDraw = R.drawable.bat3_usb;
						}else{
							batDraw = R.drawable.bat4_usb;
						}
					}else {
						if(level < 25){
							batDraw = R.drawable.bat1;
						}else if (level < 50){
							batDraw = R.drawable.bat2;
						}else if (level < 75){
							batDraw = R.drawable.bat3;
						}else{
							batDraw = R.drawable.bat4;
						}
					}
					battLevel.setText(level + "%" + add_text);
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
		// TODO перенести инициализацию настроек в onStart для их применения без перезагрузки после изменений
        // ------ загружаем настройки программы в переменные -----------------------
        initPrefsVariable();
        //=================================================================================
		// Настройка экрана приложения
		// Во весь экран
		app.fullScreen = prefs.getBoolean("fullScreen", true);
		// Скрыть заголовок программы
		app.hideTitle = prefs.getBoolean("hideTitle", true);
		// Применить настройки к экрану
		app.setOptionsWindowActivity(this);
		//app.setFullScreenIfNecessary(this);
		// =================================================================================
		// Режим сортировки
		setSortMode(prefs.getInt("sortMode", 0));
		// =================================================================================
        // Main layout
        setContentView(R.layout.layout_relaunch);
		// =================================================================================
		// формирование панелей
		drawingPanels ();
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

		//ScreenOrientation.set(this, prefs);
	}
	@Override
	protected void onStart() {
		super.onStart();
        N2EpdController.n2MainActivity = this;
        // установка начальной папки
        String start_dir = "";
		//=====================================================================================
		// загрузка сохранённой папки если сохранение последней разрешено
        if (prefs.getBoolean("saveDir", true)){
			start_dir = prefs.getString("lastdir", "");
			resource_location = prefs.getInt("saveResource", ResourceLocation.LOCAL);
        }
		// загружаем текущую папку
		if (start_dir.equals("") && currentFolder != null) {
			start_dir = currentFolder;
			resource_location = ResourceLocation.LOCAL;
		}
        // если текущая пустая то грузим в неё домашнюю
        if(start_dir.equals("") && currentHomeDir != null && currentHomeDir.length() > 0){
			start_dir = currentHomeDir;
			resource_location = utilHomeDirs.getCurrentResourceLocation();
        }
        // если предыдущие попытки получить папку не увенчались успехом
		if (start_dir.equals("")) {
			if (N2DeviceInfo.EINK_ONYX) {
				start_dir = "/mnt/storage";
			} else {
				start_dir = "/sdcard";
			}
			resource_location = ResourceLocation.LOCAL;
		}
		utilHistory.reloadHistoryArray();
        drawDirectory(start_dir, -1);
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
			screenSearch();
			return true;
		case R.id.mime_types:
			screenTypes();
			return true;
		case R.id.about:
			menuAbout();
			return true;
		case R.id.setting:
			screenSettings();
			return true;
		case R.id.lastopened:
			screenLastopened();
			return true;
		case R.id.favorites:
			screenFavorites();
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
				if (hideKnownExts) {
					List<HashMap<String, String>> rc = app.getReaders(); // получаем массив карт
					Set<String> tkeys = new HashSet<>();
					for (HashMap<String, String> aRc : rc) {
						Object[] keys = aRc.keySet().toArray();
						for (Object key : keys) {
							tkeys.add(key.toString());
						}
					}
					// получаем массив расширений
					exts = new ArrayList<>(tkeys);
					// сортируем массив расширений
					Collections.sort(exts, new ExtsComparator());
				}
				break;
			case HOME_DIR_ACT:
				int id = Integer.parseInt(data.getStringExtra("id"));
				utilHomeDirs.setHomeDirByID(id);
				this.currentHomeDir = utilHomeDirs.getCurrentHomeDir();
				positions.clear();// очищаем стек с позициями по папкам
				if (prefs.getBoolean("saveDir", true)) {
					prefsEditor.putString("lastdir", this.currentHomeDir);
					prefsEditor.putInt("saveResource", utilHomeDirs.getCurrentResourceLocation());
					prefsEditor.commit();
				}else {
					currentFolder = this.currentHomeDir;
					resource_location = utilHomeDirs.getCurrentResourceLocation();
				}
				//resource_location = utilHomeDirs.getCurrentResourceLocation();
				//positions.clear();// очищаем стек с позициями по папкам
				//drawDirectory(this.currentHomeDir, 0);
				break;
			case CURRENT_DIR_ACT:
                positions.clear();// очищаем стек с позициями по папкам
				if (prefs.getBoolean("saveDir", true)) {
					prefsEditor.putString("lastdir", data.getStringExtra("newDir"));
					prefsEditor.putInt("saveResource", data.getIntExtra("saveResource", 0));
					prefsEditor.commit();
				}else {
					currentFolder = data.getStringExtra("newDir");
					resource_location = data.getIntExtra("saveResource", 0);
				}
				//drawDirectory(data.getStringExtra("newDir"), 0);
				break;
			case FAVORITE_ACT:
                positions.clear();// очищаем стек с позициями по папкам
				if (prefs.getBoolean("saveDir", true)) {
					prefsEditor.putString("lastdir", data.getStringExtra("newDir"));
					prefsEditor.putInt("saveResource", data.getIntExtra("saveResource", 0));
					prefsEditor.commit();
				}else {
					currentFolder = data.getStringExtra("newDir");
					resource_location = data.getIntExtra("saveResource", 0);
				}
				//drawDirectory(data.getStringExtra("newDir"), 0);
				break;
			case SEARCH_ACT:
				positions.clear();// очищаем стек с позициями по папкам
				if (prefs.getBoolean("saveDir", true)) {
					prefsEditor.putString("lastdir", data.getStringExtra("newDir"));
					prefsEditor.putInt("saveResource", data.getIntExtra("saveResource", 0));
					prefsEditor.commit();
				}else {
					currentFolder = data.getStringExtra("newDir");
					resource_location = data.getIntExtra("saveResource", 0);
				}
				//drawDirectory(data.getStringExtra("newDir"), 0);
				break;
			default:
			//return;
		}
	}
    @Override
    protected void onResume() {
		app.generalOnResume(TAG);
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
        EinkScreen.setEinkController(prefs);
        super.onResume();
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

		if (prefs.getBoolean("saveDir", true)) {
			prefsEditor.putString("lastdir", currentFolder);
			prefsEditor.putInt("saveResource", resource_location);
			prefsEditor.commit();
		}

        super.onPause();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
		app.setOptionsWindowActivity(this);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME)
            return true;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
			boolean isRoot = !CheckUpDir(currentFolder);
			if (prefs.getBoolean("useBackButton", true)) {
                if (!isRoot) {
					TapUpDir(currentFolder);
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
                int total = all_view_items.size();
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
                int total = all_view_items.size();
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

	private void onContextMenuSelected(int itemId, int mPos) {
		if (itemId == CNTXT_MENU_CANCEL){
			return;
        }
        ViewItem view_item;
		int tpos = 0;

		if (mPos == -1) {
			view_item = new ViewItem();
			view_item.setFile_path(currentFolder);
			view_item.setFile_name("");
		} else {
			tpos = mPos;
			view_item = all_view_items.get(tpos);
		}
		final int pos = tpos;
		final String file_name = view_item.getFile_name();
		final String file_path = view_item.getFile_path();
		final String full_file_path;
		if (file_path.endsWith("/")) {
			full_file_path = file_path + file_name;
		}else {
			full_file_path = file_path + "/" + file_name;
		}

		switch (itemId) {
		case CNTXT_MENU_SET_STARTDIR:
			setCurrentHomeDir(full_file_path, resource_location);
			break;
		case CNTXT_MENU_ADD_STARTDIR:
			utilHomeDirs.addHomeDir(full_file_path, resource_location);
			break;
		case CNTXT_MENU_ADD:
            UtilFavorites utilFavorites = new UtilFavorites(getBaseContext());
            utilFavorites.addFav(file_path, file_name, view_item.getFile_type(), resource_location);
            utilFavorites.addFav(file_path, file_name, view_item.getFile_type(), resource_location);
			break;
		case CNTXT_MENU_MARK_READING:
			utilHistory.addToHistory(resource_location, full_file_path, BookState.READING);
            reDraw();
			break;
		case CNTXT_MENU_MARK_FINISHED:
			utilHistory.addToHistory(resource_location, full_file_path, BookState.FINISHED);
            reDraw();
			break;
		case CNTXT_MENU_MARK_FORGET:
			utilHistory.delFromHistory(resource_location, full_file_path);
            reDraw();
			break;
		case CNTXT_MENU_OPENWITH: {
			final ArrayList<String> listApp2 = utilApp.getAppList();
			CharSequence[] happlications = listApp2.toArray(new CharSequence[listApp2.size()]);
			AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
			// "Select application"
			builder.setTitle(getResources().getString(R.string.jv_relaunch_select_application));
			builder.setSingleChoiceItems(happlications, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
							RunIntent runIntent = new RunIntent(getApplicationContext());
							runIntent.start(runIntent.launchReader(listApp2.get(i), full_file_path));
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
			String re[] = file_name.split("\\.");
			List<String> ilist = new ArrayList<>();
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
							Intent in = new Intent();
							in.setAction(Intent.ACTION_VIEW);
							in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
							in.setDataAndType(Uri.parse("file://" + full_file_path),(String) intents[i]);
							dialog.dismiss();
							try {
								startActivity(in);
							} catch (ActivityNotFoundException e) {
								AlertDialog.Builder builder1 = new AlertDialog.Builder(ReLaunch.this);
								// "Activity not found"
								builder1.setTitle(getResources()
										.getString(R.string.jv_relaunch_activity_not_found_title));
								// "Activity for file \"" + full_file_path +
								// "\" with type \"" + intents[i] +
								// "\" not found"
								builder1.setMessage(getResources()
										.getString(
												R.string.jv_relaunch_activity_not_found_text1)
										+ " \""
										+ full_file_path
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
											Intent in = new Intent();
											in.setAction(Intent.ACTION_VIEW);
											in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
											in.setDataAndType(Uri.parse("file://" + full_file_path), String.valueOf(input.getText())
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
												// full_file_path + "\" with type \""
												// + input.getText() +
												// "\" not found"
												builder2.setMessage(getResources()
														.getString(
																R.string.jv_relaunch_activity_not_found_text1)
														+ " \""
														+ full_file_path
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
		case CNTXT_MENU_DELETE_D_EMPTY:
		case CNTXT_MENU_DELETE_D_NON_EMPTY:
			removeItem(all_view_items, pos);
			break;
		case CNTXT_MENU_COPY_FILE:
			select_files.clear(); // очищаем список
			for (ViewItem item: all_view_items) {
				if (item.isSelected()) {
					select_files.add(item.getFile_path() + item.getFile_name());
				}
			}
			if (select_files.size() == 0) {
				select_files.add(full_file_path);
            }
            fileOp = CNTXT_MENU_COPY_FILE;
			break;
		case CNTXT_MENU_MOVE_FILE:
			select_files.clear(); // очищаем список
			for (ViewItem item: all_view_items) {
				if (item.isSelected()) {
					select_files.add(item.getFile_path() + item.getFile_name());
				}
			}
			if (select_files.size() == 0) {
				select_files.add(full_file_path);
			}
            fileOp = CNTXT_MENU_MOVE_FILE;
			break;

		case CNTXT_MENU_PASTE:
            String dst;
            String dst_path = file_path;
			if (!file_path.endsWith("/")) {
				dst_path = file_path + "/";
			}

			for (String src : select_files) {

				String file_op_name = src.substring(src.lastIndexOf("/") + 1);

				dst = dst_path + file_op_name;

				boolean retCode = false;

				// если файлы находяться локально
				if (fileOp == CNTXT_MENU_COPY_FILE) {
					retCode = localFile.copyAll(src, dst, false);
				} else if (fileOp == CNTXT_MENU_MOVE_FILE) {
					retCode = localFile.moveFile(src, dst);
				}
				if (!retCode) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(getResources().getString(R.string.jv_relaunch_error_title));
					builder.setMessage(getResources().getString(R.string.jv_relaunch_paste_fail_text) + " " + file_op_name);
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
			select_files.clear();
            drawDirectory(file_path, -1);
			break;

		case CNTXT_MENU_TAGS_RENAME: {
			final Context mThis = this;
			String newName = utilBooks.getEbookName(file_path, file_name, bookTitleFormat);
			newName = newName.replaceAll("[\n\r]", ". ");
			if (file_name.endsWith("fb2"))
				newName = newName.concat(".fb2");
			else if (file_name.endsWith("fb2.zip"))
				newName = newName.concat(".fb2.zip");
			else if (file_name.endsWith("epub"))
				newName = newName.concat(".epub");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			input.setText(newName);
			builder.setView(input);
			builder.setTitle(getResources().getString(
					R.string.jv_relaunch_rename_title));
			// "OK"
			builder.setPositiveButton(
					getResources().getString(R.string.app_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
							String newName = String.valueOf(input.getText()).trim();
							String newFullName;
							if (file_path.endsWith("/")) {
								newFullName = file_path + newName;
							}else {
								newFullName = file_path + "/" + newName;
							}
							if (localFile.moveFile(full_file_path, newFullName)) {
								drawDirectory(file_path, -1);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
								builder.setTitle(getResources().getString(R.string.jv_relaunch_error_title));
								builder.setMessage(getResources().getString(R.string.jv_relaunch_rename_fail_text)
										                                    + " " + file_name);
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
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			input.setText(file_name);
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
							String newFullName;
							if (file_path.endsWith("/")) {
								newFullName = file_path + newName;
							}else {
								newFullName = file_path + "/" + newName;
							}
                            if (localFile.moveFile(full_file_path, newFullName)) {
								drawDirectory(file_path, -1);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
								builder.setTitle(getResources().getString(R.string.jv_relaunch_error_title));
								builder.setMessage(getResources().getString(
										R.string.jv_relaunch_rename_fail_text)+ " " + file_name);
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
							String newName = String.valueOf(input.getText()).trim();
							if (newName.equalsIgnoreCase("")) {
								return;
							}
							String newFullName;
							if (file_path.endsWith("/")) {
								newFullName = file_path + newName;
							}else {
								newFullName = file_path + "/" + newName;
							}
                            boolean f;
                            f = localFile.createDir(newFullName);
                            if (f) {
                                drawDirectory(file_path, -1);
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
            if (showBookTitles) {
				utilBooks = new UtilBooks(getApplicationContext());
			}else {
            	utilBooks = null;
			}
			drawDirectory(file_path, -1);
			break;

		case CNTXT_MENU_SHOW_BOOKINFO:
			showBookInfo(full_file_path);
			break;

		case CNTXT_MENU_FILE_INFO:
		    showFileInfo(full_file_path);
			break;
        case CNTXT_MENU_SETTINGS:
			screenSettings();
            break;
        case CNTXT_MENU_SELECTE:
			if (all_view_items.get(mPos).isSelected()) {
				all_view_items.get(mPos).setSelected(false);
			}else {
				all_view_items.get(mPos).setSelected(true);
			}
            reDraw();
            break;
        case CNTXT_MENU_FILEUNZIP:
            ZipUtil zipUtil = new ZipUtil();
            if (!zipUtil.unzip(full_file_path)){
                app.showToast(getResources().getString(R.string.jv_relaunch_fileunzip_error));
            }
            drawDirectory(file_path, -1);
            break;
		}
	}

	private void screenSearch() {
		Intent intent = new Intent(ReLaunch.this, SearchActivity.class);
		intent.putExtra("current_root", currentFolder);
		intent.putExtra("resource_location", resource_location);
		startActivityForResult(intent, SEARCH_ACT);
	}

	private void screenTypes() {
		Intent intent = new Intent(ReLaunch.this, TypesActivity.class);
		startActivityForResult(intent, TYPES_ACT);
	}

	private void screenSettings() {
		Intent intent = new Intent(ReLaunch.this, PrefsActivity.class);
		startActivity(intent);
	}

	private void screenLastopened() {
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "lastOpened");
		startActivity(intent);
	}

	private void screenFavorites() {
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "favorites");
		startActivityForResult(intent, FAVORITE_ACT);
	}

	private void openHome() {
		drawDirectory(currentHomeDir, 0);
	}

	private void screenHome() {
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "homeList");
		startActivityForResult(intent, HOME_DIR_ACT);
	}

    private void screenDropbox() {
        //Intent intent = new Intent(ReLaunch.this, DropBoxActivity.class);
        //startActivity(intent);
    }

    private void screenOPDS() {
        //
        Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
        intent.putExtra("list", "opdslist");
        intent.putExtra("rereadOnStart", true);
        startActivity(intent);
    }

	private void menuAbout() {
		app.About(this);
	}

	private Integer getDirectoryColumns(List<ViewItem> all_view_items, String dir) {
		// определяем число колонок для отображения
		Integer colsNum = utilColumns.getNumberColumns(dir);
		// override auto (not working fine in adnroid) судя по всему работает дерьмого автоколлумнилование
		if (colsNum == 0) {
			colsNum = utilColumns.getAutoColsNum(all_view_items);
		}
		if (colsNum == 0) {
			colsNum = 1;
		}
		return colsNum;
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
		if (sortMode < 0 || sortMode > orderList.length - 1) {
			sortMode = 0;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
		builder.setTitle(R.string.jv_relaunch_sort_header);
		builder.setSingleChoiceItems(orderList, sortMode,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int i) {
                        prefsEditor.putInt("sortMode", i);
                        prefsEditor.commit();
						setSortMode(i);
						dialog.dismiss();
						drawDirectory(currentFolder, -1);
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
		switch (i) {
			case SORT_FILES_ASC:
				sortType = "name";
				sortOrder = true;
				break;
			case SORT_FILES_DESC:
				sortType = "name";
				sortOrder = false;
				break;
			case SORT_DATES_ASC:
				sortType = "date";
				sortOrder = true;
				break;
			case SORT_DATES_DESC:
				sortType = "date";
				sortOrder = false;
				break;
			case SORT_SIZES_ASC:
				sortType = "size";
				sortOrder = true;
				break;
			case SORT_SIZES_DESC:
				sortType = "size";
				sortOrder = false;
				break;
			case SORT_TITLES_ASC:
				sortType = "title";
				sortOrder = true;
				break;
			case SORT_TITLES_DESC:
				sortType = "title";
				sortOrder = false;
				break;

		}
	}

	private void showBookInfo(final String file) {
		Intent intent = new Intent(ReLaunch.this, BookInfoActivity.class);
		intent.putExtra("full_file_name", file);
		startActivity(intent);
	}
	private void showFileInfo(String full_file_path) {
		HashMap<String,String> file_info = localFile.getFileInfo(full_file_path);

        final Dialog dialog = new Dialog(this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_relaunch_fileinfo);

        LinearLayout llSize = (LinearLayout) dialog.findViewById(R.id.llSize);

		if (file_info.get("dir").equals("true"))
			llSize.setVisibility(View.GONE);

		TextView tv = (TextView) dialog.findViewById(R.id.tvName);
		tv.setText(file_info.get("name"));
		tv = (TextView) dialog.findViewById(R.id.tvSize);
		tv.setText(file_info.get("size") + " bytes");
		tv = (TextView) dialog.findViewById(R.id.tvTime);
		tv.setText(file_info.get("date"));

		tv = (TextView) dialog.findViewById(R.id.tvPerm);
		tv.setText(file_info.get("permit"));
		tv = (TextView) dialog.findViewById(R.id.tvOwner);
		tv.setText(file_info.get("own"));

		Button btn = (Button) dialog.findViewById(R.id.btnOk);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
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

    private void reDraw(){
        // режим обновления экрана
        EinkScreen.PrepareController(null, false);
        // говорим что данные изменились и нужно перерисовать данные
		adapter.updateReceiptsList(all_view_items);
    }
    // ========= load settings ======================
    private void initPrefsVariable() {
		app = (ReLaunchApp) getApplicationContext();
		if (app == null) {
			finish();
		}
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefsEditor = prefs.edit();
		//EinkScreen.setEinkController(prefs);
        // ====== FLSimpleAdapter==================================================================
        firstLineIconSizePx = Integer.parseInt(prefs.getString("firstLineIconSizePx", "48"));

        hideKnownDirs = prefs.getBoolean("hideKnownDirs", false);
        hideKnownExts = prefs.getBoolean("hideKnownExts", false);
        showBookTitles = prefs.getBoolean("showBookTitles", false);
        if (showBookTitles) {
			utilBooks = new UtilBooks(getApplicationContext());
		}
		utilColumns = new UtilColumns(getBaseContext());
        utilApp = new UtilApp(getBaseContext(), getPackageManager());
		utilHistory = new UtilHistory(getBaseContext());
		utilHomeDirs = new UtilHomeDirs(getBaseContext());
		localFile = new LocalFile(getBaseContext());
		utilIcons = new UtilIcons(getBaseContext());

        disableScrollJump = prefs.getBoolean("disableScrollJump", true);
        //=================================================================================
        // ====== setUpButton==================================================================
        notLeaveStartDir = prefs.getBoolean("notLeaveStartDir", true);
		// ====== drawDirectory==================================================================
        showFullDirPath = prefs.getBoolean("showFullDirPath", false);
        showButtonParenFolder = prefs.getBoolean("showButtonParenFolder", false);
        bookTitleFormat = prefs.getString("bookTitleFormat", "%t[\n%a][. %s][-%n]");
		showFileOperation = prefs.getBoolean("showFileOperation", true);
		dateFormat = java.text.DateFormat.getDateInstance(DateFormat.SHORT);
		timeFormat = android.text.format.DateFormat.getTimeFormat(this);
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
        //=======================================================================
        fileExtendedData = prefs.getBoolean("fileExtendedFormat", false);
        if(fileExtendedData){
            fileExtendedDataFormat = prefs.getString("fileExtendedDataFormat", "");
        }

		resource_location = utilHomeDirs.getCurrentResourceLocation();
		currentHomeDir = utilHomeDirs.getCurrentHomeDir();

        app.askIfAmbiguous = prefs.getBoolean("askAmbig", false);
        // Recreate readers list
        app.setReaders();
        // скрываем известные расширения
        if (hideKnownExts) {
			List<HashMap<String, String>> rc = app.getReaders(); // получаем массив карт
            Set<String> tkeys = new HashSet<>();
            for (HashMap<String, String> aRc : rc) {
                Object[] keys = aRc.keySet().toArray();
                for (Object key : keys) {
                    tkeys.add(key.toString());
                }
            }
            // получаем массив расширений
            exts = new ArrayList<>(tkeys);
            // сортируем массив расширений
            Collections.sort(exts, new ExtsComparator());
        }
    }
    // ================ =============================
    // небольшой модуль для удалени файла или папки
	private boolean deleteFile(String full_file_name, int pos) {
		if(localFile.itemRemove(full_file_name)) {
			utilHistory.delFromHistory(resource_location, full_file_name);
			all_view_items.remove(pos);
			return true;
		}
		return false;
	}
	private void removeItem(final List<ViewItem> view_items, final int position) {
		ViewItem view_item;
		StringBuilder list_files = new StringBuilder();
		final List<Integer> num_pos = new ArrayList<>();
		int count = 0; // счетчик выделенных файлов
		// формируем список для удаления
		for (ViewItem item: view_items) {
			if (item.isSelected()) {
				list_files.append("\n");
				list_files.append(item.getFile_name());
				num_pos.add(view_items.indexOf(item));
				count++;
			}
		}

		if (count == 0 || count == 1) { // удаление файла в позиции
			final int remove_pos;
			if (count == 0) {
				remove_pos = position;
			}else {
				remove_pos = num_pos.get(0);
			}
			view_item = view_items.get(remove_pos);
			final String full_file_name = view_item.getFile_path() + view_item.getFile_name();
			//TODO проверить на ветвления при удалении
			if (prefs.getBoolean("confirmDirDelete", true) || prefs.getBoolean("confirmFileDelete", true) || prefs.getBoolean("confirmNonEmptyDirDelete", true)) {

				String title_dialog;
				String text1_dialog;
				String text2_dialog;
				if (view_item.getFile_type() == TypeResource.DIR) {
					int countList = localFile.getItemsInFolder(full_file_name);
					if (countList > 0) {
						title_dialog = getResources().getString(R.string.jv_relaunch_del_ne_dir_title);
						text1_dialog = getResources().getString(R.string.jv_relaunch_del_ne_dir_text1);
						text2_dialog = getResources().getString(R.string.jv_relaunch_del_ne_dir_text2);
					} else {
						title_dialog = getResources().getString(R.string.jv_relaunch_del_em_dir_title);
						text1_dialog = getResources().getString(R.string.jv_relaunch_del_em_dir_text1);
						text2_dialog = getResources().getString(R.string.jv_relaunch_del_em_dir_text2);
					}
				}else {
					title_dialog = getResources().getString(R.string.jv_relaunch_del_file_title);
					text1_dialog = getResources().getString(R.string.jv_relaunch_del_file_text1);
					text2_dialog = getResources().getString(R.string.jv_relaunch_del_file_text2);
				}


				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// "Delete empty directory warning"
				builder.setTitle(title_dialog);
				// "Are you sure to delete empty directory \"" + fname + "\" ?"
				builder.setMessage(text1_dialog
						+ " \""
						+ view_item.getFile_name()
						+ "\" "
						+ text2_dialog);
				// "Yes"
				builder.setPositiveButton(getResources().getString(R.string.app_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int whichButton) {
								dialog.dismiss();
								if (deleteFile(full_file_name, position)) {
									reDraw();
								}
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
				if (deleteFile(full_file_name, position)) {
					reDraw();
				}
			}
		}else { // удаление множества выделенных файлов
			if (prefs.getBoolean("confirmFileDelete", true)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// "Delete file warning"
				builder.setTitle(getResources().getString(R.string.jv_relaunch_del_sel_file_title));
				// "Are you sure to delete file \"" + fname + "\" ?"
				builder.setMessage(getResources().getString(
						R.string.jv_relaunch_del_sel_file_text1)
						+ list_files.toString()
						+ getResources().getString(R.string.jv_relaunch_del_sel_file_text2));
				// "Yes"
				builder.setPositiveButton(getResources().getString(R.string.app_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
								String full_file_name;
								ViewItem view_item;
								int pos;
								for(int h = -1, k = num_pos.size() - 1; h < k; k--) {
									pos = num_pos.get(k);
									view_item = view_items.get(pos);
									full_file_name = view_item.getFile_path() + view_item.getFile_name();
									deleteFile(full_file_name, pos);
								}
								reDraw();
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
				String full_file_name;
				int pos;
				for(int h = -1, k = num_pos.size() - 1; h < k; k--) {
					pos = num_pos.get(k);
					view_item = all_view_items.get(pos);
					full_file_name = view_item.getFile_path() + view_item.getFile_name();
					deleteFile(full_file_name, pos);
				}
				reDraw();
			}
		}
	}
    //========== Панели =============================
	// панель файлов
    private void drawingFourthPanel (LayoutInflater ltInflater, LinearLayout ll_container){
        // = Четвертая панель со списком файла
        ltInflater.inflate(R.layout.layout_relaunch_panelfiles, ll_container, true);
        //=============================================================================
        // сама панель
        gvList = (GridView) findViewById(R.id.gl_list);
        gvList.setHorizontalSpacing(0);
        if (prefs.getBoolean("customScroll", app.customScrollDef)) {
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
        }else {
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
			private String finalFullName;
			private String finalDr;
			private String finalFn;
			private int menuType;
			private int pos;
			private String[] list;
			private boolean use_file_manager_function;
			private boolean select_file_tap_icon;

            private GlSimpleOnGestureListener(Context context) {
                super();
                this.context = context;
                this.use_file_manager_function = prefs.getBoolean("useFileManagerFunctions", true);
                this.select_file_tap_icon = prefs.getBoolean("selectFileTapIcon", true);
            }
            private boolean selectTapIcon(MotionEvent e, int position) {
            	// если тап по пустому месту
            	if (position == -1) {
            		return false;
				}
				// если тап по выходу в родительскую папку
				if (showButtonParenFolder && (position) == 0){
					return false;
				}
				// определение координат тапа
				View v = gvList.getChildAt(position);
				if(v == null){
					return false;
				}
				int location[] = new int[2];
				v.getLocationOnScreen(location);
				int viewX = location[0];
				int viewY = location[1];
				float x = e.getRawX();
				float y = e.getRawY();
				// текущий размер иконки
				int icon_size_pix = utilIcons.getIconSize();
				// определяем попадание в иконку
				int ostX = (int) (x - viewX);
				int ostY = (int) (y - viewY);
				if(ostX < icon_size_pix && ostY < icon_size_pix){
					if (all_view_items.get(position).isSelected()) {
						all_view_items.get(position).setSelected(false);
					}else {
						all_view_items.get(position).setSelected(true);
					}
					reDraw();
					return true;
				}
				return false;
			}
			private int findViewByXY(MotionEvent e) {
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
                ArrayList<String> arrangeList = new ArrayList<>(10);
				UtilFavorites utilFavorites;
                switch (menuType) {
                    case 1:
						utilFavorites = new UtilFavorites(getBaseContext());
						if (! utilFavorites.isFav(dr, fn))  {
							arrangeList.add(getString(R.string.jv_relaunch_add));
						}
                        // Set as Start folders & Add to Start folders
						if (notLeaveStartDir){
							if (utilHomeDirs.isHomeDir(resource_location, fullName)) {
								arrangeList.add(getString(R.string.jv_relaunch_set_startdir));

							}else {
								arrangeList.add(getString(R.string.jv_relaunch_add_startdir));
							}
						}

                        break;
                    case 2:
						utilFavorites = new UtilFavorites(getBaseContext());
						if (! utilFavorites.isFav(dr, fn))  {
							arrangeList.add(getString(R.string.jv_relaunch_add));
						}
                        // Mark as read & unread
						int book_state = utilHistory.getState(resource_location, fullName);
						switch (book_state) {
							case 0: // app.NONE
								arrangeList.add(getString(R.string.jv_relaunch_mark));
								break;
							case 1: // app.READING
								arrangeList.add(getString(R.string.jv_relaunch_mark));
								arrangeList.add(getString(R.string.jv_relaunch_unmarkall));
								break;
							case 2: // app.FINISHED
								arrangeList.add(getString(R.string.jv_relaunch_unmark));
								arrangeList.add(getString(R.string.jv_relaunch_unmarkall));
								break;
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
						utilFavorites = new UtilFavorites(getBaseContext());
						if (! utilFavorites.isFav(dr, fn))  {
                            arrangeList.add(getString(R.string.jv_relaunch_add));
                        }
                        // Mark & unmark && all
						int book_state2 = utilHistory.getState(resource_location, fullName);
						switch (book_state2) {
							case 0: // app.NONE
								arrangeList.add(getString(R.string.jv_relaunch_mark));
								break;
							case 1: // app.READING
								arrangeList.add(getString(R.string.jv_relaunch_mark));
								arrangeList.add(getString(R.string.jv_relaunch_unmarkall));
								break;
							case 2: // app.FINISHED
								arrangeList.add(getString(R.string.jv_relaunch_unmark));
								arrangeList.add(getString(R.string.jv_relaunch_unmarkall));
								break;
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
                ArrayList<String> arrangeList = new ArrayList<>(10);
                switch (menuType) {
                    case 1:
                        // Properties
                        arrangeList.add(getString(R.string.jv_relaunch_fileinfo));
                        // Open settings windows
                        arrangeList.add(getString(R.string.app_settings_settings));
                        // Select
                        if (use_file_manager_function) {
                        	if (all_view_items.get(position).isSelected()) {
                            //if (arrSelItem.contains(position)) {
                                arrangeList.add(getString(R.string.jv_relaunch_unselecte));
                            } else {
                                arrangeList.add(getString(R.string.jv_relaunch_selecte));
                            }
                        }
                        break;
                    case 2:
                        // Properties
                        arrangeList.add(getString(R.string.jv_relaunch_fileinfo));
                        // Open settings windows
                        arrangeList.add(getString(R.string.app_settings_settings));
                        // Create Intent
                        if (prefs.getBoolean("createIntent", false)) {
                            arrangeList.add(getString(R.string.jv_relaunch_createintent));
                        }
                        // Select
						if (all_view_items.get(position).isSelected()) {
                        //if (arrSelItem.contains(position)) {
                            arrangeList.add(getString(R.string.jv_relaunch_unselecte));
                        } else {
                            arrangeList.add(getString(R.string.jv_relaunch_selecte));
                        }
                        break;
                    case 3:
                        // Properties
                        arrangeList.add(getString(R.string.jv_relaunch_fileinfo));
                        //Open settings windows
                        arrangeList.add(getString(R.string.app_settings_settings));
                        // Create Intent
                        if (prefs.getBoolean("createIntent", false)) {
                            arrangeList.add(getString(R.string.jv_relaunch_createintent));
                        }
                        // Select
						if (all_view_items.get(position).isSelected()) {
                        //if (arrSelItem.contains(position)) {
                            arrangeList.add(getString(R.string.jv_relaunch_unselecte));
                        } else {
                            arrangeList.add(getString(R.string.jv_relaunch_selecte));
                        }

                        break;

                }
                return arrangeList;
            }
			private ArrayList<String> FileOperationsMenu(){
				ArrayList<String> arrangeList = new ArrayList<>(10);

				if (use_file_manager_function) {
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
				return arrangeList;
			}

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                int position = findViewByXY(e);
                if (position == -1){
                    return true;
                }
				// флаг разрешения выделения по тапу на иконке
                if (select_file_tap_icon) {
                	if (selectTapIcon(e, position)) {
						return true;
					}
				}
                ViewItem view_item = all_view_items.get(position);
                String full_file_name = view_item.getFile_path() + view_item.getFile_name();
                if (view_item.getFile_type() == TypeResource.DIR) {
                	if (view_item.getFirst_string().equals("..")) {
						TapUpDir(currentFolder);
					}else {
						pushCurrentPos(gvList);
						drawDirectory(full_file_name, 0);
					}
				}else {
					RunIntent run_intent =new RunIntent(getApplicationContext());
					if (view_item.getFile_name().endsWith(".apk")){
						run_intent.installPackage(ReLaunch.this, full_file_name);
					}else {
						prefsEditor.putInt("posInFolder", gvList.getFirstVisiblePosition());
						prefsEditor.commit();
						String name_reader = app.readerName(full_file_name);
						if (name_reader.equals("Nope")){
							// внутренний просмотровщик
							run_intent.runInternalView(ReLaunch.this, full_file_name);
						}else {
							// Launch reader
							run_intent.launchReader(full_file_name);
						}

					}
                    drawDirectory(currentFolder, -1);
				}
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                int position = findViewByXY(e);
                if (position != -1) {
					ViewItem view_item = all_view_items.get(position);
					String full_file_name = view_item.getFile_path() + view_item.getFile_name();
                    if (full_file_name.endsWith("fb2") || full_file_name.endsWith("fb2.zip") || full_file_name.endsWith("epub")) {
                        prefsEditor.putInt("posInFolder", gvList.getFirstVisiblePosition());
                        prefsEditor.commit();
                        showBookInfo(full_file_name);
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
                ViewItem view_item;
                String file_name = null;
                String file_path = null;
                String file_full_name = "";

                ArrayList<String> aList = new ArrayList<>(10);
                if (position == -1)
                    menuType = 0;
                else {
                	view_item = all_view_items.get(position);
                	file_name = view_item.getFile_name();
                	file_path = view_item.getFile_path();
                	file_full_name = file_path +file_name;
                    if (view_item.getFile_type() == TypeResource.DIR) {
                        menuType = 1;
                    }else if (file_name.endsWith("fb2") || file_name.endsWith("fb2.zip") || file_name.endsWith("epub")) {
						menuType = 2;
					}else {
						menuType = 3;
					}
                }

                switch (menuType) {
                    case 0:
                        if (use_file_manager_function) {
                            aList.add(getString(R.string.jv_relaunch_create_folder));
                            if (fileOp != 0) {
                                aList.add(getString(R.string.jv_relaunch_paste));
                            }
                        }
                        // Open settings windows
                        aList.add(getString(R.string.app_settings_settings));
                        break;
                    case 1:
							if (use_file_manager_function) {
								// Rename
								aList.add(getString(R.string.jv_relaunch_rename));
								// Move
								aList.add(getString(R.string.jv_relaunch_move));
								// Copy
								aList.add(getString(R.string.jv_relaunch_copy));
								// Paste
								if (fileOp != 0) {
									aList.add(getString(R.string.jv_relaunch_paste));
								}
								// Delete
								int countList = localFile.getItemsInFolder(file_full_name);
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
							//System
							aList.add(getString(R.string.jv_relaunch_system));
                        break;
                    case 2:
						// Annotation
						aList.add(getString(R.string.jv_relaunch_bookinfo));
						// Add favorites
						UtilFavorites utilFavorites = new UtilFavorites(getBaseContext());
						if (! utilFavorites.isFav(file_path, file_name))  {
							aList.add(getString(R.string.jv_relaunch_add));
						}
						if (showFileOperation) {
							if (use_file_manager_function) {
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
							//System
							aList.add(getString(R.string.jv_relaunch_system));
						}else{
							// Mark & unmark && all
							int book_state = utilHistory.getState(resource_location, file_full_name);
							switch (book_state) {
								case 0: // app.NONE
									aList.add(getString(R.string.jv_relaunch_mark));
									break;
								case 1: // app.READING
									aList.add(getString(R.string.jv_relaunch_mark));
									aList.add(getString(R.string.jv_relaunch_unmarkall));
									break;
								case 2: // app.FINISHED
									aList.add(getString(R.string.jv_relaunch_unmark));
									aList.add(getString(R.string.jv_relaunch_unmarkall));
									break;
							}
							// Select
							if (use_file_manager_function) {
								if (all_view_items.get(position).isSelected()) {
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
							if (use_file_manager_function) {
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
							//System
							aList.add(getString(R.string.jv_relaunch_system));
						}else{
							// Annotation
							aList.add(getString(R.string.jv_relaunch_bookinfo));
							// Add favorites
							UtilFavorites utilFavorites1 = new UtilFavorites(getBaseContext());
							if (! utilFavorites1.isFav(file_path, file_name))  {
								aList.add(getString(R.string.jv_relaunch_add));
							}
							// Mark & unmark && all
							int book_state = utilHistory.getState(resource_location, file_full_name);
							switch (book_state) {
								case 0: // app.NONE
									aList.add(getString(R.string.jv_relaunch_mark));
									break;
								case 1: // app.READING
									aList.add(getString(R.string.jv_relaunch_mark));
									aList.add(getString(R.string.jv_relaunch_unmarkall));
									break;
								case 2: // app.FINISHED
									aList.add(getString(R.string.jv_relaunch_unmark));
									aList.add(getString(R.string.jv_relaunch_unmarkall));
									break;
							}
							// Select
							if (use_file_manager_function) {
								if (all_view_items.get(position).isSelected()) {
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

                finalFullName = file_full_name;
                finalDr = file_path;
                finalFn = file_name;
				FirstDialog(list);
            }

			private void FirstDialog(final String[] list){
				ListAdapter cmAdapter = new ArrayAdapter<>(app, R.layout.item_relaunch_contexmenu, list);

				final AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setCancelable(false);
				builder.setAdapter(cmAdapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						String s = list[item];
						if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_arrange)) ||
								s.equalsIgnoreCase(getString(R.string.jv_relaunch_system)) ||
								s.equalsIgnoreCase(getString(R.string.jv_relaunch_file_operations))){
							ArrayList<String> arrangeList = new ArrayList<>(10);
							if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_arrange))){
								arrangeList = CreateArrangeMenu(menuType, finalFullName, finalDr, finalFn);
							}else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_system))){
								arrangeList = CreateSystemMenu(menuType, pos);
							}else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_file_operations))){
								arrangeList = FileOperationsMenu();
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
				ListAdapter adapterSubMenu = new ArrayAdapter<>(app, R.layout.item_relaunch_contexmenu, zList);
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
    // все остальные панели
	private void drawingPanels (){
		LayoutInflater ltInflater = getLayoutInflater();
		LinearLayout ll_container = (LinearLayout) findViewById(R.id.conteiner);
		UtilPanels utilPanels = new UtilPanels(getBaseContext());
		// список панелей
		ArrayList<HashMap<String,String>> listPanels = utilPanels.getListPanelsOfScreen();

		for (HashMap<String,String> panel: listPanels) {
			if (utilPanels.getTypePanel(panel) == 1) { // если тип панели список файлов
				drawingFourthPanel(ltInflater, ll_container);
			}else if (utilPanels.getTypePanel(panel) == 0){
				// получаем список кнопок на панели
				ArrayList<HashMap<String,String>> listButtons = utilPanels.getListButtonsOfPanel(utilPanels.getNumberPanel(panel));

				// загрузили пустую панель
				View newPanel = ltInflater.inflate(R.layout.layout_relaunch_panel, ll_container, false);
				// вылавливаем лайоут для вставки кнопок
				LinearLayout ll_panel_main = (LinearLayout) newPanel.findViewById(R.id.linearLayout_panel_main);
				// контекст приложения
				Context context = getBaseContext();
				// настройка отступов и расположения кнопки
				final float scale = getResources().getDisplayMetrics().density;
				LinearLayout.LayoutParams Lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
				Lp.height = (int) (54 * scale + 0.5f); // высота кнопок
				Lp.leftMargin = 2; // отступ кнопки слева
				Lp.rightMargin = 2;// отступ кнопки справа
				int minWidthButton = (int) (110 * scale);		// минимальная ширина кнопки - для красоты

				for (HashMap<String,String> button: listButtons) {
					// --- получение данных по кнопке
					// одиночное нажатие
					final String jobClick = button.get("RUN_ONE_CLICK");
					final String namejobClick = button.get("NAME_ONE_CLICK");
					// двойное нажатие
					final String jobDClick = button.get("RUN_DOUBLE_CLICK");
					final String namejobDClick = button.get("NAME_DOUBLE_CLICK");
					// долгое нажатие
					final String jobLClick = button.get("RUN_LONG_CLICK");
					final String namejobLClick = button.get("NAME_LONG_CLICK");
					// идентификатор кнопки. говорит какой тип кнопки
					//0 - OneClick, 1 - DoubleClick, 2 - LongClick, 3 - TextButton, 4 - Icon from the file, 5 - GridView
					int identButton = Integer.parseInt(button.get("IDENT_ICON"));
					//===========================================================
					// обработка тапов по кнопке
					class ButtonSimpleOnGestureListener extends SimpleOnGestureListener {
						@Override
						public boolean onSingleTapConfirmed(MotionEvent e) {
							SetButtonClick(jobClick, namejobClick);
							return true;
						}

						@Override
						public boolean onDoubleTap(MotionEvent e) {
							SetButtonClick(jobDClick, namejobDClick);
							return true;
						}

						@Override
						public void onLongPress(MotionEvent e) {
							if (ReLaunch.this.hasWindowFocus())
								SetButtonClick(jobLClick, namejobLClick);
						}
					}
					ButtonSimpleOnGestureListener button_gl = new ButtonSimpleOnGestureListener();
					final GestureDetector button_gd = new GestureDetector(button_gl);
					//===========================================================
					// создаём кнопку и определяем иконку на нее

					if (identButton == 0 || identButton == 1 || identButton == 2) {
						final ImageButton imageButton = new ImageButton(context);
						imageButton.setBackgroundResource(R.drawable.main_button);

						imageButton.setMinimumWidth(minWidthButton);
						Lp.weight = 1;
						imageButton.setLayoutParams(Lp);
						// --------- загрузка иконки ------------------
						switch (identButton) {
							case (0):
								if (jobClick.equals("RUN")) {
									imageButton.setImageBitmap(utilIcons.BitmapIconForButton(namejobClick));
								} else {
									imageButton.setImageBitmap(utilIcons.getIcon(jobClick));
									if ("BATTERY".equals(jobClick)) {
										battLevelSec = imageButton;
									}
									if ("SWITCHWIFI".equals(jobClick)) {
										wifiOp = imageButton;
									}
								}

								break;
							case (1):
								if (jobDClick.equals("RUN")) {
									imageButton.setImageBitmap(utilIcons.BitmapIconForButton(namejobClick));
								} else {
									imageButton.setImageBitmap(utilIcons.getIcon(jobClick));
									if ("BATTERY".equals(jobDClick)) {
										battLevelSec = imageButton;
									}
									if ("SWITCHWIFI".equals(jobDClick)) {
										wifiOp = imageButton;
									}
								}
								break;
							case (2):
								if (jobLClick.equals("RUN")) {
									imageButton.setImageBitmap(utilIcons.BitmapIconForButton(namejobClick));
								} else {
									imageButton.setImageBitmap(utilIcons.getIcon(jobClick));
									if ("BATTERY".equals(jobLClick)) {
										battLevelSec = imageButton;
									}
									if ("SWITCHWIFI".equals(jobDClick)) {
										wifiOp = imageButton;
									}
								}
								break;
						}
						// навешиваем действия на кнопку
						imageButton.setOnTouchListener(new OnTouchListener() {
							public boolean onTouch(View v, MotionEvent event) {
								button_gd.onTouchEvent(event);
								return false;
							}
						});
						// добавляем кнопку на панель
						ll_panel_main.addView(imageButton);
					}else if (identButton == 3){
						Lp.weight = 2;
						if ("UPDIR".equals(jobClick)) {
							LayoutInflater inflater = LayoutInflater.from(context);
							final Button button_up_dir = (Button) inflater.inflate(R.layout.button_up_dir, null, false);
							button_up_dir.setLayoutParams(Lp);
							// навешиваем действия на кнопку
							button_up_dir.setOnTouchListener(new OnTouchListener() {
								public boolean onTouch(View v, MotionEvent event) {
									button_gd.onTouchEvent(event);
									return false;
								}
							});
							upButton = button_up_dir;
							// добавляем кнопку на панель
							ll_panel_main.addView(button_up_dir);
						}
						if ("UPSCROLL".equals(jobClick)) {
							LayoutInflater inflater = LayoutInflater.from(context);
							final Button button_up_scroll = (Button) inflater.inflate(R.layout.button_up_scroll, null, false);
							button_up_scroll.setLayoutParams(Lp);
							if (disableScrollJump) {
								button_up_scroll.setText(R.string.jv_relaunch_prev);
							} else {
								button_up_scroll.setText(app.scrollStep + "%");
							}
							button_up_scroll.setOnTouchListener(new OnTouchListener() {
								public boolean onTouch(View v, MotionEvent event) {
									button_gd.onTouchEvent(event);
									return false;
								}
							});
							// добавляем кнопку на панель
							ll_panel_main.addView(button_up_scroll);
						}
						if ("DOWNSCROLL".equals(jobClick)) {
							LayoutInflater inflater = LayoutInflater.from(context);
							final Button button_down_scroll = (Button) inflater.inflate(R.layout.button_down_scroll, null, false);
							button_down_scroll.setLayoutParams(Lp);
							if (disableScrollJump) {
								button_down_scroll.setText(R.string.jv_relaunch_next);
							} else {
								button_down_scroll.setText(app.scrollStep + "%");
							}
							// навешиваем действия на кнопку
							button_down_scroll.setOnTouchListener(new OnTouchListener() {
								public boolean onTouch(View v, MotionEvent event) {
									button_gd.onTouchEvent(event);
									return false;
								}
							});
							// добавляем кнопку на панель
							ll_panel_main.addView(button_down_scroll);
						}
						if ("APPMANAGER".equals(jobClick)) {

							LayoutInflater inflater = LayoutInflater.from(context);
							final LinearLayout button_mem_date = (LinearLayout) inflater.inflate(R.layout.button_memory_and_date, null, false);
							button_mem_date.setLayoutParams(Lp);
							memLevel = (TextView) button_mem_date.findViewById(R.id.memory_level);
							memTitle = (TextView) button_mem_date.findViewById(R.id.memory_title);
							// навешиваем действия на кнопку
							button_mem_date.setOnTouchListener(new OnTouchListener() {
								public boolean onTouch(View v, MotionEvent event) {
									button_gd.onTouchEvent(event);
									return false;
								}
							});
							// добавляем кнопку на панель
							ll_panel_main.addView(button_mem_date);

						}
						if ("SWITCHWIFI".equals(jobClick)) {
							LayoutInflater inflater = LayoutInflater.from(context);
							final LinearLayout button_bat_wifi = (LinearLayout) inflater.inflate(R.layout.button_battory_and_wifi, null, false);
							button_bat_wifi.setLayoutParams(Lp);

							battLevel = (TextView) button_bat_wifi.findViewById(R.id.battory_level);
							battTitle = (TextView) button_bat_wifi.findViewById(R.id.battory_title);
							// навешиваем действия на кнопку
							button_bat_wifi.setOnTouchListener(new OnTouchListener() {
								public boolean onTouch(View v, MotionEvent event) {
									button_gd.onTouchEvent(event);
									return false;
								}
							});
							// добавляем кнопку на панель
							ll_panel_main.addView(button_bat_wifi);
						}
					}else if (identButton == 4){
						final Button button_text = new Button(context);
						// настройка отступов и расположения кнопки
						LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
						layoutParams.leftMargin = 2;
						layoutParams.rightMargin = 2;
						layoutParams.weight = 2;
						layoutParams.height = (int) (54 * scale + 0.5f);
						button_text.setLayoutParams(layoutParams);
						button_text.setBackgroundResource(R.drawable.button_text);
						//TODO определиться с размером шрифта и его маштабированием
						button_text.setTextSize(26); // где-то надо установить нормальный размер
						// кнопка заголовка на которой отображается текущая папка
						tv_title = button_text;
						// навешиваем действия на кнопку
						button_text.setOnTouchListener(new OnTouchListener() {
							public boolean onTouch(View v, MotionEvent event) {
								button_gd.onTouchEvent(event);
								return false;
							}
						});
						// добавляем кнопку на панель
						ll_panel_main.addView(button_text);
					}



				}
				ll_container.addView(newPanel);
			}
		}
	}
	//===== Проверка выхода из родительской папки =====
	private boolean CheckUpDir(String curDir){
		boolean enabled;
		if (curDir.equals("/") || (notLeaveStartDir && currentHomeDir.equals(curDir))) {
			enabled = false;
		}else {
			enabled = true;
		}
		return enabled;
	}
    //===== Переход в родительскую папку =====
    private void TapUpDir(String currentFolder) {
		if (CheckUpDir(currentFolder)) {
			int p = -1;
			if (!positions.empty()){
				p = positions.pop();
			}
			if(N2DeviceInfo.EINK_ONYX || N2DeviceInfo.EINK_GMINI || N2DeviceInfo.EINK_BOEYE){
				p++;
			}
			String parent = localFile.getParentPathForDir(currentFolder);
			drawDirectory(parent, p);
		}
    }
    private void GetDateAndMewory() {
        // Date
        if (memTitle != null) {
			// Получаем текущее время и дату:
			Date currentDate = Calendar.getInstance().getTime();
			// Форматируем текущую дату в соответствии с местоположением устройства:
			String formattedCurrentDate = dateFormat.format(currentDate);

			String formattedTime = timeFormat.format(currentDate);

			String d = formattedTime + " " + formattedCurrentDate;
            memTitle.setText(d);
        }

        // Memory
        if (memLevel != null) {
			MemoryInfo mi = new MemoryInfo();
			ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			activityManager.getMemoryInfo(mi);
            // "M free"
            memLevel.setText(mi.availMem / 1048576L + getResources().getString(R.string.jv_relaunch_m_free));
            memLevel.setCompoundDrawablesWithIntrinsicBounds(null, null,new BitmapDrawable(getResources(), utilIcons.getIcon("CHIP")), null);
        }
    }

    private void WiFiReceiver(){
        if (battTitle != null || wifiOp != null) {
            WifiManager wfm = (WifiManager) (getApplicationContext()).getSystemService(Context.WIFI_SERVICE);
            if (wfm.isWifiEnabled()) {
                String nowConnected = wfm.getConnectionInfo().getSSID();
                if (battTitle != null) {
                    if (nowConnected != null && !nowConnected.equals("")) {
                        battTitle.setText(nowConnected);
                    } else {
                        battTitle.setText(getResources().getString(R.string.jv_relaunch_wifi_is_on));
                    }
                    battTitle.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(getResources(), utilIcons.getIcon("WIFIONMINI")), null, null, null);
                }
                if (wifiOp != null){
					wifiOp.setBackgroundDrawable(new BitmapDrawable(getResources(), utilIcons.getIcon("WIFION")));
                }
            } else {
                // "WiFi is off"
                if (battTitle != null) {
                    battTitle.setText(getResources().getString(R.string.jv_relaunch_wifi_is_off));
                    battTitle.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(getResources(), utilIcons.getIcon("WIFIOFFMINI")), null, null, null);
                }
                if (wifiOp != null){
					wifiOp.setBackgroundDrawable(new BitmapDrawable(getResources(), utilIcons.getIcon("WIFIOFF")));
                }
            }
        }
        GetDateAndMewory();
    }

    private void selectNumberColumns(){
		final String[] columns = getResources().getStringArray(R.array.output_columns_names);
		final CharSequence[] columnsmode = new CharSequence[columns.length + 1];
		columnsmode[0] = getResources().getString(R.string.jv_relaunch_default);
		System.arraycopy(columns, 0, columnsmode, 1, columns.length);
		Integer checked = utilColumns.getNumberColumns(currentFolder) + 1;
		// get checked
		AlertDialog.Builder builder = new AlertDialog.Builder( ReLaunch.this);
		// "Select application"
		builder.setTitle(getResources().getString(R.string.jv_relaunch_select_columns));
		builder.setSingleChoiceItems(columnsmode, checked,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int i) {
						i--;
						switch (i) {
							case (-1):
								utilColumns.delNumberColumns(currentFolder);
								break;
							default:
								utilColumns.addListColumns(currentFolder, i);
						}
						drawDirectory(currentFolder, -1);
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void SetButtonClick(String final1, String final2){
		//
		if ("RUN".equals(final1)) {// внешняя программа
			if (prefs.getBoolean("returnToMain", false) && (new RunIntent(getBaseContext()).runApp(final2))) {
				finish();
			}
		} else if ("FAVDOCN".equals(final1)) {// страница фаворитов документов
            // TODO открытие определенной страницы списка файлов из фаворитов
		} else if ("FAVDOCMENU".equals(final1)) {// всплывающее меню фаворитов документов
			showMenu("favorites");
		} else if ("FAVDOCSCREEN".equals(final1)) {// экран фаворитов документов
			screenFavorites();
		} else if ("LRUN".equals(final1)) {// страница запущенных ДОКУМЕНТОВ
            // TODO открытие определенной страницы списка файлов из недавно открытых документов
		} else if ("LRUMENU".equals(final1)) {// всплывающее меню запущенных документов
			showMenu("lastOpened");
		} else if ("LRUSCREEN".equals(final1)) {// экран запущенных документов
			screenLastopened();
		} else if ("HOMEN".equals(final1)) {// переход к выбранной домашней папке
			openHome();
		} else if ("HOMEMENU".equals(final1)) {// всплывающее меню домашних папок
			showMenu("homeDir");
		} else if ("HOMESCREEN".equals(final1)) {// экран домашних папок
			screenHome();
		} else if ("ADVANCED".equals(final1)) {// расширенные настройки
			Intent i = new Intent(ReLaunch.this, Advanced.class);
			startActivity(i);
		} else if ("SETTINGS".equals(final1)) {// настройки
			screenSettings();
		} else if ("APPMANAGER".equals(final1)) {// такс менеджер
			Intent intent = new Intent(ReLaunch.this, TaskManager.class);
			startActivity(intent);
		} else if ("BATTERY".equals(final1)) {// показ расхода по приложениям
			Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
			startActivity(intent);
		} else if ("FAVAPP".equals(final1)) {// всплывающее меню фаворитов приложений
			Intent intent = new Intent(ReLaunch.this, AllApplications.class);
			intent.putExtra("list", "app_favorites");
			startActivity(intent);
		} else if ("ALLAPP".equals(final1)) {// все приложения
			Intent intent = new Intent(ReLaunch.this, AllApplications.class);
			intent.putExtra("list", "app_all");
			startActivity(intent);
		} else if ("LASTAPP".equals(final1)) { // последние открытые приложения
			Intent intent = new Intent(ReLaunch.this, AllApplications.class);
			intent.putExtra("list", "app_last");
			startActivity(intent);
		} else if ("SEARCH".equals(final1)) { // поиск
			screenSearch();
		} else if ("LOCK".equals(final1)) { // блокировка устройства
			actionLock();
		} else if ("POWEROFF".equals(final1)) { // выключение устройства
			actionPowerOff();
		} else if ("REBOOT".equals(final1)) { // перезагрузка устройства
			actionReboot();
		} else if ("SWITCHWIFI".equals(final1)) { // переключение состояния модуля WiFi
			actionSwitchWiFi();
		} else if ("DROPBOX".equals(final1)) { // запуск клиента дропбокс
			screenDropbox();
		} else if ("OPDS".equals(final1)) { // OPDS
			screenOPDS();
		} else if ("SYSSETTINGS".equals(final1)) {
			startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
		} else if ("UPDIR".equals(final1)) {
			TapUpDir(currentFolder);
		} else if ("UPSCROLL".equals(final1)) {
			app.TapUpScrool(gvList, all_view_items.size());
		} else if ("UPSCROLLPERC".equals(final1)) {
			app.upScrollPercent(gvList, all_view_items.size(), disableScrollJump);
		} else if ("UPSCROLLBEGIN".equals(final1)) {
			app.upScrollBegin(gvList, all_view_items.size(), disableScrollJump);
		} else if ("DOWNSCROLL".equals(final1)) {
			app.TapDownScrool(gvList, all_view_items.size());
		} else if ("DOWNSCROLLPERC".equals(final1)) {
			app.downScrollPercent(gvList, all_view_items.size(), disableScrollJump);
		} else if ("DOWNSCROLLEND".equals(final1)) {
			app.downScrollEnd(gvList, all_view_items.size(), disableScrollJump);
		} else if ("SORTMENU".equals(final1)) {
			menuSort();
		} else if ("SELNUMCOL".equals(final1)) {
			selectNumberColumns();
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

	private void setCurrentHomeDir(String currentHomeDir, int resource) {
		this.currentHomeDir = currentHomeDir;
		if (resource < 0) {
			resource = 0;
		}
		utilHomeDirs.setCurrentHomeDir(currentHomeDir, resource);
		drawDirectory(currentHomeDir, -1);
	}
	private void setTitleButton(String currentFolder, int count_items) {
		// заполняем заголовок
		if (tv_title != null) {
			String title_string;
			if (showFullDirPath) { // в зависимости от настроек
                if (hideKnownDirs) {
                    int index_end = currentFolder.indexOf(currentHomeDir);
                    title_string = "~" + currentFolder.substring(index_end);
                }else {
                    title_string = currentFolder;
                }
			} else {
				title_string = localFile.getNameDir(currentFolder);
			}

			if (fileExtendedData) {
				title_string += " (" + count_items + ")";
			}

			tv_title.setText(title_string);
		}
	}

	private void showMenu(String listName) {
		final List<HashMap<String, String>> itemsArray = createItemsArray(listName);
		if (itemsArray.size() > 0) {
			final CharSequence[] lnames = new CharSequence[itemsArray.size()];
			for (int i = 0; i < itemsArray.size(); i++) {
				HashMap<String, String> item = itemsArray.get(i);
				String fname = item.get("firstLine");
				// clean extension, if needed
				if (prefs.getBoolean("hideKnownExts", false)) {
					for (String ext : exts) {
						if (fname.endsWith(ext)) {
							fname = fname.substring(0, fname.length() - ext.length());
						}
					}
				}
				lnames[i] = fname;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
			// "Select home directory"
			final String flistName = listName;
			switch (listName) {
				case "favorites":
					builder.setTitle(app.getResources().getString(R.string.jv_relaunch_fav));
					break;
				case "lastOpened":
					builder.setTitle(app.getResources().getString(R.string.jv_relaunch_lru));
					break;
				case "homeDir":
					builder.setTitle(app.getResources().getString(R.string.jv_relaunch_home));
					break;
				default:
					builder.setTitle(app.getResources().getString(R.string.jv_results_menu_title));
					break;
			}
			builder.setSingleChoiceItems(lnames, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
							// action
							runItem(flistName, i, itemsArray);
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
	private void runItem(String listName, int position, List<HashMap<String, String>> itemsArray) {

		if (position < 0 || position > itemsArray.size() - 1)
			return;

		HashMap<String, String> item = itemsArray.get(position);
        // получаем данные по файлу
		String file_path = item.get("secondLine");
		String file_name = item.get("firstLine");
		String file_full_name;
		if (file_path.equals("/")) {
			file_full_name = "/" + file_name;
		}else {
			file_full_name = file_path + "/" + file_name;
		}
		if (item.get("type").equals(String.valueOf(TypeResource.DIR))) {
			if (listName.equals("homeDir")){
				setCurrentHomeDir(file_full_name, Integer.parseInt(item.get("resource")));
			}else {
				drawDirectory(file_full_name, 0);
			}
		} else {
			RunIntent runIntent = new RunIntent(getApplicationContext());
			if (file_name.endsWith(".apk")) {
				runIntent.installPackage(this, file_full_name);
			}else {
				if (app.readerName(file_name).equals("Nope")) {
					runIntent.runInternalView(this, file_full_name);
				}else {
					// Launch reader
					runIntent.launchReader(file_full_name);
				}
			}
		}
	}
	private List<HashMap<String, String>> createItemsArray(String listName) {
		List<HashMap<String, String>> inArray = new ArrayList<>();
		//List<HashMap<String, String>> itemsArray = new ArrayList<>();
		switch (listName) {
			case "favorites":
				inArray = (new UtilFavorites(app.getBaseContext())).getArrayFavorites();
				break;
			case "lastOpened":
				inArray = (new UtilLastOpen(app.getBaseContext())).getArrayLastOpen();
				break;
			case "homeDir":
				inArray = (new UtilHomeDirs(app.getBaseContext())).getArrayHomeDir();
				break;
		}
/*
		for (HashMap<String, String> inItem: inArray) {
			String dname = inItem.get("firstLine");
			String fname = inItem.get("secondLine");

			if (!filterResults || app.filterFile(dname, fname)) {
				itemsArray.add(inItem);
			}
		}*/

		//return itemsArray;
		return inArray;
	}
}
