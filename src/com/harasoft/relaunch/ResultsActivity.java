package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
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
	ReLaunchApp app;
	HashMap<String, Drawable> icons;
	String listName;
    static String listNameSecond;
	String title;
	Boolean rereadOnStart = true;
	SharedPreferences prefs;
	FLSimpleAdapter adapter;
    Integer currentColsNum = -1;
	List<HashMap<String, String>> itemsArray = new ArrayList<HashMap<String, String>>();
	Integer currentPosition = -1;
	boolean addSView = true;
	boolean oldHome;
	Pattern purgeBracketsPattern;
    ArrayList<imageIcon> arrIcon = new ArrayList<imageIcon>();
    DBHelper dbHelper;
    public static SQLiteDatabase db;
    int total;
    // переменные из настроек===============================================
    static boolean hideKnownExts = false;
    static int firstLineFontSizePx = 20;
    static  int secondLineFontSizePx = 16;
    static boolean showNew = false;
    static boolean hideKnownDirs = false;
    static int firstLineIconSizePx = 48;
    static boolean filterResults = false;
    static boolean showBookTitles = false;

    static LayoutInflater vi;


    static class ViewHolder {
		TextView tv1;
		TextView tv2;
		LinearLayout tvHolder;
		ImageView iv;
	}

	private Bitmap scaleDrawableById(int id, int size) {
		return Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(getResources(), id), size, size,true);
	}

	private Bitmap scaleDrawable(Drawable d, int size) {
		return Bitmap.createScaledBitmap(((BitmapDrawable) d).getBitmap(),size, size, true);
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
				//LayoutInflater
				v = vi.inflate(R.layout.results_item, null);
				holder = new ViewHolder();
                if (v == null) {
                    return null;
                }
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
				String sdname = item.get("dname");
				String fullName = dname + "/" + fname;
				boolean setBold = false;

				// setup icon
                if (firstLineIconSizePx == 0) { // если отключены картинки
                    iv.setVisibility(View.GONE); // скрываем поле с ними
                }else {
                    String temp_nameIcon = item.get("nameIcon");
                    for (imageIcon anArrIcon : arrIcon) {
                        if (anArrIcon.nameIcon.equals(temp_nameIcon)) {
                            iv.setImageBitmap(anArrIcon.icon);
                            break;
                        }
                    }
                }

				// special cases in dname & fname
				// dname empty - in root dir
				// fname empty with dname empty - root dir as is
				if (dname.equals("")) {
					//dname = "/";
					sdname = "/";
					if (fname.equals("")) {
						//fname = "/";
						sname = "/";
						//dname = "";
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
            if (currentColsNum != 1) {
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

        Integer colsNum = Integer.parseInt(prefs.getString(listNameSecond, "-1"));
		// override auto (not working fine in adnroid)
		if (colsNum == -1) {
			colsNum = app.getAutoColsNum(itemsArray, "fname", ReLaunch.columnsAlgIntensity);
		}
		currentColsNum = colsNum;
		final GridView gv = (GridView) findViewById(R.id.results_list);
		gv.setNumColumns(colsNum);
		adapter.notifyDataSetChanged();
		if (currentPosition != -1)
			gv.setSelection(currentPosition);
	}

	private void start(Intent i) {
		if (i != null)
			try {
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(
						ResultsActivity.this,
						getResources().getString(
								R.string.jv_results_activity_not_found),
						Toast.LENGTH_LONG).show();
			}
	}

	private void createItemsArray() {
		itemsArray = new ArrayList<HashMap<String, String>>();
        // вычищаем иконки из массива. оставляем только стандартные
        for(int i = 5, j = arrIcon.size(); i< j; j--){
            arrIcon.remove(j-1);
        }
        if(listName.equals("opdslist")){
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
                        int ind = n[0].lastIndexOf('/');
                        if (ind == -1) {
                            item.put("fname", "");
                            item.put("sname", "");
                        } else {
                            String sname = n[0].substring(ind + 1);
                            item.put("fname", sname);
                            // clean start prefixes, if need
                            if (hideKnownDirs) {
                                for (int i = 0, j = ReLaunch.startDir.length; i < j; i++) {
                                    if (sname.startsWith(ReLaunch.startDir[i])) {
                                        sname = "~" + sname.substring(ReLaunch.startDir[i].length());
                                    }
                                }
                            }
                            item.put("sname", sname);
                            item.put("dname", n[0].substring(0, ind));
                        }
                        //item.put("type", "dir");

                        // получение иконки=========
                        if (firstLineIconSizePx != 0) {
                            item.put("nameIcon", "dir_ok");
                        }
                        //=====================
                    } else {
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

                        //item.put("type", "file");
                        // получение иконки==============================
                        if (firstLineIconSizePx != 0) {
                            String nameIcon;

                            Drawable d = app.specialIcon(n[1], false);  // получаем иконку
                            if (d != null) { // если удалось
                                imageIcon temp_icon = new imageIcon();
                                temp_icon.nameIcon = n[1];
                                temp_icon.icon = scaleDrawable(d, firstLineIconSizePx);
                                arrIcon.add(temp_icon);
                                nameIcon = n[1];
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
                                } else { // во всех остальных случаях

                                    if (app.getIcons().containsKey(rdrName)) { // у программы есть иконка?
                                        imageIcon temp_icon = new imageIcon();
                                        temp_icon.nameIcon = rdrName;
                                        temp_icon.icon = scaleDrawable(app.getIcons().get(rdrName), firstLineIconSizePx);
                                        arrIcon.add(temp_icon);
                                        nameIcon = rdrName;
                                    } else {
                                        nameIcon = "file_notok";
                                    }
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
        // ------ загружаем стандартные иконки для отображения в менеджере ----------
        loadStandartIcons();

        EinkScreen.setEinkController(prefs);

		app = ((ReLaunchApp) getApplicationContext());
        if (app != null) {
            app.setFullScreenIfNecessary(this);
        }
        setContentView(R.layout.results_layout);
        vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		icons = app.getIcons();

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
            listNameSecond = "opdslist";
            results_icon.setImageDrawable(getResources().getDrawable(R.drawable.ci_books));
            dbHelper = new DBHelper(this);
            db = dbHelper.getReadableDatabase();
            Cursor c = db.query("OPDS1", null, null, null, null, null, null);

            if(c.getCount()== 0){
                addDbOPDS("Либрусек", "http://lib.rus.ec/opds", false, null, null);
            }
            c.close();
            db.close();
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
		final GridView gv = (GridView) findViewById(R.id.results_list);
		gv.setHorizontalSpacing(0);
		Button rt = (Button) findViewById(R.id.results_title);
		rt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final String[] columns = getResources().getStringArray(R.array.output_columns_names);
				final CharSequence[] columnsmode = new CharSequence[columns.length];
                System.arraycopy(columns, 0, columnsmode, 0, columns.length);
				Integer checked = Integer.parseInt(prefs.getString(listNameSecond, "-1"));
                if (checked == -1){
                    checked = 0;
                }
				// get checked
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ResultsActivity.this);
				// "Select application"
				builder.setTitle(getResources().getString(R.string.jv_relaunch_select_columns));
				builder.setSingleChoiceItems(columnsmode, checked,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int i) {
                                SharedPreferences.Editor editor = prefs.edit();
								if (i == 0) {
                                    editor.putString(listNameSecond,"-1");
								} else {
                                    editor.putString(listNameSecond,Integer.toString(i));
								}
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
		gv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				HashMap<String, String> item = itemsArray.get(position);
				String fullName = item.get("dname") + "/" + item.get("fname");

				currentPosition = parent.getFirstVisiblePosition();
                if(listName.equals("opdslist")){
                    Intent intent = new Intent(ResultsActivity.this, OPDSActivity.class);
                    intent.putExtra("opdscat", item.get("dname"));
                    intent.putExtra("login", item.get("login"));
                    intent.putExtra("password", item.get("password"));
                    startActivity(intent);
                }else if (item.get("type").equals("dir")) {
					Intent intent = new Intent(ResultsActivity.this,ReLaunch.class);
					intent.putExtra("start_dir", fullName);
					intent.putExtra("home", ReLaunch.useHome);
					intent.putExtra("home1", ReLaunch.useHome1);
					oldHome = ReLaunch.useHome;
					startActivityForResult(intent, ReLaunch.DIR_ACT);
				} else {
					String fileName = item.get("fname");
					if (!app.specialAction(ResultsActivity.this, fullName)) {
						if (app.readerName(fileName).equals("Nope"))
							app.defaultAction(ResultsActivity.this, fullName);
						else {
							// Launch reader
							if (app.askIfAmbiguous) {
								List<String> rdrs = app.readerNames(item.get("fname"));
								if (rdrs.size() < 1)
									return;
								else if (rdrs.size() == 1)
									start(app.launchReader(rdrs.get(0),fullName));
								else {
									final CharSequence[] applications = rdrs.toArray(new CharSequence[rdrs.size()]);
									CharSequence[] happlications = app.getApps().toArray(
													new CharSequence[app.getApps().size()]);
									for (int j = 0; j < happlications.length; j++) {
										String happ = (String) happlications[j];
										String[] happp = happ.split("\\%");
										happlications[j] = happp[2];
									}
									final String rdr1 = fullName;
									AlertDialog.Builder builder = new AlertDialog.Builder(ResultsActivity.this);
									// "Select application"
									builder.setTitle(getResources().getString(R.string.jv_results_select_application));
									builder.setSingleChoiceItems(
											happlications,-1,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int i) {
													start(app.launchReader((String) applications[i],rdr1));
													dialog.dismiss();
												}
											});
									AlertDialog alert = builder.create();
									alert.show();
								}
							} else
								start(app.launchReader(app.readerName(fileName), fullName));
						}
					}
					// close in needed
					if (prefs.getBoolean("returnFileToMain", false))
						finish();
				}
			}
		});

		final Button upScroll = (Button) findViewById(R.id.upscroll_btn);
		if (!ReLaunch.disableScrollJump) {
			upScroll.setText(app.scrollStep + "%");
		} else {
			upScroll.setText(getResources()
					.getString(R.string.jv_relaunch_prev));
		}
		class upScrlSimpleOnGestureListener extends SimpleOnGestureListener {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (N2DeviceInfo.EINK_NOOK) { // nook
					MotionEvent ev;
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_DOWN, 200, 100, 0);
                    if (ev != null) {
                        gv.dispatchTouchEvent(ev);
                    }
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis() + 100,
							MotionEvent.ACTION_MOVE, 200, 200, 0);
                    if (ev != null) {
                        gv.dispatchTouchEvent(ev);
                    }
                    SystemClock.sleep(100);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
							200, 200, 0);
                    if (ev != null) {
                        gv.dispatchTouchEvent(ev);
                    }
                } else { // other devices
					int first = gv.getFirstVisiblePosition();
					int visible = gv.getLastVisiblePosition()
							- gv.getFirstVisiblePosition() + 1;
					int total = itemsArray.size();
					first -= visible;
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

		class RepeatedDownScroll {
			public void doIt(int first, int target, int shift) {
				final GridView gv = (GridView) findViewById(R.id.results_list);
				int total = gv.getCount();
				int last = gv.getLastVisiblePosition();
				if (total == last + 1)
					return;
				final int ftarget = target + shift;
				gv.clearFocus();
				gv.post(new Runnable() {
					public void run() {
						gv.setSelection(ftarget);
					}
				});
				final int ffirst = first;
				final int fshift = shift;
				gv.postDelayed(new Runnable() {
					public void run() {
						int nfirst = gv.getFirstVisiblePosition();
						if (nfirst == ffirst) {
							RepeatedDownScroll ds = new RepeatedDownScroll();
							ds.doIt(ffirst, ftarget, fshift + 1);
						}
					}
				}, 150);
			}
		}

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
				if (N2DeviceInfo.EINK_NOOK) { // nook special
					MotionEvent ev;
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_DOWN, 200, 200, 0);
                    if (ev != null) {
                        gv.dispatchTouchEvent(ev);
                    }
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis() + 100,
							MotionEvent.ACTION_MOVE, 200, 100, 0);
                    if (ev != null) {
                        gv.dispatchTouchEvent(ev);
                    }
                    SystemClock.sleep(100);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
							200, 100, 0);
                    if (ev != null) {
                        gv.dispatchTouchEvent(ev);
                    }
                } else { // other devices
					int first = gv.getFirstVisiblePosition();
					int total = itemsArray.size();
					int last = gv.getLastVisiblePosition();
					if (total == last + 1)
						return true;
					int target = last + 1;
					if (target > (total - 1))
						target = total - 1;
					RepeatedDownScroll ds = new RepeatedDownScroll();
					ds.doIt(first, target, 0);
				}
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
					RepeatedDownScroll ds = new RepeatedDownScroll();
					ds.doIt(first, target, 0);
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
						RepeatedDownScroll ds = new RepeatedDownScroll();
						ds.doIt(first, target, 0);
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
		ScreenOrientation.set(this, prefs);
	}

	@Override
	protected void onStart() {
		if (rereadOnStart)
			createItemsArray();
		redrawList();
		super.onStart();
	}
    @Override
    protected void onStop() {
        if (listName.equals("opdslist")) {
            dbHelper.close();
        }
        super.onStop();
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
			ReLaunch.useHome = oldHome;
			break;
		default:
			//return;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		int pos = info.position;
		HashMap<String, String> i = itemsArray.get(pos);
		final String dr = i.get("dname");
		final String fn = i.get("fname");
		String fullName = dr + "/" + fn;

		if (listName.equals("homeList")) {
			return;
		} else if (listNameSecond.equals("opdslist")) {
            // "Add opds catalog"
            menu.add(Menu.NONE, CNTXT_MENU_ADD_OPDS, Menu.NONE, getResources().getString(R.string.jv_results_add_opds));
            // "Remove from list"
            menu.add(Menu.NONE, CNTXT_MENU_DEL_OPDS, Menu.NONE, getResources().getString(R.string.jv_relaunch_delete));
            // "Rename"
            menu.add(Menu.NONE, CNTXT_MENU_EDIT_OPDS, Menu.NONE, getResources().getString(R.string.jv_relaunch_edit));
            // "Rename"
            menu.add(Menu.NONE, CNTXT_MENU_CLEAN_OPDS, Menu.NONE, getResources().getString(R.string.jv_relaunch_clean));

        } else if (i.get("type").equals("dir")) {
			if (pos > 0)
				// "Move one position up"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE,getResources().getString(R.string.jv_results_move_up));
			if (pos < (itemsArray.size() - 1))
				// "Move one position down"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE,getResources().getString(R.string.jv_results_move_down));
			// "Remove from favorites"
			menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, getResources()
					.getString(R.string.jv_results_remove));
			// "Delete directory"
			if (prefs.getBoolean("useFileManagerFunctions", true))
				menu.add(Menu.NONE, CNTXT_MENU_RMDIR, Menu.NONE, getResources()
						.getString(R.string.jv_results_delete_dir));
		} else if (listName.equals("favorites")) {
			if (pos > 0)
				// "Move one position up"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE,getResources().getString(R.string.jv_results_move_up));
			if (pos < (itemsArray.size() - 1))
				// "Move one position down"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE,getResources().getString(R.string.jv_results_move_down));
			// "Remove from favorites"
			menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, getResources()
					.getString(R.string.jv_results_remove));
			// "Delete file"
			if (prefs.getBoolean("useFileManagerFunctions", true))
				menu.add(
						Menu.NONE,
						CNTXT_MENU_RMFILE,
						Menu.NONE,
						getResources().getString(
								R.string.jv_results_delete_file));
		} else if (listName.equals("lastOpened")) {
			if (app.history.containsKey(fullName)) {
				if (app.history.get(fullName) == app.READING)
					// "Mark as read"
					menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE,getResources().getString(R.string.jv_results_mark));
				else if (app.history.get(fullName) == app.FINISHED)
					// "Remove \"read\" mark"
					menu.add(Menu.NONE, CNTXT_MENU_MARK_READING, Menu.NONE,getResources()
									.getString(R.string.jv_results_unmark));
				// "Forget all marks"
				menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE,getResources()
								.getString(R.string.jv_results_unmark_all));
			} else
				// "Mark as read"
				menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE,getResources().getString(R.string.jv_results_mark));
			// "Delete file"
			if (prefs.getBoolean("useFileManagerFunctions", true))
				menu.add(
						Menu.NONE,
						CNTXT_MENU_RMFILE,
						Menu.NONE,
						getResources().getString(
								R.string.jv_results_delete_file));

            // "Open dir"
            menu.add(Menu.NONE, CNTXT_MENU_OPEN_DIR, Menu.NONE, getResources().getString(R.string.jv_open_dir));
		} else if (listName.equals("searchResults")) {
			if (pos > 0){
				// "Move one position up"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE,getResources().getString(R.string.jv_results_move_up));
            }
			if (pos < (itemsArray.size() - 1))
				// "Move one position down"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE,getResources().getString(R.string.jv_results_move_down));
			if (app.history.containsKey(fullName)) {
				if (app.history.get(fullName) == app.READING)
					// "Mark as read"
					menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE,getResources().getString(R.string.jv_results_mark));
				else if (app.history.get(fullName) == app.FINISHED)
					// "Remove \"read\" mark"
					menu.add(Menu.NONE, CNTXT_MENU_MARK_READING, Menu.NONE,getResources()
									.getString(R.string.jv_results_unmark));
				// "Forget all marks"
				menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE,getResources()
								.getString(R.string.jv_results_unmark_all));
			} else
				// "Mark as read"
				menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE,getResources().getString(R.string.jv_results_mark));
			// "Delete file"
			if (prefs.getBoolean("useFileManagerFunctions", true))
				menu.add(
						Menu.NONE,
						CNTXT_MENU_RMFILE,
						Menu.NONE,
						getResources().getString(
								R.string.jv_results_delete_file));

		}
        // "Cancel"
        menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources().getString(R.string.app_cancel));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == CNTXT_MENU_CANCEL)
			return true;

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if(info == null){
            return false;
        }
		final int pos = info.position;
		HashMap<String, String> i = itemsArray.get(pos);
		final String dname = i.get("dname");
		final String fname = i.get("fname");
		String fullName = dname + "/" + fname;

		switch (item.getItemId()) {
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
			if (i.get("type").equals("dir")) {
				app.removeFromList("favorites", fullName, app.DIR_TAG);
				app.saveList("favorites");
			} else {
				app.removeFromList("favorites", dname, fname);
				app.saveList("favorites");
			}
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
				builder.setPositiveButton(
						getResources().getString(R.string.app_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								if (app.removeFile(dname, fname)) {
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
			} else if (app.removeFile(dname, fname)) {
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
					builder.setPositiveButton(
							getResources().getString(R.string.app_yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
									if (app.removeFile(dname, fname)) {
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
				} else if (app.removeFile(dname, fname)) {
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
					builder.setPositiveButton(
							getResources().getString(R.string.app_yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
									if (app.removeDirectory(dname, fname)) {
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
				} else if (app.removeDirectory(dname, fname)) {
					itemsArray.remove(pos);
					redrawList();
				}
			}
			break;
            case CNTXT_MENU_OPEN_DIR:
                Intent intent = new Intent(ResultsActivity.this,ReLaunch.class);
                intent.putExtra("start_dir", dname);
                intent.putExtra("home", ReLaunch.useHome);
                intent.putExtra("home1", ReLaunch.useHome1);
                oldHome = ReLaunch.useHome;
                startActivityForResult(intent, ReLaunch.DIR_ACT);
            break;
            case CNTXT_MENU_ADD_OPDS:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.jv_results_add_opds));

                // редактируемые поля
                final EditText inputName = new EditText(this); // Имя. Уникальное
                final EditText inputAddress = new EditText(this); // Адрес
                final EditText inputLogin = new EditText(this); // Имя пользователя для доступа
                final EditText inputPassword = new EditText(this); // Пароль
                final CheckBox checkBox = new CheckBox(this);
                checkBox.setText(getResources().getString(R.string.jv_results_checkbox_opds));

                // Заголовки полей
                TextView tAddress = new TextView(this);
                TextView tName = new TextView(this);
                final TextView tLogin = new TextView(this);
                final TextView tPassword = new TextView(this);
                // Заполняем заголовки
                tAddress.setText(getResources().getString(R.string.jv_results_addres_opds));
                tName.setText(getResources().getString(R.string.jv_results_name_opds));
                tLogin.setText(getResources().getString(R.string.jv_results_login_opds));
                tPassword.setText(getResources().getString(R.string.jv_results_pass_opds));
                // предварительно отключаем поля
                tLogin.setEnabled(false);
                tPassword.setEnabled(false);
                inputLogin.setEnabled(false);
                inputPassword.setEnabled(false);
                // при клике на чекбоксе изменяем доступность полей
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                        if(checkBox.isChecked()){
                            tLogin.setEnabled(true);
                            tPassword.setEnabled(true);
                            inputLogin.setEnabled(true);
                            inputPassword.setEnabled(true);
                        }else{
                            tLogin.setEnabled(false);
                            tPassword.setEnabled(false);
                            inputLogin.setEnabled(false);
                            inputPassword.setEnabled(false);
                        }
                    }
                });
                // начинаем заполнять форму
                LinearLayout ll=new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);// вертивальное расположение элементов
                // имя
                ll.addView(tName);
                ll.addView(inputName);
                // адрес
                ll.addView(tAddress);
                ll.addView(inputAddress);
                // чекбокс
                ll.addView(checkBox);
                // имя пользователя
                ll.addView(tLogin);

                ll.addView(inputLogin);
                // пароль
                ll.addView(tPassword);
                ll.addView(inputPassword);
                builder.setView(ll);

                // "Yes"
                builder.setPositiveButton(getResources().getString(R.string.app_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // добавление в базу данных
                                addDbOPDS(String.valueOf(inputName.getText()),
                                        String.valueOf(inputAddress.getText()),
                                        checkBox.isChecked(),
                                        String.valueOf(inputLogin.getText()),
                                        String.valueOf(inputPassword.getText()));
                                createItemsArray();
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
                break;
            case CNTXT_MENU_EDIT_OPDS:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle(getResources().getString(R.string.jv_results_add_opds));

                // редактируемые поля
                final EditText inputName2 = new EditText(this); // Имя. Уникальное
                final EditText inputAddress2 = new EditText(this); // Адрес
                final EditText inputLogin2 = new EditText(this); // Имя пользователя для доступа
                final EditText inputPassword2 = new EditText(this); // Пароль
                final CheckBox checkBox2 = new CheckBox(this);
                checkBox2.setText(getResources().getString(R.string.jv_results_checkbox_opds));
                // Заголовки полей
                TextView tAddress2 = new TextView(this);
                TextView tName2 = new TextView(this);
                final TextView tLogin2 = new TextView(this);
                final TextView tPassword2 = new TextView(this);
                // Заполняем заголовки
                tAddress2.setText(getResources().getString(R.string.jv_results_addres_opds));
                tName2.setText(getResources().getString(R.string.jv_results_name_opds));
                tLogin2.setText(getResources().getString(R.string.jv_results_login_opds));
                tPassword2.setText(getResources().getString(R.string.jv_results_pass_opds));
                // заполняем поля из базы
                inputAddress2.setText(dname);
                inputName2.setText(fname);
                if(i.get("check").equals("1")){
                    checkBox2.setChecked(true);
                    inputLogin2.setText(i.get("login"));
                    inputPassword2.setText(i.get("password"));
                    tLogin2.setEnabled(true);
                    tPassword2.setEnabled(true);
                    inputLogin2.setEnabled(true);
                    inputPassword2.setEnabled(true);
                }else{
                    checkBox2.setChecked(false);
                    tLogin2.setEnabled(false);
                    tPassword2.setEnabled(false);
                    inputLogin2.setEnabled(false);
                    inputPassword2.setEnabled(false);
                }

                // при клике на чекбоксе изменяем доступность полей
                checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                        if(checkBox2.isChecked()){
                            tLogin2.setEnabled(true);
                            tPassword2.setEnabled(true);
                            inputLogin2.setEnabled(true);
                            inputPassword2.setEnabled(true);
                        }else{
                            tLogin2.setEnabled(false);
                            tPassword2.setEnabled(false);
                            inputLogin2.setText("");
                            inputLogin2.setEnabled(false);
                            inputPassword2.setText("");
                            inputPassword2.setEnabled(false);
                        }
                    }
                });
                // начинаем заполнять форму
                LinearLayout ll2=new LinearLayout(this);
                ll2.setOrientation(LinearLayout.VERTICAL);// вертивальное расположение элементов
                // имя
                ll2.addView(tName2);
                ll2.addView(inputName2);
                // адрес
                ll2.addView(tAddress2);
                ll2.addView(inputAddress2);
                // чекбокс
                ll2.addView(checkBox2);
                // имя пользователя
                ll2.addView(tLogin2);

                ll2.addView(inputLogin2);
                // пароль
                ll2.addView(tPassword2);
                ll2.addView(inputPassword2);
                // отрисовываем
                builder2.setView(ll2);

                // "Yes"
                builder2.setPositiveButton(getResources().getString(R.string.app_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // обновление базы
                                updateDbOPDS(fname,
                                        String.valueOf(inputName2.getText()),
                                        String.valueOf(inputAddress2.getText()),
                                        checkBox2.isChecked(),
                                        String.valueOf(inputLogin2.getText()),
                                        String.valueOf(inputPassword2.getText()));
                                createItemsArray();
                            }
                        });
                // "No"
                builder2.setNegativeButton(
                        getResources().getString(R.string.app_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.dismiss();
                            }
                        });
                builder2.show();
                break;
            case CNTXT_MENU_DEL_OPDS:
                // удаление из базы
                delDbOPDS(fname);
                createItemsArray();
                break;
            case CNTXT_MENU_CLEAN_OPDS:
                // очистка базы
                db = dbHelper.getWritableDatabase();
                db.execSQL("delete from OPDS1");
                db.execSQL("reindex INDEXopds");
                db.close();
                createItemsArray();
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

    private class imageIcon {
        String nameIcon;
        Bitmap icon;
    }

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
        temp_icon.icon = scaleDrawableById(R.drawable.icon, firstLineIconSizePx);
        temp_icon.nameIcon = "icon";
        arrIcon.add(temp_icon);
        temp_icon = new imageIcon();
        temp_icon.nameIcon = "opdslist";
        temp_icon.icon = scaleDrawableById(R.drawable.ci_books, firstLineIconSizePx);
        arrIcon.add(temp_icon);
    }

    private void dbOPDS(){
        db = dbHelper.getReadableDatabase();

        // делаем запрос данных из таблицы , получаем Cursor
        Cursor c = db.query("OPDS1", null, null, null, null, null, null);
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
            int loginColIndex = c.getColumnIndex("LOGIN");
            int passColIndex = c.getColumnIndex("PASSWORD");
            int checkColIndex = c.getColumnIndex("EN_PASS");

            do {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put("dname", c.getString(ureColIndex));
                item.put("fname", c.getString(titleColIndex));
                item.put("sname", c.getString(titleColIndex));
                item.put("login", c.getString(loginColIndex));
                item.put("password", c.getString(passColIndex));
                item.put("check", String.valueOf(c.getInt(checkColIndex)));
                item.put("nameIcon", "opdslist"); // тип - файл
                itemsArray.add(item);
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }
        c.close();
        db.close();
    }

    private void addDbOPDS(String titleColIndex, String ureColIndex, Boolean en_passColIndex, String loginColIndex, String passwordColIndex){
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        // подключаемся к БД
        db = dbHelper.getWritableDatabase();

        cv.put("TITLE", titleColIndex);
        cv.put("URE", ureColIndex);
        cv.put("EN_PASS", en_passColIndex);
        if(en_passColIndex){
            cv.put("LOGIN", loginColIndex);
            cv.put("PASSWORD", passwordColIndex);
        }else{
            cv.put("LOGIN", " ");
            cv.put("PASSWORD", " ");
        }

        // вставляем запись и получаем ее ID
        db.insert("OPDS1", null, cv);

        db.close();
    }
    private void updateDbOPDS(String oldtitleColIndex, String titleColIndex, String ureColIndex, Boolean en_passColIndex, String loginColIndex, String passwordColIndex){
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        // подключаемся к БД
        db = dbHelper.getWritableDatabase();

        cv.put("TITLE", titleColIndex);
        cv.put("URE", ureColIndex);
        cv.put("EN_PASS", en_passColIndex);
        if(en_passColIndex){
            cv.put("LOGIN", loginColIndex);
            cv.put("PASSWORD", passwordColIndex);
        }else{
            cv.put("LOGIN", "");
            cv.put("PASSWORD", "");
        }

        // вставляем запись и получаем ее ID
        db.update("OPDS1", cv, "TITLE = ?", new String[]{oldtitleColIndex});

        db.close();
    }

    private void delDbOPDS(String titleColIndex){
        // подключаемся к БД
        db = dbHelper.getWritableDatabase();
        db.delete("OPDS1", "TITLE = ?" ,new String[]{titleColIndex});

        db.close();
    }

    public class DBHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "opde_database.db";
        private static final int DATABASE_VERSION = 1;
        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("create table if not exists OPDS1 ("
                    + "ID integer primary key autoincrement, "
                    + "TITLE text unique, "
                    + "URE text default '', "
                    + "EN_PASS boolean default 'false', "
                    + "LOGIN text default '', "
                    + "PASSWORD text default '') ");
            db.execSQL("create index if not exists INDEXopds on OPDS1(TITLE)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
