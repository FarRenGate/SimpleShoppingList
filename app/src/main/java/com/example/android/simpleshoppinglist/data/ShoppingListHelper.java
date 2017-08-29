package com.example.android.simpleshoppinglist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.simpleshoppinglist.data.ShoppingListContract.ShoppingListEntry;


/**
 * Created by User on 20/08/17.
 */

public class ShoppingListHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shoppingList.db";

    private static final int DATABASE_VERSION = 2;

    public ShoppingListHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_SHOPPINGLIST_TABLE = "CREATE TABLE " + ShoppingListEntry.TABLE_NAME + " (" +
                ShoppingListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ShoppingListEntry.COLUMN_ITEM + " TEXT NOT NULL," +
                ShoppingListEntry.COLUMN_CROSSED + " INTEGER NOT NULL"+
                "); ";
        db.execSQL(SQL_CREATE_SHOPPINGLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ShoppingListEntry.TABLE_NAME);
        onCreate(db);
    }
}
