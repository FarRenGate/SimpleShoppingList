package com.OlegKozlov.android.simpleshoppinglist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
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
        ShoppingListAdapter.ListItemLongClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private boolean deleteOnTap;
    private ShoppingListAdapter mShoppingListAdapter;
    private SQLiteDatabase mDb;
    private CustomEditText mItemEditText;
    RecyclerView mShoppingListView;
    LinearLayoutManager mLayoutManager;

    private boolean editTextMode;
    private long changedItemId;
    private int changedItemPosition;
    private String charactersToFilter = ".,";

    private Button mAddButton;

    private InputFilter filterWhenEditing = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source!=null && charactersToFilter.contains(""+source)) {
                return "";
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mShoppingListView = (RecyclerView)  this.findViewById(R.id.rv_ShoppingList);
        mItemEditText = (CustomEditText) this.findViewById(R.id.et_addItem);
        mAddButton = (Button) this.findViewById(R.id.b_addToShoppingList);
        mLayoutManager = new LinearLayoutManager(this);
        mShoppingListView.setLayoutManager(mLayoutManager);


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

        //needed to capture Back key when keyboard is open
        mItemEditText.setKeyImeChangeListener(new CustomEditText.KeyImeChangeListener() {
            @Override
            public void onKeyIme(int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (editTextMode) {
                        endEditing();
                    } else {
                        defocusEditText(mItemEditText);
                    }
                }
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

        mShoppingListAdapter = new ShoppingListAdapter(this,cursor,this, this);
        mShoppingListView.setAdapter(mShoppingListAdapter);

        //implements swipe
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // To prevent swiping in EditMode
                if (editTextMode) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

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

        ).attachToRecyclerView(mShoppingListView);

        //Needed to scroll the RV to the edited item if the keyboard appears
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mShoppingListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){

                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    Log.i("Scroll", "Layout changed");
                    if (bottom<oldBottom) {
                        if (editTextMode) {
                            mShoppingListView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mShoppingListView.scrollToPosition(changedItemPosition);
                                }
                            }, 100);
                            Log.i("Scroll", "Scrolled to position " + changedItemPosition);
                        }
                    }
                 }
            });
        }
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
        if (editTextMode) {
            return super.onOptionsItemSelected(item);
        }
        defocusEditTextAndHideKeyboard(mItemEditText);
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
        if (mItemEditText.getText().toString().trim().length()==0) return;
        if (editTextMode) {
            if (changedItemId!=-1) {
                DatabaseOperations.replaceItem(mDb, mShoppingListAdapter,
                        mItemEditText.getText().toString(), changedItemId);

                endEditingAndHideKeyboard();
            }
        } else {
            DatabaseOperations.addNewItem(mDb, mShoppingListAdapter, mItemEditText.getText().toString());
        }
        mItemEditText.setText("");
        editTextMode = false;
    }

    private void endEditing() {
        mShoppingListAdapter.deselectEditedItem();
        changedItemId=-1;
        changedItemPosition=-1;
        mAddButton.setText(
                getResources().getString(R.string.add_button)
        );
        defocusEditText(mItemEditText);
        mItemEditText.setText("");
        mItemEditText.setFilters(new InputFilter[]{});
        editTextMode=false;
    }

    private void endEditingAndHideKeyboard() {
        endEditing();
        hideKeyboard();
    }

    @Override
    public void onItemClick(int clickedItem) {
        if (editTextMode) {
            return;
        }
        if (mItemEditText.hasFocus()) {
            defocusEditTextAndHideKeyboard(mItemEditText);
            return;
        }
        View view = mShoppingListView.findViewHolderForAdapterPosition(clickedItem).itemView;
        DatabaseOperations.crossItem(mDb, mShoppingListAdapter, (long) view.getTag(),deleteOnTap);
    }
    @Override
    public void onItemLongClick(int clickedItem) {
        if (editTextMode) {
            return;
        }

        mItemEditText.setFilters(new InputFilter[] {filterWhenEditing});
        mAddButton.setText(
            getResources().getString(R.string.replace_button)
        );
        editTextMode = true;
        View view = mShoppingListView.findViewHolderForAdapterPosition(clickedItem).itemView;
        mShoppingListAdapter.selectEditedItem(clickedItem);
        changedItemPosition=clickedItem;
        changedItemId =  (long) view.getTag();
        String item = DatabaseOperations.receiveItem(mDb,changedItemId);
        mItemEditText.setText(item);
        mItemEditText.requestFocus();
        mItemEditText.setSelection(mItemEditText.getText().length());
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(this.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(mItemEditText,InputMethodManager.SHOW_IMPLICIT);
        mLayoutManager.scrollToPositionWithOffset(clickedItem,0);
    }

    public void defocusEditText(EditText editText) {
       editText.clearFocus();
    }

    public void defocusEditTextAndHideKeyboard(EditText editText) {
        hideKeyboard();
        editText.clearFocus();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(this.INPUT_METHOD_SERVICE);
        if (getCurrentFocus()!=null) {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
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
        defocusEditTextAndHideKeyboard(mItemEditText);
    }


}
