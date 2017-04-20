package com.udacity.stockhawk;

import android.app.Application;

import java.util.logging.Level;

import timber.log.Timber;
import yahoofinance.YahooFinance;

public class StockHawkApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        } else {
            YahooFinance.logger.setLevel(Level.SEVERE);
        }
    }
}
