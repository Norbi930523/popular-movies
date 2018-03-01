package com.udacity.popularmovies.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.network.MovieTrailerListLoader;
import com.udacity.popularmovies.network.NetworkConnectionContext;

import java.util.List;

/**
 * Created by Norbert Boros on 2018.03.01..
 */

public abstract class MovieDetailListFragment<T> extends Fragment implements LoaderManager.LoaderCallbacks<List<T>> {

    public static final String MOVIE_ID_PARAM = "movieId";

    private Long movieId;

    protected TextView loadingInfoText;

    protected LinearLayout itemList;

    protected View createView(LayoutInflater inflater, ViewGroup container, int layoutResId){
        Bundle args = getArguments();
        movieId = args.getLong(MOVIE_ID_PARAM);

        View layout = inflater.inflate(layoutResId, container, false);

        loadingInfoText = layout.findViewById(R.id.loadingInfoText);
        itemList = layout.findViewById(R.id.itemList);

        loadItems(true);

        return layout;
    }

    public void reloadItems(){
        loadItems(false);
    }

    private void loadItems(boolean initLoader){
        if(NetworkConnectionContext.getInstance().isOffline()){
            loadingInfoText.setText(getOfflineModeStringResId());
            loadingInfoText.setVisibility(View.VISIBLE);
            itemList.setVisibility(View.GONE);
            return;
        }

        loadingInfoText.setText(getLoadingStringResId());
        loadingInfoText.setVisibility(View.VISIBLE);
        itemList.setVisibility(View.GONE);

        Bundle args = new Bundle();
        args.putLong(MovieTrailerListLoader.MOVIE_ID_PARAM, movieId);

        if(initLoader){
            getActivity().getSupportLoaderManager().initLoader(getLoaderId(), args, this).forceLoad();
        } else {
            getActivity().getSupportLoaderManager().restartLoader(getLoaderId(), args, this).forceLoad();
        }
    }

    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
        itemList.removeAllViewsInLayout();

        if(data == null || data.isEmpty()){
            loadingInfoText.setText(getEmptyListStringResId());
            return;
        }

        loadingInfoText.setVisibility(View.GONE);
        itemList.setVisibility(View.VISIBLE);

        for(int i = 0; i < data.size(); i++){
            itemList.addView(createListItem(data.get(i), i == data.size() - 1));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<T>> loader) {

    }

    protected abstract View createListItem(T item, boolean isLastItem);

    protected abstract int getLoadingStringResId();

    protected abstract int getOfflineModeStringResId();

    protected abstract int getEmptyListStringResId();

    protected abstract int getLoaderId();

}
