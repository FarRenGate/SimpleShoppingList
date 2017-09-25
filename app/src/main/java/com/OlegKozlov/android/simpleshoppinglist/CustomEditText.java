package com.OlegKozlov.android.simpleshoppinglist;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.view.KeyEvent;

/**
 * Created by Oleg on 25.09.2017.
 */

public class CustomEditText extends EditText {
    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

   private KeyImeChangeListener keyImeChangeListener;

   public interface KeyImeChangeListener {
       public void onKeyIme(int keyCode, KeyEvent event);
   }

   public void setKeyImeChangeListener(KeyImeChangeListener listener) {
       keyImeChangeListener = listener;
   }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyImeChangeListener!=null) {
            keyImeChangeListener.onKeyIme(keyCode, event);
        }
        return false;
    }
}
