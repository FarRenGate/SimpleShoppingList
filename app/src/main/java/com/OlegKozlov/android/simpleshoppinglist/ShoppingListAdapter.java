package com.OlegKozlov.android.simpleshoppinglist;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.OlegKozlov.android.simpleshoppinglist.data.ShoppingListContract.ShoppingListEntry.*;

/**
 * Created by User on 22/08/17.
 */

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {

    private static final int DEFAULT_TEXT_SIZE = 22;

    private Context mContext;
    private Cursor mCursor;
    int textSize;

    final private ListItemClickListener mOnClickListener;
    final private ListItemLongClickListener mOnLongClickListener;
    private int changedItemPosition = -1;

    public interface ListItemClickListener {
        void onItemClick(int clickedItem);
    }

    public interface ListItemLongClickListener {
        void onItemLongClick(int clickedItem);
    }


    public ShoppingListAdapter(Context context, Cursor cursor, ListItemClickListener clickListener,
                               ListItemLongClickListener longClickListener) {
        mContext = context;
        mCursor = cursor;
        mOnClickListener = clickListener;
        mOnLongClickListener = longClickListener;
        textSize = DEFAULT_TEXT_SIZE;
    }

    @Override
    public ShoppingListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.shopping_list_item, parent, false);
        return new ShoppingListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShoppingListViewHolder holder, int position) {

        if (!mCursor.moveToPosition(position))
            return;

        String listItem = mCursor.getString(mCursor.getColumnIndex(COLUMN_ITEM));
        int crossed = mCursor.getInt(mCursor.getColumnIndex(COLUMN_CROSSED));
        long id = mCursor.getLong(mCursor.getColumnIndex(_ID));

        holder.itemTextView.setText(listItem);
        holder.itemView.setTag(id);

        holder.itemTextView.setTextSize(textSize);

        if (crossed == 1) {
            holder.itemTextView.setPaintFlags(holder.itemTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorBackgroundCrossedItem));
        } else {
            holder.itemTextView.setPaintFlags(holder.itemTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorBackground));
        }

        if (position == changedItemPosition) {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorBackgroundEditedItem));
        }

    }


    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public class ShoppingListViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        TextView itemTextView;

        public ShoppingListViewHolder(View itemView) {
            super(itemView);
            itemTextView = (TextView) itemView.findViewById(R.id.tv_list_element);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int clickedItem = getAdapterPosition();
            mOnClickListener.onItemClick(clickedItem);
            notifyItemChanged(clickedItem);
        }

        @Override
        public boolean onLongClick(View v) {
            int clickedItem = getAdapterPosition();
            mOnLongClickListener.onItemLongClick(clickedItem);
            notifyItemChanged(clickedItem);
            return true;
        }
    }


    public void updateList(Cursor newCursor) {
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null)
            this.notifyDataSetChanged();
    }

    public void selectEditedItem(int position) {
        changedItemPosition = position;
        notifyItemChanged(position);
    }

    public void deselectEditedItem() {
        changedItemPosition = -1;
        notifyDataSetChanged();
    }
}
