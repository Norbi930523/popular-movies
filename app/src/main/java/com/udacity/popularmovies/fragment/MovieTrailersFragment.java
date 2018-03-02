package com.udacity.popularmovies.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.MovieTrailer;
import com.udacity.popularmovies.network.MovieDbUrlFactory;
import com.udacity.popularmovies.network.MovieTrailerListLoader;

import java.util.List;

/**
 * Created by Norbert Boros on 2018.03.01..
 */

public class MovieTrailersFragment extends MovieDetailListFragment<MovieTrailer> {

    private static final int MOVIE_TRAILER_LOADER_ID = 200;

    /* From https://github.com/codepath/android_guides/wiki/Creating-and-Using-Fragments */
    public static MovieTrailersFragment newInstance(Long movieId){
        MovieTrailersFragment mtf = new MovieTrailersFragment();

        Bundle args = new Bundle();
        args.putLong(MOVIE_ID_PARAM, movieId);

        mtf.setArguments(args);

        return mtf;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_movie_trailers);
    }

    @Override
    public Loader<List<MovieTrailer>> onCreateLoader(int id, Bundle args) {
        return new MovieTrailerListLoader(getContext(), args);
    }

    private View.OnClickListener movieTrailerItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Uri youtubeTrailerUri = Uri.parse(MovieDbUrlFactory.youtubeTrailerUrl(view.getTag().toString()));

            Intent watchTrailerIntent = new Intent(Intent.ACTION_VIEW, youtubeTrailerUri);

            if(watchTrailerIntent.resolveActivity(getActivity().getPackageManager()) != null){
                startActivity(watchTrailerIntent);
            }
        }
    };

    @Override
    protected View createListItem(MovieTrailer item, boolean isLastItem) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        View movieTrailerItem = layoutInflater.inflate(R.layout.movie_trailer_item, itemList, false);
        movieTrailerItem.setTag(item.getKey());
        movieTrailerItem.setOnClickListener(movieTrailerItemClickListener);

        TextView trailerName = movieTrailerItem.findViewById(R.id.trailerName);
        trailerName.setText(item.getName());

        if(isLastItem){
            /* Hide the divider below the last item of the list */
            movieTrailerItem.findViewById(R.id.listItemDivider).setVisibility(View.GONE);
        }

        return movieTrailerItem;
    }

    @Override
    protected int getLoadingStringResId() {
        return R.string.loading_trailers;
    }

    @Override
    protected int getOfflineModeStringResId() {
        return R.string.offline_no_trailers;
    }

    @Override
    protected int getEmptyListStringResId() {
        return R.string.no_trailers;
    }

    @Override
    protected int getLoaderId() {
        return MOVIE_TRAILER_LOADER_ID;
    }
}
