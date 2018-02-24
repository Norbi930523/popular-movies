package com.udacity.popularmovies.network;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.udacity.popularmovies.model.MovieTrailer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Norbert on 2018.02.24..
 */

public class MovieTrailerListLoader extends AsyncTaskLoader<List<MovieTrailer>> {

    private static final String TAG = MovieTrailerListLoader.class.getSimpleName();

    private static final Type MOVIE_TRAILER_LIST_TYPE = new TypeToken<List<MovieTrailer>>(){}.getType();

    private static final String SITE_YOUTUBE = "YouTube";

    private static final String TYPE_TRAILER = "Trailer";

    public static final String MOVIE_ID_PARAM = "movieId";

    private final OkHttpClient client = new OkHttpClient();

    private final JsonParser jsonParser = new JsonParser();

    private final Gson gson = new Gson();

    private Long movieId;

    public MovieTrailerListLoader(Context context, Bundle args) {
        super(context);
        this.movieId = args.getLong(MOVIE_ID_PARAM);
    }

    @Override
    public List<MovieTrailer> loadInBackground() {
        Request request = new Request.Builder()
                .url(MovieDbUrlFactory.movieTrailers(getContext(), movieId))
                .build();

        try {
            Response response = client.newCall(request).execute();

            JsonObject json = jsonParser.parse(response.body().string()).getAsJsonObject();

            List<MovieTrailer> videos = gson.fromJson(json.get("results").getAsJsonArray(), MOVIE_TRAILER_LIST_TYPE);

            List<MovieTrailer> trailers = new ArrayList<>();
            for(MovieTrailer video : videos){
                /* We are interested only in trailers from YouTube */
                if(video.getSite().equalsIgnoreCase(SITE_YOUTUBE) &&
                        video.getType().equalsIgnoreCase(TYPE_TRAILER)){
                    trailers.add(video);
                }
            }

            return trailers;
        } catch(IOException e){
            Log.e(TAG, "An error occurred while loading movie trailers", e);
        }

        return null;
    }
}
