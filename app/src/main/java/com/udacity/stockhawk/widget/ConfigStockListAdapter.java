package com.udacity.stockhawk.widget;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;


/**
 * Created by antti on 13/04/17.
 */

public class ConfigStockListAdapter extends CursorAdapter {

    public ConfigStockListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_stock_config, parent, false);

        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView) view.findViewById(R.id.list_item_stock_config);

        String stock_symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        tv.setText(stock_symbol);

    }

}
