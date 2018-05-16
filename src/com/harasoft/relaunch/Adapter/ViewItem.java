package com.harasoft.relaunch.Adapter;

/**
 * Created by anatolijvit. on 10.02.2018.
 * класс для отображения сведений в адаптере
 */
public class ViewItem {
    private  String file_path;
    private String file_name;
    private String icon_name;
    private String first_string;
    private String second_string;
    private String book_name_string;
    private long file_size;
    private long file_time;
    private int file_status_read;
    private int file_type;
    private int item_height;
    private boolean selected;

    // ------------ get ----------------------
    public String getFile_path() {
        return file_path;
    }

    public String getFile_name() {
        return file_name;
    }

    public String getIcon_name() {
        return icon_name;
    }

    public String getFirst_string() {
        return first_string;
    }

    public String getSecond_string() {
        return second_string;
    }

    public String getBook_name_string() {
        return book_name_string;
    }

    public long getFile_size() {
        return file_size;
    }

    public long getFile_time() {
        return file_time;
    }

    public int getFile_status_read() {
        return file_status_read;
    }

    public int getFile_type() {
        return file_type;
    }

    public int getItem_height() {
        return item_height;
    }

    public boolean isSelected() {
        return selected;
    }

    // ----------- set -----------------------
    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public void setIcon_name(String icon_name) {
        this.icon_name = icon_name;
    }

    public void setFirst_string(String first_string) {
        this.first_string = first_string;
    }

    public void setSecond_string(String second_string) {
        this.second_string = second_string;
    }

    public void setBook_name_string(String book_name_string) {
        this.book_name_string = book_name_string;
    }

    public void setFile_size(long file_size) {
        this.file_size = file_size;
    }

    public void setFile_time(long file_time) {
        this.file_time = file_time;
    }

    public void setFile_status_read(int file_status_read) {
        this.file_status_read = file_status_read;
    }

    public void setFile_type(int file_type) {
        this.file_type = file_type;
    }

    public void setItem_height(int item_height) {
        this.item_height = item_height;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
