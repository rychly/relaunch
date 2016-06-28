package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ExtendedInfoBook extends Activity {
    ReLaunchApp app;
    ImageButton backBtn;
    TextView viewTxt;
    String fileName;
    XmlPullParser xpp;
    BufferedReader fileRead;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (ReLaunchApp) getApplicationContext();
        if(app == null){
            finish();
        }
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.viewer_layout);
        // убираем не нужные нам кнопку редактирования и область редактирования
        // кнопка
        ViewGroup layout = (ViewGroup) ( findViewById(R.id.viewedit_btn)).getParent();
        if(null!=layout)
            layout.removeView((findViewById(R.id.viewedit_btn)));
        // область редактирования
        layout = (ViewGroup) (findViewById(R.id.view_txt)).getParent();
        if(null!=layout)
            layout.removeView((findViewById(R.id.view_txt)));
        // klwjhre
        layout = (ViewGroup) (findViewById(R.id.textViewExtInfo)).getParent();
        if(null!=layout)
            layout.removeView((findViewById(R.id.textViewExtInfo)));
        //================================================================================
        LinearLayout ll = (LinearLayout)findViewById(R.id.linLayForAdd);

        ScrollView scroolTV = new ScrollView(this);
        ll.addView(scroolTV);
        // TV
        viewTxt = new TextView(this);
        viewTxt.setBackgroundColor(Color.WHITE);
        viewTxt.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1f));
        viewTxt.setTextColor(Color.BLACK);
        scroolTV.addView(viewTxt);

        // получаем файл для чтения инфо
        final Intent data = getIntent();
        if (data.getExtras() == null)
            finish();

        final String fname = data.getStringExtra("filename");
        if (fname == null)
            finish();
        fileName = fname;
        // немножко правим заголовок
        ((TextView) findViewById(R.id.view_title1)).setText(getString(R.string.srt_title_more_info_book));//"Расширенная информация");
        ((EditText) findViewById(R.id.view_title)).setText(fileName);

        // Set back button
        backBtn = (ImageButton) findViewById(R.id.view_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // удаляем файлы если они остались
                String[] SavedFiles;
                SavedFiles = getApplicationContext().fileList();
                if(SavedFiles.length > 0){
                    for (String SavedFile : SavedFiles) {
                        getApplicationContext().deleteFile(SavedFile);
                    }
                }
                finish();
            }
        });
        // получаем поток с данными
        if(fileName.lastIndexOf("fb2.zip") > -1){
            selectFileInArh(fileName);
        }
        fileRead = getInputStream(fileName);
        getHeaderBook();
    }
    private void selectFileInArh(String nameZipFile) {
        ZipFile zipFile = null;
        ArrayList<String> listFiles = new ArrayList<String>();

        try {
            // open a zip file for reading
            zipFile = new ZipFile(nameZipFile);

            // get an enumeration of the ZIP file entries
            Enumeration<? extends ZipEntry> e = zipFile.entries();

            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                // get the name of the entry
                int lenght = entry.getName().length();
                if(!entry.isDirectory() && lenght > 3 && entry.getName().substring(lenght-3, lenght).equals("fb2")){
                    listFiles.add(entry.getName());
                }
            }
        }catch (IOException ioe) {
            System.out.println("Error opening zip file" + ioe);
        }finally {
            try {
                if (zipFile!=null) {
                    zipFile.close();
                }
            }catch (IOException ioe) {
                System.out.println("Error while closing zip file" + ioe);
            }
        }
        // получив список файлов в архиве выводим диалог выбора
        int count_url = listFiles.size();
        if(count_url > 1){
            String[] list_files = new String[count_url];
            for(short i=0; i<count_url; i++){
                list_files[i] = listFiles.get(i);
            }
            //========================================================================
            AlertDialog dialog = selectFile(list_files, nameZipFile);
            dialog.show();
            //======================================================================================
        }else if(count_url == 1){
            unpackZip(listFiles.get(0), nameZipFile);
        }else{
            System.exit(0);
        }
    }
    private AlertDialog selectFile(final String[] list_files, final String nameZipFile){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.srt_select_files));//"Выбор файла для чтения");
        builder.setItems(list_files, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                unpackZip(list_files[item], nameZipFile);
            }
        });
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Отпускает диалоговое окно
            }
        });

        return builder.create();
    }
    private boolean unpackZip(String unzipFileName, String zipname){
        InputStream is;
        ZipInputStream zis;
        try{
            is = new FileInputStream(zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;
            while ((ze = zis.getNextEntry()) != null){
                if(unzipFileName.equals(ze.getName())){
                    FileOutputStream fout = this.openFileOutput(unzipFileName, Context.MODE_PRIVATE);
                    // cteni zipu a zapis
                    while ((count = zis.read(buffer)) != -1){
                        fout.write(buffer, 0, count);
                    }
                    fout.close();
                    zis.closeEntry();
                    break;
                }
            }
            zis.close();
            fileName = unzipFileName;
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private BufferedReader getInputStream(String nameFile){
        File myXML = new File(nameFile);

        String line;
        BufferedReader r;

        if(nameFile.contains("/")){
            try {
                r = new BufferedReader(new InputStreamReader(new FileInputStream(myXML)));
                line = r.readLine();
                r.close();
            } catch (FileNotFoundException e) {
                return null;
            } catch (IOException e) {
                return null;
            }

        }else{
            try {
                r = new BufferedReader(new InputStreamReader(openFileInput(nameFile)));
                line = r.readLine();
                r.close();
            } catch (FileNotFoundException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
        }

        String temp = "UTF-8";
        int start = line.indexOf("encoding");
        if(start > -1){
            int stop;
            temp = line.substring(start+9);
            stop = temp.indexOf("\"");
            temp = temp.substring(stop+1);
            stop = temp.indexOf("\"");
            temp = temp.substring(0, stop);
            temp = temp.toUpperCase();
            if("WINDOWS-1251".equals(temp)){
                temp = "CP-1251";
            }
        }
        if(nameFile.contains("/")){
            try {
                r = new BufferedReader(new InputStreamReader(new FileInputStream(nameFile), temp ));
            } catch (FileNotFoundException e) {
                return null;
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }else{
            try {
                r = new BufferedReader(new InputStreamReader(openFileInput(nameFile), temp));
            } catch (FileNotFoundException e) {
                return null;
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
        return  r;
    }
    public void getHeaderBook(){
        String fileBookName;
        String date_book;
        //================================================================================
        try {
            // получаем поток и настраиваем парсер
            //=========================================
            int eventType;

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            xpp.setInput(fileRead);
            //=================================================
            eventType = xpp.getEventType();

            String nameTag = "";
            boolean flagDescription = true;
            boolean f_genre = false;
            boolean f_date = false;

            String genre;
            String genre_match = "";
            String first_name = "";
            String middle_name = "";
            String last_name = "";
            String nickname = "";
            String home_page = "";
            String email = "";

            viewTxt.setMovementMethod(new ScrollingMovementMethod());
            Spannable text;
            while (eventType != XmlPullParser.END_DOCUMENT && flagDescription) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        nameTag = xpp.getName();
                        // обозначаем начало разделов
                        if("title-info".equalsIgnoreCase(nameTag)){
                            text = new SpannableString(getString(R.string.srt_info_book_title_info));//"- Данные о книге -\n");
                            text.setSpan(new RelativeSizeSpan(1.3f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new UnderlineSpan(), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewTxt.append(text);
                        }
                        if("src-title-info".equalsIgnoreCase(nameTag)){
                            text = new SpannableString(getString(R.string.srt_info_book_src_title_info));//"- Данные об исходнике книги -\n");
                            text.setSpan(new RelativeSizeSpan(1.3f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new UnderlineSpan(), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewTxt.append(text);
                        }
                        if("document-info".equalsIgnoreCase(nameTag)){
                            text = new SpannableString(getString(R.string.srt_info_book_document_info));//"- Информация об FB2-документе -\n");
                            text.setSpan(new RelativeSizeSpan(1.3f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new UnderlineSpan(), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewTxt.append(text);
                        }
                        if("publish-info".equalsIgnoreCase(nameTag)){
                            text = new SpannableString(getString(R.string.srt_info_book_publish_info));//"- Сведения об издании книги -\n");
                            text.setSpan(new RelativeSizeSpan(1.3f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new UnderlineSpan(), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewTxt.append(text);
                        }
                        // ----------------------------------------------------------------
                        // жанр
                        if("genre".equals(nameTag)){
                            for (short i = 0; i < xpp.getAttributeCount(); i++) {
                                if("match".equals(xpp.getAttributeName(i))){
                                    genre_match = xpp.getAttributeValue(i);
                                }
                            }
                            f_genre = true;
                        }
                        // дата
                        if("date".equals(nameTag)){
                            for (short i = 0; i < xpp.getAttributeCount(); i++) {
                                if("value".equals(xpp.getAttributeName(i))){
                                    genre_match = xpp.getAttributeValue(i);
                                }
                            }
                            f_date = true;
                        }
                        // серия и номер в серии
                        if("sequence".equals(nameTag)){
                            String name = "", number = "";
                            for (short i = 0; i < xpp.getAttributeCount(); i++) {
                                if("name".equals(xpp.getAttributeName(i))){
                                    name = xpp.getAttributeValue(i);
                                }
                                if("number".equals(xpp.getAttributeName(i))){
                                    number = xpp.getAttributeValue(i);
                                }
                            }
                            if(name.length() > 0){
                                String temp = name;
                                viewTxt.append(getString(R.string.srt_info_book_sequence_info));//"Серия:\n");
                                if(number.length() > 0){
                                    temp += ", №";
                                    temp += number;
                                }
                                text = new SpannableString(temp+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                            }
                        }
                        break;
                    case XmlPullParser.TEXT:
                        // жанры
                        if(f_genre){
                            genre = xpp.getText().trim();
                            if(genre_match.length() > 0){
                                genre += " (";
                                genre += genre_match;
                                genre += "%)";
                                genre_match = "";
                            }
                            viewTxt.append(getString(R.string.srt_info_book_genre_info));//"Жанр:\n");
                            text = new SpannableString(genre+"\n");
                            text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewTxt.append(text);
                            f_genre = false;
                        }
                        // авторы и переводчики
                        if("first-name".equals(nameTag)){
                            first_name = xpp.getText();
                        }
                        if("middle-name".equals(nameTag)){
                            middle_name = xpp.getText();
                        }
                        if("last-name".equals(nameTag)){
                            last_name = xpp.getText();
                        }
                        if("nickname".equals(nameTag)){
                            nickname = xpp.getText();
                        }
                        if("home-page".equals(nameTag)){
                            home_page = xpp.getText().trim();
                        }
                        if("email".equals(nameTag)){
                            email = xpp.getText().trim();
                        }
                        // название книги
                        if("book-title".equals(nameTag)){
                            fileBookName = xpp.getText();
                            viewTxt.append(getString(R.string.srt_info_book_book_title_info));//"Название произведения:\n");
                            text = new SpannableString(fileBookName+"\n");
                            text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewTxt.append(text);
                        }
                        // Список ключевых слов к книге
                        if("keywords".equals(nameTag)){
                            fileBookName = xpp.getText();
                            viewTxt.append(getString(R.string.srt_info_book_keywords_info));//"Список ключевых слов к книге:\n");
                            text = new SpannableString(fileBookName+"\n");
                            text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewTxt.append(text);
                        }
                        // дата
                        if(f_date){
                            date_book = xpp.getText();
                            if(date_book.trim().length()> 0){
                                if(genre_match.trim().length() > 0){
                                    date_book += " (";
                                    date_book += genre_match;
                                    date_book += ")";
                                    genre_match = "";
                                }
                                viewTxt.append(getString(R.string.srt_info_book_data_info));//"Дата:\n");
                                text = new SpannableString(date_book+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                            }
                            f_date = false;
                        }
                        // язык книги
                        if("lang".equals(nameTag)){
                            genre_match = xpp.getText().trim();
                            if(genre_match.length() > 0){
                                viewTxt.append(getString(R.string.srt_info_book_lang_info));//"Язык книги:\n");
                                text = new SpannableString(genre_match+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                                genre_match = "";
                            }
                        }
                        // язык оригинала
                        if("src-lang".equals(nameTag)){
                            genre_match = xpp.getText().trim();
                            if(genre_match.length() > 0){
                                viewTxt.append(getString(R.string.srt_info_book_src_lang_info));//"Язык оригинала:\n");
                                text = new SpannableString(genre_match+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                                genre_match = "";
                            }
                        }
                        // Перечисляются программы, которые использовались при подготовке документа.
                        if("program-used".equals(nameTag)){
                            genre_match = xpp.getText().trim();
                            if(genre_match.length() > 0){
                                viewTxt.append(getString(R.string.srt_info_book_program_used_info));//"Программы использованные при подготовке:\n");
                                text = new SpannableString(genre_match+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                                genre_match = "";
                            }
                        }
                        // Откуда взят оригинальный документ, доступный в online
                        if("src-url".equals(nameTag)){
                            genre_match = xpp.getText().trim();
                            if(genre_match.length() > 0){
                                viewTxt.append(getString(R.string.srt_info_book_src_url_info));//"Оригинальный документ:\n");
                                text = new SpannableString(genre_match+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                                genre_match = "";
                            }
                        }
                        // Автор OCR или оригинального документа
                        if("src-ocr".equals(nameTag)){
                            genre_match = xpp.getText().trim();
                            if(genre_match.length() > 0){
                                viewTxt.append(getString(R.string.srt_info_book_src_ocr_info));//"Автор OCR или оригинального документа:\n");
                                text = new SpannableString(genre_match+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                                genre_match = "";
                            }
                        }
                        // Версия FB2-документа
                        if("version".equals(nameTag)){
                            genre_match = xpp.getText().trim();
                            if(genre_match.length() > 0){
                                viewTxt.append(getString(R.string.srt_info_book_version_info));//"Версия FB2-документа:\n");
                                text = new SpannableString(genre_match+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                                genre_match = "";
                            }
                        }
                        // Правообладатель
                        if("publisher".equals(nameTag)){
                            genre_match = xpp.getText().trim();
                            if(genre_match.length() > 0){
                                viewTxt.append(getString(R.string.srt_info_book_publisher_info));//"Правообладатель:\n");
                                text = new SpannableString(genre_match+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                                genre_match = "";
                            }
                        }
                        // Год издания оригинальной (бумажной) книги
                        if("year".equals(nameTag)){
                            genre_match = xpp.getText().trim();
                            if(genre_match.length() > 0){
                                viewTxt.append(getString(R.string.srt_info_book_year_info));//"Год издания:\n");
                                text = new SpannableString(genre_match+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                                genre_match = "";
                            }
                        }
                        // ISBN оригинальной (бумажной) книги.
                        if("isbn".equals(nameTag)){
                            genre_match = xpp.getText().trim();
                            if(genre_match.length() > 0){
                                viewTxt.append("ISBN:\n");
                                text = new SpannableString(genre_match+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                                genre_match = "";
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        nameTag = xpp.getName();
                        // авторы и переводчики
                        if("author".equalsIgnoreCase(nameTag) || "translator".equalsIgnoreCase(nameTag)){
                            String str = null;
                            if("author".equalsIgnoreCase(nameTag)){
                                str = getString(R.string.srt_info_book_author_info);//"Автор:\n";
                            }
                            if("translator".equalsIgnoreCase(nameTag)){
                                str = getString(R.string.srt_info_book_translator_info);//"Переводчик:\n";
                            }
                            viewTxt.append(str);
                            str = "";
                            if(last_name.length() != 0){
                                str = last_name;
                                last_name = "";
                            }
                            if(first_name.length() != 0){
                                str += " ";
                                str += first_name;
                                first_name = "";
                            }
                            if(middle_name.length() != 0){
                                str += " ";
                                str += middle_name;
                                middle_name = "";
                            }
                            if(nickname.length() != 0){
                                if(str.length() == 0){

                                    str += nickname;
                                }else{
                                    str += (" (");
                                    str += (nickname);
                                    str += (")");
                                }
                                nickname = "";
                            }
                            text = new SpannableString(str+"\n");
                            text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewTxt.append(text);

                            if(home_page.length()> 0){
                                text = new SpannableString(home_page+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                                home_page = "";
                            }
                            if(email.length() > 0){
                                text = new SpannableString(email+"\n");
                                text.setSpan(new RelativeSizeSpan(1.2f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewTxt.append(text);
                                email = "";
                            }
                        }
                        // конец заголовка
                        if("description".equalsIgnoreCase(nameTag)){
                            flagDescription = false;
                        }
                        nameTag = "";
                        break;
                }
                xpp.next();
                eventType = xpp.getEventType();

            }
        } catch (XmlPullParserException e) {
            //return true;
        }  catch (IOException e) {
            //return true;
        }
        try {
            fileRead.close();
        } catch (IOException e) {
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}