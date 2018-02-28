package com.udacity.popularmovies.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Norbert Boros on 2018.02.28..
 */

public class StateAwareActivity extends AppCompatActivity {

    public class ActivityState {
        public static final int CREATED = 0;
        public static final int STARTED = 1;
        public static final int RESUMED = 2;
        public static final int PAUSED = 3;
        public static final int STOPPED = 4;
    }

    protected int currentActivityState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentActivityState = ActivityState.CREATED;
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentActivityState = ActivityState.STARTED;
    }

    @Override
    protected void onResume() {
        super.onResume();

        currentActivityState = ActivityState.RESUMED;
    }

    @Override
    protected void onPause() {
        super.onPause();

        currentActivityState = ActivityState.PAUSED;
    }

    @Override
    protected void onStop() {
        super.onStop();

        currentActivityState = ActivityState.STOPPED;
    }

    protected boolean isActivityOnScreen(){
        return currentActivityState == ActivityState.RESUMED;
    }
}
