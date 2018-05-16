package com.harasoft.relaunch.Support;

import com.harasoft.relaunch.Adapter.ViewItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortViewItems {
    private List<ViewItem> arr_view_items;
    private String sortType;
    private boolean sortOrder;

    public SortViewItems(List<ViewItem> arr_view_items, String sortType, boolean sortOrder){
        this.arr_view_items = arr_view_items;
        this.sortType = sortType;
        this.sortOrder = sortOrder;
    }

    private class ComparatorName implements Comparator<ViewItem>{
        @Override
        public int compare(ViewItem o1, ViewItem o2) {
            String str1 = o1.getFile_name();
            String str2 = o2.getFile_name();

            return str1.compareTo(str2);
        }
    }

    private class ComparatorDate implements Comparator<ViewItem>{
        @Override
        public int compare(ViewItem o1, ViewItem o2) {
            long time1 = o1.getFile_time();
            long time2 = o2.getFile_time();

            if(time1 == time2) return 0;
            if(time1 < time2) return 1;
            return -1;
        }
    }

    private class ComparatorSize implements Comparator<ViewItem>{
        @Override
        public int compare(ViewItem o1, ViewItem o2) {
            long size1 = o1.getFile_size();
            long size2 = o2.getFile_size();

            if(size1 == size2) return 0;
            if(size1 < size2) return 1;
            return -1;
        }
    }

    private class ComparatorTitle implements Comparator<ViewItem>{
        @Override
        public int compare(ViewItem o1, ViewItem o2) {
            String title1 = o1.getBook_name_string();
            if (title1.length() == 0) {
                title1 = o1.getFile_name();
            }
            String title2 = o2.getBook_name_string();
            if (title2.length() == 0) {
                title2 = o2.getFile_name();
            }

            return title1.compareTo(title2);
        }
    }

    public void sortViewItem() {

        if ("name".equals(sortType)) {

            if (sortOrder) {
                Collections.sort(arr_view_items, new ComparatorName());
            }else {
                Collections.sort(arr_view_items, Collections.reverseOrder(new ComparatorName()));
            }
        }

        if ("date".equals(sortType)) {

            if (sortOrder) {
                Collections.sort(arr_view_items, new ComparatorDate());
            }else {
                Collections.sort(arr_view_items, Collections.reverseOrder(new ComparatorDate()));
            }
        }

        if ("size".equals(sortType)) {
            if (arr_view_items.get(0).getFile_type() == TypeResource.DIR) {
                Collections.sort(arr_view_items, new ComparatorName());
                return;
            }

            if (sortOrder) {
                Collections.sort(arr_view_items, new ComparatorSize());
            }else {
                Collections.sort(arr_view_items, Collections.reverseOrder(new ComparatorSize()));
            }
        }

        if ("title".equals(sortType)) {
            if (arr_view_items.get(0).getFile_type() == TypeResource.DIR) {
                if (sortOrder) {
                    Collections.sort(arr_view_items, new ComparatorName());
                }else {
                    Collections.sort(arr_view_items, Collections.reverseOrder(new ComparatorName()));
                }
                return;
            }

            if (sortOrder) {
                Collections.sort(arr_view_items, new ComparatorTitle());
            }else {
                Collections.sort(arr_view_items, Collections.reverseOrder(new ComparatorTitle()));
            }
        }
    }
}
