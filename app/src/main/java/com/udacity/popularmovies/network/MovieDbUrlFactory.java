package com.udacity.popularmovies.network;

import android.content.Context;

import com.udacity.popularmovies.R;

/**
 * Created by Norbert Boros on 2018.02.22..
 */

public class MovieDbUrlFactory {

    private static final String POPULAR_MOVIES_URL = "https://api.themoviedb.org/3/movie/popular?api_key=%s";

    private static final String TOP_RATED_MOVIES_URL = "https://api.themoviedb.org/3/movie/top_rated?api_key=%s";

    private static final String MOVIE_URL = "https://api.themoviedb.org/3/movie/%d?api_key=%s";

    private static final String POSTER_URL = "http://image.tmdb.org/t/p/w185/%s";

    private static final String MOVIE_TRAILERS = "https://api.themoviedb.org/3/movie/%d/videos?api_key=%s";

    private static final String MOVIE_REVIEWS = "https://api.themoviedb.org/3/movie/%d/reviews?api_key=%s";

    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=%s";

    public static String popularMovies(Context context){
        String apiKey = context.getString(R.string.moviedb_api_key);

        return String.format(POPULAR_MOVIES_URL, apiKey);
    }

    public static String topRatedMovies(Context context){
        String apiKey = context.getString(R.string.moviedb_api_key);

        return String.format(TOP_RATED_MOVIES_URL, apiKey);
    }

    public static String posterImage(String posterPath){
        return String.format(POSTER_URL, posterPath);
    }

    public static String movie(Context context, Long movieId){
        String apiKey = context.getString(R.string.moviedb_api_key);

        return String.format(MOVIE_URL, movieId, apiKey);
    }

    public static String movieTrailers(Context context, Long movieId){
        String apiKey = context.getString(R.string.moviedb_api_key);

        return String.format(MOVIE_TRAILERS, movieId, apiKey);
    }

    public static String movieReviews(Context context, Long movieId){
        String apiKey = context.getString(R.string.moviedb_api_key);

        return String.format(MOVIE_REVIEWS, movieId, apiKey);
    }

    public static String youtubeTrailerUrl(String key){
        return String.format(YOUTUBE_URL, key);
    }
}
