package com.udacity.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.udacity.stockhawk.R;

import timber.log.Timber;

/**
 * Created by Antti on 2017-04-22.
 */

public class NetworkReceiver extends BroadcastReceiver {
    private Context mContext;
    private boolean enabled;

    public NetworkReceiver(Context context) {

        mContext = context;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("NetworkReceiver onReceive");
        if (this.isEnabled()) {
            if (networkUp()) {
                Timber.d("Network Up");
                Toast.makeText(mContext, R.string.network_receiver_up_again, Toast.LENGTH_LONG).show();
            } else {
                Timber.d("Network Down");
                Toast.makeText(mContext, R.string.network_receiver_network_went_down, Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        boolean networkStatus = (networkInfo != null && networkInfo.isConnectedOrConnecting());
        return networkStatus;
    }
}
