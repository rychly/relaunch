package com.harasoft.relaunch.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import ebook.EBook;
import ebook.parser.InstantParser;
import ebook.parser.Parser;

import java.util.regex.Pattern;

/**
 * Created by anat on 07.10.17.
 * Работа с книгами
 */
public class UtilBooks {
    private String book_name_format;

    public UtilBooks(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        book_name_format = prefs.getString("bookTitleFormat", "%t[\n%a][. %s][-%n]");
    }
    // ============== работа с книгами ================================
    // добавление книги в базу
    private Pattern purgeBracketsPattern = Pattern.compile("\\[[\\[\\]\\s\\.\\-_]*\\]");

    private String getEbookName(String fileName, String format) {
        EBook eBook;
        String file = fileName.substring(fileName.lastIndexOf('/') + 1,fileName.length());
        if ((!file.endsWith("fb2")) && (!file.endsWith("fb2.zip"))&& (!file.endsWith("epub"))) {
            return file;
        }

        Parser parser = new InstantParser();
        eBook = parser.parse(fileName);
        if (eBook.isOk) {
            if ((eBook.sequenceNumber != null)&& (eBook.sequenceNumber.length() == 1))
                eBook.sequenceNumber = "0" + eBook.sequenceNumber;
        }

        if (eBook.isOk) {
            String output = format;
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
            output = output.replace("%f", fileName);
            output = purgeBracketsPattern.matcher(output).replaceAll("");
            output = output.replace("[", "");
            output = output.replace("]", "");
            return output;
        } else {
            return file;
        }
    }
    public String getEbookName(String dir, String file, String format) {
        return getEbookName(dir + "/" + file, format);
    }
    public String getBookName(String file_path, String file_name) {
        String file_full_path;
        if (file_path.equals("/")) {
            file_full_path = file_path + file_name;
        }else {
            file_full_path = file_path + "/" + file_name;
        }
        return getEbookName(file_full_path, book_name_format);
    }
}
