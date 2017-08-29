package com.example.android.simpleshoppinglist.data;

import android.provider.BaseColumns;

/**
 * Created by User on 20/08/17.
 */

public class ShoppingListContract {
    public static final class ShoppingListEntry implements BaseColumns {
        public static final String TABLE_NAME = "shoppingList";
        public static final String COLUMN_ITEM = "item";
        public static final String COLUMN_CROSSED = "crossed";
    }
}
