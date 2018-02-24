package com.udacity.popularmovies.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.network.MovieDbUrlFactory;
import com.udacity.popularmovies.network.MovieListLoader;
import com.udacity.popularmovies.view.MovieGridAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Integer MOVIE_LOADER_ID = 1;

    private enum MovieSortOrder {
        BY_POPULARITY, BY_RATINGS
    }

    private MovieSortOrder movieSortOrder;

    private RecyclerView moviesRecyclerView;

    private MovieGridAdapter movieGridAdapter;

    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int orientation = getResources().getConfiguration().orientation;
        int spanCount = orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3;

        moviesRecyclerView = findViewById(R.id.moviesRecyclerView);
        moviesRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        loadingIndicator = findViewById(R.id.loadingIndicator);

        /* By default, sort movies by popularity */
        movieSortOrder = MovieSortOrder.BY_POPULARITY;

        loadMovies();
    }

    private void loadMovies(){
        if(!isOnline()){
            showOfflineDialog();
            return;
        }

        loadingIndicator.setVisibility(View.VISIBLE);

        Bundle args = new Bundle();

        switch (movieSortOrder) {
            case BY_POPULARITY:
                args.putString(MovieListLoader.API_ENDPOINT_PARAM, MovieDbUrlFactory.popularMovies(this));
                break;
            case BY_RATINGS:
                args.putString(MovieListLoader.API_ENDPOINT_PARAM, MovieDbUrlFactory.topRatedMovies(this));
                break;
            default:
                Log.w(TAG, "Could not create API endpoint URL for " + movieSortOrder);
                return;
        }

        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, args, this).forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);

        /* Toggle the currently visible sorting option. ( https://stackoverflow.com/a/10692826 ) */
        menu.findItem(R.id.sortByPopularity).setVisible(movieSortOrder == MovieSortOrder.BY_RATINGS);
        menu.findItem(R.id.sortByRatings).setVisible(movieSortOrder == MovieSortOrder.BY_POPULARITY);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.sortByPopularity:
                return sortBy(MovieSortOrder.BY_POPULARITY);
            case R.id.sortByRatings:
                return sortBy(MovieSortOrder.BY_RATINGS);
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean sortBy(MovieSortOrder sorting){
        movieSortOrder = sorting;
        loadMovies();

        invalidateOptionsMenu();

        return true;
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        return new MovieListLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        loadingIndicator.setVisibility(View.GONE);

        if(movieGridAdapter == null){
            movieGridAdapter = new MovieGridAdapter(data, this);
            moviesRecyclerView.setAdapter(movieGridAdapter);
        } else {
            movieGridAdapter.updateMovies(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {

    }

    private void showOfflineDialog(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.offline_dialog_title)
                .setMessage(R.string.offline_dialog_message)
                .setPositiveButton(R.string.offline_dialog_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loadMovies();
                    }
                })
                .create();

        dialog.show();
    }

    /**
     * From https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}