package com.harasoft.relaunch.Support;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.harasoft.relaunch.R;
import com.harasoft.relaunch.Utils.UtilHistory;

import java.io.*;
import java.util.*;

public class Filters {
    private Context context;
    private List<String[]> list_filters;
    private String FILT_FILE = "Filters.txt";
    private String DELIMITER = ":";
    // Filter values
    public int FLT_SELECT;
    private int FLT_STARTS;
    private int FLT_ENDS;
    private int FLT_CONTAINS;
    private int FLT_MATCHES;
    public int FLT_NEW;
    public int FLT_NEW_AND_READING;
    public boolean filters_and;
    // Reading files
    private final int FileBufferSize = 1024;

    public Filters(Context context) {
        this.context = context;
        readFromFile(FILT_FILE, DELIMITER);
        FLT_SELECT = context.getResources().getInteger(R.integer.FLT_SELECT);
        FLT_STARTS = context.getResources().getInteger(R.integer.FLT_STARTS);
        FLT_ENDS = context.getResources().getInteger(R.integer.FLT_ENDS);
        FLT_CONTAINS = context.getResources().getInteger(R.integer.FLT_CONTAINS);
        FLT_MATCHES = context.getResources().getInteger(R.integer.FLT_MATCHES);
        FLT_NEW = context.getResources().getInteger(R.integer.FLT_NEW);
        FLT_NEW_AND_READING = context.getResources().getInteger(R.integer.FLT_NEW_AND_READING);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        filters_and = prefs.getBoolean("filtersAnd", true);
    }

    private boolean filterFile1(String dname, String fname, Integer method, String value) {
        if (method == FLT_STARTS)
            return fname.startsWith(value);
        else if (method == FLT_ENDS)
            return fname.endsWith(value);
        else if (method == FLT_CONTAINS)
            return fname.contains(value);
        else if (method == FLT_MATCHES)
            return fname.matches(value);
        else if (method == FLT_NEW) {
            String fullName = dname + "/" + fname;
            return (new UtilHistory(context)).getState(ResourceLocation.LOCAL, fullName) == BookState.NEW;
        }else if (method == FLT_NEW_AND_READING) {
            String fullName = dname + "/" + fname;
            return (new UtilHistory(context)).getState(ResourceLocation.LOCAL, fullName) != BookState.NONE;
        } else
            return false;
    }

    public boolean filterFile(String dname, String fname) {
        List<String[]> filters = getList();
        if (filters.size() > 0) {
            for (String[] f : filters) {
                Integer filtMethod = 0;
                try {
                    filtMethod = Integer.parseInt(f[0]);
                } catch (NumberFormatException e) {
                    //emply
                }
                if (filters_and) {
                    // AND all filters
                    if (!filterFile1(dname, fname, filtMethod, f[1]))
                        return false;
                } else {
                    // OR all filters
                    if (filterFile1(dname, fname, filtMethod, f[1]))
                        return true;
                }
            }
            return filters_and;
        } else
            return true;
    }
    public List<String[]> getList() {
        if (list_filters != null)
            return list_filters;
        else
            return new ArrayList<>();
    }

    // Read misc. lists
    private void readFromFile(String fileName, String delimiter) {
        FileInputStream fis;
        try {
            fis = context.openFileInput(fileName);
        } catch (FileNotFoundException e) {
            fis = null;
        }
        if (fis != null) {
            InputStreamReader insr;
            try {
                insr = new InputStreamReader(fis, "utf8");
            } catch (UnsupportedEncodingException e) {
                return;
            }
            BufferedReader bufr = new BufferedReader(insr, FileBufferSize);
            String line;
            while (true) {
                try {
                    line = bufr.readLine();
                } catch (IOException e) {
                    return;
                }
                if (line == null)
                    break;
                if (line.length() > 0) {
                    addToList(line, delimiter);
                }
            }
            try {
                bufr.close();
                insr.close();
                fis.close();
            } catch (IOException e) {
                //emply
            }
        }
    }

    // Save to file miscellaneous lists
    public void writeFile(List<String[]> new_list_filters) {
        if (new_list_filters == null)
            return;

        FileOutputStream fos;
        try {
            fos = context.openFileOutput(FILT_FILE, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            return;
        }
        for (String[] list_filter : new_list_filters) {
            String line = list_filter[0] + DELIMITER + list_filter[1] + "\n";
            try {
                fos.write(line.getBytes());
            } catch (IOException e) {
                //Log.i("========= writeFile ===================", "--------------------------------error listName=" + listName);
            }
        }
        try {
            fos.close();
        } catch (IOException e) {
            //Log.i("========= writeFile ===================", "--------------------------------error listName=" + listName);
        }
        //Log.i("========= writeFile ===================", "--------------------------------end listName=" + listName);
    }
    // Add to list
    private void addToList(String fullName, String delimiter) {
        int ind = fullName.indexOf(delimiter);
        if (ind < 0)
            return;
        if (ind + delimiter.length() >= fullName.length())
            return;
        String dname = fullName.substring(0, ind);
        String fname = fullName.substring(ind + delimiter.length());
        addToList_internal(dname, fname);
    }
    private void addToList_internal(String dr, String fn) {
        if (list_filters == null) {
            list_filters = new ArrayList<>();
        }

        for (int i = 0, size = list_filters.size(); i < size; i++) {
            if (list_filters.get(i)[0].equals(dr) && list_filters.get(i)[1].equals(fn)) {
                list_filters.remove(i);
                break;
            }
        }

        String[] entry = new String[] { dr, fn };
        list_filters.add(entry);
    }
}
