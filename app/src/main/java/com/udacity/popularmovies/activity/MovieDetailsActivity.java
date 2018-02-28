package com.udacity.popularmovies.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.common.FileUtils;
import com.udacity.popularmovies.database.movie.MovieContract;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.model.MovieReview;
import com.udacity.popularmovies.model.MovieTrailer;
import com.udacity.popularmovies.network.MovieDbUrlFactory;
import com.udacity.popularmovies.network.MovieReviewListLoader;
import com.udacity.popularmovies.network.MovieTrailerListLoader;
import com.udacity.popularmovies.network.NetworkConnectionContext;
import com.udacity.popularmovies.network.NetworkConnectivityChangeReceiver;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final int REVIEW_CONTENT_SHORT_CHARS = 250;

    private static final int MOVIE_TRAILER_LOADER_ID = 200;

    private static final int MOVIE_REVIEW_LOADER_ID = 201;

    public static final String MOVIE_PARAM = "movie";

    private Movie movie;

    private TextView trailerLoadingInfoText;

    private LinearLayout movieTrailersList;

    private TextView reviewLoadingInfoText;

    private LinearLayout movieReviewsList;

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

        registerReceiver(connectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        isFavourite = isFavouriteMovie();

        populateUI();

        loadTrailers(true);

        loadReviews(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(connectivityChangeReceiver);
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

    private void loadReviews(boolean initLoader) {
        reviewLoadingInfoText = findViewById(R.id.reviewLoadingInfoText);
        movieReviewsList = findViewById(R.id.movieReviewsList);

        if(NetworkConnectionContext.getInstance().isOffline()){
            reviewLoadingInfoText.setText(R.string.offline_no_reviews);
            movieReviewsList.setVisibility(View.GONE);
            return;
        }

        Bundle args = new Bundle();
        args.putLong(MovieReviewListLoader.MOVIE_ID_PARAM, movie.getId());

        if(initLoader){
            getSupportLoaderManager().initLoader(MOVIE_REVIEW_LOADER_ID, args, movieReviewLoaderCallback).forceLoad();
        } else {
            getSupportLoaderManager().restartLoader(MOVIE_REVIEW_LOADER_ID, args, movieReviewLoaderCallback).forceLoad();
        }
    }

    private void loadTrailers(boolean initLoader) {
        trailerLoadingInfoText = findViewById(R.id.trailerLoadingInfoText);
        movieTrailersList = findViewById(R.id.movieTrailersList);

        if(NetworkConnectionContext.getInstance().isOffline()){
            trailerLoadingInfoText.setText(R.string.offline_no_trailers);
            movieTrailersList.setVisibility(View.GONE);
            return;
        }

        Bundle args = new Bundle();
        args.putLong(MovieTrailerListLoader.MOVIE_ID_PARAM, movie.getId());

        if(initLoader){
            getSupportLoaderManager().initLoader(MOVIE_TRAILER_LOADER_ID, args, movieTrailerLoaderCallback).forceLoad();
        } else {
            getSupportLoaderManager().restartLoader(MOVIE_TRAILER_LOADER_ID, args, movieTrailerLoaderCallback).forceLoad();
        }
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

    private void markMovieAsFavourite(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ContentValues movieValues = movie.getAsContentValues();

                getContentResolver().insert(MovieContract.FavouriteMovieEntry.CONTENT_URI, movieValues);

                FileUtils.savePoster(MovieDetailsActivity.this, movie.getPosterPath());

                isFavourite = true;

                MovieDetailsActivity.this.runOnUiThread(updateFavouriteButtonRunnable);
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

                MovieDetailsActivity.this.runOnUiThread(updateFavouriteButtonRunnable);
            }
        };

        AsyncTask.execute(runnable);
    }

    /* Movie trailers loader callback */
    private LoaderManager.LoaderCallbacks<List<MovieTrailer>> movieTrailerLoaderCallback = new LoaderManager.LoaderCallbacks<List<MovieTrailer>>() {
        @Override
        public Loader<List<MovieTrailer>> onCreateLoader(int id, Bundle args) {
            return new MovieTrailerListLoader(MovieDetailsActivity.this, args);
        }

        @Override
        public void onLoadFinished(Loader<List<MovieTrailer>> loader, List<MovieTrailer> data) {
            movieTrailersList.removeAllViewsInLayout();

            if(data == null || data.isEmpty()){
                trailerLoadingInfoText.setText(R.string.no_trailers);
                return;
            }

            trailerLoadingInfoText.setVisibility(View.GONE);
            movieTrailersList.setVisibility(View.VISIBLE);

            for(int i = 0; i < data.size(); i++){
                movieTrailersList.addView(createMovieTrailerItem(data.get(i), i == data.size() - 1));
            }
        }

        @Override
        public void onLoaderReset(Loader<List<MovieTrailer>> loader) {

        }

    };

    private View createMovieTrailerItem(MovieTrailer trailer, boolean isLastItem){
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        View movieTrailerItem = layoutInflater.inflate(R.layout.movie_trailer_item, movieTrailersList, false);
        movieTrailerItem.setTag(trailer.getKey());
        movieTrailerItem.setOnClickListener(movieTrailerItemClickListener);

        TextView trailerName = movieTrailerItem.findViewById(R.id.trailerName);
        trailerName.setText(trailer.getName());

        if(isLastItem){
            /* Hide the divider below the last item of the list */
            movieTrailerItem.findViewById(R.id.listItemDivider).setVisibility(View.GONE);
        }

        return movieTrailerItem;
    }

    private View.OnClickListener movieTrailerItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Uri youtubeTrailerUri = Uri.parse(MovieDbUrlFactory.youtubeTrailerUrl(view.getTag().toString()));

            Intent watchTrailerIntent = new Intent(Intent.ACTION_VIEW, youtubeTrailerUri);

            if(watchTrailerIntent.resolveActivity(getPackageManager()) != null){
                startActivity(watchTrailerIntent);
            }
        }
    };

    /* Movie reviews loader callback */
    private LoaderManager.LoaderCallbacks<List<MovieReview>> movieReviewLoaderCallback = new LoaderManager.LoaderCallbacks<List<MovieReview>>() {
        @Override
        public Loader<List<MovieReview>> onCreateLoader(int id, Bundle args) {
            return new MovieReviewListLoader(MovieDetailsActivity.this, args);
        }

        @Override
        public void onLoadFinished(Loader<List<MovieReview>> loader, List<MovieReview> data) {
            movieReviewsList.removeAllViewsInLayout();

            if(data == null || data.isEmpty()){
                reviewLoadingInfoText.setText(R.string.no_reviews);
                return;
            }

            reviewLoadingInfoText.setVisibility(View.GONE);
            movieReviewsList.setVisibility(View.VISIBLE);

            for(int i = 0; i < data.size(); i++){
                movieReviewsList.addView(createMovieReviewItem(data.get(i), i == data.size() - 1));
            }
        }

        @Override
        public void onLoaderReset(Loader<List<MovieReview>> loader) {

        }
    };

    private View createMovieReviewItem(MovieReview review, boolean isLastItem){
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        View movieReviewItem = layoutInflater.inflate(R.layout.movie_review_item, movieReviewsList, false);

        movieReviewItem.setTag(R.id.review_item_full_content_key, review.getContent());
        movieReviewItem.setTag(R.id.review_item_show_full_content_key, false);

        movieReviewItem.setOnClickListener(movieReviewItemClickListener);

        TextView reviewAuthor = movieReviewItem.findViewById(R.id.reviewAuthor);
        reviewAuthor.setText(review.getAuthor());

        TextView reviewContent = movieReviewItem.findViewById(R.id.reviewContent);
        reviewContent.setText(StringUtils.abbreviate(review.getContent(), REVIEW_CONTENT_SHORT_CHARS));

        if(isLastItem){
            /* Hide the divider below the last item of the list */
            movieReviewItem.findViewById(R.id.listItemDivider).setVisibility(View.GONE);
        }

        return movieReviewItem;
    }

    private View.OnClickListener movieReviewItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TextView reviewContent = view.findViewById(R.id.reviewContent);

            String content = view.getTag(R.id.review_item_full_content_key).toString();
            boolean showFullContent = (boolean) view.getTag(R.id.review_item_show_full_content_key);

            if(!showFullContent){
                view.setTag(R.id.review_item_show_full_content_key, true);
                reviewContent.setText(content);
            } else {
                view.setTag(R.id.review_item_show_full_content_key, false);
                reviewContent.setText(StringUtils.abbreviate(content, REVIEW_CONTENT_SHORT_CHARS));
            }
        }
    };

    private Runnable updateFavouriteButtonRunnable = new Runnable() {
        @Override
        public void run() {
            updateFavouriteButton();
        }
    };

    private NetworkConnectivityChangeReceiver connectivityChangeReceiver = new NetworkConnectivityChangeReceiver() {
        @Override
        public void onNetworkConnectivityChanged() {
            loadTrailers(false);
            loadReviews(false);
        }
    };

}
