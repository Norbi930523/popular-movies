package com.udacity.popularmovies.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.common.FileUtils;
import com.udacity.popularmovies.database.movie.MovieContract;
import com.udacity.popularmovies.fragment.MovieReviewsFragment;
import com.udacity.popularmovies.fragment.MovieTrailersFragment;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.network.MovieDbUrlFactory;
import com.udacity.popularmovies.network.NetworkConnectionContext;
import com.udacity.popularmovies.network.NetworkConnectivityChangeReceiver;

import java.text.SimpleDateFormat;

public class MovieDetailsActivity extends StateAwareActivity {

    public static final String MOVIE_PARAM = "movie";

    private NetworkConnectivityChangeListener connectivityChangeListener;

    private Movie movie;

    private MovieTrailersFragment movieTrailersFragment;

    private MovieReviewsFragment movieReviewsFragment;

    private ImageButton toggleFavouriteButton;

    private boolean isFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        movie = intent.getParcelableExtra(MOVIE_PARAM);

        if(movie == null){
            finish();
            Toast.makeText(this, R.string.error_movie_required, Toast.LENGTH_LONG).show();
            return;
        }

        /* Register a receiver to listen to network connectivity changes */
        connectivityChangeListener = new NetworkConnectivityChangeListener(NetworkConnectionContext.getInstance().isOnline());
        registerReceiver(connectivityChangeListener, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        createMovieTrailersFragment();
        createMovieReviewsFragment();

        isFavourite = isFavouriteMovie();

        populateUI();

    }

    private void createMovieTrailersFragment() {
        movieTrailersFragment = MovieTrailersFragment.newInstance(movie.getId());

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.movieTrailersFragment, movieTrailersFragment);
        fragmentTransaction.commit();

    }

    private void createMovieReviewsFragment() {
        movieReviewsFragment = MovieReviewsFragment.newInstance(movie.getId());

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.movieReviewsFragment, movieReviewsFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(connectivityChangeListener.hasConnectivityStateChanged()){
            loadTrailers();
            loadReviews();
        }
    }

    private void loadTrailers(){
        movieTrailersFragment.reloadItems();
    }

    private void loadReviews(){
        movieReviewsFragment.reloadItems();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(connectivityChangeListener);
    }

    private boolean isFavouriteMovie(){
        Uri uri = MovieContract.FavouriteMovieEntry.CONTENT_URI.buildUpon().appendPath(movie.getId().toString()).build();

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        boolean isMovieFound = false;

        if(cursor != null){
            isMovieFound = cursor.getCount() == 1;

            cursor.close();
        }

        return isMovieFound;
    }

    private void populateUI() {
        /* Poster image */
        ImageView posterImage = findViewById(R.id.posterImage);
        posterImage.setContentDescription(movie.getTitle());

        if(isFavourite && NetworkConnectionContext.getInstance().isOffline()){
            /* When offline, load a favourite movie poster from storage */
            Picasso.with(this)
                    .load(FileUtils.readPoster(this, movie.getPosterPath()))
                    .placeholder(R.drawable.picasso_placeholder_portrait)
                    .error(R.drawable.picasso_error_portrait)
                    .into(posterImage);
        } else {
            String posterImageUrl = MovieDbUrlFactory.posterImage(movie.getPosterPath());

            Picasso.with(this)
                    .load(posterImageUrl)
                    .placeholder(R.drawable.picasso_placeholder_portrait)
                    .error(R.drawable.picasso_error_portrait)
                    .into(posterImage);
        }

        /* Original title */
        TextView originalTitle = findViewById(R.id.originalTitle);
        originalTitle.setText(movie.getOriginalTitle());

        /* Localized title */
        TextView localizedTitle = findViewById(R.id.localizedTitle);
        localizedTitle.setText(movie.getTitle());

        /* Release date */
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format));

        TextView releaseDate = findViewById(R.id.releaseDate);
        releaseDate.setText(dateFormat.format(movie.getReleaseDate()));

        /* Rating */
        String ratingStr = getResources().getQuantityString(
                R.plurals.rating_pattern,
                movie.getVoteCount().intValue(),
                String.valueOf(movie.getVoteAverage()), movie.getVoteCount());
        TextView rating = findViewById(R.id.rating);
        rating.setText(ratingStr);

        /* Overview */
        TextView overview = findViewById(R.id.overview);
        overview.setText(movie.getOverview());

        /* Toggle favourite */
        toggleFavouriteButton = findViewById(R.id.toggleFavouriteButton);
        updateFavouriteButton();

        toggleFavouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFavourite){
                    markMovieAsFavourite();
                } else {
                    unmarkMovieAsFavourite();
                }

            }
        });
    }

    private void updateFavouriteButton(){
        if(isFavourite){
            toggleFavouriteButton.setContentDescription(getString(R.string.content_description_unmark_as_favourite));
            toggleFavouriteButton.setBackgroundResource(R.drawable.ic_unfav);
        } else {
            toggleFavouriteButton.setContentDescription(getString(R.string.content_description_mark_as_favourite));
            toggleFavouriteButton.setBackgroundResource(R.drawable.ic_fav);
        }
    }

    private void onFavouriteStatusChanged(){
        updateFavouriteButton();

        int messageId = isFavourite ? R.string.toast_movie_added_to_favourites : R.string.toast_movie_removed_from_favourites;
        Toast.makeText(this, getString(messageId, movie.getTitle()), Toast.LENGTH_LONG).show();
    }

    private void markMovieAsFavourite(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ContentValues movieValues = movie.getAsContentValues();

                getContentResolver().insert(MovieContract.FavouriteMovieEntry.CONTENT_URI, movieValues);

                FileUtils.savePoster(MovieDetailsActivity.this, movie.getPosterPath());

                isFavourite = true;

                MovieDetailsActivity.this.runOnUiThread(onFavouriteStatusChangedRunnable);
            }
        };

        AsyncTask.execute(runnable);
    }

    private void unmarkMovieAsFavourite(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Uri uri = MovieContract.FavouriteMovieEntry.CONTENT_URI.buildUpon().appendPath(movie.getId().toString()).build();
                getContentResolver().delete(uri, null, null);

                FileUtils.deletePoster(MovieDetailsActivity.this, movie.getPosterPath());

                isFavourite = false;

                MovieDetailsActivity.this.runOnUiThread(onFavouriteStatusChangedRunnable);
            }
        };

        AsyncTask.execute(runnable);
    }

    private Runnable onFavouriteStatusChangedRunnable = new Runnable() {
        @Override
        public void run() {
            onFavouriteStatusChanged();
        }
    };

    private class NetworkConnectivityChangeListener extends NetworkConnectivityChangeReceiver {

        public NetworkConnectivityChangeListener(boolean onlineState) {
            super(onlineState);
        }

        @Override
        public void onNetworkConnectivityChanged() {
            if(isActivityOnScreen()){
                loadTrailers();
                loadReviews();
            }
        }
    }

}
