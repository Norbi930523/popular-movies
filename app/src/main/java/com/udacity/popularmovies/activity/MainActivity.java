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
import com.udacity.popularmovies.database.movie.FavouriteMovieContentObserver;
import com.udacity.popularmovies.database.movie.MovieContract;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.network.MovieDbUrlFactory;
import com.udacity.popularmovies.network.MovieListLoader;
import com.udacity.popularmovies.network.NetworkUtils;
import com.udacity.popularmovies.view.MovieGridAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SELECTED_MOVIE_CATEGORY_KEY = "selectedMovieCategory";

    private static final int MOVIE_LOADER_ID = 100;

    private class MovieCategories {
        static final int POPULAR = 0;
        static final int TOP_RATED = 1;
        static final int FAVOURITES = 2;
    }

    private int selectedMovieCategory;

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

        getContentResolver().registerContentObserver(
                MovieContract.FavouriteMovieEntry.CONTENT_URI,
                true,
                FavouriteMovieContentObserver.getInstance());

        if(savedInstanceState != null){
            selectedMovieCategory = savedInstanceState.getInt(SELECTED_MOVIE_CATEGORY_KEY);
        } else {
            /* By default, show popular movies */
            selectedMovieCategory = MovieCategories.POPULAR;
        }

        loadMovies(true);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Boolean contentChanged = FavouriteMovieContentObserver.getInstance().hasContentChanged();

        /* Reload movies when the FavouriteMovie content changes and we are on the Favourites page */
        if(contentChanged && selectedMovieCategory == MovieCategories.FAVOURITES){
            loadMovies(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getContentResolver().unregisterContentObserver(FavouriteMovieContentObserver.getInstance());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_MOVIE_CATEGORY_KEY, selectedMovieCategory);

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

    private void loadMovies(boolean initLoader){
        if(!NetworkUtils.isOnline(this)){
            showOfflineDialog();
            return;
        }

        FavouriteMovieContentObserver.getInstance().setContentChanged(Boolean.FALSE);

        loadingIndicator.setVisibility(View.VISIBLE);

        Bundle args = new Bundle();

        switch (selectedMovieCategory) {
            case MovieCategories.POPULAR:
                args.putString(MovieListLoader.SOURCE_URI_PARAM, MovieDbUrlFactory.popularMovies(this));
                break;
            case MovieCategories.TOP_RATED:
                args.putString(MovieListLoader.SOURCE_URI_PARAM, MovieDbUrlFactory.topRatedMovies(this));
                break;
            case MovieCategories.FAVOURITES:
                args.putString(MovieListLoader.SOURCE_URI_PARAM, MovieContract.FavouriteMovieEntry.CONTENT_URI.toString());
                break;
            default:
                Log.w(TAG, "Could not create source URI for " + selectedMovieCategory);
                return;
        }

        if(initLoader){
            getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, args, this).forceLoad();
        } else {
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, args, this).forceLoad();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);

        /* Toggle the currently visible category option. ( https://stackoverflow.com/a/10692826 ) */
        menu.findItem(R.id.showPopular).setVisible(selectedMovieCategory != MovieCategories.POPULAR);
        menu.findItem(R.id.showTopRated).setVisible(selectedMovieCategory != MovieCategories.TOP_RATED);
        menu.findItem(R.id.showFavourites).setVisible(selectedMovieCategory != MovieCategories.FAVOURITES);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.showPopular:
                return showCategory(MovieCategories.POPULAR);
            case R.id.showTopRated:
                return showCategory(MovieCategories.TOP_RATED);
            case R.id.showFavourites:
                return showCategory(MovieCategories.FAVOURITES);
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean showCategory(int category){
        selectedMovieCategory = category;
        loadMovies(false);

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
                        loadMovies(false);
                    }
                })
                .create();

        dialog.show();
    }

}
