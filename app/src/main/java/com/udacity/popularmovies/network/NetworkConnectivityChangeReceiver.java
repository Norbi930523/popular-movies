package com.udacity.popularmovies.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.udacity.popularmovies.R;

/**
 * Created by Norbert Boros on 2018.02.28..
 */

public abstract class NetworkConnectivityChangeReceiver extends BroadcastReceiver {

    private boolean connectivityStateChanged = false;

    private boolean oldOnlineState = NetworkConnectionContext.getInstance().isOnline();

    public NetworkConnectivityChangeReceiver(boolean onlineState){
        this.oldOnlineState = onlineState;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean newOnlineState = NetworkUtils.isOnline(context);

        if(oldOnlineState != newOnlineState){
            /* Update the global and local network state */
            NetworkConnectionContext.getInstance().setOnline(newOnlineState);
            oldOnlineState = newOnlineState;
            connectivityStateChanged = true;

            /* Notify the context about the change */
            onNetworkConnectivityChanged();

            /* Notify the user about the change */
            int connectivityToast = newOnlineState ? R.string.toast_online_mode : R.string.toast_offline_mode;
            Toast.makeText(context, connectivityToast, Toast.LENGTH_LONG).show();
        }

    }

    public Boolean hasConnectivityStateChanged(){
        Boolean oldValue = connectivityStateChanged;

        connectivityStateChanged = false;

        return oldValue;
    }

    public abstract void onNetworkConnectivityChanged();
}
