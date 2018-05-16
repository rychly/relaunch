package com.harasoft.relaunch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.harasoft.relaunch.Utils.UtilIcons;
import ebook.EBook;
import ebook.parser.InstantParser;
import ebook.parser.Parser;

public class BookInfoActivity extends Activity {
    final String TAG = "Book Info";
    private ReLaunchApp app;
    private Bitmap cover = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Recreate readers list
        final Intent data = getIntent();
        if (data.getExtras() == null) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        app = ((ReLaunchApp) getApplicationContext());
        if (app == null) {
            finish();
        }
        app.setOptionsWindowActivity(this);
        setContentView(R.layout.layout_bookinfo);

        UtilIcons utilIcons = new UtilIcons(getBaseContext());

        // Back button - work as cancel
        ImageView book_icon_exit = (ImageView) findViewById(R.id.btnExit);
        book_icon_exit.setImageBitmap(utilIcons.getIcon("EXIT"));
        book_icon_exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        // Icon
        ImageView book_icon = (ImageView) findViewById(R.id.book_icon);
        book_icon.setImageBitmap(utilIcons.getIcon("BOOKINFO"));

        String full_file_name = data.getExtras().getString("full_file_name");
        if(full_file_name.endsWith(".fb2") || full_file_name.endsWith(".fb2.zip")){
            addButtonMore(full_file_name);
        }

        Parser parser = new InstantParser();
        EBook eBook = parser.parse(full_file_name, true);

        if (eBook.isOk) {
            getInfo(eBook, full_file_name);
        }
    }

    private void addButtonMore(final String file) {
        // добавляем кнопку дополнительной информации о файле
        Button btnMore = new Button(this);
        btnMore.setText(getString(R.string.srt_btn_more_info_book));//"More");
        btnMore.setTextSize(24);
        btnMore.setBackgroundResource(R.drawable.main_button);
        btnMore.setPadding(20,10,20,10);
        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout4);
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
    }

    private void getInfo(EBook eBook, String file) {

        if (eBook.cover != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(eBook.cover, 0,
                    eBook.cover.length);
            if (bitmap != null) {
                int COVER_MAX_W = 280;
                int width = Math.min(COVER_MAX_W, bitmap.getWidth());
                int height = (width * bitmap.getHeight())/bitmap.getWidth();
                cover = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }
        }

        ImageView img = (ImageView) findViewById(R.id.cover);
        if (cover != null) {
            img.setImageBitmap(cover);
        }else {
            img.setImageResource(R.drawable.icon_book_list);
        }
        TextView tv = (TextView) findViewById(R.id.tvTitle);
        tv.setText(eBook.title);
        tv = (TextView) findViewById(R.id.tvAnnotation);
        if (eBook.annotation != null) {
            eBook.annotation = eBook.annotation.trim()
                    .replace("<p>", "")
                    .replace("</p>", "\n");
            tv.setText(eBook.annotation);
        } else
            tv.setVisibility(View.GONE);
        ListView lv = (ListView) findViewById(R.id.authors);
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
            final ArrayAdapter<String> lvAdapter = new ArrayAdapter<>(this, R.layout.item_bookinfo, authors);
            lv.setAdapter(lvAdapter);
        }
        tv = (TextView) findViewById(R.id.tvSeries);
        if (eBook.sequenceName != null) {
            tv.setText(eBook.sequenceName);
        }

        ((TextView) findViewById(R.id.book_title)).setText(file.substring(file.lastIndexOf("/") + 1));
    }
}
