package com.udacity.popularmovies.service;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.udacity.popularmovies.common.FileUtils;
import com.udacity.popularmovies.database.movie.MovieContract;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.network.MovieDbUrlFactory;
import com.udacity.popularmovies.network.NetworkConnectionContext;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Norbert Boros on 2018.03.02..
 */

class FavouriteMovieUpdaterAsyncTask extends AsyncTask<Object, Void, Void> {

    private static final String TAG = FavouriteMovieUpdaterAsyncTask.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();

    private Gson gson;

    private WeakReference<JobService> jobService;

    private JobParameters jobParameters;

    FavouriteMovieUpdaterAsyncTask(){
        super();

        this.gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    }

    @Override
    protected Void doInBackground(Object... objects) {
        /* If the user is offline, update is not possible, so do nothing */
        if(NetworkConnectionContext.getInstance().isOffline()){
            return null;
        }

        JobService service = (JobService) objects[0];

        jobService = new WeakReference<>(service);
        jobParameters = (JobParameters) objects[1];

        /* Query favourite movies that were last updated before today 00:00:00 */
        Long today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH).getTime();

        Cursor cursor = service.getContentResolver().query(
                MovieContract.FavouriteMovieEntry.CONTENT_URI,
                ArrayUtils.toArray(MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_ID),
                MovieContract.FavouriteMovieEntry.COLUMN_LAST_UPDATE + " < ?",
                ArrayUtils.toArray(today.toString()),
                null
        );

        while(cursor.moveToNext()){
            Long movieId = cursor.getLong(cursor.getColumnIndex(MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_ID));

            if(NetworkConnectionContext.getInstance().isOffline()){
                break; // The user has gone offline while updating the movies, finish
            }

            Movie movie = getMovieById(service, movieId);

            if(movie != null){
                Uri movieUri = MovieContract.FavouriteMovieEntry.CONTENT_URI.buildUpon().appendPath(movieId.toString()).build();
                service.getContentResolver().update(
                        movieUri,
                        movie.getAsContentValues(),
                        null,
                        null
                );

                FileUtils.savePoster(service, movie.getPosterPath());
            }
        }

        cursor.close();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        JobService service = jobService.get();

        if(service != null){
            service.jobFinished(jobParameters, false);
        }

        jobService.clear();

    }

    private Movie getMovieById(Context context, Long movieId){
        Request request = new Request.Builder()
                .url(MovieDbUrlFactory.movie(context, movieId))
                .build();

        try {
            Response response = client.newCall(request).execute();

            return gson.fromJson(response.body().string(), Movie.class);
        } catch (IOException e) {
            Log.e(TAG, "An error occurred while loading movies", e);
        }

        return null;
    }
}
