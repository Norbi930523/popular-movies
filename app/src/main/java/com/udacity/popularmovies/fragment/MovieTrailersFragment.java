package com.udacity.popularmovies.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.MovieTrailer;
import com.udacity.popularmovies.network.MovieDbUrlFactory;
import com.udacity.popularmovies.network.MovieTrailerListLoader;
import com.udacity.popularmovies.network.NetworkConnectionContext;

import java.util.List;

/**
 * Created by Norbert Boros on 2018.03.01..
 * Based on https://github.com/codepath/android_guides/wiki/Creating-and-Using-Fragments
 */

public class MovieTrailersFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<MovieTrailer>> {

    public static final String MOVIE_ID_PARAM = "movieId";

    private static final int MOVIE_TRAILER_LOADER_ID = 200;

    private Long movieId;

    private TextView trailerLoadingInfoText;

    private LinearLayout movieTrailersList;

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
        Bundle args = getArguments();
        movieId = args.getLong(MOVIE_ID_PARAM);

        View layout = inflater.inflate(R.layout.fragment_movie_trailers, container, false);

        trailerLoadingInfoText = layout.findViewById(R.id.trailerLoadingInfoText);
        movieTrailersList = layout.findViewById(R.id.movieTrailersList);

        loadTrailers(true);

        return layout;
    }

    public void loadTrailers(boolean initLoader) {
        if(NetworkConnectionContext.getInstance().isOffline()){
            trailerLoadingInfoText.setText(R.string.offline_no_trailers);
            trailerLoadingInfoText.setVisibility(View.VISIBLE);
            movieTrailersList.setVisibility(View.GONE);
            return;
        }

        trailerLoadingInfoText.setText(R.string.loading_trailers);
        trailerLoadingInfoText.setVisibility(View.VISIBLE);
        movieTrailersList.setVisibility(View.GONE);

        Bundle args = new Bundle();
        args.putLong(MovieTrailerListLoader.MOVIE_ID_PARAM, movieId);

        if(initLoader){
            getActivity().getSupportLoaderManager().initLoader(MOVIE_TRAILER_LOADER_ID, args, this).forceLoad();
        } else {
            getActivity().getSupportLoaderManager().restartLoader(MOVIE_TRAILER_LOADER_ID, args, this).forceLoad();
        }
    }

    @Override
    public Loader<List<MovieTrailer>> onCreateLoader(int id, Bundle args) {
        return new MovieTrailerListLoader(getContext(), args);
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

    private View createMovieTrailerItem(MovieTrailer trailer, boolean isLastItem){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

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

            if(watchTrailerIntent.resolveActivity(getActivity().getPackageManager()) != null){
                startActivity(watchTrailerIntent);
            }
        }
    };
}
