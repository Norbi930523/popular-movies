package com.udacity.popularmovies.database.movie;

import android.net.Uri;
import android.provider.BaseColumns;

import com.udacity.popularmovies.database.BaseContentProvider;

/**
 * Created by Norbert Boros on 2018.02.25..
 */

public class MovieContract {

    public static class FavouriteMovieEntry {

        public static final String PATH_NAME = "favouriteMovies";

        public static final Uri CONTENT_URI = BaseContentProvider.BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_NAME)
                .build();

        public static final String TABLE_NAME = "FAVOURITE_MOVIES";

        public static final String COLUMN_MOVIE_ID = "MOVIE_ID";
        public static final String COLUMN_ORIGINAL_TITLE = "ORIGINAL_TITLE";
        public static final String COLUMN_LOCALIZED_TITLE = "LOCALIZED_TITLE";
        public static final String COLUMN_RELEASE_DATE = "RELEASE_DATE";
        public static final String COLUMN_VOTE_AVERAGE = "VOTE_AVERAGE";
        public static final String COLUMN_VOTE_COUNT = "VOTE_COUNT";
        public static final String COLUMN_OVERVIEW = "OVERVIEW";
        public static final String COLUMN_POSTER = "POSTER";
        public static final String COLUMN_LAST_UPDATE = "LAST_UPDATE";

        public static final String CREATE_SQL = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                COLUMN_LOCALIZED_TITLE + " TEXT, " +
                COLUMN_RELEASE_DATE + " INTEGER, " +
                COLUMN_VOTE_AVERAGE + " REAL, " +
                COLUMN_VOTE_COUNT + " INTEGER, " +
                COLUMN_OVERVIEW + " TEXT, " +
                COLUMN_POSTER + " TEXT, " +
                COLUMN_LAST_UPDATE + " INTEGER);";

    }

}
