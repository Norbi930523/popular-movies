package com.udacity.popularmovies.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.model.MovieTrailer;
import com.udacity.popularmovies.network.MovieDbUrlFactory;
import com.udacity.popularmovies.network.MovieTrailerListLoader;
import com.udacity.popularmovies.view.MovieTrailerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<MovieTrailer>> {

    private static final int MOVIE_TRAILER_LOADER_ID = 200;

    public static final String MOVIE_PARAM = "movie";

    private TextView trailerLoadingInfoText;

    private ListView movieTrailersList;

    private ArrayAdapter<MovieTrailer> trailerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        Movie movie = intent.getParcelableExtra(MOVIE_PARAM);

        if(movie == null){
            finish();
            Toast.makeText(this, R.string.error_movie_required, Toast.LENGTH_LONG).show();
            return;
        }

        populateUI(movie);

        loadTrailers(movie.getId());
    }

    private void loadTrailers(Long movieId) {
        trailerLoadingInfoText = findViewById(R.id.trailerLoadingInfoText);

        trailerAdapter = new MovieTrailerAdapter(this);

        movieTrailersList = findViewById(R.id.movieTrailersList);
        movieTrailersList.setAdapter(trailerAdapter);
        movieTrailersList.setOnItemClickListener(movieTrailerClickListener);
        movieTrailersList.setFocusable(false); // prevents ScrollView from jumping to ListView when data is loaded

        Bundle args = new Bundle();
        args.putLong(MovieTrailerListLoader.MOVIE_ID_PARAM, movieId);

        getSupportLoaderManager().restartLoader(MOVIE_TRAILER_LOADER_ID, args, this).forceLoad();
    }

    private void populateUI(Movie movie) {
        /* Poster image */
        ImageView posterImage = findViewById(R.id.posterImage);
        posterImage.setContentDescription(movie.getTitle());

        String posterImageUrl = MovieDbUrlFactory.posterImage(movie.getPosterPath());

        Picasso.with(this)
                .load(posterImageUrl)
                .placeholder(R.drawable.picasso_placeholder_portrait)
                .error(R.drawable.picasso_error_portrait)
                .into(posterImage);

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
    }

    @Override
    public Loader<List<MovieTrailer>> onCreateLoader(int id, Bundle args) {
        return new MovieTrailerListLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<List<MovieTrailer>> loader, List<MovieTrailer> data) {
        if(data == null || data.isEmpty()){
            trailerLoadingInfoText.setText(R.string.no_trailers);
            return;
        }

        trailerLoadingInfoText.setVisibility(View.GONE);
        movieTrailersList.setVisibility(View.VISIBLE);

        trailerAdapter.clear();
        trailerAdapter.addAll(data);

        setListViewHeightBasedOnItems(movieTrailersList);

        trailerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<MovieTrailer>> loader) {

    }

    private AdapterView.OnItemClickListener movieTrailerClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Uri youtubeTrailerUri = Uri.parse(MovieDbUrlFactory.youtubeTrailerUrl(view.getTag().toString()));

            Intent watchTrailerIntent = new Intent(Intent.ACTION_VIEW, youtubeTrailerUri);

            if(watchTrailerIntent.resolveActivity(getPackageManager()) != null){
                startActivity(watchTrailerIntent);
            }
        }
    };

    /**
     * Sets ListView height dynamically based on the height of the items.
     * From https://stackoverflow.com/a/35955121
     *
     * @param listView to be resized
     */
    private void setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

        }

    }
}
