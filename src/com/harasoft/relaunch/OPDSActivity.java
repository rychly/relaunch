package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class OPDSActivity extends Activity {
    public ArrayList<Item> names = new ArrayList<Item>();
    public ArrayList<BookInfo> books = new ArrayList<BookInfo>();
    public ArrayList<Item> names_tmp = new ArrayList<Item>();
    public ArrayList<BookInfo> books_tmp = new ArrayList<BookInfo>();
    public ArrayList<String> list_book_steps = new ArrayList<String>();
    public ArrayList<String> list_path_steps = new ArrayList<String>();
    String_Search_Tag search_site;
    String login_opds;
    String pass_opds;
    int eventType;
    XmlPullParser xpp;
    private boolean flag_book = false;
    public static String URLstr = "http://www.flibusta.net/opds";
    public static String URLdomen = "";
    public static String URLtemp = "";
    public String OPDS_Path_Download = "/sdcard/";
    private SharedPreferences prefs;
    WifiManager wfm;
    Activity test;
    ReLaunchApp app;
    int size_icon;
    int size_text;
    int size_text2;
    OPDS_adapter adapter;
    int count_login = 0;
    boolean addSView = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wfm = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if(wfm.getWifiState() != WifiManager.WIFI_STATE_ENABLED){
            showToast("Wi-Fi off!");
            finish();
        }
        app = ((ReLaunchApp) getApplicationContext());
        app.setFullScreenIfNecessary(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        size_icon = Integer.parseInt(prefs.getString("firstLineIconSizePx", "48"));
        size_text = Integer.parseInt(prefs.getString("firstLineFontSizePx", "20"));
        size_text2 = Integer.parseInt(prefs.getString("secondLineFontSizePx", "16"));

        setContentView(R.layout.prefs_main);
        ((ImageView) findViewById(R.id.imageView1)).setImageResource(R.drawable.ci_books);

        N2EpdController.n2MainActivity = this;
        test = this;
        Locale locale;
        String lang;
        lang = prefs.getString("lang", "default");
        if (lang.equals("default")) {
            locale = getResources().getConfiguration().locale.getDefault();
        }else {
            locale = new Locale(lang);
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);

        if(N2DeviceInfo.EINK_ONYX){
            OPDS_Path_Download = "/mnt/storage";
        }
        String OPDSPath;
        String OPDSLocalPath;
        OPDSPath = prefs.getString("OPDS catalog", "none").trim();
        OPDSLocalPath = prefs.getString("Local folder OPDS", "none").trim();
        if(!OPDSPath.equals("none")){
            URLstr = OPDSPath;
        }

        if(!OPDSLocalPath.equals("none")){
            File folder_check;
            folder_check = new File(OPDSLocalPath);
            if (!folder_check.exists()) {
                if(!folder_check.mkdirs()){
                    showToast("Невозможно создать локальный каталог или неверный путь");
                    finish();
                }
            }
            OPDS_Path_Download = OPDSLocalPath;
        }

        URLdomen = URLstr.substring(0,URLstr.lastIndexOf("/"));
        if(!(URLdomen.contains("http://"))){
            showToast("Неполный адрес каталога");
            finish();
        }
        final ListView lvMain = (ListView) findViewById(android.R.id.list);

        // создаем адаптер
        adapter = new OPDS_adapter(this, names);
        lvMain.setAdapter(adapter);
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                String url_next;
                if(names.get(position).imgHeader.equals("2")){
                    try {
                        showBookInfo(position);
                    } catch (MalformedURLException e) {
                        //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }else if(names.get(position).header.equalsIgnoreCase("Моя полка") || names.get(position).subHeader.equalsIgnoreCase("Вход для зарегистрированных пользователей")){
                    url_next =  names.get(position).urllink;

                    login_opds = prefs.getString("OPDS login", "");
                    pass_opds = prefs.getString("OPDS password", "");
                    if(load_opds(url_next, names_tmp, books_tmp, login_opds, pass_opds)){
                        names_tmp.clear();
                        books_tmp.clear();
                    }else{
                        count_login = 1;
                        list_path_steps.add(url_next);
                        URLtemp = url_next;
                        names.clear();
                        books.clear();
                        names.addAll(names_tmp);
                        books.addAll(books_tmp);
                        names_tmp.clear();
                        books_tmp.clear();
                        adapter.notifyDataSetChanged();
                        lvMain.setSelection(0);
                    }
                }else{
                    url_next =  names.get(position).urllink;

                    boolean start_pos = true;
                    if(names.get(position).header.equalsIgnoreCase("Далее")){
                        list_book_steps.add(URLtemp);
                    }else if(names.get(position).header.equalsIgnoreCase("Назад")){
                        list_book_steps.remove(list_book_steps.size()-1);
                        start_pos = false;
                    }else {
                        list_path_steps.add(url_next);
                        count_login ++;
                    }

                    if(load_opds(url_next, names_tmp, books_tmp, login_opds, pass_opds)){
                        names_tmp.clear();
                        books_tmp.clear();
                        if(names.get(position).header.equalsIgnoreCase("Далее")){
                            list_book_steps.remove(list_book_steps.size()-1);
                        }else if(names.get(position).header.equalsIgnoreCase("Назад")){
                            list_book_steps.add(URLtemp);
                        }else {
                            list_path_steps.remove(list_path_steps.size()-1);
                        }
                    }else{
                        URLtemp = url_next;
                        names.clear();
                        books.clear();
                        names.addAll(names_tmp);
                        books.addAll(books_tmp);
                        names_tmp.clear();
                        books_tmp.clear();
                        adapter.notifyDataSetChanged();
                        if(start_pos){
                            lvMain.setSelection(0);
                        }else{
                            lvMain.setSelection(names.size()-1);
                        }
                    }
                }

            }
        });

        lvMain.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                if(flag_book){
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
        //============================================================================
        ImageButton back_btn = (ImageButton) findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String url_next;

                if (list_path_steps.size() > 1) {
                    url_next = list_path_steps.get(list_path_steps.size() - 2);
                } else {
                    url_next = URLstr;
                }

                if (load_opds(url_next, names_tmp, books_tmp, login_opds, pass_opds)) {
                    names_tmp.clear();
                    books_tmp.clear();
                } else {
                    if (list_path_steps.size() > 1) {
                        list_path_steps.remove(list_path_steps.size() - 1);
                        if (count_login > 0)
                            count_login--;
                    } else {
                        list_path_steps.clear();
                        count_login = 0;
                        login_opds = "";
                        pass_opds = "";
                    }
                    names.clear();
                    books.clear();
                    names.addAll(names_tmp);
                    books.addAll(books_tmp);
                    names_tmp.clear();
                    books_tmp.clear();
                    list_book_steps.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });
        back_btn.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                names.clear();
                books.clear();
                list_book_steps.clear();
                list_path_steps.clear();
                names_tmp.clear();
                books_tmp.clear();
                count_login = 0;
                login_opds = "";
                pass_opds = "";
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
                    lvMain.dispatchTouchEvent(ev);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis() + 100,
                            MotionEvent.ACTION_MOVE, 200, 200, 0);
                    lvMain.dispatchTouchEvent(ev);
                    SystemClock.sleep(100);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_UP, 200, 200, 0);
                    lvMain.dispatchTouchEvent(ev);
                } else {
                    int first = lvMain.getFirstVisiblePosition();
                    int visible = lvMain.getLastVisiblePosition()
                            - lvMain.getFirstVisiblePosition() + 1;
                    first -= visible;
                    if (first < 0)
                        first = 0;
                    final int finfirst = first;
                    lvMain.clearFocus();
                    lvMain.post(new Runnable() {

                        public void run() {
                            lvMain.setSelection(finfirst);
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
                    lvMain.dispatchTouchEvent(ev);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis() + 100,MotionEvent.ACTION_MOVE, 200, 100, 0);
                    lvMain.dispatchTouchEvent(ev);
                    SystemClock.sleep(100);
                    ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),MotionEvent.ACTION_UP, 200, 100, 0);
                    lvMain.dispatchTouchEvent(ev);
                } else {
                    int total = lvMain.getCount();
                    int last = lvMain.getLastVisiblePosition();
                    if (total == last + 1)
                        return;
                    int target = last + 1;
                    if (target > (total - 1))
                        target = total - 1;
                    final int ftarget = target;
                    lvMain.clearFocus();
                    lvMain.post(new Runnable() {
                        public void run() {
                            lvMain.setSelection(ftarget);
                        }
                    });
                }

            }
        });

        if(load_opds(URLstr, names_tmp, books_tmp, login_opds, pass_opds)){
            names.clear();
            books.clear();
            list_book_steps.clear();
            list_path_steps.clear();
            names_tmp.clear();
            books_tmp.clear();
            finish();
        }else{
            names.clear();
            books.clear();
            names.addAll(names_tmp);
            books.addAll(books_tmp);
            names_tmp.clear();
            books_tmp.clear();
            adapter.notifyDataSetChanged();
        }
    }
    //===========================================================================================================
    private boolean load_opds(String URLstr, ArrayList<Item> names_tmp, ArrayList<BookInfo> books_tmp, String login, String pass){
        InputStream inputStream1;
        InputStreamReader in;

        try {
            HttpURLConnection conn;
            conn = (new HttpBasicAuthentication(URLstr, login, pass)).Connect();
            inputStream1 = conn.getInputStream();

        } catch (MalformedURLException e) {
            showToast(getString(R.string.srt_dbactivity_err_con_dropbox));
            return true;
        } catch (IOException e) {
            showToast(getString(R.string.srt_dbactivity_err_con_dropbox));
            return true;
        }

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            in = new InputStreamReader(inputStream1);
            xpp.setInput(in);
            eventType = xpp.getEventType();
            String_Link_Tag tmp_link;
            String link_next = "";
            String url_pref;

            if(list_book_steps.size() != 0){
                names_tmp.add(new Item("Назад", "",list_book_steps.get(list_book_steps.size()-1),"up", null));
                books_tmp.add(new BookInfo(null,null,"","","",null));
            }
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        if(xpp.getName().equalsIgnoreCase("title")){
                            ((EditText) findViewById(R.id.prefernces_title)).setText(Title_Tag(xpp));
                        }
                        if(xpp.getName().equalsIgnoreCase("entry")){
                            if(Entry_Tag(xpp, names_tmp, books_tmp)){
                                return true;
                            }
                        }
                        if(xpp.getName().equalsIgnoreCase("link")){
                            tmp_link = Link_Tag(xpp);
                            if(tmp_link.rel != null){
                                if(!(tmp_link.href.contains("http://"))){
                                    url_pref = URLdomen;
                                }else{
                                    url_pref = "";
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
                names_tmp.add(new Item("ДАЛЕЕ", "",link_next,"down", null));
            }
        } catch (XmlPullParserException e) {
            showToast("Error XmlPullParser");
            return true;
        }  catch (IOException e) {
            showToast("Error Input/Output");
            return true;
        }
        try {
            in.close();
        } catch (IOException e) {
            //
        }
        names_tmp.trimToSize();
        books_tmp.trimToSize();
        return false;
    }
    //=============================================================================
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    //====================================================================================
    private void showBookInfo(int count_book) throws MalformedURLException {

        final int book_numbe = count_book;

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bookinfo);

        ((ImageView) dialog.findViewById(R.id.book_icon)).setImageResource(R.drawable.book);
        ImageView img = (ImageView) dialog.findViewById(R.id.cover);

        if(books.get(count_book).url_cover != null &&  books.get(count_book).url_cover.length() != 0){
            DownloadImageTask downloadImage = new DownloadImageTask(img, books.get(count_book).url_cover);
            downloadImage.execute();
        }
        img.setImageResource(R.drawable.book);
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

            final ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_list_item_1, authors);
            lv.setAdapter(lvAdapter);
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
    public Bitmap getImageBitmap(String url, int type_image) {
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
        try {
            InputStream is = (InputStream) new URL(url1).getContent();
            //Drawable d = Drawable.createFromStream(is, "src");
            return Drawable.createFromStream(is, "src");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
                    namefile = books.get(book_item).book_authors.get(0);
                }
                if(!books.get(book_item).book_name.equals("")){
                    namefile = namefile.concat(books.get(book_item).book_name);
                }
                namefile = namefile+"."+ bookItem.get(item).url_type;
                namefile = namefile.replaceAll("\\p{Cntrl}", "");
                namefile = namefile.replaceAll(":","_");

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
                    String tmp_url = names.get(numbe_item).list_link.get(i).uri;
                    if(load_opds(tmp_url, names_tmp, books_tmp, "", "")){
                        names_tmp.clear();
                        books_tmp.clear();
                    }else{
                        URLtemp = tmp_url;
                        list_path_steps.add(tmp_url);
                        names.clear();
                        books.clear();
                        names.addAll(names_tmp);
                        books.addAll(books_tmp);
                        names_tmp.clear();
                        books_tmp.clear();
                        adapter.notifyDataSetChanged();
                    }
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

        @Override
        protected String doInBackground(String... sUrl) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            String base64EncodedCredentials;

            try {
                URL url = new URL(strUrl);

                try {
                    connection = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    return "Невозможно установить Интернет-соединение с источником данных";
                }

                if(login_opds != null && pass_opds != null && !login_opds.equals("") && !pass_opds.equals("")){
                    base64EncodedCredentials = Base64Coder.encodeString(login_opds + ":" + pass_opds);
                    connection.setRequestProperty("Authorization", "Basic " + base64EncodedCredentials);
                }

                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();

                // download the file
                input = connection.getInputStream();

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

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null)
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
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
                        if(xpp.getName().equalsIgnoreCase("name"))
                            name = Name_Tag(xpp).trim();
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
        //int count_aut = 0;
        flag_book = true;
        try {
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        if(xpp.getName().equalsIgnoreCase("link")){
                            tmp_link = Link_Tag(xpp);
                            if(!(tmp_link.href.contains("http://"))){
                                url_pref = URLdomen;
                            }else{
                                url_pref = "";
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
                                    if(URLstr.contains("dimonvideo.ru") || URLstr.contains("coollib.net")){
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

                            if((entry_content == null || entry_content.equals("")) && (entry_summary != null || !entry_summary.equals(""))){
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

    private class OPDS_adapter extends BaseAdapter {

        ArrayList<Item> data = new ArrayList<Item>();
        Context context;

        public OPDS_adapter(Context context, ArrayList<Item> arr) {
            if (arr != null) {
                data = arr;
            }
            this.context = context;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data.size();
        }

        @Override
        public Object getItem(int num) {
            // TODO Auto-generated method stub
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
            TextView header = (TextView) someView.findViewById(R.id.item_headerText);
            TextView subHeader = (TextView) someView.findViewById(R.id.item_subHeaderText);
            ImageView imgHeader = (ImageView) someView.findViewById((R.id.iV_cover));
            header.setTextSize(TypedValue.COMPLEX_UNIT_PX, size_text);
            header.setText(data.get(i).header);
            subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, size_text2);
            subHeader.setText(data.get(i).subHeader);
            if(data.get(i).imgHeader.equals("1"))
                imgHeader.setImageBitmap(scaleDrawableById(R.drawable.folder_books, size_icon));//.setImageResource(R.drawable.folder_books);
            else if(data.get(i).imgHeader.equals("2"))
                imgHeader.setImageBitmap(scaleDrawableById(R.drawable.book, size_icon));//.setImageResource(R.drawable.book);
            else if(data.get(i).imgHeader.equals("down"))
                imgHeader.setImageBitmap(scaleDrawableById(R.drawable.arrow_down, size_icon));//.setImageResource(R.drawable.arrow_down);
            else if(data.get(i).imgHeader.equals("up"))
                imgHeader.setImageBitmap(scaleDrawableById(R.drawable.arrow_up, size_icon));//.setImageResource(R.drawable.arrow_up);
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

        // запускаем ProgressBar в момент запуска потока
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
}
