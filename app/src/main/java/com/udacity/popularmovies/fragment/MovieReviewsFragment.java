package com.udacity.popularmovies.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.MovieReview;
import com.udacity.popularmovies.network.MovieReviewListLoader;
import com.udacity.popularmovies.network.NetworkConnectionContext;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by Norbert Boros on 2018.03.01..
 * Based on https://github.com/codepath/android_guides/wiki/Creating-and-Using-Fragments
 */

public class MovieReviewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<MovieReview>> {

    public static final String MOVIE_ID_PARAM = "movieId";

    private static final int REVIEW_CONTENT_SHORT_CHARS = 250;

    private static final int MOVIE_REVIEW_LOADER_ID = 201;

    private Long movieId;

    private TextView reviewLoadingInfoText;

    private LinearLayout movieReviewsList;

    public static MovieReviewsFragment newInstance(Long movieId) {
        MovieReviewsFragment mrf = new MovieReviewsFragment();

        Bundle args = new Bundle();
        args.putLong(MOVIE_ID_PARAM, movieId);

        mrf.setArguments(args);

        return mrf;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        movieId = args.getLong(MOVIE_ID_PARAM);

        View layout = inflater.inflate(R.layout.fragment_movie_reviews, container, false);

        reviewLoadingInfoText = layout.findViewById(R.id.reviewLoadingInfoText);
        movieReviewsList = layout.findViewById(R.id.movieReviewsList);

        loadReviews(true);

        return layout;
    }

    public void loadReviews(boolean initLoader) {
        if(NetworkConnectionContext.getInstance().isOffline()){
            reviewLoadingInfoText.setText(R.string.offline_no_reviews);
            reviewLoadingInfoText.setVisibility(View.VISIBLE);
            movieReviewsList.setVisibility(View.GONE);
            return;
        }

        reviewLoadingInfoText.setText(R.string.loading_reviews);
        reviewLoadingInfoText.setVisibility(View.VISIBLE);
        movieReviewsList.setVisibility(View.GONE);

        Bundle args = new Bundle();
        args.putLong(MovieReviewListLoader.MOVIE_ID_PARAM, movieId);

        if(initLoader){
            getActivity().getSupportLoaderManager().initLoader(MOVIE_REVIEW_LOADER_ID, args, this).forceLoad();
        } else {
            getActivity().getSupportLoaderManager().restartLoader(MOVIE_REVIEW_LOADER_ID, args, this).forceLoad();
        }
    }

    @Override
    public Loader<List<MovieReview>> onCreateLoader(int id, Bundle args) {
        return new MovieReviewListLoader(getContext(), args);
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

    private View createMovieReviewItem(MovieReview review, boolean isLastItem){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

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
}
