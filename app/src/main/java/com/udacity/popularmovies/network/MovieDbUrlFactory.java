package com.udacity.popularmovies.network;

import android.content.Context;

import com.udacity.popularmovies.R;

/**
 * Created by Norbert Boros on 2018.02.22..
 */

public class MovieDbUrlFactory {

    private static final String POPULAR_MOVIES_URL = "https://api.themoviedb.org/3/movie/popular?api_key=%s";

    private static final String TOP_RATED_MOVIES_URL = "https://api.themoviedb.org/3/movie/top_rated?api_key=%s";

    public static String popularMovies(Context context){
        String apiKey = context.getString(R.string.moviedb_api_key);

        return String.format(POPULAR_MOVIES_URL, apiKey);
    }

    public static String topRatedMovies(Context context){
        String apiKey = context.getString(R.string.moviedb_api_key);

        return String.format(TOP_RATED_MOVIES_URL, apiKey);
    }
}
