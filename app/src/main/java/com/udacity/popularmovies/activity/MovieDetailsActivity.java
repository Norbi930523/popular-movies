package com.udacity.popularmovies.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.network.MovieDbUrlFactory;

import java.text.SimpleDateFormat;

public class MovieDetailsActivity extends AppCompatActivity {

    public static final String MOVIE_PARAM = "movie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        Movie movie = (Movie) intent.getSerializableExtra(MOVIE_PARAM);

        if(movie == null){
            finish();
            Toast.makeText(this, R.string.error_movie_required, Toast.LENGTH_LONG).show();
            return;
        }

        populateUI(movie);
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
}
