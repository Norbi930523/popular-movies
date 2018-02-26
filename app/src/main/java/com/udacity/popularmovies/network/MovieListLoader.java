package com.udacity.popularmovies.network;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.udacity.popularmovies.database.movie.MovieContract;
import com.udacity.popularmovies.model.Movie;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Norbert Boros on 2018.02.21..
 */

public class MovieListLoader extends AsyncTaskLoader<List<Movie>> {

    public static final String SOURCE_URI_PARAM = "sourceUri";

    private static final String TAG = MovieListLoader.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();

    private JsonParser jsonParser = new JsonParser();

    private Type listType = new TypeToken<List<Movie>>(){}.getType();

    private Bundle args;

    private Gson gson;

    public MovieListLoader(Context context, Bundle args) {
        super(context);
        this.args = args;

        this.gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    }

    @Override
    public List<Movie> loadInBackground() {
        String uri = args.getString(SOURCE_URI_PARAM);

        if(uri.startsWith("http")){
            return loadFromUrl(uri);
        } else if(uri.startsWith("content://")){
            return loadFromDatabase(uri);
        } else {
            return new ArrayList<>();
        }
    }

    private List<Movie> loadFromUrl(String url){
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();

            JsonObject json = jsonParser.parse(response.body().string()).getAsJsonObject();

            return gson.fromJson(json.getAsJsonArray("results"), listType);
        } catch (IOException e) {
            Log.e(TAG, "An error occurred while loading movies", e);
        }

        return new ArrayList<>();
    }

    private List<Movie> loadFromDatabase(String contentUri){
        Cursor cursor = getContext().getContentResolver().query(
                Uri.parse(contentUri),
                null,
                null,
                null,
                null
        );

        List<Movie> movies = new ArrayList<>();

        while(cursor.moveToNext()){
            Movie movie = new Movie();
            movie.setId(cursor.getLong(cursor.getColumnIndex(MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_ID)));
            movie.setOriginalTitle(cursor.getString(cursor.getColumnIndex(MovieContract.FavouriteMovieEntry.COLUMN_ORIGINAL_TITLE)));
            movie.setTitle(cursor.getString(cursor.getColumnIndex(MovieContract.FavouriteMovieEntry.COLUMN_LOCALIZED_TITLE)));
            movie.setReleaseDate(new Date(cursor.getLong(cursor.getColumnIndex(MovieContract.FavouriteMovieEntry.COLUMN_RELEASE_DATE))));
            movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndex(MovieContract.FavouriteMovieEntry.COLUMN_VOTE_AVERAGE)));
            movie.setVoteCount(cursor.getLong(cursor.getColumnIndex(MovieContract.FavouriteMovieEntry.COLUMN_VOTE_COUNT)));
            movie.setOverview(cursor.getString(cursor.getColumnIndex(MovieContract.FavouriteMovieEntry.COLUMN_OVERVIEW)));
            movie.setPosterPath(cursor.getString(cursor.getColumnIndex(MovieContract.FavouriteMovieEntry.COLUMN_POSTER)));
            movie.setFromDatabase(Boolean.TRUE);

            movies.add(movie);
        }

        cursor.close();

        return movies;
    }
}
