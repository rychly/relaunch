package com.harasoft.relaunch.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.harasoft.relaunch.R;
import com.harasoft.relaunch.Support.TypeResource;
import com.harasoft.relaunch.Utils.UtilIcons;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haron. on 10.02.2018.
 * очередная попытка сделать универсальный адаптер
 */
public class AdapterRL extends BaseAdapter {
    private Context context;
    private LayoutInflater lInflater;
    private List<ViewItem> view_items = new ArrayList<>();

    private int firstLineFontSizePx;
    private int secondLineFontSizePx;
    private boolean showRowSeparator;
    private boolean singleLineMode;
    private boolean showStatysRead;

    private UtilIcons util_Icons;

    private int colum_width;
    private int currentColsNum;

    public AdapterRL(Context context, List<ViewItem> view_items, int colum_width, int columns_num) {
        this.context = context;
        this.colum_width = colum_width;
        this.currentColsNum = columns_num;
        this.lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        firstLineFontSizePx = Integer.parseInt(prefs.getString("firstLineFontSizePx", "20"));
        secondLineFontSizePx = Integer.parseInt(prefs.getString("secondLineFontSizePx", "16"));
        showRowSeparator = prefs.getBoolean("showRowSeparator", false);
        singleLineMode = prefs.getBoolean("singleLineMode", false);
        showStatysRead = prefs.getBoolean("showNewBook", true);

        util_Icons = new UtilIcons(context);
        updateReceiptsList(view_items);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return view_items.size();
    }

    // элемент по позиции
    @Override
    public ViewItem getItem(int position) {
        return view_items.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        ViewHolder holder;
        ViewItem vitem = getItem(position);
        if (view == null) {
            view = lInflater.inflate(R.layout.item_adapterrl, parent, false);

            holder = new ViewHolder();
            holder.first_line = (TextView) view.findViewById(R.id.fl_text);
            holder.second_line = (TextView) view.findViewById(R.id.fl_text2);
            holder.icon = (ImageView) view.findViewById(R.id.fl_icon);
            holder.separator = (ImageView) view.findViewById(R.id.fl_separator);
            holder.tvHolder = (LinearLayout) view.findViewById(R.id.grid_cell);
            holder.position = position;
            // проверяем на однострочный режим
            if (singleLineMode) {
                holder.first_line.setLines(1); // первой - только одна строка
                holder.first_line.setHorizontallyScrolling(true); // разрешить прокрутку по горизонтали
                holder.first_line.setEllipsize(TextUtils.TruncateAt.END); // многоточие на конце видимой части
                holder.second_line.setLines(1);  // только одна строка
                holder.second_line.setHorizontallyScrolling(true); // прокрутка
                holder.second_line.setEllipsize(TextUtils.TruncateAt.END);   // многоточие
            }
            // если разделитель запрещен
            if (!showRowSeparator){
                holder.separator.setVisibility(View.GONE);// выключаем его
            }

            view.setTag(holder);
        } else{
            holder = (ViewHolder) view.getTag();
        }

        // ------------ иконка -------------------------------------------
        if (vitem.getIcon_name().equals("none")){
            holder.icon.setVisibility(View.GONE); // скрываем поле с ними
        }else {
            holder.icon.setImageBitmap(util_Icons.getIconFile(vitem.getIcon_name()));
        }
        //=================================================================
        // ------------ размеры шрифтов -----------------------------------
        holder.first_line.setTextSize(TypedValue.COMPLEX_UNIT_PX, firstLineFontSizePx);
        holder.second_line.setTextSize(TypedValue.COMPLEX_UNIT_PX, secondLineFontSizePx);
        //=================================================================
        // ------------ оформление строк ----------------------------------
        // предварительные цвета
        int color_txt = context.getResources().getColor(R.color.file_new_fg);
        int backgroung_txt = context.getResources().getColor(R.color.file_new_bg);

        // выделение жирым
        boolean setBold = false;

        // цвета в зависимости от условий
        if (vitem.getFile_type() == TypeResource.DIR) {
            holder.second_line.setVisibility(View.GONE); // скрываем вторую строку
        } else {
            if (showStatysRead) {  // прочитанные/новые
                switch (vitem.getFile_status_read()) {
                    case 0: // BookState.NONE
                    case 1: // BookState.NEW
                        color_txt = context.getResources().getColor(R.color.file_new_fg);
                        backgroung_txt = context.getResources().getColor(R.color.file_new_bg);
                        setBold = true;
                        break;
                    case 2: // BookState.READING
                        color_txt = context.getResources().getColor(R.color.file_reading_fg);
                        backgroung_txt = context.getResources().getColor(R.color.file_reading_bg);
                        break;
                    case 3: // BookState.FINISHED
                        color_txt = context.getResources().getColor(R.color.file_finished_fg);
                        backgroung_txt = context.getResources().getColor(R.color.file_finished_bg);
                        break;
                }
            }

        }

        // при выделении ячейки
        if(vitem.isSelected()){
            holder.first_line.setTextColor(backgroung_txt);
            holder.second_line.setTextColor(backgroung_txt);
            holder.tvHolder.setBackgroundColor(color_txt);//context.getResources().getColor(R.color.file_finished_bg));
        }else{
            holder.first_line.setTextColor(color_txt);
            holder.second_line.setTextColor(color_txt);
            holder.tvHolder.setBackgroundColor(backgroung_txt);
        }
        //
        if (showStatysRead && vitem.getFile_type() != TypeResource.DIR) {
            SpannableString span = new SpannableString(vitem.getFirst_string());
            span.setSpan(new StyleSpan(setBold ? Typeface.BOLD : Typeface.ITALIC), 0, vitem.getFirst_string().length(), 0);
            holder.first_line.setText(span);
        } else {
            holder.first_line.setText(vitem.getFirst_string());
        }
        if (vitem.getSecond_string() == null){
            holder.second_line.setVisibility(View.GONE); // скрываем вторую строку
        }else {
            //holder.second_line.setVisibility(View.VISIBLE); // показываем вторую строку
            holder.second_line.setText(vitem.getSecond_string());
        }
        // ===================================================================
        // если у грида не одна колонка, то выравниваем ячейки по высоте в одной строке
        if (currentColsNum > 1) {
            int recalc_num = position; // номер позиции
            int recalc_height = 0;
            View temp_v;
            while (recalc_num % currentColsNum != 0) {  // находим последний элемент в строке
                recalc_num --;
                temp_v = getView(recalc_num, null, parent);
                if(temp_v != null){
                    temp_v.measure(View.MeasureSpec.EXACTLY | colum_width, View.MeasureSpec.UNSPECIFIED);
                    int p_height = temp_v.getMeasuredHeight();
                    if (p_height > 0)
                        recalc_height = p_height;
                }
            }
            if (recalc_height > 0) {
                view.setMinimumHeight(recalc_height);
            }
        }

        return view;
    }

    public void setColumn(int column_count, int grid_view_width) {
        if (column_count > 1) {
            this.currentColsNum = column_count;
            this.colum_width = grid_view_width / currentColsNum;
        }
    }

    public void updateReceiptsList(List<ViewItem> new_view_items) {
        if (view_items == null) {
            view_items = new ArrayList<>();
        }
        view_items.clear();
        if (new_view_items != null) {
            view_items.addAll(new_view_items);
        }
        this.notifyDataSetChanged(); }
}
