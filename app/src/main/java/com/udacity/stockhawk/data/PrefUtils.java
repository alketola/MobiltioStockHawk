package com.udacity.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.udacity.stockhawk.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

public final class PrefUtils {

    private PrefUtils() {
    }

    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        boolean initialized = prefs.getBoolean(initializedKey, false);

        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            editor.putStringSet(stocksKey, defaultStocks);
            editor.apply();
            return defaultStocks;
        }
        return prefs.getStringSet(stocksKey, new HashSet<String>());

    }

    /* Rewritten this because it didn't originally work correctly.
     * putStringSet has the bug that it doesn't replace the Set<String>, but leaves it intact
     * if there's one with the same key!
     *
     * Prefs must be read, clear()ed and built and put up again.
     */
    private static synchronized void editStockPref(Context context, String symbol, Boolean add) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        boolean initialized = prefs.getBoolean(initializedKey, false);

        String displayMode = getDisplayMode(context);
        String displayModeKey = context.getString(R.string.pref_display_mode_key);
        String stocksKey = context.getString(R.string.pref_stocks_key);

        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putBoolean(initializedKey, initialized);
        editor.putString(displayModeKey, displayMode);

        Set<String> stocks = getStocks(context);

        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }

        editor.putStringSet(stocksKey, stocks);
        editor.commit(); //.apply();
    }

    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        Timber.d("removeStock(context=%s, symbol=%s", context.toString(), symbol);
        editStockPref(context, symbol, false);
    }

    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.commit(); //.apply();
    }

}
