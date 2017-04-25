package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.udacity.stockhawk.sync.QuoteSyncJob.ACTION_DATA_UPDATED;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler,
        StockChartFragment.OnFragmentInteractionListener {

    private static final int STOCK_LOADER = 0;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;
    private StockAdapter adapter;

    private int mDisplayHeight;
    private String mSymbol = "";
    private NetworkReceiver mNetworkReceiver;

    @Override
    public void onClick(String symbol) {
        Timber.d("Symbol clicked: %s", symbol);
        //AK: Ok here we can put the chart showing
        // Intent with symbol extra string
        mSymbol = symbol;

        showStockChart(mSymbol);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkReceiver = new NetworkReceiver(this);
        this.registerReceiver(mNetworkReceiver, filter);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDisplayHeight = metrics.heightPixels;

        adapter = new StockAdapter(this, this);

        swipeRefreshLayout.setOnRefreshListener(this);
        stockRecyclerView.setAdapter(adapter);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(getApplicationContext(), symbol);
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
                Intent dataUpdatedIntent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
                getApplicationContext().sendBroadcast(dataUpdatedIntent);

            }
        }).attachToRecyclerView(stockRecyclerView);

        if (savedInstanceState != null) return;

    }

    @Override
    protected void onResume() {
        super.onResume();
        mNetworkReceiver.setEnabled(true);
    }

    @Override
    protected void onPause() {
        mNetworkReceiver.setEnabled(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(mNetworkReceiver);
        super.onDestroy();
    }

    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(this);

        if (!mNetworkReceiver.networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, getString(R.string.error_no_network), Toast.LENGTH_LONG).show();
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
        } else if (!mNetworkReceiver.networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(this).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, getString(R.string.error_no_stocks), Toast.LENGTH_LONG).show();
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);
            ifLandPutStockChart(mSymbol);
        } else {
            error.setVisibility(View.GONE);
        }
    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {

            if (mNetworkReceiver.networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                @SuppressLint({"StringFormatInvalid", "LocalSuppress"})
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            PrefUtils.addStock(this, symbol);
            QuoteSyncJob.syncImmediately(this);
        }
    }

    /* This task is here just because one can't swap a fragment in onLoadFinish
     * Better to have a new little thread, which works.
     *
     * module variable reference: mSymbol
     */
    private class ChartChangeTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ifLandPutStockChart(mSymbol);
            return null;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);

        if (data.getCount() != 0) {
            error.setVisibility(View.GONE);
        }
        data.moveToFirst();
        int count = data.getCount();
        if (count > 0) {
            mSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            Timber.d("Load finished. default symbol=%s", mSymbol);
            adapter.setCursor(data);
            new ChartChangeTask().execute(); //execute ifLandPutStockChart(mSymbol); on thread
        } else {
            Timber.v("Cursor with no data");
        }

    }



    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
            item.setTitleCondensed(getString(R.string.content_desc_main_menu_percentage));
        } else {
            item.setIcon(R.drawable.ic_dollar);
            item.setTitleCondensed(getString(R.string.content_desc_main_menu_currency));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.action_goto_statistics) {
            UiUtil.startStatsActivity(this, mSymbol);
            return true;
        } else if (id == R.id.action_show_big_graph) {
            UiUtil.startChartActivity(this, mSymbol);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void ifLandPutStockChart(String symbol) {
        if (null != findViewById(R.id.fl_chart_in_main_land)) {
            UiUtil.replaceChartFragment(this, symbol);
        }
    }

    private void showStockChart(String symbol) {
        if (null != findViewById(R.id.fl_chart_in_main_land)) {
            UiUtil.replaceChartFragment(this, symbol);
        } else {
            UiUtil.startChartActivity(this, symbol);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Timber.d("onFragmentInteraction,    Uri: %s", uri);
    }
}
