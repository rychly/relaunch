package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.widget.*;
import com.harasoft.relaunch.LocalFile.LocalFile;
import com.harasoft.relaunch.OPDS.OPDSActivity;
import com.harasoft.relaunch.Preferences.PrefsActivity;
import com.harasoft.relaunch.Preferences.TypesActivity;
import com.harasoft.relaunch.Support.BookState;
import com.harasoft.relaunch.Support.RunIntent;
import com.harasoft.relaunch.Support.TypeResource;
import com.harasoft.relaunch.Utils.*;

import java.io.File;
import java.util.*;

public class ResultsActivity extends Activity {
	final String TAG = "Results";
    private final int CNTXT_MENU_RMFAV = 1;
    private final int CNTXT_MENU_RMFILE = 2;
    private final int CNTXT_MENU_CANCEL = 3;
    private final int CNTXT_MENU_MOVEUP = 4;
    private final int CNTXT_MENU_MOVEDOWN = 5;
    private final int CNTXT_MENU_MARK_FINISHED = 6;
    private final int CNTXT_MENU_MARK_READING = 7;
    private final int CNTXT_MENU_MARK_FORGET = 8;
    private final int CNTXT_MENU_RMDIR = 9;
    private final int CNTXT_MENU_OPEN_DIR = 10;
    // OPDS
    private final int CNTXT_MENU_ADD_OPDS = 11;
    private final int CNTXT_MENU_EDIT_OPDS = 12;
    private final int CNTXT_MENU_DEL_OPDS = 13;
    private final int CNTXT_MENU_CLEAN_OPDS = 14;
    // Last openings
    private final int CNTXT_MENU_RMLO = 15;
    // Home Dir
    private final int CNTXT_MENU_RM_HOMEDIR = 16;
    private final int CNTXT_MENU_ADD_HOMEDIR = 17;
    private final int CNTXT_MENU_EDIT_HOMEDIR = 18;

	ReLaunchApp app;
    private String listName;
    private String listNameSecond;
	private String title;
    private SharedPreferences prefs;
	private ResultAdapter resAdapter;
    private Integer currentColsNum = 0;
	private List<HashMap<String, String>> itemsArray = new ArrayList<>();
    private Integer currentPosition = -1;
    private boolean addSView = true;
    private UtilOPDS utilOPDS;
    private UtilFavorites utilFavorites;
    private UtilLastOpen utilLastOpen;
    private UtilHistory utilHistory;
    private UtilIcons utilIcons;
    private UtilHomeDirs utilHomeDirs;
    private int total;


    private LayoutInflater vi;
    private GridView gv;

    static class ViewHolder {
		TextView tv1;
		TextView tv2;
		LinearLayout tvHolder;
		ImageView iv;
	}

    class ResultAdapter extends ArrayAdapter<HashMap<String, String>> {
        private int firstLineFontSizePx;
        private int secondLineFontSizePx;
        ResultAdapter(Context context, int resource, List<HashMap<String, String>> data) {
            super(context, resource, data);
            firstLineFontSizePx = Integer.parseInt(prefs.getString("firstLineFontSizePx", "20"));
            secondLineFontSizePx = Integer.parseInt(prefs.getString("secondLineFontSizePx", "16"));
        }

        @Override
        public int getCount() {
            return itemsArray.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // если позиция за пределами массива выходим
            if (position >= itemsArray.size()) {
                return null;
            }

            ViewHolder holder;
            View v = convertView;
            // если ещё не создавалась
            if (v == null) {
                v = vi.inflate(R.layout.item_results,  parent, false);
                if (v == null) { // ошибка при создании
                    return null;
                }
                holder = new ViewHolder();
                holder.tv1 = (TextView) v.findViewById(R.id.res_fname);
                holder.tv2 = (TextView) v.findViewById(R.id.res_dname);
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

            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, firstLineFontSizePx);
            tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, secondLineFontSizePx);



            HashMap<String, String> item = itemsArray.get(position);
            if (item != null) {
                // первая строка в выводе
                String firstLine = item.get("firstLine");
                tv1.setText(firstLine);
                // вторая строка в выводе
                String secondLine = item.get("secondLine");
                tv2.setText(secondLine);


                int color_txt;
                tv2.setTypeface(null, Typeface.NORMAL);
                switch (item.get("color")){
                    case "reading":
                        color_txt = getResources().getColor(R.color.file_reading_fg);
                        tvHolder.setBackgroundColor(getResources() .getColor(R.color.file_reading_bg));
                        break;
                    case "finished":
                        color_txt = getResources().getColor(R.color.file_finished_fg);
                        tvHolder.setBackgroundColor(getResources().getColor(R.color.file_finished_bg));
                        break;
                    case "new":
                        color_txt = getResources().getColor(R.color.file_new_fg);
                        tvHolder.setBackgroundColor(getResources().getColor(R.color.file_new_bg));
                        tv1.setTypeface(null, Typeface.BOLD);
                        break;
                    default:
                        color_txt = getResources().getColor(R.color.normal_fg);
                        tvHolder.setBackgroundColor(getResources().getColor(R.color.normal_bg));
                        tv1.setTypeface(null, Typeface.BOLD);
                }
                tv1.setTextColor(color_txt);
                tv2.setTextColor(color_txt);


                // setup icon
                String name_icon = item.get("nameIcon");
                if (name_icon.equals("")) { // если отключены картинки
                    iv.setVisibility(View.GONE); // скрываем поле с ними
                }else {
                    iv.setImageBitmap(utilIcons.getIconFile(name_icon));
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
		gv.setNumColumns(currentColsNum);

		resAdapter.notifyDataSetChanged();

		if (currentPosition != -1)
			gv.setSelection(currentPosition);
	}

	private void createItemsArray() {
		itemsArray = new ArrayList<>();
        TextView rt = (TextView) findViewById(R.id.result_title);
        switch (listName) {
            case "opdslist": {
                itemsArray = utilOPDS.getListOPDS();
                setIconsForArray(itemsArray);
                setColorsForArray(itemsArray);

                rt.setText(title + " (" + itemsArray.size() + ")");
                break;
            }
            case "homeList": {
                itemsArray = utilHomeDirs.getArrayHomeDir();
                setIconsForArray(itemsArray);
                setColorsForArray(itemsArray);

                rt.setText(title + " (" + itemsArray.size() + ")");
                break;
            }
            case "favorites": {
                itemsArray = utilFavorites.getArrayFavorites();
                setIconsForArray(itemsArray);
                setColorsForArray(itemsArray);

                rt.setText(title + " (" + itemsArray.size() + ")");
                break;
            }
            case "lastOpened": {
                itemsArray = utilLastOpen.getArrayLastOpen();
                setIconsForArray(itemsArray);
                setColorsForArray(itemsArray);

                rt.setText(title + " (" + itemsArray.size() + ")");
                break;
            }
            case "searchResults": {
                itemsArray = SearchActivity.getSearch_results();
                setIconsForArray(itemsArray);
                setColorsForArray(itemsArray);

                if (total == -1) {
                    rt.setText(title + " (" + itemsArray.size() + ")");
                } else {
                    rt.setText(title + " (" + itemsArray.size() + "/" + total + ")");
                }
            }
        }

        resAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());


		app = ((ReLaunchApp) getApplicationContext());
        if(app == null ) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        app.setOptionsWindowActivity(this);
        setContentView(R.layout.layout_results);
        vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Recreate readers list
		final Intent data = getIntent();
		if (data.getExtras() == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		listName = data.getExtras().getString("list");

        utilIcons = new UtilIcons(getBaseContext());
        utilHomeDirs = new UtilHomeDirs(getBaseContext());

        ImageView result_exit = (ImageView) findViewById(R.id.result_btn_exit);
        result_exit.setImageBitmap(utilIcons.getIcon("EXIT"));
        result_exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });


		// set results icon
		ImageView results_icon = (ImageView) findViewById(R.id.result_icon);
		switch (listName){
            case ("homeList"):
                listNameSecond = "columnsHomeList";
                title = getResources().getString(R.string.jv_relaunch_home);
                results_icon.setImageBitmap(utilIcons.getIcon("HOMESCREEN"));
                break;
            case ("favorites"):
                utilFavorites = new UtilFavorites(getBaseContext());
                listNameSecond = "columnsFAV";
                title = getResources().getString(R.string.jv_relaunch_fav);
                results_icon.setImageBitmap(utilIcons.getIcon("FAVDOCSCREEN"));
                break;
            case ("lastOpened"):
                utilLastOpen = new UtilLastOpen(getBaseContext());
                listNameSecond = "columnsLRU";
                title = getResources().getString(R.string.jv_relaunch_lru);
                results_icon.setImageBitmap(utilIcons.getIcon("LRUSCREEN"));
                break;
            case ("searchResults"):
                listNameSecond = "columnsSearch";
                title = getResources().getString(R.string.jv_search_results);

                total = data.getExtras().getInt("total", -1);

                results_icon.setImageBitmap(utilIcons.getIcon("SEARCH"));
                break;
            case ("opdslist"):
                utilOPDS = new UtilOPDS(getBaseContext());
                listNameSecond = "columnsOpdsList";
                title = getResources().getString(R.string.jv_relaunch_OPDS_catalogs);
                results_icon.setImageBitmap(utilIcons.getIcon("OPDS"));
        }
        utilHistory = new UtilHistory(getBaseContext());

		currentPosition = -1;
		gv = (GridView) findViewById(R.id.result_list);
		gv.setHorizontalSpacing(0);

        resAdapter = new ResultAdapter(this, R.layout.item_results, itemsArray);
        gv.setAdapter(resAdapter);
		registerForContextMenu(gv);
		if (prefs.getBoolean("customScroll", app.customScrollDef)) {
			if (addSView) {
				int scrollW;
				try {
					scrollW = Integer.parseInt(prefs.getString("scrollWidth","25"));
				} catch (NumberFormatException e) {
					scrollW = 25;
				}

				LinearLayout ll = (LinearLayout) findViewById(R.id.result_fl);
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

            private GlSimpleOnGestureListener(Context context) {
                super();
                this.context = context;
            }
            private int findViewByXY(MotionEvent e) {
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

                // получаем данные по файлу
                String file_path = item.get("secondLine");
                String file_name = item.get("firstLine");
                String file_full_name;
                // формируем полный путь к файлу
                if (file_path.equals("/")) {
                    file_full_name = "/" + file_name;
                }else {
                    file_full_name = file_path + "/" + file_name;
                }

                currentPosition = gv.getFirstVisiblePosition();
                Intent intent;
                switch (listName){
                    case ("homeList"):
                        intent = new Intent();
                        intent.putExtra("id", item.get("id"));
                        setResult(RESULT_OK, intent);
                        finish();
                        break;
                    case ("favorites"):
                        // если это папка то возвращаемся в основное окно и делаем папку основной
                        if (item.get("type").equals(String.valueOf(TypeResource.DIR))) {
                            intent = new Intent();
                            intent.putExtra("newDir", file_full_name);
                            setResult(RESULT_OK, intent);
                            finish();
                        }else { // с файлами всё по старому
                            RunIntent runIntent = new RunIntent(app.getBaseContext());
                            if (file_name.endsWith(".apk")) {
                                runIntent.installPackage(ResultsActivity.this, file_full_name);
                            }else {
                                if (app.readerName(file_name).equals("Nope")) {
                                    runIntent.runInternalView(ResultsActivity.this, file_full_name);
                                }else {
                                    // Launch reader
                                    runIntent.launchReader(file_full_name);
                                }
                            }
                            // close in needed
                            if (prefs.getBoolean("returnFileToMain", false)) {
                                setResult(Activity.RESULT_CANCELED);
                                finish();
                            }
                        }
                        break;
                    case ("searchResults"):
                        // если это папка то возвращаемся в основное окно и делаем папку основной
                        if (item.get("type").equals(String.valueOf(TypeResource.DIR))) {
                            intent = new Intent(ResultsActivity.this,ReLaunch.class);
                            intent.putExtra("file", false);
                            intent.putExtra("newDir", file_full_name);
                            setResult(RESULT_OK, intent);
                            finish();
                        }else { // с файлами всё по старому
                            RunIntent runIntent = new RunIntent(app.getBaseContext());
                            if (file_name.endsWith(".apk")) {
                                runIntent.installPackage(ResultsActivity.this, file_full_name);
                            }else {
                                if (app.readerName(file_name).equals("Nope")) {
                                    runIntent.runInternalView(ResultsActivity.this, file_full_name);
                                }else {
                                    // Launch reader
                                    runIntent.launchReader(file_full_name);
                                }
                            }
                            // close in needed
                            if (prefs.getBoolean("returnFileToMain", false)) {
                                intent = new Intent(ResultsActivity.this,ReLaunch.class);
                                intent.putExtra("file", true);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                        break;
                    case ("lastOpened"):
                        RunIntent runIntent = new RunIntent(app.getBaseContext());
                        if (file_name.endsWith(".apk")) {
                            runIntent.installPackage(ResultsActivity.this, file_full_name);
                        }else {
                            if (app.readerName(file_name).equals("Nope")) {
                                runIntent.runInternalView(ResultsActivity.this, file_full_name);
                            }else {
                                // Launch reader
                                runIntent.launchReader(file_full_name);
                            }
                        }
                        // close in needed
                        if (prefs.getBoolean("returnFileToMain", false)) {
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                        }
                        break;
                    case ("opdslist"):
                        intent = new Intent(ResultsActivity.this, OPDSActivity.class);
                        intent.putExtra("opds_id", item.get("id"));
                        startActivity(intent);
                        // close in needed
                        if (prefs.getBoolean("returnFileToMain", false)) {
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                        }
                }
                createItemsArray();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                final int position = findViewByXY(e);
                HashMap<String, String> item;
                String file_full_name;
                ArrayList<String> aList = new ArrayList<>(10);
                if (position > -1) {
                    item = itemsArray.get(position);
                    // получаем данные по файлу
                    String file_path = item.get("secondLine");
                    String file_name = item.get("firstLine");
                    int resource = Integer.parseInt(item.get("resource"));
                    // формируем полный путь к файлу
                    if (file_path.equals("/")) {
                        file_full_name = "/" + file_name;
                    }else {
                        file_full_name = file_path + "/" + file_name;
                    }

                    switch (listName) {
                        case "homeList":
                            // "Remove from start dir"
                            aList.add(getResources().getString(R.string.jv_results_home_list_remove));
                            // "Add start dir"
                            aList.add(getResources().getString(R.string.jv_results_edit_homedir));
                            if (position > 0) {
                                // "Move one position up"
                                aList.add(getResources().getString(R.string.jv_results_move_up));
                            }
                            if (position < (itemsArray.size() - 1)) {
                                // "Move one position down"
                                aList.add(getResources().getString(R.string.jv_results_move_down));
                            }
                            break;
                        case "opdslist":
                            // "Add opds catalog"
                            aList.add(getResources().getString(R.string.jv_results_add_opds));
                            // "Remove from list"
                            aList.add(getResources().getString(R.string.jv_relaunch_delete));
                            // "Rename"
                            aList.add(getResources().getString(R.string.jv_relaunch_edit));
                            // Clean
                            aList.add(getResources().getString(R.string.jv_relaunch_clean_opds));
                            if (position > 0) {
                                // "Move one position up"
                                aList.add(getResources().getString(R.string.jv_results_move_up));
                            }
                            if (position < (itemsArray.size() - 1)) {
                                // "Move one position down"
                                aList.add(getResources().getString(R.string.jv_results_move_down));
                            }
                            break;
                        case "favorites":
                            if (position > 0) {
                                // "Move one position up"
                                aList.add(getResources().getString(R.string.jv_results_move_up));
                            }
                            if (position < (itemsArray.size() - 1)) {
                                // "Move one position down"
                                aList.add(getResources().getString(R.string.jv_results_move_down));
                            }
                            // "Remove from favorites"
                            aList.add(getResources().getString(R.string.jv_results_remove));
                            // "Delete file"
                            if (prefs.getBoolean("useFileManagerFunctions", true)) {
                                aList.add(getResources().getString(R.string.jv_results_delete_file));
                            }
                            // "Open dir"
                            aList.add(getResources().getString(R.string.jv_open_dir));
                            break;
                        case "lastOpened":
                            int book_state = utilHistory.getState(resource, file_full_name);
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
                            // "Remove from last opened"
                            aList.add(getResources().getString(R.string.jv_results_remove_last_opened));
                            // "Delete file"
                            if (prefs.getBoolean("useFileManagerFunctions", true)) {
                                aList.add(getResources().getString(R.string.jv_results_delete_file));
                            }
                            // "Open dir"
                            aList.add(getResources().getString(R.string.jv_open_dir));
                            break;
                        case "searchResults":
                            int book_state1 = utilHistory.getState(resource, file_full_name);
                            switch (book_state1) {
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
                            // "Delete file"
                            if (prefs.getBoolean("useFileManagerFunctions", true)) {
                                aList.add(getResources().getString(R.string.jv_results_delete_file));
                            }
                            // "Open dir"
                            aList.add(getResources().getString(R.string.jv_open_dir));
                            break;
                    }
                    // "Cancel"
                    aList.add(getResources().getString(R.string.app_cancel));
                }else {
                    switch (listName) {
                        case "homeList":
                            // "Add start dir"
                            aList.add(getResources().getString(R.string.jv_results_add_homedir));
                            // "Cancel"
                            aList.add(getResources().getString(R.string.app_cancel));
                            break;
                        case "opdslist":
                            // "Add opds catalog"
                            aList.add(getResources().getString(R.string.jv_results_add_opds));
                            // "Cancel"
                            aList.add(getResources().getString(R.string.app_cancel));
                            break;
                        case "favorites":
                            break;
                        case "lastOpened":
                            break;
                        case "searchResults":
                            break;
                    }
                }


                final String[] list = aList.toArray(new String[aList.size()]);
                ListAdapter cmAdapter = new ArrayAdapter<>(app, R.layout.item_relaunch_contexmenu, list);
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
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_remove_last_opened)) && listName.equals("lastOpened"))
                            onContextMenuSelected(CNTXT_MENU_RMLO, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_home_list_remove))&& listName.equals("homeList"))
                            onContextMenuSelected(CNTXT_MENU_RM_HOMEDIR, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_add_homedir)))
                            onContextMenuSelected(CNTXT_MENU_ADD_HOMEDIR, position);
                        else if (s.equalsIgnoreCase(getString(R.string.jv_results_edit_homedir)))
                            onContextMenuSelected(CNTXT_MENU_EDIT_HOMEDIR, position);

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
        //-------------------------------------------------------------
//кнопка на экран вверх
		final ImageButton upScroll = (ImageButton) findViewById(R.id.result_btn_scrollup);
		upScroll.setImageBitmap(utilIcons.getIcon("UPENABLE"));
        final ImageButton downScroll = (ImageButton) findViewById(R.id.result_btn_scrolldown);
        downScroll.setImageBitmap(utilIcons.getIcon("DOWNENABLE"));

		class upScrlSimpleOnGestureListener extends SimpleOnGestureListener {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
                int first = gv.getFirstVisiblePosition();
                int last = gv.getLastVisiblePosition();
                int visible = last - first + 1;
                first -= visible;
                if (first < 0) {
                    first = 0;
                }
                if (N2DeviceInfo.EINK_NOOK) {
                    MotionEvent ev;
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_DOWN, 200, 100, 0);
                    gv.dispatchTouchEvent(ev);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis() + 100,
                            MotionEvent.ACTION_MOVE, 200, 200, 0);
                    gv.dispatchTouchEvent(ev);
                    SystemClock.sleep(100);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_UP, 200, 200, 0);
                    gv.dispatchTouchEvent(ev);
                } else {
                    final int finfirst = first;
                    gv.clearFocus();
                    gv.post(new Runnable() {

                        public void run() {
                            gv.setSelection(finfirst);
                        }
                    });
                }
				return true;
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
// кнопка на экран вниз


		class dnScrlSimpleOnGestureListener extends SimpleOnGestureListener {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
                int total = gv.getCount();
                int last = gv.getLastVisiblePosition();
                int target = last + 1;
                if (target > (total - 1)){
                    target = total - 1;
                }
                if (N2DeviceInfo.EINK_NOOK) {
                    MotionEvent ev;
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_DOWN, 200, 200, 0);
                    gv.dispatchTouchEvent(ev);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis() + 100,
                            MotionEvent.ACTION_MOVE, 200, 100, 0);
                    gv.dispatchTouchEvent(ev);
                    SystemClock.sleep(100);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_UP, 200, 100, 0);
                    gv.dispatchTouchEvent(ev);
                } else {
                    if (total != last + 1) {
                        final int ftarget = target;
                        gv.clearFocus();
                        gv.post(new Runnable() {
                            public void run() {
                                gv.setSelection(ftarget);
                            }
                        });
                    }
                }
                return true;
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
	//---------------------------------------------------------------------
        int colsNum = Integer.valueOf(prefs.getString(listNameSecond, "-1"));
        // override auto (not working fine in adnroid)
        if (colsNum == 0) {
            colsNum = (new UtilColumns(getApplicationContext())).getAutoColsNum(itemsArray, "fname");
        }
        currentColsNum = colsNum;
        gv.setNumColumns(currentColsNum);
        EinkScreen.PrepareController(null, false);
        //ScreenOrientation.set(this, prefs);
	}
	@Override
	protected void onStart() {
        createItemsArray();
		super.onStart();
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

    private void onContextMenuSelected(int item, final int pos) {
        if (item == CNTXT_MENU_CANCEL) {
            return;
        }

        HashMap<String, String> item_Array;
        String fullName = "";
        String file_name = "";
        String file_path = "";
        int resource = 0;
        int type = 0;

        if (pos > -1) {
            item_Array = itemsArray.get(pos);
            resource = Integer.parseInt(item_Array.get("resource"));
            type = Integer.parseInt(item_Array.get("type"));
            file_path = item_Array.get("secondLine");
            file_name = item_Array.get("firstLine");
            fullName = file_path + "/" + file_name;
        }
        switch (item) {
            case CNTXT_MENU_MARK_READING:
                utilHistory.addToHistory(resource, fullName, BookState.READING);
                redrawList();
                break;
            case CNTXT_MENU_MARK_FINISHED:
                utilHistory.addToHistory(resource, fullName, BookState.FINISHED);
                redrawList();
                break;
            case CNTXT_MENU_MARK_FORGET:
                utilHistory.delFromHistory(resource, fullName);
                redrawList();
                break;
            case CNTXT_MENU_RMFAV:
                utilFavorites.delFav(file_path, file_name, type, resource);
                itemsArray.remove(pos);
                redrawList();
                break;
            case CNTXT_MENU_MOVEUP:
                if (pos > 0) {
                    HashMap<String, String> it = itemsArray.get(pos);

                    itemsArray.remove(pos);
                    itemsArray.add(pos - 1, it);
                    switch (listName){
                        case "homeList":
                            utilHomeDirs.setListHomeDir(itemsArray);
                            break;
                        case "favorites":
                            utilFavorites.setListAppFav(itemsArray);
                            break;
                        case "opdslist":
                            utilOPDS.setListOPDS(itemsArray);
                            break;
                    }
                    redrawList();
                }
                break;
            case CNTXT_MENU_MOVEDOWN:
                if (pos < (itemsArray.size() - 1)) {
                    HashMap<String, String> it = itemsArray.get(pos);

                    int size = itemsArray.size();
                    itemsArray.remove(pos);
                    if (pos + 1 >= size - 1) {
                        itemsArray.add(it);
                    } else {
                        itemsArray.add(pos + 1, it);
                    }
                    switch (listName){
                        case "homeList":
                            utilHomeDirs.setListHomeDir(itemsArray);
                            break;
                        case "favorites":
                            utilFavorites.setListAppFav(itemsArray);
                            break;
                        case "opdslist":
                            utilOPDS.setListOPDS(itemsArray);
                            break;
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
                            + file_name
                            + "\" "
                            + getResources().getString(
                            R.string.jv_results_delete_file_text2));
                    // "Yes"
                    final String finalDname = file_path;
                    final String finalFname = file_name;
                    final int finalResource2 = resource;
                    final String finalFullName = fullName;
                    builder.setPositiveButton(
                            getResources().getString(R.string.app_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.dismiss();
                                    if((new LocalFile(app)).fileRemove(new File(finalFullName))){
                                    //if(app.fileRemove(new File(finalDname + "/" + finalFname))) {
                                        utilHistory.delFromHistory(finalResource2, finalDname + "/" + finalFname);
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
                } else if ((new LocalFile(app)).fileRemove(new File(fullName))) {
                    utilHistory.delFromHistory(resource, fullName);
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
                                + file_name
                                + "\" "
                                + getResources().getString(
                                R.string.jv_results_delete_em_dir_text2));
                        // "Yes"
                        final String finalDname1 = file_path;
                        final String finalFname1 = file_name;
                        final int finalResource1 = resource;
                        final String finalFullName1 = fullName;
                        builder.setPositiveButton(
                                getResources().getString(R.string.app_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        dialog.dismiss();
                                        if((new LocalFile(app)).fileRemove(new File(finalFullName1))) {
                                            utilHistory.delFromHistory(finalResource1, finalDname1 + "/" + finalFname1);
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
                    } else if ((new LocalFile(app)).fileRemove(new File(fullName))) {
                        utilHistory.delFromHistory(resource, fullName);
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
                                + file_name
                                + "\" "
                                + getResources().getString(
                                R.string.jv_results_delete_ne_dir_text2));
                        // "Yes"
                        final String finalDname2 = file_path;
                        final String finalFname2 = file_name;
                        final int finalResource = resource;
                        final String finalFullName2 = fullName;
                        builder.setPositiveButton(
                                getResources().getString(R.string.app_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        dialog.dismiss();
                                        if((new LocalFile(app)).fileRemove(new File(finalFullName2))) {
                                            utilHistory.delFromHistory(finalResource, finalDname2 + "/" + finalFname2);
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
                    } else if ((new LocalFile(app)).fileRemove(new File(fullName))) {
                        utilHistory.delFromHistory(resource, fullName);
                        itemsArray.remove(pos);
                        redrawList();
                    }
                }
                break;
            case CNTXT_MENU_OPEN_DIR:
                Intent intent = new Intent();
                if (listName.equals("searchResults")) {
                    intent.putExtra("file", false);
                }
                intent.putExtra("newDir", file_path);
                setResult(RESULT_OK, intent);
                finish();
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
                View llOPDS = getLayoutInflater().inflate(R.layout.dialog_layout_opds, null);
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
                        HashMap<String, String> temp_item = utilOPDS.getdbOPDS(pos);
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
                    final String finalFname3 = file_path;
                    builderOPDS.setPositiveButton(getResources().getString(R.string.app_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    InfoConnectOPDS infoConnectOPDS = new InfoConnectOPDS();
                                    infoConnectOPDS.setTitle(String.valueOf(inputName.getText()));
                                    infoConnectOPDS.setLink(String.valueOf(inputAddress.getText()));
                                    infoConnectOPDS.setEnable_pass(cbLogin.isChecked()? "true": "false");
                                    infoConnectOPDS.setLogin(String.valueOf(inputLogin.getText()));
                                    infoConnectOPDS.setPassword(String.valueOf(inputPassword.getText()));
                                    infoConnectOPDS.setEnable_proxy("false");
                                    infoConnectOPDS.setProxy_type("");
                                    infoConnectOPDS.setProxy_name("");
                                    infoConnectOPDS.setProxy_port("");
                                    // добавление в базу данных


                                    if (menuidOPDS == CNTXT_MENU_ADD_OPDS) {
                                        utilOPDS.addDbOPDS(infoConnectOPDS);
                                    }else{
                                        // обновление базы
                                        utilOPDS.updateDbOPDS(finalFname3, infoConnectOPDS);
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
                utilOPDS.delDbOPDS(file_name);
                createItemsArray();
                break;
            case CNTXT_MENU_CLEAN_OPDS:
                // очистка базы
                utilOPDS.resetDb();
                createItemsArray();
                break;
            case CNTXT_MENU_RMLO:
                utilLastOpen.delLastOpen(file_path, file_name, type, resource);
                itemsArray.remove(pos);
                redrawList();
                break;
            case CNTXT_MENU_RM_HOMEDIR:
                utilHomeDirs.delHomeDir(file_path, file_name, resource);
                itemsArray.remove(pos);
                redrawList();
                break;
            case CNTXT_MENU_ADD_HOMEDIR:
                String title = getResources().getString(R.string.jv_results_add_homedir);
                homeDialog(title, pos);
                break;
            case CNTXT_MENU_EDIT_HOMEDIR:
                String title1 = getResources().getString(R.string.jv_results_edit_homedir);
                homeDialog(title1, pos);
                break;
        }
    }

	@Override
	protected void onResume() {
        EinkScreen.setEinkController(prefs);
		super.onResume();
		app.generalOnResume(TAG);
	}

	// =======================================================
    // получение номера иконки из массива
	private void setIconsForArray(List<HashMap<String, String>> itemsArray) {
        for (HashMap<String, String> item : itemsArray) {
            item.put("nameIcon", utilIcons.getNameIcon(item, listName));
        }
    }

    //========================================================
    // формирование цвета
    private void setColorsForArray(List<HashMap<String, String>> itemsArray) {
        switch (listName) {
            case "homeList":
            case "opdslist":
                for (HashMap<String, String> item: itemsArray) {
                    item.put("color", "new");
                }
                break;
            case "favorites":
            case "lastOpened":
            case "searchResults":
                for (HashMap<String, String> item: itemsArray) {
                    item.put("color", getColors(item));
                }
                break;
        }
    }
    private String getColors(HashMap<String, String> item) {
        if (item.get("type").equals(String.valueOf(TypeResource.FILE))) {
            String file_full_name = item.get("secondLine") + "/" + item.get("firstLine");
            int resource = Integer.parseInt(item.get("resource"));
            switch (utilHistory.getState(resource, file_full_name)){
                case 0: // app.NONE
                    return "new";
                case 1: // app.READING
                    return "reading";
                case 2: // app.FINISHED
                    return "finished";
                default:
                    return "new";
            }
        }
        return "new";
    }
    // ========================================================
    // Диалог для домашних папок
    private void homeDialog(String title, final int position){
        final String file_full_name;
        if (position > -1) {
            HashMap<String, String> item_Array = itemsArray.get(position);
            if (item_Array.get("secondLine").equals("/")){
                file_full_name = "/" + item_Array.get("firstLine");
            }else {
                file_full_name = item_Array.get("secondLine") + "/" + item_Array.get("firstLine");
            }
        }else {
            file_full_name = "/";
        }
        AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
        builder3.setTitle(title);

        // Поле ввода пути новой стартовой папки
        final EditText inputName3 = new EditText(this); // Имя. Уникальное
        // Заголовок поля ввода
        TextView tPath3 = new TextView(this);

        if (N2DeviceInfo.EINK_NOOK) {
            tPath3.setTextColor(getResources().getColor(R.color.file_unknown_fg));
        }
        // Заполняем заголовки
        tPath3.setText(getResources().getString(R.string.jv_results_addres_homedir));
        inputName3.setText(file_full_name);


        // начинаем заполнять форму
        LinearLayout ll3 = new LinearLayout(this);
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
                        if (position > -1) {

                            utilHomeDirs.delHomeDir(itemsArray.get(position).get("secondLine"),
                                    itemsArray.get(position).get("firstLine"),
                                    Integer.parseInt(itemsArray.get(position).get("resource")));
                            itemsArray.remove(position);
                        }
                        if (!utilHomeDirs.isHomeDir(0, fullPath)) {
                            utilHomeDirs.addHomeDir(fullPath, 0);
                        }
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
    }
}
