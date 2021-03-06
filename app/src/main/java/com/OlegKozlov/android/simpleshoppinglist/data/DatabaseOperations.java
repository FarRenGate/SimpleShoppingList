package com.OlegKozlov.android.simpleshoppinglist.data;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.OlegKozlov.android.simpleshoppinglist.ShoppingListAdapter;


import static com.OlegKozlov.android.simpleshoppinglist.data.ShoppingListContract.ShoppingListEntry.*;

/**
 * Created by User on 22/08/17.
 */

public class DatabaseOperations {

    public static Cursor getCursor(SQLiteDatabase db){
        return db.query(TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                _ID);
    }

    public static void addNewItem (SQLiteDatabase db, ShoppingListAdapter adapter, String itemString) {
        String[] items = itemString.split(",|\\.");
        ContentValues cv = new ContentValues();
        for (String item: items) {
            item=item.trim();
            if (item.isEmpty()) {
                continue;
            }
            cv.put(COLUMN_ITEM, item);
            cv.put(COLUMN_CROSSED, 0);
            db.insert(TABLE_NAME, null, cv);
        }
        adapter.updateList(getCursor(db));
    }

    public static boolean removeItem (SQLiteDatabase db, long id) {
        return db.delete(TABLE_NAME, _ID+"="+id,null)>0;
    }

    public static void replaceItem (SQLiteDatabase db, ShoppingListAdapter adapter, String newItem,long id) {
        ContentValues cv = new ContentValues();
        String whereClause = _ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        cv.put(COLUMN_ITEM,newItem);
        db.update(TABLE_NAME,cv, whereClause, whereArgs);
        adapter.updateList(getCursor(db));
    }

    public static void crossItem (SQLiteDatabase db, ShoppingListAdapter adapter, long id, boolean deleteOnTap) {
        if (deleteOnTap) {
            removeItem(db, id);
            adapter.updateList(getCursor(db));
        } else {
            ContentValues cv = new ContentValues();
            String whereClause = _ID + "=?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            Cursor cursor = db.query(TABLE_NAME,
                    new String[]{COLUMN_CROSSED},
                    whereClause,
                    whereArgs,
                    null,
                    null,
                    null);
            cursor.moveToFirst();
            int crossedValue = cursor.getInt(cursor.getColumnIndex(COLUMN_CROSSED));
            if (crossedValue == 0) crossedValue = 1;
            else crossedValue = 0;
            cv.put(COLUMN_CROSSED, crossedValue);
            db.update(TABLE_NAME, cv, whereClause, whereArgs);
            adapter.updateList(getCursor(db));
            cursor.close();
        }
    }

    public static String receiveItem (SQLiteDatabase db, long id) {
        String receivedItem = null;
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ITEM},
                _ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null);
        cursor.moveToFirst();
        receivedItem = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM));
        return receivedItem;
    }

    public static void removeCrossedItems(SQLiteDatabase db, ShoppingListAdapter adapter) {
        db.delete(TABLE_NAME, COLUMN_CROSSED + "=?", new String[] { "1" });
        adapter.updateList(getCursor(db));
    }

    public static int numberOfCrossedItems(SQLiteDatabase db) {
        Cursor c = db.query(
                TABLE_NAME,
                new String[] {COLUMN_CROSSED},
                COLUMN_CROSSED + "=?",
                new String[] {"1"},
                null,
                null,
                null
        );
        int result = c.getCount();
        c.close();
        return result;

    }

    public static void removeAllItems(SQLiteDatabase db, ShoppingListAdapter adapter) {

        db.delete(TABLE_NAME, null, null);
        adapter.updateList(getCursor(db));
        
    }
}
