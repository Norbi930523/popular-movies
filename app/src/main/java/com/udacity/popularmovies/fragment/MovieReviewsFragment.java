package com.udacity.popularmovies.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.MovieReview;
import com.udacity.popularmovies.network.MovieReviewListLoader;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by Norbert Boros on 2018.03.01..
 */

public class MovieReviewsFragment extends MovieDetailListFragment<MovieReview> {

    private static final int REVIEW_CONTENT_SHORT_CHARS = 250;

    private static final int MOVIE_REVIEW_LOADER_ID = 201;

    /* From https://github.com/codepath/android_guides/wiki/Creating-and-Using-Fragments */
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
        return createView(inflater, container, R.layout.fragment_movie_reviews);
    }

    @Override
    public Loader<List<MovieReview>> onCreateLoader(int id, Bundle args) {
        return new MovieReviewListLoader(getContext(), args);
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

    @Override
    protected View createListItem(MovieReview item, boolean isLastItem) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        View movieReviewItem = layoutInflater.inflate(R.layout.movie_review_item, itemList, false);

        movieReviewItem.setTag(R.id.review_item_full_content_key, item.getContent());
        movieReviewItem.setTag(R.id.review_item_show_full_content_key, false);

        movieReviewItem.setOnClickListener(movieReviewItemClickListener);

        TextView reviewAuthor = movieReviewItem.findViewById(R.id.reviewAuthor);
        reviewAuthor.setText(item.getAuthor());

        TextView reviewContent = movieReviewItem.findViewById(R.id.reviewContent);
        reviewContent.setText(StringUtils.abbreviate(item.getContent(), REVIEW_CONTENT_SHORT_CHARS));

        if(isLastItem){
            /* Hide the divider below the last item of the list */
            movieReviewItem.findViewById(R.id.listItemDivider).setVisibility(View.GONE);
        }

        return movieReviewItem;
    }

    @Override
    protected int getLoadingStringResId() {
        return R.string.loading_reviews;
    }

    @Override
    protected int getOfflineModeStringResId() {
        return R.string.offline_no_reviews;
    }

    @Override
    protected int getEmptyListStringResId() {
        return R.string.no_reviews;
    }

    @Override
    protected int getLoaderId() {
        return MOVIE_REVIEW_LOADER_ID;
    }
}
