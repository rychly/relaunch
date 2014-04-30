package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DropboxSelect extends Activity {
    DropboxAPI<AndroidAuthSession> mDBApiDB;
    private ArrayList<DropboxItems> names = new ArrayList<DropboxItems>();
    private ArrayList<String> list_path_steps = new ArrayList<String>();

    ImageButton btn_exit;
    ReLaunchApp app;
    Activity test;
    String DBLocalPath;
    int count_download_files = 0;
    static private boolean flag_download=false;
    DropboxAdapter adapter;
    SharedPreferences prefs;
    int size_icon;
    int size_text;
    boolean flag_rt;
    boolean addSView = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((ReLaunchApp) getApplicationContext());
        if (app != null) {
            app.setFullScreenIfNecessary(this);
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        setContentView(R.layout.prefs_main);
        ((ImageView) findViewById(R.id.imageView1)).setImageResource(R.drawable.ci_dropbox);
        ((EditText) findViewById(R.id.prefernces_title)).setText("Dropbox");
        size_icon = Integer.parseInt(prefs.getString("firstLineIconSizePx", "48"));
        size_text = Integer.parseInt(prefs.getString("firstLineFontSizePx", "20"));
        test = this;
        mDBApiDB = DropBoxActivity.mDBApi;

        Intent intent = getIntent();
        DBLocalPath = intent.getStringExtra("DBLocalPath");

        if(!listFilesDB("/", names)){
            app.showToast(getString(R.string.srt_dbactivity_err_con_dropbox));//"Ошибка доступа к дропбоксу");
        }
        // создаем адаптер
        adapter = new DropboxAdapter(this,names);
        final ListView lvDropB = (ListView) findViewById(android.R.id.list);

        lvDropB.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                if(!names.get(position).getfFile()){
                    String PathDB =names.get(position).getfullName();
                    list_path_steps.add(PathDB);
                    names.clear();
                    if(!listFilesDB(PathDB, names)){
                        app.showToast(getString(R.string.srt_dbactivity_err_con_dropbox));//"Ошибка доступа к дропбоксу");
                    }
                    lvDropB.clearChoices();
                    adapter.notifyDataSetChanged();
                    if(flag_download)
                        flag_rt = true;
                }
            }
        });
        lvDropB.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                AlertDialog dialog = DowloadSelDialog(test, position);
                dialog.show();
                return false;
            }
        });
        lvDropB.setAdapter(adapter);

        if (prefs.getBoolean("customScroll", app.customScrollDef)) {
            if (addSView) {
                int scrollW;
                try {
                    scrollW = Integer.parseInt(prefs.getString("scrollWidth",
                            "25"));
                } catch (NumberFormatException e) {
                    scrollW = 25;
                }

                LinearLayout ll = (LinearLayout) findViewById(R.id.LLlist);
                final SView sv = new SView(getBaseContext());
                LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(
                        scrollW, ViewGroup.LayoutParams.FILL_PARENT, 2f);
                sv.setLayoutParams(pars);
                ll.addView(sv);
                lvDropB.setOnScrollListener(new AbsListView.OnScrollListener() {
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
            lvDropB.setOnScrollListener(new AbsListView.OnScrollListener() {
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    EinkScreen.PrepareController(null, false);
                }

                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }
            });
        }
        //============================================================================

        btn_exit = (ImageButton) findViewById(R.id.back_btn);

        btn_exit.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                String PathDB;
                if(list_path_steps.size() > 1){
                    PathDB = list_path_steps.get(list_path_steps.size()-2);
                    list_path_steps.remove(list_path_steps.size()-1);
                }else{
                    PathDB = "/";
                    list_path_steps.clear();
                }
                names.clear();
                if(!listFilesDB(PathDB, names)){
                    app.showToast(getString(R.string.srt_dbactivity_err_con_dropbox));//"Ошибка доступа к дропбоксу");
                }

                lvDropB.clearChoices();
                adapter.notifyDataSetChanged();
                if(flag_download)
                    flag_rt = true;
            }
        } );
        btn_exit.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                names.clear();
                list_path_steps.clear();
                finish();
                return true;
            }
        });
        ImageButton bu = (ImageButton) findViewById(R.id.btn_scrollup);
        bu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (N2DeviceInfo.EINK_NOOK) {
                    MotionEvent ev;
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_DOWN, 200, 100, 0);
                    lvDropB.dispatchTouchEvent(ev);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis() + 100,
                            MotionEvent.ACTION_MOVE, 200, 200, 0);
                    lvDropB.dispatchTouchEvent(ev);
                    SystemClock.sleep(100);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_UP, 200, 200, 0);
                    lvDropB.dispatchTouchEvent(ev);
                } else {
                    int first = lvDropB.getFirstVisiblePosition();
                    int visible = lvDropB.getLastVisiblePosition()
                            - lvDropB.getFirstVisiblePosition() + 1;
                    first -= visible;
                    if (first < 0)
                        first = 0;
                    final int finfirst = first;
                    lvDropB.clearFocus();
                    lvDropB.post(new Runnable() {

                        public void run() {
                            lvDropB.setSelection(finfirst);
                        }
                    });
                }
            }
        });

        ImageButton bd = (ImageButton) findViewById(R.id.btn_scrolldown);
        bd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (N2DeviceInfo.EINK_NOOK) {
                    MotionEvent ev;
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),MotionEvent.ACTION_DOWN, 200, 200, 0);
                    lvDropB.dispatchTouchEvent(ev);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis() + 100,MotionEvent.ACTION_MOVE, 200, 100, 0);
                    lvDropB.dispatchTouchEvent(ev);
                    SystemClock.sleep(100);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),MotionEvent.ACTION_UP, 200, 100, 0);
                    lvDropB.dispatchTouchEvent(ev);
                } else {
                    int total = lvDropB.getCount();
                    int last = lvDropB.getLastVisiblePosition();
                    if (total == last + 1)
                        return;
                    int target = last + 1;
                    if (target > (total - 1))
                        target = total - 1;
                    final int ftarget = target;
                    lvDropB.clearFocus();
                    lvDropB.post(new Runnable() {
                        public void run() {
                            lvDropB.setSelection(ftarget);
                        }
                    });
                }

            }
        });
    }

    private boolean listFilesDB(String PathDB, ArrayList<DropboxItems> fullListFilesDB){
        DropboxAPI.Entry entries;
        try {
            entries = mDBApiDB.metadata(PathDB, 0, null, true, null);
        } catch (DropboxException e) {
            app.showToast(getString(R.string.srt_dbactivity_err_con_dropbox));//"Error connect from DropBox");
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
    public class DropboxAdapter extends ArrayAdapter<DropboxItems> {

        private final ArrayList<DropboxItems> list;
        private final Activity context;

        public DropboxAdapter(Activity context, ArrayList<DropboxItems> list) {
            super(context, R.layout.button_dropbox_select, list);
            this.context = context;
            this.list = list;
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
                LayoutInflater inflator = LayoutInflater.from(context);
                view = inflator.inflate(R.layout.button_dropbox_select, null);
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
                holder.icon.setImageBitmap(scaleDrawableById(R.drawable.ci_dropbox, size_icon));
            }else{
                holder.icon.setImageBitmap(scaleDrawableById(R.drawable.dir_ok, size_icon));
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
                        AlertDialog seldel = SelDel(test);
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

        try {
            entries = mDBApiDB.metadata(DropBox_Path, 0, null, true, null);
        } catch (DropboxException e) {
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

                        try {
                            mDBApiDB.getFile(e.path, null, mFos, null);
                        } catch (DropboxException e1) {
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
                            try {
                                mDBApiDB.getFile(load_names.get(i).fullName, null, mFos, null);
                            } catch (DropboxException e1) {
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
                    }else{
                        start_pos_path_db = load_names.get(i).fullName.length();
                        file = new File(DBLocalPath +"/" + load_names.get(i).name);
                        if (!file.exists()) { file.mkdirs(); }
                        if(!loadFiles(load_names.get(i).fullName, start_pos_path_db, load_names.get(i).name, fullListFiles))
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
                Log.i("Relaunch", load_name.fullName);
                try {
                    mDBApiDB.delete(load_name.fullName);
                } catch (DropboxException e1) {
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
}