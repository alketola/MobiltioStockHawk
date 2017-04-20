package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.udacity.stockhawk.R;

import timber.log.Timber;

/**
 * Created by antti on 2017-04-19.
 */

public class UiUtil {

    // This utility method sends an intent to Chart Activity to show the
    // stockChartFragment for the desired stock symbol

    public static void showStatsDisplay(Context context, String symbol) {

        Intent gotoStats = new Intent(context, com.udacity.stockhawk.ui.StatsActivity.class);
        gotoStats.putExtra(StockChartFragment.ARG_STOCK_TICKER, symbol);

        context.startActivity(gotoStats);
        Timber.d("Started Stats Activity from %s", context.toString());
    }

    // The purpose of this utility method is to split an acronym with spaces
    // in order to prevent Acessibility Screen Reader (i.e.) TalkBack
    // to pronounce the acronym as word, instead, it will pronounce each letter.
    // Sounds less stupid to hear G O O G instead of a guug :-D

    public static String spaceOutAcronym(String acronym) {
        if (acronym == null) return "";

        char[] characters = acronym.toCharArray();
        String result = "";
        String s = "";
        for (int i = 0; i < characters.length; i++) {
            s = Character.toString(characters[i]);
            result += s + " ";
        }
        return result;
    }


    public static void startChartActivity(Context context, String symbol) {
        Intent showChart = new Intent(context, ChartActivity.class);
        Timber.d("setStockChartFragment, starting ChartActivity");
        showChart.putExtra(StockChartFragment.ARG_STOCK_TICKER, symbol);

        context.startActivity(showChart);
    }

    public static void startStatsActivity(Context context, String symbol) {

        Intent gotoStats = new Intent(context, com.udacity.stockhawk.ui.StatsActivity.class);
        gotoStats.putExtra(StockChartFragment.ARG_STOCK_TICKER, symbol);

        context.startActivity(gotoStats);
        Timber.d("Started Stats Activity from %s", context.toString());
    }

    public static void replaceChartFragment(FragmentActivity activity, String symbol) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_chart_in_main_land, new StockChartFragment().newInstance(symbol));
        ft.commit();
    }

}
