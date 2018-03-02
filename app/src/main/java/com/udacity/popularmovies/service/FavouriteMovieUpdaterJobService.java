package com.udacity.popularmovies.service;

import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by Norbert Boros on 2018.03.02..
 */

public class FavouriteMovieUpdaterJobService extends JobService {

    private AsyncTask<Object, Void, Void> backgroundTask;

    @Override
    public boolean onStartJob(JobParameters job) {
        backgroundTask = new FavouriteMovieUpdaterAsyncTask();
        backgroundTask.execute(this, job);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if(backgroundTask != null){
            backgroundTask.cancel(true);
        }

        return true;
    }

}
