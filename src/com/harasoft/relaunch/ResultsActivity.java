package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.widget.*;
import ebook.EBook;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class ResultsActivity extends Activity {
	final String TAG = "Results";
	final int CNTXT_MENU_RMFAV = 1;
	final int CNTXT_MENU_RMFILE = 2;
	final int CNTXT_MENU_CANCEL = 3;
	final int CNTXT_MENU_MOVEUP = 4;
	final int CNTXT_MENU_MOVEDOWN = 5;
	final int CNTXT_MENU_MARK_FINISHED = 6;
	final int CNTXT_MENU_MARK_READING = 7;
	final int CNTXT_MENU_MARK_FORGET = 8;
	final int CNTXT_MENU_RMDIR = 9;
    final int CNTXT_MENU_OPEN_DIR = 10;
    final int CNTXT_MENU_ADD_OPDS = 11;
    final int CNTXT_MENU_EDIT_OPDS = 12;
    final int CNTXT_MENU_DEL_OPDS = 13;
    final int CNTXT_MENU_CLEAN_OPDS = 14;
    final int CNTXT_MENU_RMLO = 15;
    final int CNTXT_MENU_RM_HOMEDIR = 16;
    final int CNTXT_MENU_ADD_HOMEDIR = 17;
    final int CNTXT_MENU_ADD_FTP = 18;
    final int CNTXT_MENU_EDIT_FTP = 19;
    final int CNTXT_MENU_DEL_FTP = 20;
    final int CNTXT_MENU_CLEAN_FTP = 21;
	ReLaunchApp app;
	String listName;
    static String listNameSecond;
	String title;
	Boolean rereadOnStart = true;
	static SharedPreferences prefs;
	FLSimpleAdapter adapter;
    Integer currentColsNum = 0;
	List<HashMap<String, String>> itemsArray = new ArrayList<HashMap<String, String>>();
	Integer currentPosition = -1;
	boolean addSView = true;
	//boolean oldHome;
	Pattern purgeBracketsPattern;
    MyDBHelper dbHelper;
    public static SQLiteDatabase db;
    int total;

    // переменные из настроек===============================================
    static boolean hideKnownExts = false;
    static int firstLineFontSizePx = 20;
    static int secondLineFontSizePx = 16;
    static boolean showNew = false;
    static boolean hideKnownDirs = false;
    static int firstLineIconSizePx = 48;
    static boolean filterResults = false;
    static boolean showBookTitles = false;

    static LayoutInflater vi;
    GridView gv;

    static class ViewHolder {
		TextView tv1;
		TextView tv2;
		LinearLayout tvHolder;
		ImageView iv;
	}

	class FLSimpleAdapter extends ArrayAdapter<HashMap<String, String>> {
		FLSimpleAdapter(Context context, int resource,
				List<HashMap<String, String>> data) {
			super(context, resource, data);
		}

		@Override
		public int getCount() {
			return itemsArray.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v = convertView;
            HashMap<String, String> item = itemsArray.get(position);
			if (v == null) {
				v = vi.inflate(R.layout.results_item,  parent, false);
                if (v == null) {
                    return null;
                }
				holder = new ViewHolder();
                holder.tv1 = (TextView) v.findViewById(R.id.res_dname);
                holder.tv2 = (TextView) v.findViewById(R.id.res_fname);
				holder.tvHolder = (LinearLayout) v.findViewById(R.id.res_holder);
				holder.iv = (ImageView) v.findViewById(R.id.res_icon);
				v.setTag(holder);
			} else{
				holder = (ViewHolder) v.getTag();
            }

			TextView tv1 = holder.tv1;
			TextView tv2 = holder.tv2;
            LinearLayout tvHolder = holder.tvHolder;
            ImageView iv = holder.iv;

            tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, firstLineFontSizePx);
            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, secondLineFontSizePx);

			if (position >= itemsArray.size()) {
				v.setVisibility(View.INVISIBLE);
				tv1.setVisibility(View.INVISIBLE);
				tv2.setVisibility(View.INVISIBLE);
				iv.setVisibility(View.INVISIBLE);
				return v;
			}

			if (item != null) {
				String fname = item.get("fname");
				String sname = item.get("sname");
				String dname = item.get("dname");
				String sdname = dname;
				String fullName = dname + "/" + fname;
				boolean setBold = false;

				// setup icon
                if (firstLineIconSizePx == 0) { // если отключены картинки
                    iv.setVisibility(View.GONE); // скрываем поле с ними
                }else {
                    if (listName.equals("opdslist")) {
                        iv.setImageDrawable(getResources().getDrawable(R.drawable.ci_opds_catalog));
                    }else if (listName.equals("ftplist")) {
                        iv.setImageDrawable(getResources().getDrawable(R.drawable.ci_ftp_catalog));
                    }else {
                        String temp_nameIcon = item.get("nameIcon");
                        for (ReLaunch.imageIcon anArrIcon : ReLaunch.arrIcon) {
                            if (anArrIcon.nameIcon.equals(temp_nameIcon)) {
                                iv.setImageBitmap(anArrIcon.icon);
                                break;
                            }
                        }
                    }
                }

				// special cases in dname & fname
				// dname empty - in root dir
				// fname empty with dname empty - root dir as is
				if (dname.equals("")) {
					sdname = "/";
					if (fname.equals("")) {
						sname = "/";
						sdname = "";
					}
				}

                if (showNew) {
                    int color_txt;
                    if (app.history.containsKey(fullName)) {
                        int baseHistory = app.history.get(fullName);
                        if (baseHistory == app.READING) {
                            color_txt = getResources().getColor(R.color.file_reading_fg);
                            tvHolder.setBackgroundColor(getResources() .getColor(R.color.file_reading_bg));
                        } else if (baseHistory == app.FINISHED) {
                            color_txt = getResources().getColor(R.color.file_finished_fg);
                            tvHolder.setBackgroundColor(getResources().getColor(R.color.file_finished_bg));
                        } else {
                            color_txt = getResources().getColor(R.color.file_unknown_fg);
                            tvHolder.setBackgroundColor(getResources().getColor(R.color.file_unknown_bg));
                        }
                    } else {
                        color_txt = getResources().getColor(R.color.file_new_fg);
                        tvHolder.setBackgroundColor(getResources().getColor(R.color.file_new_bg));
                        if (getResources().getBoolean(R.bool.show_new_as_bold))
                            setBold = true;
                    }
                    tv1.setTextColor(color_txt);
                    tv2.setTextColor(color_txt);

                    SpannableString s = new SpannableString(sname);
                    s.setSpan(new StyleSpan(setBold ? Typeface.BOLD : Typeface.NORMAL), 0, sname.length(), 0);
                    tv1.setText(sdname);
                    tv2.setText(s);
                }else {
                    tvHolder.setBackgroundColor(getResources().getColor(R.color.normal_bg));
                    int color_txt = getResources().getColor(R.color.normal_fg);
                    tv1.setTextColor(color_txt);
                    tv2.setTextColor(color_txt);
                    tv1.setText(sdname);
                    tv2.setText(sname);
                }

			}
			// fixes on rows height in grid
            // если у грида не одна колонка, то выравниваем ячейки по высоте в одной строке
            if (currentColsNum > 1) {
                GridView pgv = (GridView) parent;
                int colw = (pgv.getWidth()) / currentColsNum; // получаем ширину колонки
                int recalc_num = position; // номер позиции
                int recalc_height = 0;
                while (recalc_num % currentColsNum != 0) {  // находим последний элемент в строке
                    recalc_num = recalc_num - 1;
                    View temp_v = getView(recalc_num, null, parent);
                    if (temp_v != null) {
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

	private void redrawList() {
        EinkScreen.PrepareController(null, false);
		if (filterResults) {
			List<HashMap<String, String>> newItemsArray = new ArrayList<HashMap<String, String>>();

			for (HashMap<String, String> item : itemsArray) {
				if (app.filterFile(item.get("dname"), item.get("fname")) || item.get("type").equals("dir"))
					newItemsArray.add(item);
			}
			itemsArray = newItemsArray;
		}
		gv.setNumColumns(currentColsNum);
		adapter.notifyDataSetChanged();
		if (currentPosition != -1)
			gv.setSelection(currentPosition);
	}

	private void createItemsArray() {
		itemsArray = new ArrayList<HashMap<String, String>>();
        // вычищаем иконки из массива. оставляем только стандартные

        if(listName.equals("ftplist")){
            dbFTP();
        }else if(listName.equals("opdslist")){
            dbOPDS();
        }else {

            Button rt = (Button) findViewById(R.id.results_title);
            if (total == -1){
                rt.setText(title + " (" + app.getList(listName).size() + ")");
            }else{
                rt.setText(title + " (" + app.getList(listName).size() + "/"+ total + ")");
            }
            for (String[] n : app.getList(listName)) {
                if (!filterResults || (filterResults && app.filterFile(n[0], n[1])) || (n[1].equals(app.DIR_TAG))) {
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put("dname", n[0]);
                    item.put("fname", n[1]);

                    if (n[1].equals(app.DIR_TAG)) {
                        item.put("type", "dir");
                        int ind = n[0].lastIndexOf('/');
                        if (ind == -1) {
                            item.put("fname", "");
                            item.put("sname", "");
                        } else {
                            String sname = n[0].substring(ind + 1);
                            item.put("fname", sname);
                            // clean start prefixes, if need
                            if (hideKnownDirs && !listName.equals("homeList")) {
                                for (int i = 0, j = ReLaunch.startDir.length; i < j; i++) {
                                    if (sname.startsWith(ReLaunch.startDir[i])) {
                                        sname = "~" + sname.substring(ReLaunch.startDir[i].length());
                                    }
                                }
                            }
                            item.put("sname", sname);
                            item.put("dname", n[0].substring(0, ind));
                        }

                        // получение иконки=========
                        if (firstLineIconSizePx != 0) {
                            item.put("nameIcon", "dir_ok");
                        }
                        //=====================
                    } else {
                        item.put("type", "file");
                        String fname = n[1];
                        if (showBookTitles) {
                            item.put("sname", getEbookName(n[0], n[1]));
                        } else {
                            if (hideKnownExts) {// clean extension, if needed
                                for (int i = 0, j = ReLaunch.exts.size(); i < j; i++) {
                                    if (fname.endsWith(ReLaunch.exts.get(i))) {
                                        fname = fname.substring(0, fname.length() - ReLaunch.exts.get(i).length());
                                        //break;
                                    }
                                }
                            }
                            item.put("sname", fname);
                        }

                        // получение иконки==============================
                        if (firstLineIconSizePx != 0) {
                            String nameIcon;

                            if (n[1].endsWith(".apk")){ // если удалось
                                nameIcon = "install";
                            } else {  // иначе
                                String rdrName = app.readerName(n[1]); // в поле реадера читаем обработчик

                                if (rdrName.startsWith("Intent:")) { // если ответ начинается с ...
                                    nameIcon = "icon";
                                } else if (rdrName.equals("Nope")) { // если не известен
                                    File fil = new File(n[1]); // получаем файл
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
                        //=============================================
                    }
                    itemsArray.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        hideKnownExts = prefs.getBoolean("hideKnownExts", false);
        firstLineFontSizePx = Integer.parseInt(prefs.getString("firstLineFontSizePx", "20"));
        secondLineFontSizePx = Integer.parseInt(prefs.getString("secondLineFontSizePx", "16"));
        showNew = prefs.getBoolean("showNew", true);
        hideKnownDirs = prefs.getBoolean("hideKnownDirs", false);
        firstLineIconSizePx = Integer.parseInt(prefs.getString("firstLineIconSizePx", "48"));
        filterResults = prefs.getBoolean("filterResults", false);
        showBookTitles = prefs.getBoolean("showBookTitles", false);

		app = ((ReLaunchApp) getApplicationContext());
        if(app == null ) {
            finish();
        }
        EinkScreen.setEinkController(prefs);
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.results_layout);
        vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);


		if (app.dataBase == null){
			app.dataBase = new BooksBase(this);
        }
		if (!BooksBase.db.isOpen()){
			app.dataBase = new BooksBase(this);
        }
		purgeBracketsPattern = Pattern.compile("\\[[\\s\\.\\-_]*\\]");

		// Recreate readers list
		final Intent data = getIntent();
		if (data.getExtras() == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		listName = data.getExtras().getString("list");
		title = data.getExtras().getString("title");
		rereadOnStart = data.getExtras().getBoolean("rereadOnStart");
		total = data.getExtras().getInt("total", -1);

		(findViewById(R.id.results_btn)).setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});

		// set results icon
		ImageView results_icon = (ImageView) findViewById(R.id.results_icon);
		if (listName.equals("homeList")) {
            listNameSecond = "columnsHomeList";
			results_icon.setImageDrawable(getResources().getDrawable(R.drawable.ci_home));
		}
		if (listName.equals("favorites")) {
            listNameSecond = "columnsFAV";
			results_icon.setImageDrawable(getResources().getDrawable(R.drawable.ci_fav));
		}
		if (listName.equals("lastOpened")) {
            listNameSecond = "columnsLRU";
			results_icon.setImageDrawable(getResources().getDrawable(R.drawable.ci_lre));
		}
		if (listName.equals("searchResults")) {
            listNameSecond = "columnsSearch";
			results_icon.setImageDrawable(getResources().getDrawable(R.drawable.ci_search));
		}
        if (listName.equals("opdslist")) {
            listNameSecond = "columnsOpdsList";
            results_icon.setImageDrawable(getResources().getDrawable(R.drawable.ci_books));
            dbHelper = new MyDBHelper(this, "OPDS");
            db = dbHelper.getReadableDatabase();
            Cursor c;
            if(db != null) {
                c = db.query("OPDS", null, null, null, null, null, null);
                if(c.getCount()== 0){
                    addDbOPDS("Либрусек", "http://lib.rus.ec/opds", "false", null, null);
                }
                c.close();
                db.close();
            }
        }
        if (listName.equals("ftplist")) {
            listNameSecond = "columnsFtpList";
            results_icon.setImageDrawable(getResources().getDrawable(R.drawable.ci_home));
            dbHelper = new MyDBHelper(this, "FTP");
            db = dbHelper.getReadableDatabase();
            Cursor c;
            if(db != null) {
                c = db.query("FTP", null, null, null, null, null, null);
                if(c.getCount()== 0){
                    addDbFTP("files.3dnews.ru", 21, "/pub", null, null);
                }
                c.close();
                db.close();
            }
        }

		// may be "dead end" of code now(?) now UP functionality in this
		// screens?
		// so force to DISABLED
		final Button up = (Button) findViewById(R.id.goup_btn);
		up.setEnabled(false);
		// end of (possible) "dead end" code
		final ImageButton adv = (ImageButton) findViewById(R.id.advanced_btn);
		if (adv != null) {
			class advSimpleOnGestureListener extends SimpleOnGestureListener {
				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					Intent i = new Intent(ResultsActivity.this, Advanced.class);
					startActivity(i);
					return true;
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

		currentPosition = -1;
		gv = (GridView) findViewById(R.id.results_list);
		gv.setHorizontalSpacing(0);
		Button rt = (Button) findViewById(R.id.results_title);
		rt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final String[] columns = getResources().getStringArray(R.array.output_columns_names);
				final CharSequence[] columnsmode = new CharSequence[columns.length];
                System.arraycopy(columns, 0, columnsmode, 0, columns.length);
				int checked = Integer.valueOf(prefs.getString(listNameSecond, "-1"));
                if (checked == -1){
                    checked = 0;
                }
				// get checked
				AlertDialog.Builder builder = new AlertDialog.Builder(ResultsActivity.this);
				// "Select application"
				builder.setTitle(getResources().getString(R.string.jv_relaunch_select_columns));
				builder.setSingleChoiceItems(columnsmode, checked, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int i) {
                                SharedPreferences.Editor editor = prefs.edit();
								if (i == 0) {
                                    editor.putString(listNameSecond, "-1");
								} else {
                                    editor.putString(listNameSecond, String.valueOf(i));
								}
                                currentColsNum = i;
                                editor.commit();
								redrawList();
								dialog.dismiss();
							}
						});
                AlertDialog alert = builder.create();
                alert.show();

			}
		});

		//createItemsArray();
		adapter = new FLSimpleAdapter(this, R.layout.results_item, itemsArray);
		gv.setAdapter(adapter);
		registerForContextMenu(gv);
		if (prefs.getBoolean("customScroll", app.customScrollDef)) {
			if (addSView) {
				int scrollW;
				try {
					scrollW = Integer.parseInt(prefs.getString("scrollWidth","25"));
				} catch (NumberFormatException e) {
					scrollW = 25;
				}

				LinearLayout ll = (LinearLayout) findViewById(R.id.results_fl);
				final SView sv = new SView(getBaseContext());
				LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(
						scrollW, ViewGroup.LayoutParams.FILL_PARENT, 1f);
				sv.setLayoutParams(pars);
				ll.addView(sv);
				gv.setOnScrollListener(new AbsListView.OnScrollListener() {
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
			gv.setOnScrollListener(new AbsListView.OnScrollListener() {
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
                    EinkScreen.PrepareController(null, false);
				}

				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
				}
			});
		}
// Новый обработчик тапов
        class GlSimpleOnGestureListener extends SimpleOnGestureListener {
            Context context;

            public GlSimpleOnGestureListener(Context context) {
                super();
                this.context = context;
            }
            public int findViewByXY(MotionEvent e) {
                int location[] = new int[2];
                float x = e.getRawX();
                float y = e.getRawY();
                int first = gv.getFirstVisiblePosition();
                int last = gv.getLastVisiblePosition();
                int count = last -first + 1;
                for (int i = 0; i<count; i++) {
                    View v = gv.getChildAt(i);
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

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                int position = findViewByXY(e);
                if (position == -1){
                    return true;
                }

                HashMap<String, String> item = itemsArray.get(position);
                String fullName = item.get("dname") + "/" + item.get("fname");

                currentPosition = gv.getFirstVisiblePosition();
                if(listName.equals("ftplist")){
                    Intent intent = new Intent(ResultsActivity.this, ReLaunch.class);
                    intent.putExtra("ftplist", "list");
                    intent.putExtra("id", position + 1);
                    intent.putExtra("path", "FTP| " + item.get("dname"));
                    startActivity(intent);
                }else if(listName.equals("opdslist")){
                    Intent intent = new Intent(ResultsActivity.this, OPDSActivity.class);
                    intent.putExtra("opdscat", item.get("dname"));
                    intent.putExtra("login", item.get("login"));
                    intent.putExtra("password", item.get("password"));
                    startActivity(intent);
                }else if (item.get("type").equals("dir")) {
                    Intent intent = new Intent(ResultsActivity.this,ReLaunch.class);
                    intent.putExtra("start_dir", fullName);
                    //intent.putExtra("home", ReLaunch.useHome);
                    //oldHome = ReLaunch.useHome;
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, ReLaunch.DIR_ACT);
                    finish();
                } else {
                    String fileName = item.get("fname");
                    if (!app.specialAction(ResultsActivity.this, fullName)) {
                        if (app.readerName(fileName).equals("Nope"))
                            app.defaultAction(ResultsActivity.this, fullName);
                        else {
                            // Launch reader
                            app.LaunchReader(fullName);
                        }
                    }
                    // close in needed
                    if (prefs.getBoolean("returnFileToMain", false))
                        finish();
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                HashMap<String, String> i;
                String dr;
                String fn;
                String fullName = "";
                final int position = findViewByXY(e);

                if (position > -1) {
                    i = itemsArray.get(position);
                    dr = i.get("dname");
                    fn = i.get("fname");
                    fullName = dr + "/" + fn;
                }

                ArrayList<String> aList = new ArrayList<String>(10);

                if (listName.equals("homeList")) {
                    if (position > -1) {
                        // "Remove from start dir"
                        aList.add(getResources().getString(R.string.jv_results_home_list_remove));
                    }
                    // "Add start dir"
                    aList.add(getResources().getString(R.string.jv_results_add_homedir));
                    if (position > 0)
                        // "Move one position up"
                        aList.add(getResources().getString(R.string.jv_results_move_up));
                    if (position < (itemsArray.size() - 1) && position != -1)
                        // "Move one position down"
                        aList.add(getResources().getString(R.string.jv_results_move_down));
                } else if (listName.equals("opdslist")) {
                    // "Add opds catalog"
                    aList.add(getResources().getString(R.string.jv_results_add_opds));
                    if (position > -1) {
                        // "Remove from list"
                        aList.add(getResources().getString(R.string.jv_relaunch_delete));
                        // "Rename"
                        aList.add(getResources().getString(R.string.jv_relaunch_edit));
                        // Clean
                        aList.add(getResources().getString(R.string.jv_relaunch_clean_opds));
                    }

                } else if (listName.equals("favorites")) {
                    if (position > 0)
                        // "Move one position up"
                        aList.add(getResources().getString(R.string.jv_results_move_up));
                    if (position < (itemsArray.size() - 1))
                        // "Move one position down"
                        aList.add(getResources().getString(R.string.jv_results_move_down));
                    if (position > -1) {
                        // "Remove from favorites"
                        aList.add(getResources().getString(R.string.jv_results_remove));
                        // "Delete file"
                        if (prefs.getBoolean("useFileManagerFunctions", true))
                            aList.add(getResources().getString(R.string.jv_results_delete_file));
                    }
                } else if (listName.equals("lastOpened")) {
                        if (app.history.containsKey(fullName)) {
                            if (app.history.get(fullName) == app.READING)
                                // "Mark as read"
                                aList.add(getResources().getString(R.string.jv_results_mark));
                            else if (app.history.get(fullName) == app.FINISHED)
                                // "Remove \"read\" mark"
                                aList.add(getResources().getString(R.string.jv_results_unmark));
                            // "Forget all marks"
                            aList.add(getResources().getString(R.string.jv_results_unmark_all));
                        } else
                            // "Mark as read"
                            aList.add(getResources().getString(R.string.jv_results_mark));
                        // "Remove from last opened"
                        aList.add(getResources().getString(R.string.jv_results_remove_last_opened));
                        // "Delete file"
                        if (prefs.getBoolean("useFileManagerFunctions", true))
                            aList.add(getResources().getString(R.string.jv_results_delete_file));

                        // "Open dir"
                        aList.add(getResources().getString(R.string.jv_open_dir));
                } else if (listName.equals("searchResults")) {
                        if (position > 0) {
                            // "Move one position up"
                            aList.add(getResources().getString(R.string.jv_results_move_up));
                        }
                        if (position < (itemsArray.size() - 1))
                            // "Move one position down"
                            aList.add(getResources().getString(R.string.jv_results_move_down));
                        if (app.history.containsKey(fullName)) {
                            if (app.history.get(fullName) == app.READING)
                                // "Mark as read"
                                aList.add(getResources().getString(R.string.jv_results_mark));
                            else if (app.history.get(fullName) == app.FINISHED)
                                // "Remove \"read\" mark"
                                aList.add(getResources().getString(R.string.jv_results_unmark));
                            // "Forget all marks"
                            aList.add(getResources().getString(R.string.jv_results_unmark_all));
                        } else
                            // "Mark as read"
                            aList.add(getResources().getString(R.string.jv_results_mark));
                        // "Delete file"
                        if (prefs.getBoolean("useFileManagerFunctions", true))
                            aList.add(getResources().getString(R.string.jv_results_delete_file));
                        aList.add(getResources().getString(R.string.jv_open_dir));
                } else if (listName.equals("ftplist")) {
                    // "Add opds catalog"
                    aList.add(getResources().getString(R.string.jv_results_add_ftp));
                    if (position > 0) {
                        // "Remove from list"
                        aList.add(getResources().getString(R.string.jv_relaunch_delete));
                        // "Rename"
                        aList.add(getResources().getString(R.string.jv_relaunch_edit));
                        // "Rename"
                        aList.add(getResources().getString(R.string.jv_relaunch_clean_ftp));
                    }

                }
                // "Cancel"
                aList.add(getResources().getString(R.string.app_cancel));

                final String[] list = aList.toArray(new String[aList.size()]);
                ListAdapter cmAdapter = new ArrayAdapter<String>(app, R.layout.cmenu_list_item, list);
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setAdapter(cmAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String s = list[item];
                        if (s.equalsIgnoreCase(getString(R.string.jv_results_remove)))
                            onContextMenuSelected(CNTXT_MENU_RMFAV, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_delete_file)))
                            onContextMenuSelected(CNTXT_MENU_RMFILE, position);
                        else if (s.equalsIgnoreCase(getString(R.string.app_cancel)))
                            onContextMenuSelected(CNTXT_MENU_CANCEL, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_move_up)))
                            onContextMenuSelected(CNTXT_MENU_MOVEUP, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_move_down)))
                            onContextMenuSelected(CNTXT_MENU_MOVEDOWN, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_mark)))
                            onContextMenuSelected(CNTXT_MENU_MARK_FINISHED, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_unmark)))
                            onContextMenuSelected(CNTXT_MENU_MARK_READING, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_unmark_all)))
                            onContextMenuSelected(CNTXT_MENU_MARK_FORGET, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_createintent)))
                            onContextMenuSelected(CNTXT_MENU_RMDIR, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_open_dir)))
                            onContextMenuSelected(CNTXT_MENU_OPEN_DIR, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_add_opds)))
                            onContextMenuSelected(CNTXT_MENU_ADD_OPDS, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_edit)))
                            onContextMenuSelected(CNTXT_MENU_EDIT_OPDS, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_delete)))
                            onContextMenuSelected(CNTXT_MENU_DEL_OPDS, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_clean_opds)))
                            onContextMenuSelected(CNTXT_MENU_CLEAN_OPDS, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_remove_last_opened)))
                            onContextMenuSelected(CNTXT_MENU_RMLO, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_home_list_remove)))
                            onContextMenuSelected(CNTXT_MENU_RM_HOMEDIR, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_add_homedir)))
                            onContextMenuSelected(CNTXT_MENU_ADD_HOMEDIR, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_add_ftp)))
                            onContextMenuSelected(CNTXT_MENU_ADD_FTP, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_edit)))
                            onContextMenuSelected(CNTXT_MENU_EDIT_FTP, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_delete)))
                            onContextMenuSelected(CNTXT_MENU_DEL_FTP, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_relaunch_clean_ftp)))
                            onContextMenuSelected(CNTXT_MENU_CLEAN_FTP, position);

                    }
                });
                AlertDialog alert = builder.create();
                alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alert.show();

            }
        }
        GlSimpleOnGestureListener gv_gl = new GlSimpleOnGestureListener(this);
        final GestureDetector gv_gd = new GestureDetector(gv_gl);
        gv.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                gv_gd.onTouchEvent(event);
                return false;
            }
        });

		final Button upScroll = (Button) findViewById(R.id.upscroll_btn);
		if (!ReLaunch.disableScrollJump) {
			upScroll.setText(app.scrollStep + "%");
		} else {
			upScroll.setText(getResources().getString(R.string.jv_relaunch_prev));
		}
		class upScrlSimpleOnGestureListener extends SimpleOnGestureListener {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
                app.TapUpScrool(gv, itemsArray.size());
				return true;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (!ReLaunch.disableScrollJump) {
					int first = gv.getFirstVisiblePosition();
					int total = itemsArray.size();
					first -= (total * app.scrollStep) / 100;
					if (first < 0)
						first = 0;
					gv.setSelection(first);
					// some hack workaround against not scrolling in some cases
					if (total > 0) {
						gv.requestFocusFromTouch();
						gv.setSelection(first);
					}
				}
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if (upScroll.hasWindowFocus()) {
					if (!ReLaunch.disableScrollJump) {
						int first = 0;// = gv.getFirstVisiblePosition();
						int total = itemsArray.size();
						gv.setSelection(first);
						// some hack workaround against not scrolling in some
						// cases
						if (total > 0) {
							gv.requestFocusFromTouch();
							gv.setSelection(first);
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

		final Button downScroll = (Button) findViewById(R.id.downscroll_btn);
		if (!ReLaunch.disableScrollJump) {
			downScroll.setText(app.scrollStep + "%");
		} else {
			downScroll.setText(getResources().getString(
					R.string.jv_relaunch_next));
		}
		class dnScrlSimpleOnGestureListener extends SimpleOnGestureListener {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
                app.TapDownScrool(gv, itemsArray.size());
				return true;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (!ReLaunch.disableScrollJump) {
					int first = gv.getFirstVisiblePosition();
					int total = itemsArray.size();
					int last = gv.getLastVisiblePosition();
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
                    app.RepeatedDownScroll(gv, first, target, 0);
				}
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if (downScroll.hasWindowFocus()) {
					if (!ReLaunch.disableScrollJump) {
						int first = gv.getFirstVisiblePosition();
						int total = itemsArray.size();
						int last = gv.getLastVisiblePosition();
						if (total == last + 1)
							return;
						int target = total - 1;
						if (target <= last)
							target = last + 1; // Special for NOOK, otherwise it
												// won't redraw the listview
						if (target > (total - 1))
							target = total - 1;
                        //RepeatedDownScroll ds = new RepeatedDownScroll();
                        //ds.doIt(first, target, 0);
                        app.RepeatedDownScroll(gv, first, target, 0);
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
        int colsNum = Integer.valueOf(prefs.getString(listNameSecond, "-1"));
        // override auto (not working fine in adnroid)
        if (colsNum == 0) {
            colsNum = app.getAutoColsNum(itemsArray, "fname", ReLaunch.columnsAlgIntensity);
        }
        currentColsNum = colsNum;
        gv.setNumColumns(currentColsNum);
        createItemsArray();
		ScreenOrientation.set(this, prefs);
	}
	@Override
	protected void onStart() {
		if (rereadOnStart)
		    redrawList();
		super.onStart();
	}
    @Override
    protected void onStop() {
        if (listName.equals("opdslist") || listName.equals("ftplist")) {
            dbHelper.close();
        }
        super.onStop();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        dbHelper.close();
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.resultsmenu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mime_types:
			Intent intent1 = new Intent(ResultsActivity.this, TypesActivity.class);
			startActivityForResult(intent1, ReLaunch.TYPES_ACT);
			return true;
		case R.id.about:
			app.About(this);
			return true;
		case R.id.setting:
			Intent intent3 = new Intent(ResultsActivity.this, PrefsActivity.class);
			startActivity(intent3);
			return true;
		default:
			return true;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ReLaunch.TYPES_ACT:
			if (resultCode != Activity.RESULT_OK)
				return;

			String newTypes = ReLaunch.createReadersString(app.getReaders());

			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("types", newTypes);
			editor.commit();

			redrawList();
			break;
		case ReLaunch.DIR_ACT:
			//ReLaunch.useHome = oldHome;
			break;
		default:
		}
	}

    public boolean onContextMenuSelected(int item, final int pos) {
        if (item == CNTXT_MENU_CANCEL) {
            return true;
        }
        HashMap<String, String> i = null;
        String dname = "";
        String fname = "";
        String fullName = "";

        if (pos > -1) {
            i = itemsArray.get(pos);
            dname = i.get("dname");
            fname = i.get("fname");
            fullName = dname + "/" + fname;
        }

        switch (item) {
            case CNTXT_MENU_MARK_READING:
                app.history.put(fullName, app.READING);
                app.saveList("history");
                redrawList();
                break;
            case CNTXT_MENU_MARK_FINISHED:
                app.history.put(fullName, app.FINISHED);
                app.saveList("history");
                redrawList();
                break;
            case CNTXT_MENU_MARK_FORGET:
                app.history.remove(fullName);
                app.saveList("history");
                redrawList();
                break;
            case CNTXT_MENU_RMFAV:
                if (i != null && i.get("type").equals("dir")) {
                    app.removeFromList("favorites", fullName, app.DIR_TAG);
                } else {
                    app.removeFromList("favorites", dname, fname);
                }
                app.saveList("favorites");
                itemsArray.remove(pos);
                redrawList();
                break;
            case CNTXT_MENU_MOVEUP:
                if (pos > 0) {
                    List<String[]> f = app.getList(listName);
                    HashMap<String, String> it = itemsArray.get(pos);
                    String[] fit = f.get(pos);

                    itemsArray.remove(pos);
                    f.remove(pos);
                    itemsArray.add(pos - 1, it);
                    f.add(pos - 1, fit);
                    app.setList(listName, f);

                    if(listName.equals("homeList") ){
                        StringBuilder new_StartDir = new StringBuilder();
                        for (String[] anItemsArray : f) {
                            if (new_StartDir.length() > 0) {
                                new_StartDir.append(",");
                            }
                            new_StartDir.append(anItemsArray[0]);
                        }

                        app.setStartDir(new_StartDir.toString());
                        ReLaunch.startDir = new_StartDir.toString().split(",");
                    }



                    redrawList();
                }
                break;
            case CNTXT_MENU_MOVEDOWN:
                if (pos < (itemsArray.size() - 1)) {
                    List<String[]> f = app.getList(listName);
                    HashMap<String, String> it = itemsArray.get(pos);
                    String[] fit = f.get(pos);

                    int size = itemsArray.size();
                    itemsArray.remove(pos);
                    f.remove(pos);
                    if (pos + 1 >= size - 1) {
                        itemsArray.add(it);
                        f.add(fit);
                    } else {
                        itemsArray.add(pos + 1, it);
                        f.add(pos + 1, fit);
                    }
                    app.setList(listName, f);
                    if(listName.equals("homeList") ){
                        StringBuilder new_StartDir = new StringBuilder();
                        for (String[] anItemsArray : f) {
                            if (new_StartDir.length() > 0) {
                                new_StartDir.append(",");
                            }
                            new_StartDir.append(anItemsArray[0]);
                        }

                        app.setStartDir(new_StartDir.toString());
                        ReLaunch.startDir = new_StartDir.toString().split(",");
                    }
                    redrawList();
                }
                break;
            case CNTXT_MENU_RMFILE:
                if (prefs.getBoolean("confirmFileDelete", true)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    // "Delete file warning"
                    builder.setTitle(getResources().getString(
                            R.string.jv_results_delete_file_title));
                    // "Are you sure to delete file \"" + fname + "\" ?");
                    builder.setMessage(getResources().getString(
                            R.string.jv_results_delete_file_text1)
                            + " \""
                            + fname
                            + "\" "
                            + getResources().getString(
                            R.string.jv_results_delete_file_text2));
                    // "Yes"
                    final String finalDname = dname;
                    final String finalFname = fname;
                    builder.setPositiveButton(
                            getResources().getString(R.string.app_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.dismiss();
                                    if(app.fileRemove(new File(finalDname + "/" + finalFname))) {
                                        app.fileRemoveAllList(finalDname, finalFname);
                                        itemsArray.remove(pos);
                                        redrawList();
                                    }

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
                } else if (app.fileRemove(new File(dname + "/" + fname))) {
                    app.fileRemoveAllList(dname, fname);
                    itemsArray.remove(pos);
                    redrawList();
                }
                break;
            case CNTXT_MENU_RMDIR:
                File d = new File(fullName);
                boolean isEmpty = d.list().length < 1;
                if (isEmpty) {
                    if (prefs.getBoolean("confirmDirDelete", true)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        // "Delete empty directory warning"
                        builder.setTitle(getResources().getString(
                                R.string.jv_results_delete_em_dir_title));
                        // "Are you sure to delete empty directory \"" + fname +
                        // "\" ?");
                        builder.setMessage(getResources().getString(
                                R.string.jv_results_delete_em_dir_text1)
                                + " \""
                                + fname
                                + "\" "
                                + getResources().getString(
                                R.string.jv_results_delete_em_dir_text2));
                        // "Yes"
                        final String finalDname1 = dname;
                        final String finalFname1 = fname;
                        builder.setPositiveButton(
                                getResources().getString(R.string.app_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        dialog.dismiss();
                                        if(app.fileRemove(new File(finalDname1 + "/" + finalFname1))) {
                                            app.fileRemoveAllList(finalDname1, finalFname1);
                                            itemsArray.remove(pos);
                                            redrawList();
                                        }
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
                    } else if (app.fileRemove(new File(dname + "/" + fname))) {
                        app.fileRemoveAllList(dname, fname);
                        itemsArray.remove(pos);
                        redrawList();
                    }
                } else {
                    if (prefs.getBoolean("confirmNonEmptyDirDelete", true)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        // "Delete non empty directory warning"
                        builder.setTitle(getResources().getString(
                                R.string.jv_results_delete_ne_dir_title));
                        // "Are you sure to delete non-empty directory \"" + fname +
                        // "\" (dangerous) ?");
                        builder.setMessage(getResources().getString(
                                R.string.jv_results_delete_ne_dir_text1)
                                + " \""
                                + fname
                                + "\" "
                                + getResources().getString(
                                R.string.jv_results_delete_ne_dir_text2));
                        // "Yes"
                        final String finalDname2 = dname;
                        final String finalFname2 = fname;
                        builder.setPositiveButton(
                                getResources().getString(R.string.app_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        dialog.dismiss();
                                        if(app.fileRemove(new File(finalDname2 + "/" + finalFname2))) {
                                            app.fileRemoveAllList(finalDname2, finalFname2);
                                            itemsArray.remove(pos);
                                            redrawList();
                                        }
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
                    } else if (app.fileRemove(new File(dname + "/" + fname))) {
                        app.fileRemoveAllList(dname, fname);
                        itemsArray.remove(pos);
                        redrawList();
                    }
                }
                break;
            case CNTXT_MENU_OPEN_DIR:
                Intent intent = new Intent(ResultsActivity.this,ReLaunch.class);
                intent.putExtra("start_dir", dname);
                //intent.putExtra("home", ReLaunch.useHome);
                //oldHome = ReLaunch.useHome;
                startActivityForResult(intent, ReLaunch.DIR_ACT);
                break;
            case CNTXT_MENU_ADD_OPDS:
            case CNTXT_MENU_EDIT_OPDS:
                final int menuidOPDS = item;
                AlertDialog.Builder builderOPDS = new AlertDialog.Builder(this);
                if (menuidOPDS == CNTXT_MENU_ADD_OPDS) {
                    builderOPDS.setTitle(getResources().getString(R.string.jv_results_add_opds));
                }else {
                    builderOPDS.setTitle(getResources().getString(R.string.jv_results_edit_opds));
                }
                View llOPDS = getLayoutInflater().inflate(R.layout.ll_opds_dialog, null);
                if (llOPDS != null) {
                    builderOPDS.setView(llOPDS);
                    final EditText inputName = (EditText) llOPDS.findViewById(R.id.et_name_server); // Имя. Уникальное
                    final EditText inputAddress = (EditText) llOPDS.findViewById(R.id.et_path); // Адрес
                    final CheckBox cbLogin = (CheckBox) llOPDS.findViewById(R.id.cb_login);
                    final EditText inputLogin = (EditText) llOPDS.findViewById(R.id.et_login); // Имя пользователя для доступа
                    final EditText inputPassword = (EditText) llOPDS.findViewById(R.id.et_password); // Пароль

                    // Заголовки полей
                    final TextView tv_login = (TextView) llOPDS.findViewById(R.id.tv_login);
                    final TextView tv_password = (TextView) llOPDS.findViewById(R.id.tv_password);

                    // ======================================
                    cbLogin.setChecked(false);
                    tv_login.setEnabled(false);
                    tv_password.setEnabled(false);
                    inputLogin.setText("");
                    inputLogin.setEnabled(false);
                    inputPassword.setText("");
                    inputPassword.setEnabled(false);
                    //=================================================

                    if (N2DeviceInfo.EINK_NOOK) {
                        tv_login.setTextColor(getResources().getColor(R.color.backgorund_task_fg));
                        tv_password.setTextColor(getResources().getColor(R.color.backgorund_task_fg));
                        cbLogin.setTextColor(getResources().getColor(R.color.backgorund_task_fg));

                        TextView tvtemt = (TextView) llOPDS.findViewById(R.id.tv1);
                        tvtemt.setTextColor(getResources().getColor(R.color.backgorund_task_fg));
                        tvtemt = (TextView) llOPDS.findViewById(R.id.textView);
                        tvtemt.setTextColor(getResources().getColor(R.color.backgorund_task_fg));
                    }

                    if (menuidOPDS == CNTXT_MENU_EDIT_OPDS) {
                        HashMap<String, String> temp_item = getdbOPDS(pos);
                        inputName.setText(temp_item.get("SERVER"));
                        inputAddress.setText(temp_item.get("PATH"));
                        if (temp_item.get("EN_PASS").equals("true")) {
                            cbLogin.setChecked(true);
                            tv_login.setEnabled(true);
                            tv_password.setEnabled(true);
                            inputLogin.setEnabled(true);
                            inputPassword.setEnabled(true);
                            inputLogin.setText(temp_item.get("LOGIN"));
                            inputPassword.setText(temp_item.get("PASSWORD"));
                        }
                    }

                    cbLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                            if(cbLogin.isChecked()){
                                tv_login.setEnabled(true);
                                tv_password.setEnabled(true);
                                inputLogin.setEnabled(true);
                                inputPassword.setEnabled(true);
                            }else{
                                tv_login.setEnabled(false);
                                tv_password.setEnabled(false);
                                inputLogin.setText("");
                                inputLogin.setEnabled(false);
                                inputPassword.setText("");
                                inputPassword.setEnabled(false);
                            }
                        }
                    });


                    // "Yes"
                    final String finalFname3 = fname;
                    builderOPDS.setPositiveButton(getResources().getString(R.string.app_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // добавление в базу данных
                                    String temp_en_login;
                                    if (cbLogin.isChecked()){
                                        temp_en_login = "true";
                                    }else{
                                        temp_en_login = "false";
                                    }

                                    if (menuidOPDS == CNTXT_MENU_ADD_OPDS) {
                                        addDbOPDS(String.valueOf(inputName.getText()),
                                                String.valueOf(inputAddress.getText()),
                                                temp_en_login,
                                                String.valueOf(inputLogin.getText()),
                                                String.valueOf(inputPassword.getText()));
                                    }else{
                                        // обновление базы
                                        updateDbOPDS(finalFname3, String.valueOf(inputName.getText()),
                                                String.valueOf(inputAddress.getText()),
                                                temp_en_login,
                                                String.valueOf(inputLogin.getText()),
                                                String.valueOf(inputPassword.getText()));
                                    }
                                    createItemsArray();
                                }
                            });
                    // "No"
                }
                builderOPDS.setNegativeButton(
                        getResources().getString(R.string.app_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.dismiss();
                            }
                        });
                builderOPDS.show();
                break;
            case CNTXT_MENU_DEL_OPDS:
                // удаление из базы
                delDbOPDS(fname);
                createItemsArray();
                break;
            case CNTXT_MENU_CLEAN_OPDS:
                // очистка базы
                db = dbHelper.getReadableDatabase();
                if(db != null) {
                    dbHelper.resetDb(db);
                    createItemsArray();
                    db.close();
                }
                break;
            case CNTXT_MENU_RMLO:
                app.removeFromList("lastOpened", dname, fname);
                app.saveList("lastOpened");
                itemsArray.remove(pos);
                redrawList();
                break;
            case CNTXT_MENU_RM_HOMEDIR:
                app.removeFromList("homeList", dname, fname);
                app.saveList("homeList");
                itemsArray.remove(pos);

                StringBuilder new_StartDir = new StringBuilder();
                for (HashMap<String, String> anItemsArray : itemsArray) {
                    if (new_StartDir.length() > 0) {
                        new_StartDir.append(",");
                    }
                    new_StartDir.append(anItemsArray.get("dname"));
                    new_StartDir.append("/");
                    new_StartDir.append(anItemsArray.get("fname"));
                }

                app.setStartDir(new_StartDir.toString());
                ReLaunch.startDir = new_StartDir.toString().split(",");
                redrawList();
                break;
            case CNTXT_MENU_ADD_HOMEDIR:
                AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
                builder3.setTitle(getResources().getString(R.string.jv_results_add_homedir));

                // Поле ввода пути новой стартовой папки
                final EditText inputName3 = new EditText(this); // Имя. Уникальное
                // Заголовок поля ввода
                TextView tPath3 = new TextView(this);

                if (N2DeviceInfo.EINK_NOOK) {
                    tPath3.setTextColor(getResources().getColor(R.color.file_unknown_fg));
                }
                // Заполняем заголовки
                tPath3.setText(getResources().getString(R.string.jv_results_addres_homedir));
                inputName3.setText("/");


                // начинаем заполнять форму
                LinearLayout ll3=new LinearLayout(this);
                ll3.setOrientation(LinearLayout.VERTICAL);// вертивальное расположение элементов
                // Добавляем элементы
                ll3.addView(tPath3);
                ll3.addView(inputName3);

                // отрисовываем
                builder3.setView(ll3);

                // "Yes"
                builder3.setPositiveButton(getResources().getString(R.string.app_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // обновление базы
                                String fullPath = String.valueOf(inputName3.getText());

                                List<String[]> homeList = app.getList(listName);
                                String[] homeEl = new String[2];
                                homeEl[0] = fullPath;
                                homeEl[1] = app.DIR_TAG;
                                homeList.add(homeEl);
                                app.setList("homeList", homeList);
                                StringBuilder new_StartDir = new StringBuilder();
                                for (String[] anItemsArray : homeList) {
                                    if (new_StartDir.length() > 0) {
                                        new_StartDir.append(",");
                                    }
                                    new_StartDir.append(anItemsArray[0]);
                                }

                                app.setStartDir(new_StartDir.toString());
                                ReLaunch.startDir = new_StartDir.toString().split(",");
                                createItemsArray();
                            }
                        });
                // "No"
                builder3.setNegativeButton(
                        getResources().getString(R.string.app_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.dismiss();
                            }
                        });
                builder3.show();
                break;
            case CNTXT_MENU_ADD_FTP:
            case CNTXT_MENU_EDIT_FTP:
                final int menuid = item;
                AlertDialog.Builder builderFTP1 = new AlertDialog.Builder(this);
                builderFTP1.setTitle(getResources().getString(R.string.jv_results_add_ftp));
                View linearlayout = getLayoutInflater().inflate(R.layout.ll_ftp_server_dialog, null);
                if (linearlayout != null) {
                    builderFTP1.setView(linearlayout);

                    final EditText addServerFTP = (EditText) linearlayout.findViewById(R.id.et_serverNameFtp);
                    final EditText addPortFTP = (EditText) linearlayout.findViewById(R.id.et_portFtp);
                    final EditText addPathFTP = (EditText) linearlayout.findViewById(R.id.et_pathFtp);
                    final EditText addLoginFTP = (EditText) linearlayout.findViewById(R.id.et_loginFtp);
                    final EditText addPassFTP = (EditText) linearlayout.findViewById(R.id.et_passFtp);

                    if (N2DeviceInfo.EINK_NOOK) {
                        TextView tvtemt = (TextView) linearlayout.findViewById(R.id.tv_serverNameFtp);
                        tvtemt.setTextColor(getResources().getColor(R.color.backgorund_task_fg));
                        tvtemt = (TextView) linearlayout.findViewById(R.id.tv_portFtp);
                        tvtemt.setTextColor(getResources().getColor(R.color.backgorund_task_fg));
                        tvtemt = (TextView) linearlayout.findViewById(R.id.tv_pathFtp);
                        tvtemt.setTextColor(getResources().getColor(R.color.backgorund_task_fg));
                        tvtemt = (TextView) linearlayout.findViewById(R.id.tv_loginFtp);
                        tvtemt.setTextColor(getResources().getColor(R.color.backgorund_task_fg));
                        tvtemt = (TextView) linearlayout.findViewById(R.id.tv_passFtp);
                        tvtemt.setTextColor(getResources().getColor(R.color.backgorund_task_fg));
                    }

                    if (menuid == CNTXT_MENU_ADD_FTP) {
                        addPortFTP.setText("21");
                        addPathFTP.setText("/");
                        addLoginFTP.setText("anonymous");
                        addPassFTP.setText("anonymous");
                    }else{
                        HashMap<String, String> temp_item = getdbFTP(pos);
                        addServerFTP.setText(temp_item.get("SERVER"));
                        addPortFTP.setText(temp_item.get("PORT"));
                        addPathFTP.setText(temp_item.get("PATH"));
                        addLoginFTP.setText(temp_item.get("LOGIN"));
                        addPassFTP.setText(temp_item.get("PASSWORD"));
                    }

                    // "Yes"
                    builderFTP1.setPositiveButton(getResources().getString(R.string.app_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // добавление в базу данных
                                    if (menuid == CNTXT_MENU_ADD_FTP) {
                                        addDbFTP(String.valueOf(addServerFTP.getText()),
                                                Integer.valueOf(String.valueOf(addPortFTP.getText())),
                                                String.valueOf(addPathFTP.getText()),
                                                String.valueOf(addLoginFTP.getText()),
                                                String.valueOf(addPassFTP.getText()));
                                    }else{
                                        // обновление базы
                                        updateDbFTP(pos + 1, String.valueOf(addServerFTP.getText()),
                                                Integer.valueOf(String.valueOf(addPortFTP.getText())),
                                                String.valueOf(addPathFTP.getText()),
                                                String.valueOf(addLoginFTP.getText()),
                                                String.valueOf(addPassFTP.getText()));
                                    }
                                    createItemsArray();
                                }
                            });
                    // "No"
                }
                builderFTP1.setNegativeButton(
                        getResources().getString(R.string.app_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.dismiss();
                            }
                        });
                builderFTP1.show();
                break;
            case CNTXT_MENU_DEL_FTP:
                // удаление из базы
                delDbFTP(pos);
                createItemsArray();
                break;
            case CNTXT_MENU_CLEAN_FTP:
                // очистка базы
                db = dbHelper.getReadableDatabase();
                if(db != null) {
                    dbHelper.resetDb(db);
                    createItemsArray();
                    db.close();
                }
                break;
        }
        return true;
    }

	@Override
	protected void onResume() {
        EinkScreen.setEinkController(prefs);
		super.onResume();
		app.generalOnResume(TAG);
	}

	private String getEbookName(String dir, String file) {
		EBook eBook;
		if ((!file.endsWith("fb2")) && (!file.endsWith("fb2.zip")) &&(!file.endsWith("epub")))
			return file;
        eBook = app.dataBase.getBookByFileName(dir + "/" + file);
		if (eBook.isOk) {
			String output = prefs.getString("bookTitleFormat", "[%a. ]%t");
			if (eBook.authors.size() > 0) {
				String author = "";
				if (eBook.authors.get(0).firstName != null)
					author += eBook.authors.get(0).firstName;
				if (eBook.authors.get(0).lastName != null)
					author += " " + eBook.authors.get(0).lastName;
					output = output.replace("%a", author);
			}
			if (eBook.title != null)
				output = output.replace("%t", eBook.title);
			if (eBook.sequenceName != null)
				output = output.replace("%s", eBook.sequenceName);
			else
				output = output.replace("%s", "");
			if (eBook.sequenceNumber != null)
				output = output.replace("%n", eBook.sequenceNumber);
			else
				output = output.replace("%n", "");
			output = purgeBracketsPattern.matcher(output).replaceAll("");
			output = output.replace("[", "");
			output = output.replace("]", "");
			return output;
		} else
			return file;
	}
    // =====  OPDS
    private void dbOPDS(){
        db = dbHelper.getReadableDatabase();
        if(db == null){
            return;
        }
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query("OPDS", null, null, null, null, null, null);
        Button rt = (Button) findViewById(R.id.results_title);
        if (total == -1){
            rt.setText(title + " (" + c.getCount() + ")");
        }else{
            rt.setText(title + " (" + c.getCount() + "/"+ total + ")");
        }
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            int titleColIndex = c.getColumnIndex("TITLE");
            int ureColIndex = c.getColumnIndex("URE");

            do {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put("dname", c.getString(ureColIndex));
                item.put("fname", c.getString(titleColIndex));
                item.put("sname", c.getString(titleColIndex));
                item.put("nameIcon", "ci_opds_catalog"); // тип - файл
                itemsArray.add(item);
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }
        c.close();
        db.close();
    }
    private void addDbOPDS(String titleColIndex, String ureColIndex, String en_passColIndex, String loginColIndex, String passwordColIndex){
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        // подключаемся к БД
        db = dbHelper.getWritableDatabase();
        if(db == null){
            return;
        }
        cv.put("TITLE", titleColIndex);
        cv.put("URE", ureColIndex);
        cv.put("EN_PASS", en_passColIndex);
        if(en_passColIndex.equals("true")){
            cv.put("LOGIN", loginColIndex);
            cv.put("PASSWORD", passwordColIndex);
        }else{
            cv.put("LOGIN", " ");
            cv.put("PASSWORD", " ");
        }

        // вставляем запись и получаем ее ID
        db.insert("OPDS", null, cv);

        db.close();
    }
    private void updateDbOPDS(String oldtitleColIndex, String titleColIndex, String ureColIndex, String en_passColIndex, String loginColIndex, String passwordColIndex){
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        // подключаемся к БД
        db = dbHelper.getWritableDatabase();
        if(db == null){
            return;
        }
        cv.put("TITLE", titleColIndex);
        cv.put("URE", ureColIndex);
        cv.put("EN_PASS", String.valueOf(en_passColIndex));
        if(en_passColIndex.equals("true")){
            cv.put("LOGIN", loginColIndex);
            cv.put("PASSWORD", passwordColIndex);
        }else{
            cv.put("LOGIN", "");
            cv.put("PASSWORD", "");
        }

        // вставляем запись и получаем ее ID
        db.update("OPDS", cv, "TITLE = ?", new String[]{oldtitleColIndex});

        db.close();
    }
    private void delDbOPDS(String titleColIndex){
        // подключаемся к БД
        db = dbHelper.getWritableDatabase();
        if(db == null){
            return;
        }
        db.delete("OPDS", "TITLE = ?" ,new String[]{titleColIndex});
        db.close();
    }
    private HashMap<String, String> getdbOPDS(int id){
        db = dbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        HashMap<String, String> item = new HashMap<String, String>();
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query("OPDS", null, null, null, null, null, null);
        if (c.moveToPosition(id)) {
            // определяем номера столбцов по имени в выборке
            int titleColIndex = c.getColumnIndex("TITLE");
            int ureColIndex = c.getColumnIndex("URE");
            int enpassColIndex = c.getColumnIndex("EN_PASS");
            int loginColIndex = c.getColumnIndex("LOGIN");
            int passColIndex = c.getColumnIndex("PASSWORD");
            int enproxyColIndex = c.getColumnIndex("EN_PROXY");
            int typeproxyColIndex = c.getColumnIndex("TYPE_PROXY");
            int proxynameColIndex = c.getColumnIndex("PROXY_NAME");
            int proxyportColIndex = c.getColumnIndex("PROXY_PORT");

            item.put("PATH", c.getString(ureColIndex));
            item.put("SERVER", c.getString(titleColIndex));
            item.put("EN_PASS", c.getString(enpassColIndex));
            item.put("LOGIN", c.getString(loginColIndex));
            item.put("PASSWORD", c.getString(passColIndex));
            item.put("EN_PROXY", c.getString(enproxyColIndex));
            item.put("TYPE_PROXY", c.getString(typeproxyColIndex));
            item.put("PROXY_NAME", c.getString(proxynameColIndex));
            item.put("PROXY_PORT", String.valueOf(c.getInt(proxyportColIndex)));
        }
        c.close();
        db.close();
        return item;
    }
    // =====  FTP
    private void addDbFTP(String serverColIndex, int port, String pathColIndex, String loginColIndex, String passwordColIndex){
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        // подключаемся к БД
        db = dbHelper.getWritableDatabase();
        if(db == null){
            return;
        }
        cv.put("SERVER", serverColIndex);
        cv.put("PORT", port);
        cv.put("PATH", pathColIndex);
        if(loginColIndex == null){
            cv.put("LOGIN", "anonymous");
            cv.put("PASSWORD", "anonymous");
        }else{
            cv.put("LOGIN", loginColIndex);
            cv.put("PASSWORD", passwordColIndex);
        }

        // вставляем запись и получаем ее ID
        db.insert("FTP", null, cv);

        db.close();
    }
    private void dbFTP(){
        db = dbHelper.getReadableDatabase();
        if(db == null){
            return;
        }
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query("FTP", null, null, null, null, null, null);
        Button rt = (Button) findViewById(R.id.results_title);
        if (total == -1){
            rt.setText(title + " (" + c.getCount() + ")");
        }else{
            rt.setText(title + " (" + c.getCount() + "/"+ total + ")");
        }
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            int titleColIndex = c.getColumnIndex("SERVER");
            int ureColIndex = c.getColumnIndex("PATH");
            int portColIndex = c.getColumnIndex("PORT");
            int loginColIndex = c.getColumnIndex("LOGIN");
            int passColIndex = c.getColumnIndex("PASSWORD");

            do {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put("dname", c.getString(ureColIndex));
                item.put("fname", c.getString(titleColIndex));
                item.put("sname", c.getString(titleColIndex));
                item.put("port", c.getString(portColIndex));
                item.put("login", c.getString(loginColIndex));
                item.put("password", c.getString(passColIndex));
                item.put("nameIcon", "ci_ftp_catalog"); // тип - файл
                itemsArray.add(item);
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }
        c.close();
        db.close();
    }
    private void delDbFTP(int IDFtp){
        // подключаемся к БД
        db = dbHelper.getWritableDatabase();
        if(db == null){
            return;
        }
        db.delete("FTP", "ID = ?" ,new String[]{String.valueOf(IDFtp)});
        db.close();
    }
    private void updateDbFTP(int id, String serverColIndex, int port, String pathColIndex, String loginColIndex, String passwordColIndex){
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        // подключаемся к БД
        db = dbHelper.getWritableDatabase();
        if(db == null){
            return;
        }
        cv.put("SERVER", serverColIndex);
        cv.put("PORT", port);
        cv.put("PATH", pathColIndex);
        if(loginColIndex != null && loginColIndex.length() >0){
            cv.put("LOGIN", loginColIndex);
            cv.put("PASSWORD", passwordColIndex);
        }else{
            cv.put("LOGIN", "anonymous");
            cv.put("PASSWORD", "anonymous");
        }

        // вставляем запись и получаем ее ID
        db.update("FTP", cv, "ID = ?", new String[]{String.valueOf(id)});

        db.close();
    }
    private HashMap<String, String> getdbFTP(int id){
        db = dbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        HashMap<String, String> item = new HashMap<String, String>();
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query("FTP", null, null, null, null, null, null);
        if (c.moveToPosition(id)) {
            // определяем номера столбцов по имени в выборке
            int titleColIndex = c.getColumnIndex("SERVER");
            int ureColIndex = c.getColumnIndex("PATH");
            int portColIndex = c.getColumnIndex("PORT");
            int loginColIndex = c.getColumnIndex("LOGIN");
            int passColIndex = c.getColumnIndex("PASSWORD");

            item.put("PATH", c.getString(ureColIndex));
            item.put("SERVER", c.getString(titleColIndex));
            item.put("PORT", c.getString(portColIndex));
            item.put("LOGIN", c.getString(loginColIndex));
            item.put("PASSWORD", c.getString(passColIndex));
        }
        c.close();
        db.close();
        return item;
    }
}
