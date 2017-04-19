package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

/**
 * Created by antti on 2017-04-19.
 */

public class UiUtil {
    public static void showStatsDisplay(Context context, String symbol) {

        Intent gotoStats = new Intent(context, com.udacity.stockhawk.ui.StatsActivity.class);
        gotoStats.putExtra(StockChartFragment.ARG_STOCK_TICKER, symbol);

        context.startActivity(gotoStats);
        Timber.d("Started Stats Activity from %s", context.toString());
    }
}
