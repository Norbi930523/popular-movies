package com.udacity.popularmovies.network;

/**
 * Created by Norbert Boros on 2018.02.27..
 */

public class NetworkConnectionContext {

    private static final NetworkConnectionContext INSTANCE = new NetworkConnectionContext();

    private Boolean isOnline;

    public static NetworkConnectionContext getInstance(){
        return INSTANCE;
    }

    public Boolean isOnline() {
        return isOnline;
    }

    public Boolean isOffline(){
        return !isOnline();
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }
}
