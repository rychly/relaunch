package com.harasoft.relaunch.LocalFile;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.harasoft.relaunch.Adapter.ViewItem;
import com.harasoft.relaunch.Support.Filters;
import com.harasoft.relaunch.Support.TypeResource;
import com.harasoft.relaunch.Utils.UtilBooks;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

public class LocalFile {
    private boolean show_book_name;
    private Context context;
    private boolean showHidden;
    private boolean showOnlyKnownExts;
    private ArrayList<String> exts;
    private boolean filterResults;
    private Filters filters;

    public LocalFile(Context context) {
        this.context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.show_book_name = prefs.getBoolean("showBookTitles", false);
        this.showHidden = prefs.getBoolean("showHidden", false);
        this.showOnlyKnownExts = prefs.getBoolean("showOnlyKnownExts", false);
        if (showOnlyKnownExts) {
            // получаем массив расширений
            exts = getExts();
        }
        //=================================================================================
        // ====== redrawList==================================================================
        // включает дополнительный фильтр по файлам.
        filterResults = prefs.getBoolean("filterResults", false);
        if (filterResults) {
            filters = new Filters(context);
        }
    }

    public List<ViewItem> getListItems(String folder) {
        List<ViewItem> list_items = new ArrayList<>();
        // получаем папку как объект
        File dir = new File(folder);
        // получаем список подпапок и файлов
        File[] all_entries = dir.listFiles();

        // полученный массив элементов разбиваем на папки и файлы
        if (all_entries != null) {
            UtilBooks  util_books = new UtilBooks(context);
            for (File entry : all_entries) {
                String file_name = entry.getName();
                if (!showHidden && file_name.startsWith(".")) {
                    continue;
                }
                ViewItem file = new ViewItem();


                if (entry.isDirectory()) {
                    file.setFile_type(TypeResource.DIR);
                    file.setFile_name(file_name);
                    file.setFile_time(entry.lastModified());
                } else {
                    // фильтр по зарег. расширениям
                    if (showOnlyKnownExts) {  // пропускаем если расширение не зарегистрировано
                        boolean hide = true;
                        for (String ext : exts) { // прогоняем все расширения через имя файла
                            if (entry.getName().endsWith(ext)) {
                                hide = false; // зарегистрировано
                            }
                        }
                        if (hide) {// ели не зарегистрировано, то пропускаем
                            continue;
                        }
                    }
                    // если установлены фильтры
                    if (filterResults && !filters.filterFile(entry.getPath(), file_name)) {
                        continue;
                    }

                    file.setFile_name(file_name);
                    file.setFile_time(entry.lastModified());
                    file.setFile_type(TypeResource.FILE);
                    file.setFile_size(entry.length());
                    if (show_book_name) {
                        if ((file_name.endsWith("fb2")) || (file_name.endsWith("fb2.zip")) || (file_name.endsWith("epub")))
                            file.setBook_name_string(util_books.getBookName(folder, file_name));
                    }
                }

                list_items.add(file);
            }
        }
        return list_items;
    }
    public long getFileSize (String file_full_name) {
        return (new File(file_full_name)).length();
    }
    public String getParentPathForDir(String dir){
        return (new File(dir)).getParent();
    }
    public String getNameDir(String file_full_name){
        if (file_full_name.equals("/")) {
            return "/";
        }else {
            return (new File(file_full_name)).getName();
        }
    }
    public int getItemsInFolder(String full_dir_path) {
        File d = new File(full_dir_path);
        if (d.list() == null) {
            return 0;
        }else {
            return d.list().length;
        }
    }
    // delete
    public boolean itemRemove(String full_file_name){
        File file = new File(full_file_name);

        return fileRemove(file);
    }
    public boolean fileRemove(File file) {

        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            File[] allEntries = file.listFiles();
            if (allEntries == null) {
                return false;
            }
            for (File allEntry : allEntries) {
                if (allEntry.isDirectory()) {
                    if (!fileRemove(allEntry)) {
                        return false;
                    }
                } else {
                    if (!allEntry.delete()) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }
    //Copy file src to dst
    public boolean copyPrefs(String from, String to) {
        File fromDir = new File(from);
        File toDir = new File(to);
        if (!fromDir.exists())
            return false;
        if (!toDir.exists())
            if (!toDir.mkdir())
                return false;
        File tDir = new File(toDir.getAbsolutePath() + "/files");
        if (!tDir.exists()) {
            if (!tDir.mkdir())
                return false;
        }else{
            fileRemove(new File(toDir.getAbsolutePath() + "/files"));
            if (!tDir.mkdir())
                return false;
        }
        tDir = new File(toDir.getAbsolutePath() + "/databases");
        if (!tDir.exists()) {
            if (!tDir.mkdir())
                return false;
        }else{
            fileRemove(new File(toDir.getAbsolutePath() + "/databases"));
            if (!tDir.mkdir())
                return false;
        }
        tDir = new File(toDir.getAbsolutePath() + "/shared_prefs");
        if (!tDir.exists()){
            if (!tDir.mkdir())
                return false;
        }else{
            fileRemove(new File(toDir.getAbsolutePath() + "/shared_prefs"));
            if (!tDir.mkdir())
                return false;
        }
        String file_name = "Filters.txt";
        String src1 = fromDir.getAbsolutePath() + "/files/" + file_name;
        String dst1 = toDir.getAbsolutePath() + "/files/" + file_name;
        File file = new File(src1);
        if(file.exists()) {
            copyAll(src1, dst1, true);
        }

        File dirName = new File(fromDir.getAbsolutePath() + "/databases");
        String[] DBlist = dirName.list();
        for (String aDBlist : DBlist) {
            String src = fromDir.getAbsolutePath() + "/databases/" + aDBlist;
            String dst = toDir.getAbsolutePath() + "/databases/" + aDBlist;
            copyAll(src, dst, true);
        }

        String src = fromDir.getAbsolutePath() + "/shared_prefs/com.harasoft.relaunch_preferences.xml";
        String dst = toDir.getAbsolutePath() + "/shared_prefs/com.harasoft.relaunch_preferences.xml";

        return copyAll(src, dst, true);
    }
    private boolean copyFile(String from, String to, boolean rewrite) {
        File srcFile = new File(from);
        File dstFile = new File(to);
        FileChannel src;
        FileChannel dst;

        if ((!srcFile.canRead()) || ((dstFile.exists()) && (!rewrite)))
            return false;
        try {
            if(dstFile.createNewFile()) {
                src = new FileInputStream(srcFile).getChannel();
                dst = new FileOutputStream(dstFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    private boolean copyDir(String from, String to, boolean rewrite) {
        File toDir = new File(to);
        String[] strDirList = (new File(from)).list();

        if(!toDir.exists()){
            if(!createDir(to)){
                return false;
            }
        }
        for (String aStrDirList1 : strDirList) {
            File f1 = new File(from +"/" + aStrDirList1);
            if (f1.isFile()) {
                if (!copyFile(from +"/" + aStrDirList1, to +"/" + aStrDirList1, rewrite)){
                    return false;
                }
            } else {
                if (!copyDir(from +"/" + aStrDirList1, to + "/" +aStrDirList1, rewrite)){
                    return false;
                }
            }
        }
        return true;
    }
    public boolean copyAll(String from, String to, boolean rewrite) {
        File source = new File(from);
        if (source.isFile()) {
            if (!copyFile(from, to, rewrite)) {
                return false;
            }
        } else {
            if (!copyDir(from, to, rewrite)) {
                return false;
            }
        }
        return true;
    }
    //Move file src to dst
    public boolean moveFile(String from, String to) {

        boolean ret;
        if (from.split("/")[0].equalsIgnoreCase(to.split("/")[0])) {
            File src = new File(from);
            File dst = new File(to);
            ret = src.renameTo(dst);
        } else {
            File file = new File(from);
            ret = file.renameTo(new File(to));
        }
        return ret;
    }
    public boolean createDir(String dst) {
        return (new File(dst)).mkdirs();

    }
    public HashMap<String,String> getFileInfo(String full_file_path) {
        File file;
        String fileSize;
        String fileTime;
        String filePerm = "";
        String fileOwn = "";
        String fileDir = "false";

        file = new File(full_file_path);
        fileSize = String.valueOf(file.length());
        fileTime = (new Date(file.lastModified())).toLocaleString();
        if (file.isDirectory()){
            fileDir = "true";
        }
        String fileAttr;
        try {
            Runtime rt = Runtime.getRuntime();
            String[] args;
            if (file.isDirectory()){
                args = new String[]{"ls", "-l", file.getParent(), "|grep", full_file_path};
            }else {
                args = new String[]{"ls", "-l", full_file_path};
            }
            Process proc = rt.exec(args);
            //String str = filename.replace(" ", "\\ ");
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = br.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            br.close();
            proc.waitFor();
            fileAttr = output.toString();
        } catch (Throwable t) {
            fileAttr = "";
        }
        if(fileAttr != null && fileAttr.length()>0) {
            fileAttr = fileAttr.replaceAll(" +", " ");
            int iPerm = fileAttr.indexOf(" ");
            int iOwner = fileAttr.indexOf(" ", iPerm + 1);
            int iGroup = fileAttr.indexOf(" ", iOwner + 1);
            filePerm = fileAttr.substring(1, iPerm);
            fileOwn = fileAttr.substring(iPerm + 1, iOwner) + "/" + fileAttr.substring(iOwner + 1, iGroup);
        }
        HashMap<String,String> file_info = new HashMap<>();
        file_info.put("name", file.getName());
        file_info.put("size", fileSize);
        file_info.put("date", fileTime);
        file_info.put("permit", filePerm);
        file_info.put("own", fileOwn);
        file_info.put("dir", fileDir);

        return file_info;
    }

    private ArrayList<String> getExts() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String typesString = prefs.getString("types", "");

        ArrayList<String> rc = new ArrayList<>();
        String[] rdrs = typesString.split("\\|");
        for (String rdr : rdrs) {
            String[] re = rdr.split(":");
            String[] exts = re[0].split(",");
            Collections.addAll(rc, exts);
        }
        return rc;
    }

}
