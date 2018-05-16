package com.harasoft.relaunch.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.harasoft.relaunch.DB.ListButtonsPanel;
import com.harasoft.relaunch.DB.ListPanels;
import com.harasoft.relaunch.DB.RelaunchDBHelper;
import com.harasoft.relaunch.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anat on 08.10.17.
 * Работа с панелями
 */
public class UtilPanels {
    private Context context;

    public UtilPanels(Context context) {
        this.context = context;
    }
    // ============== работа с Buttons Panel ================================
    // формирование стандартных панелей
    private void addStandartPanels() {
        // ==========создаем запись с первой панелью==========
        // сама панель
        addFirstStandatrPanel (0, 0);
        // ===================================================
        // ==========создаем запись со второй панелью==========
        addSecondStandatrPanel (1, 1);
        // ===================================================
        // ==========создаем запись с третьей панелью==========
        addThirdStandatrPanel (2, 2);
        // ===================================================
        // ==========создаем запись с четвертой панелью==========
        addFourthStandatrPanel (3, 3);
        // ===================================================
        // ==========создаем запись с пятой панелью==========
        addFifthStandatrPanel (4, 4);
    }
    private void addFirstStandatrPanel (int id_panel, int pos_screen) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        if(db == null){
            return;
        }
        // ==========создаем запись с первой панелью==========
        // сама панель
        cv.put(ListPanels.COLUMN_NUMBER_PANEL, id_panel);
        cv.put(ListPanels.COLUMN_PANEL_NAME, context.getResources().getString(R.string.pref_i_showOnePanel_name));
        cv.put(ListPanels.COLUMN_NUMBER_ON_SCREEN, pos_screen);
        // тип панели// тип панели
        cv.put(ListPanels.COLUMN_TYPE_PANEL, ListPanels.PANEL_FOR_BUTTONS);
        // вставляем запись
        db.insert(ListPanels.TABLE_NAME, null, cv);
        // создаём стандартные кнопки для нее
        // ------- кнопка Домой
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "HOMEN");
        cv.put(ListButtonsPanel.COLUMN_RUN_DOUBLE_CLICK, "HOMEMENU");
        cv.put(ListButtonsPanel.COLUMN_RUN_LONG_CLICK, "HOMESCREEN");
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 0);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);
        // -------кнопка Настройки
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "SETTINGS");
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 4);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);
        // ------- кнопка Поиск
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "SEARCH");
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 3);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);
        // ------- кнопка Последние открытые книги
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "LRUSCREEN");
        cv.put(ListButtonsPanel.COLUMN_RUN_DOUBLE_CLICK, "LRUMENU");
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 1);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);
        // ------- кнопка Фавориты
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "FAVDOCSCREEN");
        cv.put(ListButtonsPanel.COLUMN_RUN_DOUBLE_CLICK, "FAVDOCMENU");
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 2);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);

        closeDb(DbHelper, db, null);
    }
    private void addSecondStandatrPanel (int id_panel, int pos_screen) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        if(db == null){
            return;
        }
        // ==========создаем запись со второй панелью==========
        // сама панель
        cv.put(ListPanels.COLUMN_NUMBER_PANEL, id_panel);
        cv.put(ListPanels.COLUMN_PANEL_NAME, context.getResources().getString(R.string.pref_i_showTwoPanel_name));
        cv.put(ListPanels.COLUMN_NUMBER_ON_SCREEN, pos_screen);
        // тип панели// тип панели
        cv.put(ListPanels.COLUMN_TYPE_PANEL, ListPanels.PANEL_FOR_BUTTONS);
        // вставляем запись
        db.insert(ListPanels.TABLE_NAME, null, cv);
        // создаём стандартные кнопки для нее
        // ------- кнопка на которой показывается текущий каталог
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "SORTMENU");
        cv.put(ListButtonsPanel.COLUMN_RUN_LONG_CLICK, "SELNUMCOL");
        cv.put(ListButtonsPanel.COLUMN_IDENT_ICON, ListButtonsPanel.ICON_TEXT);
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 0);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);

        closeDb(DbHelper, db, null);
    }
    private void addThirdStandatrPanel (int id_panel, int pos_screen) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        if(db == null){
            return;
        }
        // ==========создаем запись с третьей панелью==========
        // сама панель
        cv.put(ListPanels.COLUMN_NUMBER_PANEL, id_panel);
        cv.put(ListPanels.COLUMN_PANEL_NAME, context.getResources().getString(R.string.pref_i_showThreePanel_name));
        cv.put(ListPanels.COLUMN_NUMBER_ON_SCREEN, pos_screen);
        cv.put(ListPanels.COLUMN_TYPE_PANEL, ListPanels.PANEL_FOR_BUTTONS);
        // вставляем запись
        db.insert(ListPanels.TABLE_NAME, null, cv);
        // создаём стандартные кнопки для нее
        // ------- кнопка Дополнительные настройки
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "ADVANCED");
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 3);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);
        // ------- кнопка выход в родительский каталог
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "UPDIR");
        cv.put(ListButtonsPanel.COLUMN_IDENT_ICON, ListButtonsPanel.ICON_IMAGE_TEXT);
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 0);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);
        // ------- кнопка листания вверх
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "UPSCROLL");
        cv.put(ListButtonsPanel.COLUMN_RUN_DOUBLE_CLICK, "UPSCROLLPERC");
        cv.put(ListButtonsPanel.COLUMN_RUN_LONG_CLICK, "UPSCROLLBEGIN");
        cv.put(ListButtonsPanel.COLUMN_IDENT_ICON, ListButtonsPanel.ICON_IMAGE_TEXT);
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 1);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);
        // ------- кнопка листания вниз
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "DOWNSCROLL");
        cv.put(ListButtonsPanel.COLUMN_RUN_DOUBLE_CLICK, "DOWNSCROLLPERC");
        cv.put(ListButtonsPanel.COLUMN_RUN_LONG_CLICK, "DOWNSCROLLEND");
        cv.put(ListButtonsPanel.COLUMN_IDENT_ICON, ListButtonsPanel.ICON_IMAGE_TEXT);
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 2);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);

        closeDb(DbHelper, db, null);
    }
    private void addFourthStandatrPanel (int id_panel, int pos_screen) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        if(db == null){
            return;
        }
        // ==========создаем запись с четвертой панелью==========
        // сама панель
        cv.put(ListPanels.COLUMN_NUMBER_PANEL, id_panel);
        cv.put(ListPanels.COLUMN_PANEL_NAME, context.getResources().getString(R.string.pref_i_showFourPanel_name));
        cv.put(ListPanels.COLUMN_NUMBER_ON_SCREEN, pos_screen);
        // тип панели// тип панели
        cv.put(ListPanels.COLUMN_TYPE_PANEL, ListPanels.PANEL_FOR_LIST_FILES);
        // вставляем запись
        db.insert(ListPanels.TABLE_NAME, null, cv);

        closeDb(DbHelper, db, null);
    }
    private void addFifthStandatrPanel (int id_panel, int pos_screen) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        if(db == null){
            return;
        }
        // ==========создаем запись с пятой панелью==========
        // сама панель
        cv.put(ListPanels.COLUMN_NUMBER_PANEL, id_panel);
        cv.put(ListPanels.COLUMN_PANEL_NAME, context.getResources().getString(R.string.pref_i_showFifthPanel_name));
        cv.put(ListPanels.COLUMN_NUMBER_ON_SCREEN, pos_screen);
        // тип панели
        cv.put(ListPanels.COLUMN_TYPE_PANEL, ListPanels.PANEL_FOR_BUTTONS);
        // вставляем запись
        db.insert(ListPanels.TABLE_NAME, null, cv);
        // создаём стандартные кнопки для нее
        // ------- кнопка с датой и памятью
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "APPMANAGER");
        cv.put(ListButtonsPanel.COLUMN_IDENT_ICON, ListButtonsPanel.ICON_IMAGE_TEXT);
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 0);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);

        // ------- кнопка последних приложений
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "LASTAPP");
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 1);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);
        // ------- кнопка всех приложений
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "ALLAPP");
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 2);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);
        // ------- кнопка всех приложений
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "FAVAPP");
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 3);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);

        // ------- кнопка с wifi и батареей
        cv.clear();
        // уникальный номер панели для прикрепления
        cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, id_panel);
        // действия
        cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, "SWITCHWIFI");
        cv.put(ListButtonsPanel.COLUMN_IDENT_ICON, ListButtonsPanel.ICON_IMAGE_TEXT);
        // позиция
        cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, 4);
        // вставляем запись
        db.insert(ListButtonsPanel.TABLE_NAME, null, cv);

        closeDb(DbHelper, db, null);
    }
    // получение порядка расположения панелей на экране
    public ArrayList<HashMap<String,String>> getListPanelsOfScreen() {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        // делаем запрос данных из таблицы , получаем Cursor
        Cursor cursor = db.query(ListPanels.TABLE_NAME, null, null, null, null, null, null);
        // определяем индексы нужных столбцов
        int numberPanelIndex = cursor.getColumnIndex(ListPanels.COLUMN_NUMBER_PANEL);
        int panelNameIndex = cursor.getColumnIndex(ListPanels.COLUMN_PANEL_NAME);
        int numberOfScreenIndex = cursor.getColumnIndex(ListPanels.COLUMN_NUMBER_ON_SCREEN);
        int typeScreenIndex = cursor.getColumnIndex(ListPanels.COLUMN_TYPE_PANEL);
        // формируем список панелей
        HashMap<String, String> itemPanel;
        ArrayList<HashMap<String, String>> list_panels = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do{
                itemPanel = new HashMap<>();
                itemPanel.put(ListPanels.COLUMN_NUMBER_PANEL, cursor.getString(numberPanelIndex));
                itemPanel.put(ListPanels.COLUMN_PANEL_NAME, cursor.getString(panelNameIndex));
                itemPanel.put(ListPanels.COLUMN_NUMBER_ON_SCREEN, cursor.getString(numberOfScreenIndex));
                itemPanel.put(ListPanels.COLUMN_TYPE_PANEL, cursor.getString(typeScreenIndex));
                list_panels.add(itemPanel);
            } while (cursor.moveToNext());
        } else {
            addStandartPanels();
            cursor.close();
            cursor = db.query(ListPanels.TABLE_NAME, null, null, null, null, null, null);
            cursor.moveToFirst();
            do{
                itemPanel = new HashMap<>();
                itemPanel.put(ListPanels.COLUMN_NUMBER_PANEL, cursor.getString(numberPanelIndex));
                itemPanel.put(ListPanels.COLUMN_PANEL_NAME, cursor.getString(panelNameIndex));
                itemPanel.put(ListPanels.COLUMN_NUMBER_ON_SCREEN, cursor.getString(numberOfScreenIndex));
                itemPanel.put(ListPanels.COLUMN_TYPE_PANEL, cursor.getString(typeScreenIndex));

                list_panels.add(itemPanel);
            } while (cursor.moveToNext());
        }

        closeDb(DbHelper, db, cursor);
        list_panels.trimToSize();

        return sortListPanels(list_panels);
    }
    private ArrayList<HashMap<String,String>> sortListPanels(ArrayList<HashMap<String,String>> inputArray) {
        ArrayList<HashMap<String,String>> outputArray = new ArrayList<>();

        int ind;
        int count;
        int temp;
        do{
            ind = 0;
            count = Integer.parseInt(inputArray.get(0).get(ListPanels.COLUMN_NUMBER_ON_SCREEN));

            for (int z = 0, size = inputArray.size(); z < size; z++) {
                temp = Integer.parseInt(inputArray.get(z).get(ListPanels.COLUMN_NUMBER_ON_SCREEN));
                if (count > temp) {
                    count = temp;
                    ind = z;
                }
            }
            outputArray.add(inputArray.get(ind));
            inputArray.remove(ind);
        }while (inputArray.size() > 0);
        outputArray.trimToSize();
        return outputArray;
    }
    public void updatePanels(List<HashMap<String,String>> itemArray) {
        // очищаем от старых данных
        resetPanelsDb();
        int count = 0;
        // заносим новые
        for (HashMap<String,String> item: itemArray) {
            addNewPanel(item, count);
            count ++;
        }
    }
    private void addNewPanel(HashMap<String, String> panel, int num_on_screen) {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // создаем объект для данных
        ContentValues cv = new ContentValues();

        if(db == null){
            return;
        }

        // сама панель
        cv.put(ListPanels.COLUMN_NUMBER_PANEL, Integer.parseInt(panel.get(ListPanels.COLUMN_NUMBER_PANEL)));
        cv.put(ListPanels.COLUMN_PANEL_NAME, panel.get(ListPanels.COLUMN_PANEL_NAME));
        cv.put(ListPanels.COLUMN_NUMBER_ON_SCREEN, num_on_screen);
        // тип панели// тип панели
        cv.put(ListPanels.COLUMN_TYPE_PANEL, Integer.parseInt(panel.get(ListPanels.COLUMN_TYPE_PANEL)));
        // вставляем запись
        System.out.println(db.insert(ListPanels.TABLE_NAME, null, cv));




        closeDb(DbHelper, db, null);
    }
    public int getTypePanel(HashMap<String,String> panel) {
        return Integer.parseInt(panel.get(ListPanels.COLUMN_TYPE_PANEL));
    }
    public int getNumberPanel (HashMap<String,String> panel) {
        return Integer.parseInt(panel.get(ListPanels.COLUMN_NUMBER_PANEL));
    }
    public HashMap<String,String> getNewPanel(int numPanel, String namePanel, int typePanel) {
        HashMap<String,String> item = new HashMap<>();
        // сама панель
        item.put(ListPanels.COLUMN_NUMBER_PANEL, String.valueOf(numPanel));
        item.put(ListPanels.COLUMN_PANEL_NAME, namePanel);
        item.put(ListPanels.COLUMN_NUMBER_ON_SCREEN, "0");
        // тип панели// тип панели
        item.put(ListPanels.COLUMN_TYPE_PANEL, String.valueOf(typePanel));

        return item;
    }
    public HashMap<String,String> getStandartPanel(int add_panel, int uniqNum, int posScreen, String namePanel) {
        int typePanel = ListPanels.PANEL_FOR_BUTTONS;
        switch (add_panel) {
            case 1:
                addFirstStandatrPanel (uniqNum, posScreen);
                break;
            case 2:
                addSecondStandatrPanel (uniqNum, posScreen);
                break;
            case 3:
                addThirdStandatrPanel (uniqNum, posScreen);
                break;
            case 4:
                addFourthStandatrPanel (uniqNum, posScreen);
                typePanel = ListPanels.PANEL_FOR_LIST_FILES;
                break;
            case 5:
                addFifthStandatrPanel (uniqNum, posScreen);
                break;
        }
        HashMap<String,String> item = new HashMap<>();
        // сама панель
        item.put(ListPanels.COLUMN_NUMBER_PANEL, String.valueOf(uniqNum));
        item.put(ListPanels.COLUMN_PANEL_NAME, namePanel);
        item.put(ListPanels.COLUMN_NUMBER_ON_SCREEN, String.valueOf(posScreen));
        // тип панели// тип панели
        item.put(ListPanels.COLUMN_TYPE_PANEL, String.valueOf(typePanel));

        return item;
    }
    public void removePanel(int id) {
        System.out.println("removePanel");
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        if(db == null){
            return;
        }
        // настраиваем фильтр на удаление в таблице панелей
        String selection = ListPanels.COLUMN_NUMBER_PANEL + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(id) };
        // удаляем панель
        db.delete(ListPanels.TABLE_NAME, selection, selectionArgs);

        // настраиваем фильтр на удаление в таблице кнопок
        selection = ListButtonsPanel.COLUMN_NUMBER_PANEL + " = ?";
        // удаляем кнопки
        db.delete(ListButtonsPanel.TABLE_NAME, selection, selectionArgs);

        closeDb(DbHelper, db, null);
    }
    public String getNamePanel(HashMap<String, String> item) {
        return item.get(ListPanels.COLUMN_PANEL_NAME);
    }
    public boolean isPathPanel(HashMap<String, String> item) {
        return Integer.parseInt(item.get(ListPanels.COLUMN_TYPE_PANEL)) == ListPanels.PANEL_FOR_LIST_FILES;
    }
    // получаем все кнопки на панели. уже отсортированные как надо.
    public ArrayList<HashMap<String,String>> getListButtonsOfPanel(int num_panel) {
        ArrayList<HashMap<String,String>> list_buttons = new ArrayList<>();

        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // подключаемся в режиме чтения
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        if(db == null){
            return null;
        }
        // ищем запись в базе
        Cursor cursor;
        String selection = ListButtonsPanel.COLUMN_NUMBER_PANEL + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(num_panel) };
        // делаем запрос данных из таблицы , получаем Cursor
        cursor = db.query(ListButtonsPanel.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        // определяем индексы нужных столбцов
        int runOneClickIndex = cursor.getColumnIndex(ListButtonsPanel.COLUMN_RUN_ONE_CLICK);
        int nameOneClickIndex = cursor.getColumnIndex(ListButtonsPanel.COLUMN_NAME_ONE_CLICK);
        int runDoubleClickIndex = cursor.getColumnIndex(ListButtonsPanel.COLUMN_RUN_DOUBLE_CLICK);
        int nameDoubleClickIndex = cursor.getColumnIndex(ListButtonsPanel.COLUMN_NAME_DOUBLE_CLICK);
        int runLongClickIndex = cursor.getColumnIndex(ListButtonsPanel.COLUMN_RUN_LONG_CLICK);
        int nameLongClockIndex = cursor.getColumnIndex(ListButtonsPanel.COLUMN_NAME_LONG_CLICK);
        int numberIconIndex = cursor.getColumnIndex(ListButtonsPanel.COLUMN_IDENT_ICON);
        int buttonPositionIndex = cursor.getColumnIndex(ListButtonsPanel.COLUMN_BUTTON_POSITION);

        // формируем список кнопок
        HashMap<String, String> itemButton;
        if (cursor.moveToFirst()) {
            do{
                itemButton = new HashMap<>();
                itemButton.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, cursor.getString(runOneClickIndex));
                itemButton.put(ListButtonsPanel.COLUMN_NAME_ONE_CLICK, cursor.getString(nameOneClickIndex));

                itemButton.put(ListButtonsPanel.COLUMN_RUN_DOUBLE_CLICK, cursor.getString(runDoubleClickIndex));
                itemButton.put(ListButtonsPanel.COLUMN_NAME_DOUBLE_CLICK, cursor.getString(nameDoubleClickIndex));

                itemButton.put(ListButtonsPanel.COLUMN_RUN_LONG_CLICK, cursor.getString(runLongClickIndex));
                itemButton.put(ListButtonsPanel.COLUMN_NAME_LONG_CLICK, cursor.getString(nameLongClockIndex));

                itemButton.put(ListButtonsPanel.COLUMN_IDENT_ICON, cursor.getString(numberIconIndex));
                itemButton.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, cursor.getString(buttonPositionIndex));

                list_buttons.add(itemButton);
            } while (cursor.moveToNext());
        }

        closeDb(DbHelper, db, cursor);

        list_buttons.trimToSize();
        if (list_buttons.size() > 0) {
            list_buttons = sortListButtonsOfPanel(list_buttons);
        }


        return list_buttons;
    }
    private ArrayList<HashMap<String,String>> sortListButtonsOfPanel(ArrayList<HashMap<String,String>> inputArray) {
        ArrayList<HashMap<String,String>> outputArray = new ArrayList<>();

        int ind;
        int count;
        int temp;
        do{
            ind = 0;
            count = Integer.parseInt(inputArray.get(0).get(ListButtonsPanel.COLUMN_BUTTON_POSITION));

            for (int z = 0, size = inputArray.size(); z < size; z++) {
                temp = Integer.parseInt(inputArray.get(z).get(ListButtonsPanel.COLUMN_BUTTON_POSITION));
                if (count > temp) {
                    count = temp;
                    ind = z;
                }
            }
            outputArray.add(inputArray.get(ind));
            inputArray.remove(ind);
        }while (inputArray.size() > 0);
        outputArray.trimToSize();
        return outputArray;
    }
    public ArrayList<HashMap<String, String>> getPanelButon(int id){
        // выходной массив
        ArrayList<HashMap<String, String>> outArray = new ArrayList<>();
        // получаем отсортированный массив кнопок
        ArrayList<HashMap<String, String>> itemsArray = getListButtonsOfPanel(id);
        // проверяем, что массив не пустой
        if (!itemsArray.isEmpty()) {
            for (HashMap<String, String> item: itemsArray) {
                HashMap<String, String> i = new HashMap<>();
                i.put("getClick", item.get(ListButtonsPanel.COLUMN_RUN_ONE_CLICK));
                i.put("addGetClick", item.get(ListButtonsPanel.COLUMN_NAME_ONE_CLICK));
                i.put("getDClick", item.get(ListButtonsPanel.COLUMN_RUN_DOUBLE_CLICK));
                i.put("addGetDClick", item.get(ListButtonsPanel.COLUMN_NAME_DOUBLE_CLICK));
                i.put("getLClick", item.get(ListButtonsPanel.COLUMN_RUN_LONG_CLICK));
                i.put("addGetLClick", item.get(ListButtonsPanel.COLUMN_NAME_LONG_CLICK));
                i.put(ListButtonsPanel.COLUMN_IDENT_ICON, item.get(ListButtonsPanel.COLUMN_IDENT_ICON));
                i.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, item.get(ListButtonsPanel.COLUMN_BUTTON_POSITION));
                outArray.add(i);
            }
        }
        return  outArray;
    }
    public void updatePanelButons(int id_panel, List<HashMap<String,String>> mapPanelButtons){
        System.out.println("updatePanelButons");
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // создаем объект для данных
        //ContentValues cv = new ContentValues();

        if(db == null){
            return;
        }
        // настраиваем фильтр на удаление в таблице кнопок
        String selection = ListButtonsPanel.COLUMN_NUMBER_PANEL + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(id_panel) };
        // удаляем все кнопки с панели
        db.delete(ListButtonsPanel.TABLE_NAME, selection, selectionArgs);

        // обходим весь массив кнопок панели
        int position = 0;
        for (HashMap<String,String> button: mapPanelButtons) {
            ContentValues cv = new ContentValues();
            cv.put(ListButtonsPanel.COLUMN_NUMBER_PANEL, String.valueOf(id_panel));
            cv.put(ListButtonsPanel.COLUMN_RUN_ONE_CLICK, button.get("getClick"));
            cv.put(ListButtonsPanel.COLUMN_NAME_ONE_CLICK, button.get("addGetClick"));
            cv.put(ListButtonsPanel.COLUMN_RUN_DOUBLE_CLICK, button.get("getDClick"));
            cv.put(ListButtonsPanel.COLUMN_NAME_DOUBLE_CLICK, button.get("addGetDClick"));
            cv.put(ListButtonsPanel.COLUMN_RUN_LONG_CLICK, button.get("getLClick"));
            cv.put(ListButtonsPanel.COLUMN_NAME_LONG_CLICK, button.get("addGetLClick"));
            cv.put(ListButtonsPanel.COLUMN_NAME_LONG_CLICK, button.get("addGetLClick"));
            cv.put(ListButtonsPanel.COLUMN_IDENT_ICON, button.get(ListButtonsPanel.COLUMN_IDENT_ICON));
            cv.put(ListButtonsPanel.COLUMN_BUTTON_POSITION, position);

            // вставляем запись
            System.out.println(" ------------ insert = " + db.insert(ListButtonsPanel.TABLE_NAME, null, cv));
            position++;
            cv.clear();
        }

        closeDb(DbHelper, db, null);
    }
    public int getButtonPosition(HashMap<String, String> button) {
        return Integer.parseInt(button.get(ListButtonsPanel.COLUMN_BUTTON_POSITION));
    }
    // ====================================================
    //  общие методы
    private void closeDb(RelaunchDBHelper DbHelper, SQLiteDatabase db, Cursor c) {
        if (c != null) {
            c.close();
        }
        db.close();
        DbHelper.close();
    }
    public void resetDb() {
        resetPanelsDb();
        resetButtonsDb();
    }
    private void resetPanelsDb() {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        db.execSQL("delete from " + ListPanels.TABLE_NAME);
        closeDb(DbHelper, db, null);
    }
    private void resetButtonsDb() {
        // подготавливаемся к подключению к базе
        RelaunchDBHelper DbHelper = new RelaunchDBHelper(context);
        // открываем базу на запись
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        db.execSQL("delete from " + ListButtonsPanel.TABLE_NAME);
        closeDb(DbHelper, db, null);
    }
}
