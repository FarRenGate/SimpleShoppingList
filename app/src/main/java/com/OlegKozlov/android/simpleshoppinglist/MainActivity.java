package com.OlegKozlov.android.simpleshoppinglist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.OlegKozlov.android.simpleshoppinglist.data.DatabaseOperations;
import com.OlegKozlov.android.simpleshoppinglist.data.ShoppingListHelper;

public class MainActivity extends AppCompatActivity
        implements ShoppingListAdapter.ListItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

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


       /* mItemEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });*/

       //needed to implement "enter" behavior (send line to the list) but keep the keyboard on screen
        mItemEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Button addButton = (Button) findViewById(R.id.b_addToShoppingList);
                    addButton.performClick();
                    return true;
                }
                return false;
            }
        });


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

        mShoppingListAdapter = new ShoppingListAdapter(this,cursor,this);
        shoppingListView.setAdapter(mShoppingListAdapter);

        //implements swipe
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = (long) viewHolder.itemView.getTag();
                DatabaseOperations.removeItem(mDb,id);
                mShoppingListAdapter.updateList(DatabaseOperations.getCursor(mDb));
            }
        }

        ).attachToRecyclerView(shoppingListView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_menu,menu);
        if (deleteOnTap) {
            MenuItem cleanUpItem = menu.findItem(R.id.m_delete_crossed);
            cleanUpItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupSharedPreferences();
        if (deleteOnTap) {
            try {
                DatabaseOperations.removeCrossedItems(mDb, mShoppingListAdapter);
            } catch (Exception e) {
                Toast.makeText(this,"Error in deleting items",Toast.LENGTH_SHORT).show();
            }
        }
        invalidateOptionsMenu();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        defocusEditText(mItemEditText);
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

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        getTapActionsFromPrefs(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    private void getTapActionsFromPrefs(SharedPreferences sharedPreferences) {
        deleteOnTap =sharedPreferences.getBoolean(getString(R.string.pref_delete_on_tap_key),
                getResources().getBoolean(R.bool.pref_delete_on_tap_default));
    }

    public void addToShoppingList(View view) {
        if (mItemEditText.getText().length()==0) return;
        DatabaseOperations.addNewItem(mDb, mShoppingListAdapter, mItemEditText.getText().toString());
        mItemEditText.setText("");
    }

    @Override
    public void onItemClick(int clickedItem) {
        if (mItemEditText.hasFocus()) {
            defocusEditText(mItemEditText);
            return;
        }
        View view = shoppingListView.findViewHolderForAdapterPosition(clickedItem).itemView;
        DatabaseOperations.crossItem(mDb, mShoppingListAdapter, (long) view.getTag(),deleteOnTap);
    }

    public void defocusEditText(EditText editText) {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(this.INPUT_METHOD_SERVICE);
        if (getCurrentFocus()!=null) {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
        editText.clearFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this).
                unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    public void defocusEditTextProcess(View view) {
        defocusEditText(mItemEditText);
    }
}
