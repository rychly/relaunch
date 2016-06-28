package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.dropbox.client2.DropboxAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DropboxSelect extends Activity {
    private MyDropboxClient dropboxClient;
    private ArrayList<DropboxItems> names = new ArrayList<DropboxItems>();
    private String dirParent;

    private ReLaunchApp app;
    private Activity DropboxSelect;
    private String DBLocalPath;
    private int count_download_files = 0;
    static private boolean flag_download=false;
    private DropboxAdapter adapter;
    private boolean flag_rt;
    private boolean addSView = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((ReLaunchApp) getApplicationContext());
        if(app == null ) {
            finish();
        }

        app.setFullScreenIfNecessary(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        setContentView(R.layout.results_layout);
        ((ImageView) findViewById(R.id.results_icon)).setImageResource(R.drawable.ci_dropbox);
        ((Button) findViewById(R.id.results_title)).setText("Dropbox");
        DropboxSelect = this;

        dropboxClient = new MyDropboxClient(prefs, DropboxSelect.this);

        Intent intent = getIntent();
        DBLocalPath = intent.getStringExtra("DBLocalPath");

        // создаем адаптер
        adapter = new DropboxAdapter(this,names);
        final GridView gv = (GridView) findViewById(R.id.results_list);
        gv.setHorizontalSpacing(0);
        gv.setNumColumns(1);
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
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                if(!names.get(position).getfFile()){
                    String PathDB =names.get(position).getfullName();
                    LoadListFileDropbox llfd = new LoadListFileDropbox(PathDB);
                    llfd.execute();
                }
            }
        });
        gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                AlertDialog dialog = DowloadSelDialog(DropboxSelect, position);
                dialog.show();
                return false;
            }
        });
        //  up scroll
        final Button upScroll = (Button) findViewById(R.id.upscroll_btn);
        if (!ReLaunch.disableScrollJump) {
            upScroll.setText(app.scrollStep + "%");
        } else {
            upScroll.setText(getResources().getString(R.string.jv_relaunch_prev));
        }
        class upScrlSimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                app.TapUpScrool(gv, names.size());
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!ReLaunch.disableScrollJump) {
                    int first = gv.getFirstVisiblePosition();
                    int total = names.size();
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
                        int total = names.size();
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
        upScroll.setOnTouchListener(new View.OnTouchListener() {
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

        class dnScrlSimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                app.TapDownScrool(gv, names.size());
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!ReLaunch.disableScrollJump) {
                    int first = gv.getFirstVisiblePosition();
                    int total = names.size();
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
                        int total = names.size();
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
        downScroll.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                dnscrl_gd.onTouchEvent(event);
                return false;
            }
        });
        //============================================================================
        ImageButton btn_exit = (ImageButton) findViewById(R.id.results_btn);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                names.clear();
                finish();
            }
        });

        Button up = (Button) findViewById(R.id.goup_btn);
        up.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                LoadListFileDropbox llfd = new LoadListFileDropbox(dirParent);
                llfd.execute();
            }
        } );

        ImageButton adv = (ImageButton) findViewById(R.id.advanced_btn);
        adv.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(DropboxSelect.this, Advanced.class);
                startActivity(i);
            }
        });
        LoadListFileDropbox llfd = new LoadListFileDropbox("/");
        llfd.execute();
    }

    private boolean listFilesDB(String PathDB, ArrayList<DropboxItems> fullListFilesDB){
        DropboxAPI.Entry entries;
        entries = dropboxClient.metadata(PathDB);
        if (entries == null){
            return false;
        }

        for (DropboxAPI.Entry e : entries.contents) {
            if (!e.isDeleted) {
                if(e.isDir){
                    fullListFilesDB.add(new DropboxItems(false, e.fileName(), e.path));
                }
            }
        }
        for (DropboxAPI.Entry e : entries.contents) {
            if (!e.isDeleted) {
                if(!e.isDir){
                    fullListFilesDB.add(new DropboxItems(true, e.fileName(), e.path));
                }
            }
        }
        dirParent = entries.parentPath();
        return true;
    }
    private class DropboxItems {
        private boolean fFile;
        private String name;
        private String fullName;
        private boolean selected;

        public DropboxItems(boolean fFile, String name, String fullName) {
            this.fFile = fFile;
            this.name = name;
            this.fullName = fullName;
            selected = false;
        }

        public boolean getfFile() {
            return fFile;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        public String getfullName() {
            return fullName;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

    }
    private class DropboxAdapter extends ArrayAdapter<DropboxItems> {

        private final ArrayList<DropboxItems> list;
        int size_icon;
        int size_text;
        Bitmap bitmapDropbox;
        Bitmap bitmapFolder;
        LayoutInflater vi;

        public DropboxAdapter(Activity context, ArrayList<DropboxItems> list) {
            super(context, R.layout.button_dropbox_select, list);
            this.list = list;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            size_icon = Integer.parseInt(prefs.getString("firstLineIconSizePx", "48"));
            size_text = Integer.parseInt(prefs.getString("firstLineFontSizePx", "20"));
            bitmapDropbox = scaleDrawableById(R.drawable.ci_dropbox, size_icon);
            bitmapFolder = scaleDrawableById(R.drawable.dir_ok, size_icon);
            vi = (LayoutInflater) app.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        class ViewHolder {
            protected ImageView icon;
            protected TextView text;
            protected CheckBox checkbox;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = vi.inflate(R.layout.button_dropbox_select,  parent, false);
                if(view == null){
                    return null;
                }
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.icon = (ImageView) view.findViewById(R.id.icon_item);
                viewHolder.text = (TextView) view.findViewById(R.id.dropItem);
                viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
                viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        DropboxItems element = (DropboxItems) viewHolder.checkbox.getTag();
                        element.setSelected(buttonView.isChecked());
                    }
                });
                view.setTag(viewHolder);
                viewHolder.checkbox.setTag(list.get(position));
            } else {
                view = convertView;
                ((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
            }
            ViewHolder holder = (ViewHolder) view.getTag();
            if(list.get(position).getfFile()){
                holder.icon.setImageBitmap(bitmapDropbox);
            }else{
                holder.icon.setImageBitmap(bitmapFolder);
            }
            holder.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size_text);
            holder.text.setText(list.get(position).getName());
            holder.checkbox.setChecked(list.get(position).isSelected());
            return view;
        }
        private Bitmap scaleDrawableById(int id, int size) {
            return Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getResources(), id), size, size,
                    true);
        }
    }
    private AlertDialog DowloadSelDialog(final Activity activity, int position){
        boolean selFlag = false;
        final String[] list_url = new String[2];

        for (DropboxItems name : names) {
            if (name.isSelected()) {
                selFlag = true;
                break;
            }
        }
        if(!selFlag){
            names.get(position).setSelected(true);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getString(R.string.srt_dbactivity_file_down_title));//"Скачать файлы");
        if(flag_download){
            builder.setMessage(getString(R.string.srt_dbactivity_file_down_mess));//"Дождитесь окончания скачки!");
        }else {
            list_url[0] = getString(R.string.srt_dbactivity_file_down_mess_2);
            list_url[1] = getString(R.string.srt_dbactivity_file_del);//"Удалить выбранное";
            builder.setItems(list_url, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if(item == 0){
                        Download_Dropbox downloadFile = new Download_Dropbox();
                        downloadFile.execute();
                    }else if(item == 1){
                        AlertDialog seldel = SelDel(DropboxSelect);
                        seldel.show();
                    }
                    dialog.dismiss(); // Отпускает диалоговое окно
                }
            });
        }
        builder.setCancelable(true);
        builder.setNegativeButton(getResources().getString(R.string.app_cancel), new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Отпускает диалоговое окно
            }
        });
        return builder.create();
    }
    private boolean loadFiles(String DropBox_Path, int start_pos_path_db, String startFolder, ArrayList<String> fullListFiles) {
        DropboxAPI.Entry entries;
        FileOutputStream mFos;
        File file;

        entries = dropboxClient.metadata(DropBox_Path);
        if (entries == null){
            app.showToast(getString(R.string.srt_dbactivity_err_con_dropbox));//"Error connect from DropBox");
            return false;
        }
        String FullPath;
        for (DropboxAPI.Entry e : entries.contents) {
            if (!e.isDeleted) {
                FullPath = "/" + startFolder + e.path.substring(start_pos_path_db);
                if(!fullListFiles.contains(FullPath)){
                    file=new File(DBLocalPath + FullPath);

                    if(e.isDir){
                        if (!file.exists()) { file.mkdirs(); }
                        if(!loadFiles(e.path, start_pos_path_db, startFolder, fullListFiles)){ return false; }
                    }else{
                        try {
                            mFos = new FileOutputStream(file);
                        } catch (FileNotFoundException e1) {  return false;  }

                        if (!dropboxClient.getFile(e.path, mFos)){
                            file.delete();
                            return false;
                        }
                        try {
                            mFos.close();
                        } catch (IOException e1) { return false; }
                        count_download_files++;
                    }
                }
            }
        }
        return true;
    }
    private class Download_Dropbox extends AsyncTask<Void, Void, Void> {
        boolean flagErrorDowload = false;
        ArrayList<String> fullListFiles =  new ArrayList<String>();
        ArrayList<DropboxItems> load_names = new ArrayList<DropboxItems>();
        protected Void doInBackground(Void... params) {
            count_download_files = 0;
            FileOutputStream mFos = null;
            File file;
            int start_pos_path_db;
            listFiles(DBLocalPath, fullListFiles, DBLocalPath.length());
            for(int i=0; i < load_names.size(); i++){
                    if(load_names.get(i).getfFile()){
                        if(!fullListFiles.contains(File.separator + load_names.get(i).getName())){
                            file = new File(DBLocalPath + File.separator + load_names.get(i).getName());
                            try {
                                mFos = new FileOutputStream(file);
                            } catch (FileNotFoundException e1) {
                                flagErrorDowload = true;
                            }
                            if (!dropboxClient.getFile(load_names.get(i).fullName, mFos)){
                                file.delete();
                                flagErrorDowload = true;
                            }
                            try {
                                mFos.close();
                            } catch (IOException e1) {
                                flagErrorDowload = true;
                            }
                            count_download_files++;
                        }
                    }else {
                        start_pos_path_db = load_names.get(i).fullName.length();
                        file = new File(DBLocalPath + "/" + load_names.get(i).name);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        if (!loadFiles(load_names.get(i).fullName, start_pos_path_db, load_names.get(i).name, fullListFiles)){
                            flagErrorDowload = true;
                        }
                    }
            }
            return null;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            for (DropboxItems name : names) {
                if (name.isSelected()) {
                    load_names.add(name);
                }
            }
            load_names.trimToSize();
            flag_download = true;
            flag_rt =false;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(flagErrorDowload){
                app.showToast(getString(R.string.srt_dbactivity_err_down_dropbox));//"Error dowload files from DropBox");
            }
            app.showToast(getString(R.string.srt_dbactivity_file_down) + Integer.toString(count_download_files) + getString(R.string.srt_dbactivity_file_down2));//"Download " +" files"
            if(!flag_rt){
                for (DropboxItems name : names) {
                    if (name.isSelected()) {
                        name.setSelected(false);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            flag_download = false;
            fullListFiles.clear();
            flag_rt = false;
        }
        private void listFiles(String Path, ArrayList<String> fullListFiles, int start_pos_path){
            File openDir = new File(Path);
            String[] strDirList = openDir.list();
            String strPath = Path.substring(start_pos_path);

            for (String aStrDirList : strDirList) {
                File f1 = new File(Path + File.separator + aStrDirList);

                if (f1.isFile()) {
                    fullListFiles.add(strPath + File.separator + aStrDirList);
                } else {
                    listFiles(Path + File.separator + aStrDirList, fullListFiles, start_pos_path);
                }
            }
        }
    }
    private class Delete_Dropbox extends AsyncTask<Void, Void, Void> {
        boolean flagErrorDowload = false;
        ArrayList<DropboxItems> load_names = new ArrayList<DropboxItems>();
        protected Void doInBackground(Void... params) {

            for (DropboxItems load_name : load_names) {
                if (!dropboxClient.delete(load_name.fullName)){
                    flagErrorDowload = true;
                }
            }
            return null;
        }

        protected void onPreExecute() {
            super.onPreExecute();

            for (DropboxItems name : names) {
                if (name.isSelected()) {
                    load_names.add(name);
                }
            }
            load_names.trimToSize();
            flag_download = true;
            flag_rt =false;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(flagErrorDowload){
                app.showToast(getString(R.string.srt_dbactivity_err_down_dropbox));//"Error dowload files from DropBox");
            }
            if(!flag_rt){
                for(int i = names.size()-1; i > -1; i--){
                    if(names.get(i).isSelected()){
                        names.get(i).setSelected(false);
                        names.remove(i);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            app.showToast(getString(R.string.jv_prefs_rsr_ok_text));//"Удаление закончено");
            flag_download = false;
            flag_rt = false;
        }
    }
    private AlertDialog SelDel(final Activity activity){

        AlertDialog.Builder selDel = new AlertDialog.Builder(activity);
        selDel.setTitle("Удаление");
        selDel.setMessage(getString(R.string.srt_dbactivity_file_del));//"Вы хотите удалить выбранное?");
        selDel.setPositiveButton(getString(R.string.jv_relaunch_delete), new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Delete_Dropbox deleteFile = new Delete_Dropbox();
                deleteFile.execute();
                dialog.dismiss(); // Отпускает диалоговое окно
            }
        });
        selDel.setNegativeButton(getResources().getString(R.string.app_cancel), new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Отпускает диалоговое окно
            }
        });

        return selDel.create();
    }
    // загрузка списка файлов с Dropbox
    private class LoadListFileDropbox extends AsyncTask<Void, String, Void> {
        private ArrayList<DropboxItems> filesList = new ArrayList<DropboxItems>();
        ProgressDialog prog1 = new ProgressDialog(DropboxSelect.this);
        String PathDB;
        boolean load;

        public LoadListFileDropbox(String PathDB) {
            this.PathDB = PathDB;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            prog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            prog1.setMessage(getString(R.string.opds_file_load));
            prog1.setIndeterminate(true); // выдать значек ожидания
            prog1.setCancelable(true);
            prog1.show();
        }
        @Override
        protected Void doInBackground(Void... res) {
            load = listFilesDB(PathDB, filesList);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            prog1.cancel();
                if(load){ // при успешной загрузке ссылки
                    names.clear();
                    names.addAll(filesList);
                    // -- говорим адаптеру, что данные обновились --
                    adapter.notifyDataSetChanged();
                    if(flag_download) {
                        flag_rt = true;
                    }
                }else{ // -- ошибка загрузки --
                    // -- выводим сообщение о ошибке загрузки
                    app.showToast(getString(R.string.srt_dbactivity_err_con_dropbox));
                }
        }
    }
}