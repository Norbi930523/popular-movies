package com.udacity.popularmovies.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Norbert Boros on 2018.02.28..
 */

public abstract class NetworkConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean oldOnlineState = NetworkConnectionContext.getInstance().isOnline();
        boolean newOnlineState = NetworkUtils.isOnline(context);

        if(oldOnlineState != newOnlineState){
            NetworkConnectionContext.getInstance().setOnline(newOnlineState);

            onNetworkConnectivityChanged();
        }

    }

    public abstract void onNetworkConnectivityChanged();
}
