package com.udacity.popularmovies.database;

import android.content.ContentProvider;
import android.net.Uri;

/**
 * Created by Norbert Boros on 2018.02.25..
 */

public abstract class BaseContentProvider extends ContentProvider {

    protected static final String AUTHORITY = "com.udacity.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

}
