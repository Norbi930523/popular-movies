package com.udacity.popularmovies.network;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.Movie;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Norbert Boros on 2018.02.21..
 */

public class MovieListLoader extends AsyncTaskLoader<List<Movie>> {

    private static final String POPULAR_MOVIES_URL = "https://api.themoviedb.org/3/movie/popular?api_key=%s";

    public MovieListLoader(Context context) {
        super(context);
    }

    @Override
    public List<Movie> loadInBackground() {
        String url = String.format(POPULAR_MOVIES_URL, getContext().getString(R.string.moviedb_api_key));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        try {
            Response response = client.newCall(request).execute();

            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
