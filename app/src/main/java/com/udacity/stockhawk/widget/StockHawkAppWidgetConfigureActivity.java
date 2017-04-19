package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import org.w3c.dom.Text;

import timber.log.Timber;

/**
 * The configuration screen for the {@link StockHawkAppWidget StockHawkAppWidget} AppWidget.
 */
public class StockHawkAppWidgetConfigureActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "com.udacity.stockhawk.widget.StockHawkAppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Cursor mCursor;
    private String mSelectedStock;

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = StockHawkAppWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            saveWidgetStockPref(context, mAppWidgetId, mSelectedStock);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            StockHawkAppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);

            finish();
        }
    };


    public StockHawkAppWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveWidgetStockPref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadWidgetStockPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String symbolToViewInWidget = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (symbolToViewInWidget != null) {
            return symbolToViewInWidget;
        } else {
            return context.getString(R.string.appwidget_stock_error);
        }
    }

    static void deleteWidgetStockPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        Timber.d("...configuring...");
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        Context context = StockHawkAppWidgetConfigureActivity.this;
        setResult(RESULT_CANCELED);

        setContentView(R.layout.stock_hawk_app_widget_configure);

        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Context context = StockHawkAppWidgetConfigureActivity.this;
        //mAppWidgetText.setText(loadWidgetStockPref(StockHawkAppWidgetConfigureActivity.this, mAppWidgetId))
        Uri queryUri = Contract.Quote.URI;
        mCursor = context.getContentResolver()
                .query(queryUri, null, null, null, null);
        mCursor.moveToFirst();
        mSelectedStock = mCursor.getString(Contract.Quote.POSITION_SYMBOL);


        ConfigStockListAdapter adapter =
                new ConfigStockListAdapter(context, mCursor, false);
        final ListView listView = (ListView) findViewById(R.id.list_view_stocks_config);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.list_item_stock_config);
                String symbol = tv.getText().toString();
                mSelectedStock = symbol;
                Timber.d("mSelectedStock=%s", mSelectedStock);
                listView.setSelected(true);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCursor.close();
    }
}

