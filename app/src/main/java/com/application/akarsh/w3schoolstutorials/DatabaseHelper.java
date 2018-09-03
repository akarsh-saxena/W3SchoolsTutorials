package com.application.akarsh.w3schoolstutorials;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Akarsh on 17-06-2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "favorites.db";
    public static final String TABLE_NAME = "favorites";
    public static final String COLUMN_COURSE_LINK = "item_link";
    public static final String COLUMN_COURSE_NAME = "item_name";
    public static final String COLUMN_COURSE_GROUP = "item_group";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+TABLE_NAME+" ("+COLUMN_COURSE_LINK+" varchar(100), "+COLUMN_COURSE_NAME+" VARCHAR(50), "+COLUMN_COURSE_GROUP+" VARCHAR(50))";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertFavorite(String link, String name, String group) throws IOException {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_COURSE_LINK, link);
        contentValues.put(COLUMN_COURSE_NAME, name);
        contentValues.put(COLUMN_COURSE_GROUP, group);

        if(sqLiteDatabase.insert(TABLE_NAME, null, contentValues) == -1)
            return false;
        else
            return true;
    }

    public boolean isFavorite(String link){

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_COURSE_LINK+" = "+"'"+link+"'";

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        else
            return true;
    }

    public boolean removeFavorite(String link){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        if((sqLiteDatabase.delete(TABLE_NAME, COLUMN_COURSE_LINK+" = ?", new String[] {link})) > 0)
            return true;
        else
            return false;
    }

    public List<DatabaseModel> getDetails() {
        List<DatabaseModel> databaseModels = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM "+TABLE_NAME+" ORDER BY "+COLUMN_COURSE_GROUP+" , "+COLUMN_COURSE_NAME;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if(cursor.getCount()<=0) {
            Log.d("alka", "returning null");
            return null;
        }
        cursor.moveToFirst();
        do {
            DatabaseModel databaseModel = new DatabaseModel();
            databaseModel.setLink(cursor.getString(cursor.getColumnIndex(COLUMN_COURSE_LINK)));
            databaseModel.setName(cursor.getString(cursor.getColumnIndex(COLUMN_COURSE_NAME)));
            databaseModel.setGroup(cursor.getString(cursor.getColumnIndex(COLUMN_COURSE_GROUP)));
            databaseModels.add(databaseModel);
        } while(cursor.moveToNext());

        return databaseModels;
    }
}
