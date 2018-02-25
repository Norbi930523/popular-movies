package com.udacity.popularmovies.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.database.movie.MovieContract;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.network.MovieDbUrlFactory;
import com.udacity.popularmovies.network.MovieListLoader;
import com.udacity.popularmovies.network.NetworkUtils;
import com.udacity.popularmovies.view.MovieGridAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int MOVIE_LOADER_ID = 100;

    private class MovieSortOrder {
        public static final int BY_POPULARITY = 0;
        public static final int BY_RATINGS = 1;
        public static final int FAVOURITES = 2;
    }

    private int movieSortOrder;

    private RecyclerView moviesRecyclerView;

    private MovieGridAdapter movieGridAdapter;

    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moviesRecyclerView = findViewById(R.id.moviesRecyclerView);
        moviesRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns()));

        loadingIndicator = findViewById(R.id.loadingIndicator);

        /* By default, sort movies by popularity */
        movieSortOrder = MovieSortOrder.BY_POPULARITY;

        loadMovies();
    }

    /**
     * From Stage 1 review
     */
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int posterWidth = 185;
        float screenWidth = displayMetrics.widthPixels / displayMetrics.density;

        int columns = (int) (screenWidth / posterWidth);

        return columns < 2 ? 2 : columns;
    }

    private void loadMovies(){
        if(!NetworkUtils.isOnline(this)){
            showOfflineDialog();
            return;
        }

        loadingIndicator.setVisibility(View.VISIBLE);

        Bundle args = new Bundle();

        switch (movieSortOrder) {
            case MovieSortOrder.BY_POPULARITY:
                args.putString(MovieListLoader.SOURCE_URI_PARAM, MovieDbUrlFactory.popularMovies(this));
                break;
            case MovieSortOrder.BY_RATINGS:
                args.putString(MovieListLoader.SOURCE_URI_PARAM, MovieDbUrlFactory.topRatedMovies(this));
                break;
            case MovieSortOrder.FAVOURITES:
                args.putString(MovieListLoader.SOURCE_URI_PARAM, MovieContract.FavouriteMovieEntry.CONTENT_URI.toString());
                break;
            default:
                Log.w(TAG, "Could not create source URI for " + movieSortOrder);
                return;
        }

        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, args, this).forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);

        /* Toggle the currently visible sorting option. ( https://stackoverflow.com/a/10692826 ) */
        menu.findItem(R.id.sortByPopularity).setVisible(movieSortOrder != MovieSortOrder.BY_POPULARITY);
        menu.findItem(R.id.sortByRatings).setVisible(movieSortOrder != MovieSortOrder.BY_RATINGS);
        menu.findItem(R.id.showFavourites).setVisible(movieSortOrder != MovieSortOrder.FAVOURITES);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.sortByPopularity:
                return sortBy(MovieSortOrder.BY_POPULARITY);
            case R.id.sortByRatings:
                return sortBy(MovieSortOrder.BY_RATINGS);
            case R.id.showFavourites:
                return sortBy(MovieSortOrder.FAVOURITES);
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean sortBy(int sorting){
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

}
