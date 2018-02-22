package com.udacity.popularmovies.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.network.MovieDbUrlFactory;

import java.util.List;

/**
 * Created by Norbert Boros on 2018.02.22..
 */

public class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {

    private Context context;

    private List<Movie> movies;

    public MovieGridAdapter(List<Movie> movies, Context context){
        super();
        this.movies = movies;
        this.context = context;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ImageView poster = (ImageView) layoutInflater.inflate(R.layout.movie_view_holder, parent, false);

        return new MovieViewHolder(poster);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);

        Picasso.with(context)
                .load(MovieDbUrlFactory.posterImage(movie.getPosterPath()))
                .fit()
                .centerCrop()
                .into(holder.poster);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void updateMovies(List<Movie> movies){
        this.movies = movies;
        notifyDataSetChanged();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {

        private ImageView poster;

        public MovieViewHolder(ImageView itemView) {
            super(itemView);
            this.poster = itemView;
        }
    }

}
