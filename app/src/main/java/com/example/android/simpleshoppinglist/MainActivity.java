package com.example.android.simpleshoppinglist;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.simpleshoppinglist.data.DatabaseOperations;
import com.example.android.simpleshoppinglist.data.ShoppingListHelper;

public class MainActivity extends AppCompatActivity implements ShoppingListAdapter.ListItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private int textSize;
    private boolean deleteOnTap;
    private ShoppingListAdapter mShoppingListAdapter;
    private SQLiteDatabase mDb;
    private EditText mItemEditText;
    RecyclerView shoppingListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shoppingListView = (RecyclerView)  this.findViewById(R.id.rv_ShoppingList);
        mItemEditText = (EditText) this.findViewById(R.id.et_addItem);
        shoppingListView.setLayoutManager(new LinearLayoutManager(this));

        ShoppingListHelper shoppingListDbHelper = new ShoppingListHelper(this);


        mDb = shoppingListDbHelper.getWritableDatabase();
        Cursor cursor;

        try {
             cursor = DatabaseOperations.getCursor(mDb); }
        catch (Exception e) {
            shoppingListDbHelper.onCreate(mDb);
            mDb = shoppingListDbHelper.getWritableDatabase();
            cursor = DatabaseOperations.getCursor(mDb);
        }

        setupSharedPreferences();

        mShoppingListAdapter = new ShoppingListAdapter(this,cursor,this);
        mShoppingListAdapter.setTextSize(textSize);

        shoppingListView.setAdapter(mShoppingListAdapter);




        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                defocusTextEdit();
                long id = (long) viewHolder.itemView.getTag();
                DatabaseOperations.removeItem(mDb,id);
                mShoppingListAdapter.updateList(DatabaseOperations.getCursor(mDb));
            }
        }

        ).attachToRecyclerView(shoppingListView);
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        getTextSizeFromPrefs(sharedPreferences);
        getTapActionsFromPrefs(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    private void getTapActionsFromPrefs(SharedPreferences sharedPreferences) {
        deleteOnTap =sharedPreferences.getBoolean(getString(R.string.pref_delete_on_tap_key),getResources().getBoolean(R.bool.pref_delete_on_tap_default));
    }

    private void getTextSizeFromPrefs(SharedPreferences sharedPreferences) {
        textSize = Integer.parseInt(
            sharedPreferences.getString(getString(R.string.pref_text_size_key), getString(R.string.pref_text_size_default))
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_menu,menu);

        if (deleteOnTap) {
            MenuItem cleanUpItem = menu.findItem(R.id.m_delete_crossed);
            cleanUpItem.setVisible(false);
            invalidateOptionsMenu();
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        defocusTextEdit();
        int menuId = item.getItemId();
        switch (menuId) {
            case R.id.m_delete_crossed:
                DatabaseOperations.removeCrossedItems(mDb,mShoppingListAdapter);
                break;
            case R.id.m_delete_all:
                DatabaseOperations.removeAllItems(mDb,mShoppingListAdapter);
                break;
            case R.id.m_settings:
                Intent settingsIntent = new Intent(this, PrefActivity.class);
                startActivity(settingsIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addToShoppingList(View view) {
        if (mItemEditText.getText().length()==0) return;
        DatabaseOperations.addNewItem(mDb, mShoppingListAdapter, mItemEditText.getText().toString());
        mItemEditText.setText("");
    }


    @Override
    public void onItemClick(int clickedItem) {
        defocusTextEdit();
        View view = shoppingListView.findViewHolderForAdapterPosition(clickedItem).itemView;
        DatabaseOperations.crossItem(mDb, mShoppingListAdapter, (long) view.getTag(),deleteOnTap);
    }

    public void defocusTextEdit () {
        mItemEditText.clearFocus();

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(this.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
