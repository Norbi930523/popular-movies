package com.udacity.popularmovies.activity;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
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
import com.udacity.popularmovies.network.NetworkConnectionContext;
import com.udacity.popularmovies.network.NetworkConnectivityChangeReceiver;
import com.udacity.popularmovies.network.NetworkUtils;
import com.udacity.popularmovies.view.MovieGridAdapter;

import java.util.List;

public class MainActivity extends StateAwareActivity implements LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SELECTED_MOVIE_CATEGORY_KEY = "selectedMovieCategory";

    private static final int MOVIE_LOADER_ID = 100;

    private class MovieCategory {
        static final int POPULAR = 0;
        static final int TOP_RATED = 1;
        static final int FAVOURITES = 2;
    }

    private NetworkConnectivityChangeListener connectivityChangeListener;

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

        /* Before doing anything, initialize the network connection context */
        NetworkConnectionContext.getInstance().setOnline(NetworkUtils.isOnline(this));

        /* Register a receiver to listen to network connectivity changes */
        connectivityChangeListener = new NetworkConnectivityChangeListener(NetworkConnectionContext.getInstance().isOnline());
        registerReceiver(connectivityChangeListener, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        /* Register observer to react to fav / unfav actions */
        getContentResolver().registerContentObserver(
                MovieContract.FavouriteMovieEntry.CONTENT_URI,
                true,
                FavouriteMovieContentObserver.getInstance());

        if(savedInstanceState != null){
            selectedMovieCategory = savedInstanceState.getInt(SELECTED_MOVIE_CATEGORY_KEY);
        } else {
            /* By default, show popular movies */
            selectedMovieCategory = MovieCategory.POPULAR;
        }

        if(NetworkConnectionContext.getInstance().isOffline()){
            /* Only the Favourite movies are available in offline mode */
            selectedMovieCategory = MovieCategory.FAVOURITES;
            showOfflineDialog();
        }

        loadMovies(true);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Boolean contentChanged = FavouriteMovieContentObserver.getInstance().hasContentChanged();
        Boolean connectivityStateChanged = connectivityChangeListener.hasConnectivityStateChanged();

        /* Reload movies when the network connectivity state or
           the FavouriteMovie content changes and we are on the Favourites page */
        if(connectivityStateChanged || contentChanged && selectedMovieCategory == MovieCategory.FAVOURITES){
            loadMovies(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(connectivityChangeListener);

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
        if(NetworkConnectionContext.getInstance().isOffline() && isNetworkRequiredForSelectedCategory()){
            Log.w(TAG, "Tried to load movies in offline mode for a category that requires online mode: " + selectedMovieCategory);
            return;
        }

        FavouriteMovieContentObserver.getInstance().setContentChanged(Boolean.FALSE);

        loadingIndicator.setVisibility(View.VISIBLE);

        Bundle args = new Bundle();

        switch (selectedMovieCategory) {
            case MovieCategory.POPULAR:
                args.putString(MovieListLoader.SOURCE_URI_PARAM, MovieDbUrlFactory.popularMovies(this));
                break;
            case MovieCategory.TOP_RATED:
                args.putString(MovieListLoader.SOURCE_URI_PARAM, MovieDbUrlFactory.topRatedMovies(this));
                break;
            case MovieCategory.FAVOURITES:
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

    private boolean isNetworkRequiredForSelectedCategory(){
        return selectedMovieCategory != MovieCategory.FAVOURITES;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(NetworkConnectionContext.getInstance().isOffline()){
            /* If the user is offline, only the Favourites page is available,
             * so it is unnecessary to render the settings menu */
            return false;
        }

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);

        /* When online, toggle the currently visible category option. ( https://stackoverflow.com/a/10692826 ) */
        menu.findItem(R.id.showPopular).setVisible(selectedMovieCategory != MovieCategory.POPULAR);
        menu.findItem(R.id.showTopRated).setVisible(selectedMovieCategory != MovieCategory.TOP_RATED);
        menu.findItem(R.id.showFavourites).setVisible(selectedMovieCategory != MovieCategory.FAVOURITES);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.showPopular:
                return showCategory(MovieCategory.POPULAR);
            case R.id.showTopRated:
                return showCategory(MovieCategory.TOP_RATED);
            case R.id.showFavourites:
                return showCategory(MovieCategory.FAVOURITES);
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
                .setTitle(R.string.offline_dialog_title)
                .setMessage(R.string.offline_dialog_message)
                .setPositiveButton(R.string.offline_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();

        dialog.show();
    }

    private class NetworkConnectivityChangeListener extends NetworkConnectivityChangeReceiver {

        public NetworkConnectivityChangeListener(boolean onlineState) {
            super(onlineState);
        }

        @Override
        public void onNetworkConnectivityChanged() {
            invalidateOptionsMenu();

            if(NetworkConnectionContext.getInstance().isOffline()){
                /* If the user has gone offline, go to the Favourites page */
                selectedMovieCategory = MovieCategory.FAVOURITES;

                if(isActivityOnScreen()){
                    loadMovies(false);
                }
            }
        }
    }

}
