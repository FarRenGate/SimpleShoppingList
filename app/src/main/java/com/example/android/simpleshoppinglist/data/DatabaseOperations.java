package com.example.android.simpleshoppinglist.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android.simpleshoppinglist.ShoppingListAdapter;
import com.example.android.simpleshoppinglist.data.ShoppingListContract.ShoppingListEntry;

import static com.example.android.simpleshoppinglist.data.ShoppingListContract.ShoppingListEntry.*;

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
        String[] items = itemString.split(",");
        ContentValues cv = new ContentValues();
        for (String item: items) {
            cv.put(COLUMN_ITEM, item.trim());
            cv.put(COLUMN_CROSSED, 0);
            db.insert(TABLE_NAME,null,cv);
        }
        adapter.updateList(getCursor(db));
    }

    public static boolean removeItem (SQLiteDatabase db, long id) {
        return db.delete(TABLE_NAME, _ID+"="+id,null)>0;
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

    public static void removeCrossedItems(SQLiteDatabase db, ShoppingListAdapter adapter) {
        db.delete(TABLE_NAME, COLUMN_CROSSED + "=?", new String[] { "1" });
        adapter.updateList(getCursor(db));
    }

    public static void removeAllItems(SQLiteDatabase db, ShoppingListAdapter adapter) {

        db.delete(TABLE_NAME, null, null);
        adapter.updateList(getCursor(db));
        
    }
}
