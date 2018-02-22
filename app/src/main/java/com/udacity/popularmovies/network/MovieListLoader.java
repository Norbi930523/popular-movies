package com.udacity.popularmovies.network;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.Movie;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Norbert Boros on 2018.02.21..
 */

public class MovieListLoader extends AsyncTaskLoader<List<Movie>> {

    public static final String API_ENDPOINT_PARAM = "apiEndpoint";

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
        Request request = new Request.Builder()
                .url(args.getString(API_ENDPOINT_PARAM))
                .build();

        try {
            Response response = client.newCall(request).execute();

            JsonObject json = jsonParser.parse(response.body().string()).getAsJsonObject();

            return gson.fromJson(json.getAsJsonArray("results"), listType);
        } catch (IOException e) {
            Log.e(TAG, "An error occurred while loading movies", e);
        }

        return null;
    }
}
