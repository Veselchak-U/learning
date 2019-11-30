package ru.julls.p063_db_query;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    final String LOG_TAG = "myLog";

    String name[] = { "Китай", "США", "Бразилия", "Россия", "Япония", "Германия", "Египет",
            "Италия", "Франция", "Канада" };
    String region[] = { "Азия", "Америка", "Америка", "Европа", "Азия", "Европа", "Африка",
            "Европа", "Европа", "Америка" };
    int people[] = { 1400, 311, 195, 142, 128, 82, 80, 60, 66, 35 };

    Button btnAdd, btnFunc, btnPeople, btnGroup, btnPeopleRegion, btnSort, btnClear;
    EditText etFunc, etPeople, etPeopleRegion;
    RadioGroup rgSort;

    DBHelper dbHelper;
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region инициализируем экранные view
        btnAdd = (Button) findViewById(R.id.btnAll);
        btnAdd.setOnClickListener(this);

        btnFunc = (Button) findViewById(R.id.btnFunc);
        btnFunc.setOnClickListener(this);

        btnPeople = (Button) findViewById(R.id.btnPeople);
        btnPeople.setOnClickListener(this);

        btnGroup = (Button) findViewById(R.id.btnGroup);
        btnGroup.setOnClickListener(this);

        btnPeopleRegion = (Button) findViewById(R.id.btnPeopleRegion);
        btnPeopleRegion.setOnClickListener(this);

        btnSort = (Button) findViewById(R.id.btnSort);
        btnSort.setOnClickListener(this);

        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);

        etFunc = (EditText) findViewById(R.id.etFunc);
        etPeople = (EditText) findViewById(R.id.etPeople);
        etPeopleRegion = (EditText) findViewById(R.id.etPeopleRegion);

        rgSort = (RadioGroup) findViewById(R.id.rgSort);
        //endregion

        // подключаемся к БД
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        // проверка существования записей
        Cursor c = db.query("country", null, null, null, null, null, null);
        if (c.getCount() == 0) {
            ContentValues cv = new ContentValues();
            // Заполняем таблицу
            for (int i = 0; i < name.length; i++) {
                cv.put(DBHelper.KEY_NAME, name[i]);
                cv.put(DBHelper.KEY_PEOPLE, people[i]);
                cv.put(DBHelper.KEY_REGION, region[i]);
                Log.d(LOG_TAG, "Заполнение таблицы, id = " + db.insert(DBHelper.TABLE_COUNTRY, null, cv));
            }
        }
        c.close();
        dbHelper.close();

        // Эмулируем нажатие кнопки btnAll
        onClick(btnAdd);

    }

    @Override
    public void onClick(View v) {

        // подключаемся к БД
        db = dbHelper.getWritableDatabase();

        // значение полей с экрана
        String sFunc = etFunc.getText().toString();
        String sPeople = etPeople.getText().toString();
        String sPeopleRegion = etPeopleRegion.getText().toString();

        // переменные для query
        String columns[] = null;
        String selection = null;
        String selectionArgs[] = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;

        // курсор
        Cursor c = null;

        switch (v.getId()) {
            case R.id.btnAll:
                Log.d(LOG_TAG, "--- Все записи ---");
                break;
            case R.id.btnFunc:
                Log.d(LOG_TAG, "--- Функция " + sFunc + " ---");
                columns = new String[] { sFunc };
                break;
            case R.id.btnPeople:
                Log.d(LOG_TAG, "--- Население больше " + sPeople + " ---");
                selection = DBHelper.KEY_PEOPLE + " > ?";
                selectionArgs = new String[] { sPeople };
                break;
            case R.id.btnGroup:
                Log.d(LOG_TAG, "--- Население по региону ---");
                columns = new String[] { DBHelper.KEY_REGION, "sum(people) as people" };
                groupBy = DBHelper.KEY_REGION;
                break;
            case R.id.btnPeopleRegion:
                Log.d(LOG_TAG, "--- Регионы с населением больше " + sPeopleRegion + " ---");
                columns = new String[] { DBHelper.KEY_REGION, "sum(people) as people" };
                groupBy = DBHelper.KEY_REGION;
                having = "sum(people) > " + sPeopleRegion;
                break;
            case R.id.btnSort:
                // в зависимости от выбранного радиобаттона
                switch ( rgSort.getCheckedRadioButtonId() ) {
                    case R.id.rName:
                        Log.d(LOG_TAG, "--- Сортировка по наименованию ---");
                        orderBy = DBHelper.KEY_NAME;
                        break;
                    case R.id.rPeople:
                        Log.d(LOG_TAG, "--- Сортировка по населению ---");
                        orderBy = DBHelper.KEY_PEOPLE;
                        break;
                    case R.id.rRegion:
                        Log.d(LOG_TAG, "--- Сортировка по региону ---");
                        orderBy = DBHelper.KEY_REGION;
                        break;
                }
                break;
            case R.id.btnClear:
                Log.d(LOG_TAG, "--- Очищаем БД ---");
                db.delete(dbHelper.TABLE_COUNTRY, null, null);
                break;
        }

        // запрос к БД со сформированными выше условиями
        c = db.query(dbHelper.TABLE_COUNTRY, columns, selection, selectionArgs, groupBy, having, orderBy);

        if ( c == null ) {
            Log.d(LOG_TAG, "Курсор пустой.");
        } else {
            if ( c.moveToFirst() ) {
                String str;
                do {
                    str = "";
                    for (String cn : c.getColumnNames()) {
                        str = str.concat(cn + "=" + c.getString(c.getColumnIndex(cn)) + "; ");
                    }
                    Log.d(LOG_TAG, str);
                } while (c.moveToNext());
            }
            c.close();
        }

        dbHelper.close();
    }

}
