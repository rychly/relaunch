package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import ebook.EBook;
import ebook.parser.InstantParser;
import ebook.parser.Parser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OPDSActivity extends Activity {
    public ArrayList<Item> names = new ArrayList<Item>();
    public ArrayList<BookInfo> books = new ArrayList<BookInfo>();

    public ArrayList<String> list_book_steps = new ArrayList<String>();
    public ArrayList<String> list_path_steps = new ArrayList<String>();
    String_Search_Tag search_site;
    // db
    int opdsID;

    XmlPullParser xpp;
    private String URL_START_LINK; // стартовая страница каталога
    private String OPDS_URL_CURRENT = ""; // текущая страница каталога

    private String OPDS_Path_Download;
    public static List<String> sCookie =null;

    Activity test;
    ReLaunchApp app;
    OPDS_adapter adapter;
    boolean addSView = true;
    private int countDownload;
    static GridView lvMain;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((ReLaunchApp) getApplicationContext());
        if(app == null ) {
            finish();
        }
        if(!ReLaunch.haveNetworkConnection(getApplicationContext())){
            app.showToast(getString(R.string.srt_dbactivity_no_inet));
            finish();
        }else{
            app.setFullScreenIfNecessary(this);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            countDownload = 0;
            setContentView(R.layout.results_layout);
            ((ImageView) findViewById(R.id.results_icon)).setImageResource(R.drawable.ci_books);

            N2EpdController.n2MainActivity = this;
            test = this;

            
            //=======================================================================
            // проверяем наличие интента
            final Intent data = getIntent();
            if (data.getExtras() == null) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
            // получаем переданные данные
            opdsID = data.getExtras().getInt("dbID");
            URL_START_LINK = data.getExtras().getString("opdscat");
            if(URL_START_LINK == null || URL_START_LINK.equals("none") || !(URL_START_LINK.startsWith("http"))){
                app.showToast(getString(R.string.srt_opdsactivity_err_url));
                finish();
            }

            OPDS_Path_Download = prefs.getString("Local folder OPDS", "none").trim();
            if(!OPDS_Path_Download.equals("none")){
                File folder_check;
                folder_check = new File(OPDS_Path_Download);
                if (!folder_check.exists()) {
                    if(!folder_check.mkdirs()){
                        app.showToast(getString(R.string.srt_opdsactivity_err_folder));//"Невозможно создать локальный каталог или неверный путь");
                        finish();
                    }
                }
            }else if(N2DeviceInfo.EINK_ONYX){
                OPDS_Path_Download = "/mnt/storage";
            }else{
                finish();
            }

            lvMain = (GridView) findViewById(R.id.results_list);
            lvMain.setHorizontalSpacing(0);
            lvMain.setNumColumns(1);
            // создаем адаптер
            adapter = new OPDS_adapter(this, names);
            lvMain.setAdapter(adapter);
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
                    lvMain.setOnScrollListener(new AbsListView.OnScrollListener() {
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
                lvMain.setOnScrollListener(new AbsListView.OnScrollListener() {
                    public void onScroll(AbsListView view, int firstVisibleItem,
                                         int visibleItemCount, int totalItemCount) {
                        EinkScreen.PrepareController(null, false);
                    }

                    public void onScrollStateChanged(AbsListView view,
                                                     int scrollState) {
                    }
                });
            }
            lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                    if(names.get(position).imgHeader.equals("2")){
                        showBookInfo(position);
                    }else{
                        For_Load_OPDS tempFLO = new For_Load_OPDS();
                        tempFLO.url_next = names.get(position).urllink;
                        tempFLO.ID = opdsID;
                        //========выбор действия=======================================================
                        if(names.get(position).header.equalsIgnoreCase("Далее")){
                            tempFLO.action = "next"; // следующая часть списка книг
                        }else if(names.get(position).header.equalsIgnoreCase("Назад")){
                            tempFLO.action = "prev"; // предыдущая часть списка книг
                        }else {
                            tempFLO.action = "enter"; // в дочернюю папку
                        }
                        LoadOPDS foneLoadOPDS = new LoadOPDS(tempFLO);
                        foneLoadOPDS.execute();
                    }
                }
            });

            lvMain.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                    if(names.get(position).imgHeader.equals("2")){
                        AlertDialog dialog = DowloadDialog(test, position);
                        dialog.show();
                    }else{
                        AlertDialog dialog = ViewDialog(test, position);
                        dialog.show();
                    }
                    return false;
                }
            });
            //===========================================================================
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
                    if (lvMain.getFirstVisiblePosition() == 0) {
                        if (names.get(0).header.equalsIgnoreCase("Назад")) {
                            For_Load_OPDS tempFLO = new For_Load_OPDS();
                            tempFLO.url_next = names.get(0).urllink;
                            tempFLO.ID = opdsID;
                            tempFLO.action = "prev";
                            LoadOPDS foneLoadOPDS = new LoadOPDS(tempFLO);
                            foneLoadOPDS.execute();
                        }
                    }else {
                        app.TapUpScrool(lvMain, names.size());
                    }
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (!ReLaunch.disableScrollJump) {
                        int first = lvMain.getFirstVisiblePosition();
                        int total = names.size();
                        first -= (total * app.scrollStep) / 100;
                        if (first < 0)
                            first = 0;
                        lvMain.setSelection(first);
                        // some hack workaround against not scrolling in some cases
                        if (total > 0) {
                            lvMain.requestFocusFromTouch();
                            lvMain.setSelection(first);
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
                            lvMain.setSelection(first);
                            // some hack workaround against not scrolling in some
                            // cases
                            if (total > 0) {
                                lvMain.requestFocusFromTouch();
                                lvMain.setSelection(first);
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
                    int last1 = lvMain.getLastVisiblePosition();
                    if (lvMain.getCount() == lvMain.getLastVisiblePosition() + 1) {
                        if(names.get(last1).header.equalsIgnoreCase("Далее")){
                            For_Load_OPDS tempFLO = new For_Load_OPDS();
                            tempFLO.url_next = names.get(last1).urllink;
                            tempFLO.ID = opdsID;
                            tempFLO.action = "next";
                            LoadOPDS foneLoadOPDS = new LoadOPDS(tempFLO);
                            foneLoadOPDS.execute();
                        }
                    } else{
                        app.TapDownScrool(lvMain, names.size());
                    }
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (!ReLaunch.disableScrollJump) {
                        int first = lvMain.getFirstVisiblePosition();
                        int total = names.size();
                        int last = lvMain.getLastVisiblePosition();
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
                        app.RepeatedDownScroll(lvMain, first, target, 0);
                    }
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    if (downScroll.hasWindowFocus()) {
                        if (!ReLaunch.disableScrollJump) {
                            int first = lvMain.getFirstVisiblePosition();
                            int total = names.size();
                            int last = lvMain.getLastVisiblePosition();
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
                            app.RepeatedDownScroll(lvMain, first, target, 0);
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
            Button goup = (Button) findViewById(R.id.goup_btn);
            goup.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {// На предыдущий раздел каталога
                    For_Load_OPDS tempFLO = new For_Load_OPDS();
                    if (list_path_steps.size() > 0) {
                        tempFLO.url_next = list_path_steps.get(list_path_steps.size() - 1);
                        tempFLO.ID = opdsID;
                        tempFLO.action = "updir";
                        LoadOPDS foneLoadOPDS = new LoadOPDS(tempFLO);
                        foneLoadOPDS.execute();
                    }
                }
            });
            ImageButton back_btn = (ImageButton) findViewById(R.id.results_btn);

            // Выход из каталога
            back_btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    names.clear();
                    books.clear();
                    list_book_steps.clear();
                    list_path_steps.clear();
                    finish();
                }
            });
            ImageButton adv = (ImageButton) findViewById(R.id.advanced_btn);
            adv.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent(OPDSActivity.this, Advanced.class);
                    startActivity(i);
                }
            });

            // Загрузка первой страницы с каталога
            OPDS_URL_CURRENT = URL_START_LINK;
            For_Load_OPDS tempFLO = new For_Load_OPDS();
            tempFLO.ID = opdsID;
            tempFLO.url_next = URL_START_LINK;
            tempFLO.action = "enter";
            LoadOPDS foneLoadOPDS = new LoadOPDS(tempFLO);
            foneLoadOPDS.execute();
        }
    }

    //====================================================================================
    private void showBookInfo(int count_book){

        final int book_numbe = count_book;

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bookinfo);

        ((ImageView) dialog.findViewById(R.id.book_icon)).setImageResource(R.drawable.icon);
        ImageView img = (ImageView) dialog.findViewById(R.id.cover);

        if(books.get(count_book).url_cover != null &&  books.get(count_book).url_cover.length() != 0){
            DownloadImageTask downloadImage = new DownloadImageTask(img, books.get(count_book).url_cover);
            downloadImage.execute();
        }
        img.setImageResource(R.drawable.icon_book_list);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = DowloadDialog(test, book_numbe);
                dialog.show();
            }
        });
        // ================= название книги =======================
        if(books.get(count_book).book_name != null)
            ((TextView) dialog.findViewById(R.id.tvTitle)).setText(books.get(count_book).book_name);

        //==================== бред про книгу ===========================
        if(books.get(count_book).book_annotation != null)
            ((TextView) dialog.findViewById(R.id.tvAnnotation)).setText(books.get(count_book).book_annotation);

        // ================== авторы книги =========================
        if(books.get(count_book).book_authors != null){
            ListView lv = (ListView) dialog.findViewById(R.id.authors);

            lv.setDivider(null);
            final int count_author;
            count_author = books.get(count_book).book_authors.size();
            final String[] authors = new String[count_author];
            for (int i = 0; i < count_author; i++) {
                authors[i] = books.get(count_book).book_authors.get(i);
            }
            Context context = getApplicationContext();
            if(context != null) {
                final ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(context, R.layout.simple_list_item_1, authors);
                lv.setAdapter(lvAdapter);
            }
        }
        ///====================== серия книги ==========================================
        if(books.get(count_book).book_series != null){
            ((TextView) dialog.findViewById(R.id.tvSeries)).setText(books.get(count_book).book_series);}
        //================================== заголовок окна ===========================
        if(books.get(count_book).book_name != null)
            ((TextView) dialog.findViewById(R.id.book_title)).setText(getString(R.string.str_book_info));

        ImageButton btn = (ImageButton) dialog.findViewById(R.id.btnExit);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    //============================================================================================================
    private class BookInfo {
        String url_cover = null;
        ArrayList<String> book_authors = null;
        String book_series = null;
        String book_name = null;
        String book_annotation = null;
        ArrayList<BookLink> book_link = null;

        BookInfo(String uc, ArrayList<String> ba, String bs, String bn, String ban, ArrayList<BookLink> bl){
            url_cover = uc;
            book_authors=ba;
            book_series = bs;
            book_name = bn;
            book_annotation = ban;
            book_link = bl;
        }
    }
    //=============================================================================================
    private Bitmap getImageBitmap(String url, int type_image) {
        Bitmap bm = null;
        Bitmap cover = null;
        int COVER_MAX_W = 0;
        if(type_image == 1)
            COVER_MAX_W = 280;
        if(type_image == 2)
            COVER_MAX_W = 48;

        Drawable image = ImageOperations(url);
        if (image != null)
            bm = ((BitmapDrawable)image).getBitmap();
        if (bm != null) {
            int width = Math.min(COVER_MAX_W, bm.getWidth());
            int height = width * bm.getHeight() /bm.getWidth();
            cover = Bitmap.createScaledBitmap(bm, width, height, true);
        }
        return cover;
    }
    //=================================================================================================
    private Drawable ImageOperations(String url1) {
            InputStream is =  (new HttpBasicAuthentication(url1, opdsID, app)).Connect();
            return Drawable.createFromStream(is, "src");
    }
    //============================================================================
    private class BookLink {
        String url_type = null;
        String booklink = null;

        BookLink(String ut,String l){
            url_type = ut;
            booklink = l;
        }
    }

    private AlertDialog DowloadDialog(Activity activity, int item){
        final int book_item = item;
        final ArrayList<BookLink> bookItem = books.get(item).book_link;
        int count_url = bookItem.size();
        final String[] list_url = new String[count_url];

        for(int i=0; i<count_url; i++){
            list_url[i] = bookItem.get(i).url_type;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getString(R.string.str_book_dowload_info_title));
        builder.setItems(list_url, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                String url = bookItem.get(item).booklink;

                String namefile = "";

                if(books.get(book_item).book_authors.size() != 0){
                    namefile = books.get(book_item).book_authors.get(0) + " ";
                }
                if(!books.get(book_item).book_name.equals("")){
                    namefile = namefile.concat(books.get(book_item).book_name);
                }
                namefile = namefile.replaceAll("\\p{Cntrl}", "");
                namefile = namefile.replaceAll("[\\\\/:*?\"<>|]","_");
                namefile = namefile+"."+ bookItem.get(item).url_type;

                DownloadTask downloadFile = new DownloadTask(getBaseContext(),namefile,url);
                downloadFile.execute();
            }
        });
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.app_cancel), new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Отпускает диалоговое окно
            }
        });

        return builder.create();
    }
    private AlertDialog ViewDialog(Activity activity, int item){
        int count_url = names.get(item).list_link.size();
        final int numbe_item = item;
        String[] list_url = new String[count_url];

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Перейти");

        if(count_url != 0){
            for(int i=0; i<count_url; i++){
                list_url[i] = names.get(numbe_item).list_link.get(i).name;
            }
            builder.setItems(list_url, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    For_Load_OPDS tempFLO = new For_Load_OPDS();
                    tempFLO.url_next = names.get(numbe_item).list_link.get(i).uri;
                    tempFLO.ID = opdsID;
                    tempFLO.action = "enter";
                    LoadOPDS foneLoadOPDS = new LoadOPDS(tempFLO);
                    foneLoadOPDS.execute();
                }
            });
        }else{
            builder.setMessage("Ссылки не найдены.");
        }
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.app_cancel), new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Отпускает диалоговое окно
            }
        });

        return builder.create();
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private String name_file;
        private String strUrl;

        public DownloadTask(Context context, String namefile, String url) {
            this.context = context;
            this.name_file = namefile;
            this.strUrl = url;
        }
        protected void onPreExecute() {
            super.onPreExecute();
            countDownload++;
            dowloadTitleCount(String.valueOf(((Button) findViewById(R.id.results_title)).getText()));

        }

        @Override
        protected String doInBackground(String... sUrl) {

            InputStream input = null;
            OutputStream output = null;

            try {

                input = (new HttpBasicAuthentication(strUrl, opdsID, app)).Connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (input == null) {
                    return "Connection error";
                }
                //Log.i("DownloadTask", "---- connect ----- ");
                output = new FileOutputStream(OPDS_Path_Download +"/" + name_file);

                byte data[] = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled())
                        return null;
                    // publishing the progress....
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                }
                catch (IOException ignored) { }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            countDownload--;
            dowloadTitleCount(String.valueOf(((Button) findViewById(R.id.results_title)).getText()));
            if (result != null)
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else{
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
                if (name_file.endsWith("fb2") || name_file.endsWith("fb2.zip") || name_file.endsWith("epub")) {
                    Parser parser = new InstantParser();
                    EBook eBook = parser.parse(OPDS_Path_Download + File.separator + name_file, true);
                    if (eBook.isOk) {
                        String fileMoveDir = "";
                        if (eBook.authors.size() > 0) {
                            // проверить/создать папку по имени автора
                            String author = "";
                            if (eBook.authors.get(0).lastName != null){
                                author +=  eBook.authors.get(0).lastName;//;
                            }
                            if (eBook.authors.get(0).firstName != null){
                                if (eBook.authors.get(0).firstName.length() > 0){
                                    author += " " + eBook.authors.get(0).firstName;
                                }
                            }
                            if (eBook.authors.get(0).middleName != null){
                                if (eBook.authors.get(0).middleName.length() > 0){
                                    author += " " + eBook.authors.get(0).middleName;
                                }
                            }
                            author = author.trim();
                            // замена недопустимых файлов
                            author = author.replaceAll("[\\\\/:*?\"<>|]", "_");

                            fileMoveDir = OPDS_Path_Download + File.separator  + author + File.separator;
                            File newDir = new File(fileMoveDir);
                            if(!newDir.isDirectory()){
                                if (!app.createDir(fileMoveDir)){
                                    return;
                                }
                            }
                            if (eBook.sequenceName != null && eBook.sequenceName.length() != 0) {
                                // проверить/создать папку серии
                                String sequenceN = eBook.sequenceName.trim();
                                sequenceN = sequenceN.replaceAll("[\\\\/:*?\"<>|]", "_");
                                newDir = new File(fileMoveDir + sequenceN + File.separator);
                                if(!newDir.isDirectory()){
                                    if (app.createDir(fileMoveDir + sequenceN + File.separator)){
                                        fileMoveDir += sequenceN + File.separator;
                                    }
                                }else{
                                    fileMoveDir += sequenceN + File.separator;
                                }
                            }
                        }
                        app.moveFile(OPDS_Path_Download + File.separator + name_file, fileMoveDir + File.separator + name_file);
                    }
                }
            }
        }

    }

    private class String_Link_Tag{
        String href=null;
        String rel=null;
        String type=null;
        String title=null;

        String_Link_Tag(String h, String r, String t, String i){
            href = h;
            rel = r;
            type = t;
            title = i;
        }
    }

    private String_Link_Tag Link_Tag(XmlPullParser xpp){
        String href = null;
        String rel1 = null;
        String type=null;
        String title=null;

        String attr_val;
        String attr_name;
        try {
            if(xpp.getEventType() == XmlPullParser.START_TAG) {
                if(xpp.getAttributeCount()>0)
                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                        attr_name = xpp.getAttributeName(i);
                        attr_val = xpp.getAttributeValue(i);
                        if(attr_name.equalsIgnoreCase("href"))
                            href = attr_val;
                        else if(attr_name.equalsIgnoreCase("rel"))
                            rel1 = attr_val;
                        else if(attr_name.equalsIgnoreCase("type"))
                            type = attr_val;
                        else if(attr_name.equalsIgnoreCase("title"))
                            title = attr_val;
                    }
            }
            do {
                xpp.next();
            } while (xpp.getEventType() !=  XmlPullParser.END_TAG);
        } catch (XmlPullParserException e) {
            return null;
        }catch (IOException e) {
            return null;
        }
        return (new String_Link_Tag(href, rel1, type, title));
    }

    private class String_Author_Tag{
        String name=null;
        String uri=null;

        String_Author_Tag(String n, String u){
            name = n;
            uri = u;
        }
    }

    private String_Author_Tag Author_Tag(XmlPullParser xpp){
        String name = null;
        String uri = null;

        try {
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        if(xpp.getName().equalsIgnoreCase("name")) {
                            name = Name_Tag(xpp);
                            if (name != null) {
                                name = name.trim();
                            }
                        }
                        if(xpp.getName().equalsIgnoreCase("uri")) {
                            uri = Uri_Tag(xpp);
                            if (uri != null) {
                                uri = uri.trim();
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(xpp.getName().equalsIgnoreCase("author"))
                            return (new String_Author_Tag(name, uri));
                        break;
                    default:
                        break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    private String Title_Tag(XmlPullParser xpp){
        String title = null;
        try {
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.TEXT:

                        title = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if(xpp.getName().equalsIgnoreCase("title"))
                            return title;
                        break;

                    default:
                        break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    private class String_Category_Tag{
        String term=null;
        String label=null;

        String_Category_Tag(String t, String l){
            term = t;
            label = l;
        }
    }
    private String_Category_Tag Category_Tag(XmlPullParser xpp){
        String term = null;
        String label = null;

        String attr_val;
        String attr_name;
        try {
            switch (xpp.getEventType()) {

                case XmlPullParser.START_TAG:
                    if(xpp.getAttributeCount()>0){
                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
                            attr_name = xpp.getAttributeName(i);
                            attr_val = xpp.getAttributeValue(i);
                            if(attr_name.equalsIgnoreCase("term"))
                                term = attr_val;
                            else if(attr_name.equalsIgnoreCase("label"))
                                label = attr_val;
                        }
                    }
                    break;
                default:
                    break;
            }
            do {
                xpp.next();
            } while (xpp.getEventType() !=  XmlPullParser.END_TAG);
        } catch (XmlPullParserException e) {
            return null;
        }catch (IOException e) {
            return null;
        }
        return (new String_Category_Tag(term, label));
    }
    private String Content_Tag(XmlPullParser xpp){
        String content = null;
        String tag_type = "";


        String attr_name;
        try {
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        if(xpp.getAttributeCount()>0){
                            for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                attr_name = xpp.getAttributeName(i);
                                if(attr_name.equalsIgnoreCase("type"))
                                    tag_type = xpp.getAttributeValue(i);
                            }
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if(tag_type.equals("text/html")){
                            content = Html.fromHtml(xpp.getText()).toString().trim();
                        }else
                            content = xpp.getText().trim();
                        break;
                    case XmlPullParser.END_TAG:
                        if(xpp.getName().equalsIgnoreCase("content"))
                            return content;
                        break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    private String Summary_Tag(XmlPullParser xpp){
        String summary = null;

        try {
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.TEXT:
                            summary = xpp.getText().trim();
                        break;
                    case XmlPullParser.END_TAG:
                        if(xpp.getName().equalsIgnoreCase("summary"))
                            return summary;
                        break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    private boolean Entry_Tag(XmlPullParser xpp, ArrayList<Item> names_tmp, ArrayList<BookInfo> books_tmp){
        String bt_header;
        String bt_subHeader = "";
        String bt_cover;
        String bt_url = null;
        String_Link_Tag tmp_link;

        String entry_title = null;
        String entry_author = null;
        String entry_content = null;
        String entry_summary = null;
        String entry_category = null;
        String entry_cover2 = null;
        ArrayList<String> entry_authors_list = new ArrayList<String>();
        ArrayList<BookLink> entry_books_link = new ArrayList<BookLink>();
        ArrayList<String_Search_Tag> tmp_list_link = new ArrayList<String_Search_Tag>();
        String url_pref;
        boolean flag_book = true;
        try {
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        if(xpp.getName().equalsIgnoreCase("link")){
                            tmp_link = Link_Tag(xpp);
                            if (tmp_link.href.startsWith("http")){
                                url_pref = "";
                            }else {
                                url_pref = URL_START_LINK.substring(0,URL_START_LINK.lastIndexOf("/"));
                            }
                            if(tmp_link.type != null){
                                if(tmp_link.type.contains("image") && tmp_link.rel.contains("image")){
                                    entry_cover2 = url_pref + tmp_link.href;
                                }
                                if(tmp_link.type.contains("profile=opds-catalog") ){
                                    if(tmp_link.rel == null & tmp_link.title == null){
                                        bt_url = url_pref + tmp_link.href;
                                        flag_book = false;
                                    }
                                    if(tmp_link.title != null){
                                        tmp_list_link.add(new String_Search_Tag(tmp_link.title, url_pref + tmp_link.href));
                                    }
                                }else if(tmp_link.type.equals("application/fb2+zip")){
                                    entry_books_link.add(new BookLink("fb2.zip", url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/fb2")){
                                    entry_books_link.add(new BookLink("fb2",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/txt+zip")){
                                    entry_books_link.add(new BookLink("txt.zip",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/txt")){
                                    entry_books_link.add(new BookLink("txt",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/epub")){
                                    entry_books_link.add(new BookLink("epub",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/epub+zip")){
                                    if(URL_START_LINK.contains("dimonvideo.ru") || URL_START_LINK.contains("coollib.net") || URL_START_LINK.contains("zone4iphone.ru")){
                                        entry_books_link.add(new BookLink("epub",url_pref + tmp_link.href));
                                    }else
                                        entry_books_link.add(new BookLink("epub.zip",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/html+zip")){
                                    entry_books_link.add(new BookLink("html.zip",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/html")){
                                    entry_books_link.add(new BookLink("html",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/rtf+zip")){
                                    entry_books_link.add(new BookLink("rtf.zip",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/rtf")){
                                    entry_books_link.add(new BookLink("rtf",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/doc+zip")){
                                    entry_books_link.add(new BookLink("doc.zip",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/doc")){
                                    entry_books_link.add(new BookLink("doc",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/pdf+zip")){
                                    entry_books_link.add(new BookLink("pdf.zip",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/pdf")){
                                    entry_books_link.add(new BookLink("pdf",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/djvu+zip")){
                                    entry_books_link.add(new BookLink("djvu.zip",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/djvu")){
                                    entry_books_link.add(new BookLink("djvu",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/x-mobipocket-ebook+zip")){
                                    entry_books_link.add(new BookLink("mobi.zip",url_pref + tmp_link.href));
                                }else if(tmp_link.type.equals("application/x-mobipocket-ebook")){
                                    entry_books_link.add(new BookLink("mobi",url_pref + tmp_link.href));
                                }else{
                                    if(tmp_link.rel == null & tmp_link.title == null){
                                        bt_url = url_pref + tmp_link.href;
                                        flag_book = false;
                                    }
                                    if(tmp_link.title != null){
                                        tmp_list_link.add(new String_Search_Tag(tmp_link.title, url_pref + tmp_link.href));
                                    }
                                }
                            }
                        }else if(xpp.getName().equalsIgnoreCase("author")){
                            String_Author_Tag author1 = Author_Tag(xpp);

                            if(author1!=null){
                                entry_authors_list.add(author1.name);
                                if(entry_author == null){
                                    entry_author = author1.name;
                                }else {
                                    entry_author = entry_author.concat(", ");
                                    entry_author = entry_author.concat(author1.name);
                                }
                            }
                        }else if(xpp.getName().equalsIgnoreCase("title")){
                            entry_title = Title_Tag(xpp);

                        }else if(xpp.getName().equalsIgnoreCase("category")){
                            String_Category_Tag category = Category_Tag(xpp);
                            entry_category = category.label;
                        }else if(xpp.getName().equalsIgnoreCase("content")){
                            entry_content = Content_Tag(xpp);
                        }else if(xpp.getName().equalsIgnoreCase("summary")){
                            entry_summary = Summary_Tag(xpp);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(xpp.getName().equalsIgnoreCase("entry")){
                            bt_header = entry_title;
                            if(entry_books_link.size() != 0){
                                flag_book = true;
                            }
                            if(flag_book){
                                bt_cover = "2";
                                if(entry_author != null){
                                    bt_subHeader = entry_author;
                                }
                                if(entry_category !=null){
                                    bt_subHeader = bt_subHeader.concat(" (") + entry_category + ")";
                                }
                            }else{
                                bt_cover ="1";
                                if(entry_content != null){
                                    bt_subHeader = entry_content;
                                }
                            }

                            names_tmp.add(new Item(bt_header, bt_subHeader , bt_url, bt_cover, tmp_list_link));

                            if((entry_content == null || entry_content.length() == 0) && (entry_summary != null &&  entry_summary.length() > 0)){
                                entry_content = entry_summary;
                            }

                            if(flag_book){
                                books_tmp.add(new BookInfo(entry_cover2,entry_authors_list,entry_category,entry_title,entry_content,entry_books_link));
                            }

                            return false;
                        }
                        break;


                    default:
                        break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException e) {
            return true;
        } catch (IOException e) {
            return true;
        }
        return false;
    }

    private String Name_Tag(XmlPullParser xpp){
        String name = null;
        try {
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.TEXT:
                        name = xpp.getText().trim();
                        break;
                    case XmlPullParser.END_TAG:
                        if(xpp.getName().equalsIgnoreCase("name"))
                            return name;
                        break;

                    default:
                        break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    private String Uri_Tag(XmlPullParser xpp){
        String uri = null;
        try {
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.TEXT:
                        uri = xpp.getText().trim();
                        break;
                    case XmlPullParser.END_TAG:
                        if(xpp.getName().equalsIgnoreCase("uri"))
                            return uri;
                        break;

                    default:
                        break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    private class OPDS_adapter extends BaseAdapter {

        ArrayList<Item> data = new ArrayList<Item>();
        Context context;
        int size_text;
        int size_text2;
        Bitmap bitmapFolder;
        Bitmap bitmapBook;
        Bitmap bitmapDown;
        Bitmap bitmapUP;

        public OPDS_adapter(Context context, ArrayList<Item> arr) {
            if (arr != null) {
                data = arr;
            }
            this.context = context;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            int size_icon = Integer.parseInt(prefs.getString("firstLineIconSizePx", "48"));
            size_text = Integer.parseInt(prefs.getString("firstLineFontSizePx", "20"));
            size_text2 = Integer.parseInt(prefs.getString("secondLineFontSizePx", "16"));
            bitmapFolder = scaleDrawableById(R.drawable.folder_books_list, size_icon);
            bitmapBook = scaleDrawableById(R.drawable.icon_book_list, size_icon);
            bitmapDown = scaleDrawableById(R.drawable.ci_arrowdown_big, size_icon);
            bitmapUP = scaleDrawableById(R.drawable.ci_arrowup_big, size_icon);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int num) {
            return data.get(num);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int i, View someView, ViewGroup arg2) {
            LayoutInflater inflater = LayoutInflater.from(context);
            if (someView == null) {
                someView = inflater.inflate(R.layout.button_opds, arg2, false);
            }
            if (someView == null) {
                return null;
            }
            TextView header = (TextView) someView.findViewById(R.id.item_headerText);
            TextView subHeader = (TextView) someView.findViewById(R.id.item_subHeaderText);
            ImageView imgHeader = (ImageView) someView.findViewById((R.id.iV_cover));
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, size_text);
            header.setText(data.get(i).header);
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, size_text2);
            subHeader.setText(data.get(i).subHeader);
            if(data.get(i).imgHeader.equals("1"))
                imgHeader.setImageBitmap(bitmapFolder);
            else if(data.get(i).imgHeader.equals("2"))
                imgHeader.setImageBitmap(bitmapBook);
            else if(data.get(i).imgHeader.equals("down"))
                imgHeader.setImageBitmap(bitmapDown);
            else if(data.get(i).imgHeader.equals("up"))
                imgHeader.setImageBitmap(bitmapUP);
            return someView;
        }

        private Bitmap scaleDrawableById(int id, int size) {
            return Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getResources(), id), size, size,
                    true);
        }
    }
    private class String_Search_Tag{
        String name=null;
        String uri=null;

        String_Search_Tag(String n, String u){
            name = n;
            uri = u;
        }
    }
    public class Item {
        String header;
        String subHeader;
        String urllink;
        String imgHeader;
        ArrayList<String_Search_Tag> list_link = new ArrayList<String_Search_Tag>();

        Item(String h, String s, String u, String i, ArrayList<String_Search_Tag> ll){
            this.header=h;
            this.subHeader=s;
            this.urllink=u;
            this.imgHeader=i;
            if(ll != null && ll.size() != 0){
                this.list_link.addAll(ll);
            }
        }

    }

    public class DownloadImageTask extends AsyncTask<Void, Integer, Bitmap>{

        ImageView img;
        String urlImage;

        public DownloadImageTask(ImageView source, String url) {
            this.img = source;
            this.urlImage = url;
        }

        protected Bitmap doInBackground(Void... params) {
            Bitmap cover;
            try {
                cover = getImageBitmap(urlImage, 1);
            } catch (Exception e) {
                e.printStackTrace();
                cover = null;
            }
            return cover;
        }

        protected void onPostExecute(Bitmap cover) {
            super.onPostExecute(cover);
            if(cover != null){
                img.setImageBitmap(cover);
            }
        }
    }

    private void dowloadTitleCount(String title){
        int indexLast = title.lastIndexOf("(");
        String temp = "";
        if (countDownload > 0){
            temp = "(" + String.valueOf(countDownload) + ")";
        }
        if (indexLast > 0 ){
            title = title.substring(0, indexLast) + temp;
        }else{
            title += temp;
        }
        ((Button) findViewById(R.id.results_title)).setText(title);
    }

    // загрузка файла для начала обработки книги
    private class LoadOPDS extends AsyncTask<Void, For_Load_OPDS, Void> {
        int opdsID;
        String urlLoad;
        String action;
        boolean book = false;
        boolean start_pos = true;
        boolean addBookSteps = false;
        Request_Load_OPDS rez = null;
        ProgressDialog prog1 = new ProgressDialog(OPDSActivity.this);
        public LoadOPDS(For_Load_OPDS for_load_opds) {
            this.opdsID = for_load_opds.ID;
            this.urlLoad = for_load_opds.url_next;
            this.action = for_load_opds.action;
            this.start_pos = for_load_opds.start_pos;
            this.addBookSteps = for_load_opds.addBookSteps;
            this.book = for_load_opds.book;
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
            String flag_lll = "";
            if ((list_book_steps.size() == 0 && action.equalsIgnoreCase("next"))){
                flag_lll = "second";
            }
            if ((list_book_steps.size() == 1 && action.equalsIgnoreCase("prev"))){
                flag_lll = "first";
            }
            rez = load_opds_catalog(urlLoad, opdsID, flag_lll);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            prog1.cancel();
            if (rez != null){
                if(rez.result){ // при успешной загрузке ссылки
                    // -- выводим новый заголовок страницы - если есть ----
                    if (rez.title.length() > 0) {
                        dowloadTitleCount(rez.title);
                    }
                    // -- говорим адаптеру, что данные обновились --
                    adapter.notifyDataSetChanged();
                    // начинаем разбор действий
                    if (action.equalsIgnoreCase("enter")){
                        if (!URL_START_LINK.equalsIgnoreCase(urlLoad)) { //
                            list_path_steps.add(OPDS_URL_CURRENT);
                        }
                        lvMain.setSelection(0);
                    }
                    if (action.equalsIgnoreCase("next")){
                        list_book_steps.add(OPDS_URL_CURRENT);
                        lvMain.setSelection(0);
                    }
                    if (action.equalsIgnoreCase("prev")){
                        list_book_steps.remove(list_book_steps.size() - 1);
                        lvMain.setSelection(names.size() - 1);
                    }
                    if (action.equalsIgnoreCase("updir")){
                        if(list_path_steps.size() > 0) {
                            list_path_steps.remove(list_path_steps.size() - 1);
                        }
                        list_book_steps.clear();
                        lvMain.setSelection(0);
                    }

                    OPDS_URL_CURRENT = urlLoad;
                }else{ // -- ошибка загрузки --
                    // -- выводим сообщение о ошибке загрузки
                    app.showToast(getString(R.string.srt_dbactivity_err_con_dropbox));

                    if (URL_START_LINK.equalsIgnoreCase(urlLoad)) {
                        finish();
                    }
                }
            }else{
                finish();
            }
        }
    }
    private class Request_Load_OPDS{
        boolean result = false;
        String title = "";
    }
    private class For_Load_OPDS{
        boolean book = false;
        boolean start_pos = true;
        boolean addBookSteps = false;
        int ID;
        String url_next;
        String action;
    }
    //===========================================================================================================
    private Request_Load_OPDS load_opds_catalog(String URL_LINK, int opdsID, String flag){
        InputStream inputStream1;
        InputStreamReader in = null;
        Request_Load_OPDS retry = new Request_Load_OPDS();

        inputStream1 = (new HttpBasicAuthentication(URL_LINK, opdsID, app)).Connect();
        if(inputStream1 == null){
            return null;
        }
        ArrayList<Item> names_tmp = new ArrayList<Item>();
        ArrayList<BookInfo> books_tmp = new ArrayList<BookInfo>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            in = new InputStreamReader(inputStream1);
            xpp.setInput(in);
            String_Link_Tag tmp_link;
            String link_next = "";
            String url_pref;

            if(list_book_steps.size() > 0 ) {
                if (!flag.equalsIgnoreCase("first")){
                    names_tmp.add(new Item("Назад", "", list_book_steps.get(list_book_steps.size() - 1), "up", null));
                    books_tmp.add(new BookInfo(null, null, "", "", "", null));
                }
            }else if (flag.equalsIgnoreCase("second")){
                names_tmp.add(new Item("Назад", "", OPDS_URL_CURRENT, "up", null));
                books_tmp.add(new BookInfo(null, null, "", "", "", null));
            }

            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        if(xpp.getName().equalsIgnoreCase("title")){
                            retry.title = Title_Tag(xpp);
                        }
                        if(xpp.getName().equalsIgnoreCase("entry")){
                            Entry_Tag(xpp, names_tmp, books_tmp);
                        }
                        if(xpp.getName().equalsIgnoreCase("link")){
                            tmp_link = Link_Tag(xpp);
                            if(tmp_link != null && tmp_link.rel != null){
                                if (tmp_link.href.startsWith("http")){
                                    url_pref = "";
                                }else {
                                    url_pref = URL_START_LINK.substring(0,URL_START_LINK.lastIndexOf("/"));
                                }
                                if(tmp_link.rel.equalsIgnoreCase("next")){
                                    link_next = url_pref + tmp_link.href;
                                }
                                if(tmp_link.rel.equals("search") && tmp_link.type.equals("application/atom+xml")){
                                    search_site =new String_Search_Tag("Search", url_pref + tmp_link.href);
                                }                                
                            }
                        }
                        break;
                }
                xpp.next();
            }
            if(link_next.length() != 0){
                names_tmp.add(new Item("ДАЛЕЕ", "", link_next,"down", null));
            }
            retry.result = true;
        } catch (XmlPullParserException e) {
            retry.result = false;
        } catch (IOException e) {
            retry.result = false;
        }

        try {
            if (in != null) {
                in.close();
            }
            inputStream1.close();
            retry.result = true;
        } catch (IOException e) {
            retry.result = false;
        }

        if (retry.result){
            names_tmp.trimToSize();
            books_tmp.trimToSize();
            names.clear();
            books.clear();
            names.addAll(names_tmp);
            books.addAll(books_tmp);
        }

        return retry;
    }
}
