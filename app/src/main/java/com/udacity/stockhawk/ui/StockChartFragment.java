package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static com.github.mikephil.charting.charts.Chart.PAINT_INFO;
import static com.udacity.stockhawk.data.Contract.Quote.makeUriForStock;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StockChartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StockChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StockChartFragment extends Fragment {

    // COMPLETED: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_STOCK_TICKER = "com.udacity.stockhawk.argument.stocksymbol";

    // TODO: Rename and change types of parameters
    private String mStockTicker;

    private OnFragmentInteractionListener mListener;


    public StockChartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param stockTicker Parameter 1.
     * @return A new instance of fragment StockChartFragment.
     */

    public static StockChartFragment newInstance(String stockTicker) {
        StockChartFragment fragment = new StockChartFragment();
        Timber.d("NewInstance stockTicker=%s", stockTicker);
        Bundle args = new Bundle();
        args.putString(ARG_STOCK_TICKER, stockTicker);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStockTicker = getArguments().getString(ARG_STOCK_TICKER);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mStockTicker = getArguments().getString(ARG_STOCK_TICKER);
        Timber.d("mStockTicker=%s", mStockTicker);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_stock_chart, container, false);

//        TextView dtv = (TextView) v.findViewById(R.id.dtxt);
//        dtv.setText(mStockTicker);

//        int containerWidth = container.getMeasuredWidth();
//        int containerHeight = container.getMeasuredHeight();
//        Timber.d("containerWidth = %d, containerHeight = %d", containerWidth, containerHeight);

        Uri queryUri = makeUriForStock(mStockTicker);
        Cursor cursor = getContext().getContentResolver()
                .query(queryUri, null, null, null, null);


        String history = "";
        if (cursor != null) {
            cursor.moveToFirst();
            history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));

            Timber.d("History = %s", history);
            cursor.close();
        } else {
            Timber.d("No cursor.");
        }

        List<String> valuepairs = Arrays.asList(history.split("\n"));
        LineChart mLineChart;
        mLineChart = (LineChart) v.findViewById(R.id.chart_view);
        mLineChart.setScaleEnabled(true);

        List<Entry> entries = new ArrayList<Entry>();
        int entryCount = 0;
        long now = System.currentTimeMillis();
        long threeMonthsAgo = now - (86400000L * 90);
        for (String valuepair : valuepairs) {
            //Timber.d("valuepair=%s", valuepair);
            List<String> pair = Arrays.asList(valuepair.split(","));
            float time_ms;
            float quote_dollars;
            try {
                time_ms = Float.valueOf(pair.get(0)); // Had crashed here!

                //Timber.d("time_ms=%f", time_ms);
                if (time_ms > threeMonthsAgo) {
                    quote_dollars = Float.valueOf(pair.get(1));
                    //Timber.d("quote_dollars=%4.4f", quote_dollars);
                    entries.add(new Entry(time_ms, quote_dollars));
                    entryCount++;
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        Timber.d("Processed history data; entry count = %d", entryCount);
        if (entryCount < 2) {
            Timber.e("Insufficient chart data available: %d entries", entryCount);
            TextView errorView = new TextView(getActivity());
            errorView.setText("ERROR. CHART DATA NOT FOUND");
            return (View) errorView;
        }
        Collections.sort(entries, new EntryXComparator());

        LineDataSet dataSet = new LineDataSet(entries, mStockTicker);
        dataSet.setDrawCircles(false);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setColor(Color.DKGRAY);
        dataSet.setHighLightColor(Color.RED);
        dataSet.setLineWidth(2.0f);
        dataSet.setVisible(true);

        XAxis xAxis = mLineChart.getXAxis();
//        xAxis.setAxisMaximum(dataSet.getXMax());
//        xAxis.setAxisMinimum(dataSet.getXMin());
        xAxis.setValueFormatter(new DateAxisFormatter());

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setTextSize(12f);
        leftAxis.setAxisMaximum(dataSet.getYMax());
        leftAxis.setAxisMinimum(dataSet.getYMin());

        mLineChart.getDescription().setText("");
        mLineChart.getLegend().setEnabled(true);
        mLineChart.setDrawMarkers(false);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(dataSet);
        LineData lineData = new LineData(dataSets);

        Paint paint = mLineChart.getPaint(PAINT_INFO);
        mLineChart.setData(lineData);
        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();


        return v;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class DateAxisFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            //TODO should analyse how much space from axis
            SimpleDateFormat formatter = new SimpleDateFormat(" d/M ", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) value);
            String formattedValue = formatter.format(calendar.getTime());

            return formattedValue;
        }
    }
}
