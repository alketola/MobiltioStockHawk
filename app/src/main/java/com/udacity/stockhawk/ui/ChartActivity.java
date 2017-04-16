package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;

import timber.log.Timber;

public class ChartActivity extends AppCompatActivity implements StockChartFragment.OnFragmentInteractionListener {

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
            String stock = bundle.getString(StockChartFragment.ARG_STOCK_TICKER);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.activity_chart_frame, new StockChartFragment().newInstance(stock));
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
}
