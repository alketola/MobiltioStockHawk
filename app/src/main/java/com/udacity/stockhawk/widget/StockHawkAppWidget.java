package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.ChartActivity;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockChartFragment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import timber.log.Timber;

import static com.udacity.stockhawk.widget.StockHawkAppWidgetConfigureActivity.loadWidgetStockPref;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link StockHawkAppWidgetConfigureActivity StockHawkAppWidgetConfigureActivity}
 */
public class StockHawkAppWidget extends AppWidgetProvider {
    public static final String WIDGET_IDS_KEY = "com.udacity.stockhawk.mobilitio.widgetids";
    public static final String WIDGET_DATA_KEY = "com.udacity.stockhawk.mobilitio.widgetdata";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_hawk_app_widget);
        String stockSymbol = loadWidgetStockPref(context, appWidgetId);
        Timber.d("updateAppWidget(appWidgetId=%d); stockSymbol=%s", appWidgetId, stockSymbol);
        Uri queryUri = Contract.Quote.makeUriForStock(stockSymbol);
        Cursor cursor = context.getContentResolver()
                .query(queryUri, null, null, null, null);
        if (cursor.getCount() < 1) return;
        cursor.moveToFirst();
        String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

//        if (rawAbsoluteChange > 0) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                views.setTextColor(R.id.widget_change,context.getColor(R.color.material_green_700));//change.setBackgroundResource(R.drawable.percent_change_pill_green);
//            }
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                views.setTextColor(R.id.widget_change, context.getColor(R.color.material_red_700));
//            }
//        }
        DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        //String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

        String percentage = percentageFormat.format(percentageChange / 100);

        String price = dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE));

        views.setTextViewText(R.id.widget_symbol, symbol);
        views.setTextViewText(R.id.widget_price, price);
        views.setTextViewText(R.id.widget_change, percentage);
        if (rawAbsoluteChange > 0) {
            views.setImageViewResource(R.id.widget_percentage_background, R.drawable.percent_change_pill_green);
        } else {
            views.setImageViewResource(R.id.widget_percentage_background, R.drawable.percent_change_pill_red);
        }
        boolean CLICK_REFRESHES = false;
        if (CLICK_REFRESHES) {
            Intent updateIntent = new Intent();
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(new ComponentName(context, StockHawkAppWidget.class));
            updateIntent.putExtra(StockHawkAppWidget.WIDGET_IDS_KEY, ids);
            updateIntent.putExtra(WIDGET_DATA_KEY, symbol);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widget_frame_layout, pendingIntent);
        } else if (false) {
            Intent showChart = new Intent(context, ChartActivity.class);

            showChart.putExtra(StockChartFragment.ARG_STOCK_TICKER, symbol);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, showChart, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widget_change, pendingIntent);

        }

        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra(StockChartFragment.ARG_STOCK_TICKER, symbol);
            Timber.d("context.getPackageName()=%s", context.getPackageName());
            intent.setComponent(new ComponentName(context.getPackageName(),
                    "com.udacity.stockhawk.ui.ChartActivity"));
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, 0);

            views.setOnClickPendingIntent(R.id.widget_frame_layout, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context.getApplicationContext(),
                    "There was a problem loading the application: ",
                    Toast.LENGTH_SHORT).show();
        }


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void showChart(Context context, RemoteViews views, int appWidgetId, String symbol) {

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        if (appWidgetIds == null) return;
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

        }
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
        if (intent.hasExtra(WIDGET_IDS_KEY)) {
            int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
            if (intent.hasExtra(WIDGET_DATA_KEY)) {
                String data = intent.getExtras().getString(WIDGET_DATA_KEY);
                Timber.d("Widget onReceive data=%s", (String) data);
            }
            // How to force update on DB`?
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
        } else super.onReceive(context, intent);
    }
}

