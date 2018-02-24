package com.udacity.popularmovies.network;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.udacity.popularmovies.model.MovieReview;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Norbert Boros on 2018.02.24..
 */

public class MovieReviewListLoader extends AsyncTaskLoader<List<MovieReview>> {

    private static final String TAG = MovieReviewListLoader.class.getSimpleName();

    private static final Type MOVIE_REVIEW_LIST_TYPE = new TypeToken<List<MovieReview>>(){}.getType();

    public static final String MOVIE_ID_PARAM = "movieId";

    private final OkHttpClient client = new OkHttpClient();

    private final JsonParser jsonParser = new JsonParser();

    private final Gson gson = new Gson();

    private Long movieId;

    public MovieReviewListLoader(Context context, Bundle args) {
        super(context);
        this.movieId = args.getLong(MOVIE_ID_PARAM);
    }

    @Override
    public List<MovieReview> loadInBackground() {
        Request request = new Request.Builder()
                .url(MovieDbUrlFactory.movieReviews(getContext(), movieId))
                .build();

        try {
            Response response = client.newCall(request).execute();

            JsonObject json = jsonParser.parse(response.body().string()).getAsJsonObject();

            return gson.fromJson(json.get("results").getAsJsonArray(), MOVIE_REVIEW_LIST_TYPE);
        } catch (IOException e){
            Log.e(TAG, "An error occurred while loading movie reviews", e);
        }

        return null;
    }
}
