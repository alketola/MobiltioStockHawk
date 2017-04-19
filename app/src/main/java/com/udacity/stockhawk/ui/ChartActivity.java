package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.udacity.stockhawk.R;

import timber.log.Timber;

public class ChartActivity extends AppCompatActivity implements StockChartFragment.OnFragmentInteractionListener {

    String mSymbol = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        // There should be the activity chart frame layout available
        if (findViewById(R.id.activity_chart_frame) != null) {
            if (savedInstanceState != null) return;

            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            mSymbol = bundle.getString(StockChartFragment.ARG_STOCK_TICKER);
            Timber.d("onCreate received request for symbol: %s", mSymbol);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.activity_chart_frame, new StockChartFragment().newInstance(mSymbol));
            ft.commit();
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Timber.d("onFragmentInteraction:Uri=%s", uri.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chart_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_goto_statistics) {
            UiUtil.showStatsDisplay(ChartActivity.this, mSymbol);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
