package com.udacity.popularmovies.database.movie;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by Norbert Boros on 2018.02.26..
 */

public class FavouriteMovieContentObserver extends ContentObserver {

    private static final FavouriteMovieContentObserver INSTANCE = new FavouriteMovieContentObserver();

    private Boolean contentChanged;

    private FavouriteMovieContentObserver() {
        super(new Handler());
    }

    public static FavouriteMovieContentObserver getInstance(){
        return INSTANCE;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        contentChanged = true;
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

    public Boolean getContentChanged() {
        return contentChanged;
    }

    public void setContentChanged(Boolean contentChanged) {
        this.contentChanged = contentChanged;
    }
}
