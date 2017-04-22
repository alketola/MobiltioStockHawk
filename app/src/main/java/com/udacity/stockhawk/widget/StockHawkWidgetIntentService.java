package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import butterknife.BindView;
import timber.log.Timber;

/**
 * Created by Antti on 2017-04-21.
 */

public class StockHawkWidgetIntentService extends IntentService {
    private StockHawkRemoteViewsService remoteViewsService = new StockHawkRemoteViewsService();
    RemoteViewsService.RemoteViewsFactory mFactory;

    public StockHawkWidgetIntentService() {
        super("StockHawkWidgetIntentService");

    }

    @BindView(R.id.lv_widget_stock_list)
    ListView lv_stock_list;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Retrieve our widget ids: these are the widgets we need to update
        Timber.d("onHandleIntent: %s", intent.toString());
        mFactory = remoteViewsService.onGetViewFactory(intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                StockHawkAppWidget.class));

        if (appWidgetIds == null) return;
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(this, appWidgetManager, appWidgetId);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.stock_hawk_app_widget);

        Timber.d("updateAppWidget %d", appWidgetId);
        // Set up the collection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setRemoteAdapter(context, views);
        } else {
            setRemoteAdapterV11(context, views);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

//    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
//    private void setRemoteContentDescription(RemoteViews views, String description) {
//        views.setContentDescription(R.id.widget_icon, description);
//    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.lv_widget_stock_list,
                new Intent(context, StockHawkRemoteViewsService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private static void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.lv_widget_stock_list,
                new Intent(context, StockHawkRemoteViewsService.class));
    }
}

//        do {
//
//            String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
//
//            float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
//            float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
//
//            // Find and fill remote views
//            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
//            //String change = dollarFormatWithPlus.format(rawAbsoluteChange);
//            DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
//            percentageFormat.setMaximumFractionDigits(2);
//            percentageFormat.setMinimumFractionDigits(2);
//            percentageFormat.setPositivePrefix("+");
//
//            String percentage = percentageFormat.format(percentageChange / 100);
//
//            String price = dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE));
//
//            views.setTextViewText(R.id.widget_symbol, symbol);
//            views.setContentDescription(R.id.widget_symbol, UiUtil.spaceOutAcronym(symbol));
//            views.setTextViewText(R.id.widget_price, price);
//            views.setTextViewText(R.id.widget_change, percentage);
//            if (rawAbsoluteChange > 0) {
//                views.setImageViewResource(R.id.widget_change_background, R.drawable.percent_change_pill_green);
//            } else {
//                views.setImageViewResource(R.id.widget_change_background, R.drawable.percent_change_pill_red);
//            }
//
//
//        } while (cursor.moveToNext());