package com.udacity.popularmovies.database.movie;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.udacity.popularmovies.database.BaseContentProvider;
import com.udacity.popularmovies.database.DatabaseOpenHelper;

/**
 * Created by Norbert Boros on 2018.02.25..
 */

public class MovieContentProvider extends BaseContentProvider {

    private static final String MIME_TYPE_DIR = "vnd.android.cursor.dir/vnd.%s.%s";
    private static final String MIME_TYPE_ITEM = "vnd.android.cursor.item/vnd.%s.%s";

    private static final int FAVOURITE_MOVIES = 100;
    private static final int FAVOURITE_MOVIE_WITH_ID = 101;

    private static final UriMatcher URI_MATCHER = buildUriMatcher();

    private DatabaseOpenHelper dbHelper;

    private static UriMatcher buildUriMatcher(){
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(AUTHORITY, MovieContract.FavouriteMovieEntry.PATH_NAME, FAVOURITE_MOVIES);
        matcher.addURI(AUTHORITY, MovieContract.FavouriteMovieEntry.PATH_NAME + "/#", FAVOURITE_MOVIE_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseOpenHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor = null;

        switch (URI_MATCHER.match(uri)) {
            case FAVOURITE_MOVIES:
                cursor = database.query(
                        MovieContract.FavouriteMovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;
            case FAVOURITE_MOVIE_WITH_ID:
                String movieId = uri.getPathSegments().get(1);

                cursor = database.query(
                        MovieContract.FavouriteMovieEntry.TABLE_NAME,
                        projection,
                        MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{ movieId },
                        null,
                        null,
                        null);

                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation 'query' for URI: " + uri.toString());
        }

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        Uri returnUri;

        switch (URI_MATCHER.match(uri)) {
            case FAVOURITE_MOVIES:
                long id = database.insert(MovieContract.FavouriteMovieEntry.TABLE_NAME, null, contentValues);

                if(id > 0){
                    returnUri = ContentUris.withAppendedId(MovieContract.FavouriteMovieEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert favourite movie.");
                }

                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation 'insert' for URI: " + uri.toString());
        }

        getContext().getContentResolver().notifyChange(returnUri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        Uri affectedUri = null;
        int affectedRows = 0;

        switch (URI_MATCHER.match(uri)) {
            case FAVOURITE_MOVIES:
                affectedRows = database.delete(
                        MovieContract.FavouriteMovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                affectedUri = MovieContract.FavouriteMovieEntry.CONTENT_URI;
                break;
            case FAVOURITE_MOVIE_WITH_ID:
                String movieId = uri.getPathSegments().get(1);

                affectedRows = database.delete(
                        MovieContract.FavouriteMovieEntry.TABLE_NAME,
                        MovieContract.FavouriteMovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{ movieId });

                affectedUri = MovieContract.FavouriteMovieEntry.CONTENT_URI;
                break;
        }

        if(affectedRows > 0 && affectedUri != null){
            getContext().getContentResolver().notifyChange(affectedUri, null);
        }

        return affectedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)){
            case FAVOURITE_MOVIES:
                return String.format(MIME_TYPE_DIR, AUTHORITY, MovieContract.FavouriteMovieEntry.PATH_NAME);
            case FAVOURITE_MOVIE_WITH_ID:
                return String.format(MIME_TYPE_ITEM, AUTHORITY, MovieContract.FavouriteMovieEntry.PATH_NAME);
        }

        return null;
    }
}
