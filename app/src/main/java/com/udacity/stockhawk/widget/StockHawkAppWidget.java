package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link StockHawkAppWidgetConfigureActivity StockHawkAppWidgetConfigureActivity}
 */
public class StockHawkAppWidget extends AppWidgetProvider {
    public static final String WIDGET_IDS_KEY = "com.udacity.stockhawk.mobilitio.widgetids";
    public static final String WIDGET_DATA_KEY = "com.udacity.stockhawk.mobilitio.widgetdata";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        context.startService(new Intent(context, StockHawkWidgetIntentService.class));

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            StockHawkAppWidgetConfigureActivity.deleteWidgetStockPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, StockHawkWidgetIntentService.class));
        if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            Timber.d("Widget onReceive ACTION_DATA_UPDATED");
            context.startService(new Intent(context, StockHawkWidgetIntentService.class));
        } else if (intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")) {
            Timber.d("Widget onReceive APPWIDGET_UPDATE");
            if (intent.hasExtra(WIDGET_IDS_KEY)) {
                int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
                Timber.d("Widget onReceive ids=%s", (String) ids.toString());
                AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(ids, R.id.lv_widget_stock_list);
                if (intent.hasExtra(WIDGET_DATA_KEY)) {
                    String data = intent.getExtras().getString(WIDGET_DATA_KEY);
                    Timber.d("Widget onReceive data=%s", (String) data);
                }
                // How to force update on DB`?
                this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
            } else super.onReceive(context, intent);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        context.startService(new Intent(context, StockHawkWidgetIntentService.class));
    }
}

