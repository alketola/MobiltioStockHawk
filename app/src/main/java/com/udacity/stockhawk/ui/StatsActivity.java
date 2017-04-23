package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.udacity.stockhawk.R;

import org.w3c.dom.Text;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockDividend;
import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.quotes.stock.StockStats;

/**
 * Created by Antti on 2017-04-16.
 */

public class StatsActivity extends AppCompatActivity {

    private String mSymbol = "";
    private Stock mStock = null;
    private StockDividend dividend = null;
    private StockStats stats = null;

    @BindView(R.id.stats_name)
    TextView tv_stock_name;
    @BindView(R.id.tv_last_trade_date)
    TextView tv_last_trade_date;
    @BindView(R.id.stats_currency)
    TextView tv_currency;
    @BindView(R.id.stats_ask_price)
    TextView tv_ask_price;
    @BindView(R.id.stats_bid_price)
    TextView tv_bid_price;
    @BindView(R.id.tv_change)
    TextView tv_change;
    @BindView(R.id.tv_change_50d_percent)
    TextView tv_change_50d_percent;
    @BindView(R.id.tv_change_200d_percent)
    TextView tv_change_200d_percent;
    @BindView(R.id.tv_bvps)
    TextView tv_bvps;
    @BindView(R.id.stats_ebitda)
    TextView tv_ebitda;
    @BindView(R.id.stats_eps)
    TextView tv_eps;
    @BindView(R.id.tv_est_curr_y)
    TextView tv_est_curr_y;
    @BindView(R.id.tv_est_next_q)
    TextView tv_est_next_q;
    @BindView(R.id.stats_epstarget)
    TextView tv_epstarget;
    @BindView(R.id.tv_market_cap)
    TextView tv_market_cap;
    @BindView(R.id.tv_1y_target_price)
    TextView tv_1y_target_price;
    @BindView(R.id.stats_pe)
    TextView tv_pe;
    @BindView(R.id.tv_peg)
    TextView tv_peg;
    @BindView(R.id.stats_pb)
    TextView tv_pb;
    @BindView(R.id.stats_ps)
    TextView tv_ps;
    @BindView(R.id.tv_revenue)
    TextView tv_revenue;
    @BindView(R.id.tv_roe)
    TextView tv_roe;
    @BindView(R.id.tv_shares_float)
    TextView tv_shares_float;
    @BindView(R.id.tv_shares_outstanding)
    TextView tv_shares_outstanding;
    @BindView(R.id.tv_shares_owned)
    TextView tv_shares_owned;
    @BindView(R.id.tv_short_ratio)
    TextView tv_short_ratio;

    @BindView(R.id.ll_activity_stats)
    LinearLayout ll_stats_display;
    @BindView(R.id.tv_activity_stats_error)
    TextView tv_stats_error;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_stats);
        ButterKnife.bind(this);

        if (savedInstanceState != null) return;

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mSymbol = bundle.getString(StockChartFragment.ARG_STOCK_TICKER);
        Configuration config = getResources().getConfiguration();
        String myTitle;

        myTitle = getString(R.string.app_name)
                + " - "
                + getString(R.string.stats_title)
                + " : "
                + mSymbol;
        setTitle(myTitle);
        FinanceStatGetter getter = new FinanceStatGetter();
        getter.execute(mSymbol);
        showError(false); // Now there's one piece of information set at least even if N/A
    }

    @Override
    protected void onResume() {
        super.onResume();
        FinanceStatGetter getter = new FinanceStatGetter();
        getter.execute(mSymbol);
        showError(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showError(boolean please) {
        if (please) {
            ll_stats_display.setVisibility(View.INVISIBLE);
            tv_stats_error.setVisibility(View.VISIBLE);
        } else {
            ll_stats_display.setVisibility(View.VISIBLE);
            tv_stats_error.setVisibility(View.INVISIBLE);
        }
    }

    private class FinanceStatGetter extends AsyncTask<String, Void, Stock> {

        @Override
        protected Stock doInBackground(String... strings) {
            String symbol = strings[0];
            Stock stock = null;
            Timber.d("FinanceStatGetter.doInBackground(%s)", symbol);
            try {
                stock = YahooFinance.get(symbol);
            } catch (Exception e) {
                Timber.d("Exception; %s", e.toString());
                return null;
            }

            return stock;
        }

        @Override
        protected void onPostExecute(Stock stock) {
            super.onPostExecute(stock);
            if (stock == null) return; // Can't do anything further
            StockDividend dividend = stock.getDividend();
            StockStats stats = stock.getStats();
            StockQuote quote = stock.getQuote();

            Timber.d("stats.toString = %s", stats.toString());
            if (stats == null) {
                showError(true);
                return;
            }

            tv_stock_name.setText(stock.getName());
            tv_last_trade_date.setText(quote.getLastTradeDateStr());
            tv_currency.setText(stock.getCurrency());
            setStatString(quote.getAsk(), tv_ask_price);
            setStatString(quote.getBid(), tv_bid_price);
            setStatString(quote.getChange(), tv_change);
            setStatStringPerCent(quote.getChangeFromAvg50InPercent(), tv_change_50d_percent);
            setStatStringPerCent(quote.getChangeFromAvg200InPercent(), tv_change_200d_percent);
            setStatString(stats.getBookValuePerShare(), tv_bvps);
            setStatStringM(stats.getEBITDA(), tv_ebitda);
            setStatString(stats.getEps(), tv_eps);
            setStatString(stats.getEpsEstimateCurrentYear(), tv_est_curr_y);
            setStatString(stats.getEpsEstimateNextQuarter(), tv_est_next_q);
            setStatString(stats.getEpsEstimateNextYear(), tv_epstarget);
            setStatStringM(stats.getMarketCap(), tv_market_cap);
            setStatString(stats.getOneYearTargetPrice(), tv_1y_target_price);
            setStatString(stats.getPe(), tv_pe);
            setStatString(stats.getPeg(), tv_peg);
            setStatString(stats.getPriceBook(), tv_pb);
            setStatString(stats.getPriceSales(), tv_ps);
            setStatStringM(stats.getRevenue(), tv_revenue);
            setStatString(stats.getROE(), tv_roe);
            setStatStringM(stats.getSharesFloat(), tv_shares_float);
            setStatStringM(stats.getSharesOutstanding(), tv_shares_outstanding);
            Long owned = stats.getSharesOwned();
            setStatString(owned, tv_shares_owned);
            setStatString(stats.getShortRatio(), tv_short_ratio);

        }
    }

    private void setStatString(BigDecimal bd, TextView tv) {
        String s;
        try {
            s = bd.toString();
        } catch (Exception e) {
            s = getString(R.string.value_not_available);
        }
        tv.setText(s);
    }

    private void setStatString(Long a_number, TextView tv) {
        String s;
        try {
            s = Long.toString(a_number);
        } catch (Exception e) {
            s = getString(R.string.value_not_available);
        }
        tv.setText(s);
    }

    private void setStatStringM(BigDecimal bd, TextView tv) {
        String s;
        BigDecimal million = new BigDecimal("1000000");
        BigDecimal bd_m = bd.divide(million);

        try {
            s = bd_m.toString() + " " + getString(R.string.m_letter_for_million_values);
        } catch (Exception e) {
            s = getString(R.string.value_not_available);
        }
        tv.setText(s);
    }

    private void setStatStringM(Long long_int, TextView tv) {
        String s;
        BigDecimal million = new BigDecimal("1000000");
        BigDecimal bd_long = new BigDecimal(long_int);
        BigDecimal bd_m = bd_long.divide(million);

        try {
            s = bd_m.toString() + " " + getString(R.string.m_letter_for_million_values);
        } catch (Exception e) {
            s = getString(R.string.value_not_available);
        }
        tv.setText(s);
    }

    private void setStatStringPerCent(BigDecimal bd, TextView tv) {
        String s;

        try {
            s = bd.toString() + " %";
        } catch (Exception e) {
            s = getString(R.string.value_not_available);
        }
        tv.setText(s);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("StatsStockSymbol", mSymbol);

        super.onSaveInstanceState(outState);
        Timber.d("Saved instance state");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSymbol = savedInstanceState.getString("StatsStockSymbol");
        Timber.d("Restored instance state, mSymbol=%s", mSymbol);
    }
}
