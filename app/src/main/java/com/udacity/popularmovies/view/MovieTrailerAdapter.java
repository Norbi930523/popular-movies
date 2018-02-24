package com.udacity.popularmovies.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.MovieTrailer;

/**
 * Created by Norbert Boros on 2018.02.24..
 */

public class MovieTrailerAdapter extends ArrayAdapter<MovieTrailer> {

    public MovieTrailerAdapter(@NonNull Context context) {
        super(context, R.layout.movie_trailer_item);
    }

    /**
     * From https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
     *
     */

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.movie_trailer_item, parent, false);
        }

        TextView trailerName = convertView.findViewById(R.id.trailerName);

        MovieTrailer trailer = getItem(position);
        if(trailer != null){
            trailerName.setText(trailer.getName());
            convertView.setTag(trailer.getKey());
        }

        return convertView;
    }

}
