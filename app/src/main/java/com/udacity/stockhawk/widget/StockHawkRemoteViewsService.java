package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.ChartActivity;
import com.udacity.stockhawk.ui.StockChartFragment;
import com.udacity.stockhawk.ui.UiUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Antti on 2017-04-21.
 */

public class StockHawkRemoteViewsService extends RemoteViewsService {
    private Cursor mCursor = null;
    private DecimalFormat dollarFormatWithPlus = null;
    private DecimalFormat dollarFormat = null;
    private DecimalFormat percentageFormat = null;
    Context mContex = this;

    public StockHawkRemoteViewsService() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(mContex, intent);
    }

    class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        protected Context mContext;
        protected int mAppWidgetId;

        public StockRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            Uri uri_for_all_stock = Contract.Quote.URI;
            Cursor cursor = getContentResolver()
                    .query(uri_for_all_stock,
                            Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                            null,
                            null,
                            Contract.Quote.COLUMN_SYMBOL);
            if (cursor == null) {
                return;
            }
            if (!cursor.moveToFirst()) {
                cursor.close();
                return;
            }

            int stockCount = cursor.getCount();
            Timber.d("Widget sees %d stocks", stockCount);

        }


        @Override
        public void onDataSetChanged() {
            Timber.d("onDataSetChanged");
            if (mCursor != null) {
                mCursor.close();
            }
            final long identityToken = Binder.clearCallingIdentity();
            Uri uri_for_all_stock = Contract.Quote.URI;
            mCursor = getContentResolver()
                    .query(uri_for_all_stock,
                            Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                            null,
                            null,
                            Contract.Quote.COLUMN_SYMBOL);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }

        @Override
        public int getCount() {
            Timber.d("GetCount...");
            if (mCursor != null) {
                return mCursor.getCount();
            } else {
                Timber.d("GetCount=0");
                return 0;
            }
        }

        @Override
        public RemoteViews getViewAt(int i) {//^((?!YahooFinance).)*$
            Timber.d("StockHawkRemoteViewsService.factory.getViewAt( %d )", i);
            if (i == AdapterView.INVALID_POSITION ||
                    mCursor == null) {
                return null;
            }
            if (!mCursor.moveToPosition(i)) {
                return null;
            }

            RemoteViews remoteListRow =
                    new RemoteViews(getPackageName(), R.layout.stock_hawk_widget_line);
            String stockSymbol = mCursor.getString(Contract.Quote.POSITION_SYMBOL);
            remoteListRow.setTextViewText(R.id.tv_widget_symbol, stockSymbol);
            String spelledStockSymbol = UiUtil.spaceOutAcronym(stockSymbol);
            remoteListRow.setContentDescription(R.id.tv_widget_symbol, spelledStockSymbol);
            remoteListRow.setTextViewText(R.id.tv_widget_price,
                    dollarFormat.format(mCursor.getFloat(Contract.Quote.POSITION_PRICE)));

            float rawAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
            String percentage = percentageFormat.format(percentageChange / 100);
            if (rawAbsoluteChange > 0) {
                remoteListRow.setImageViewResource(R.id.widget_change_background,
                        R.drawable.percent_change_pill_green);
            } else {
                remoteListRow.setImageViewResource(R.id.widget_change_background,
                        R.drawable.percent_change_pill_red);
            }
            remoteListRow.setTextViewText(R.id.tv_widget_change, percentage);

            Intent detailFillInIntent = new Intent();
            detailFillInIntent.putExtra(StockChartFragment.ARG_STOCK_TICKER, stockSymbol);
            remoteListRow.setOnClickFillInIntent(R.id.stock_hawk_widget_line, detailFillInIntent);

            return remoteListRow;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.stock_hawk_widget_line);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
