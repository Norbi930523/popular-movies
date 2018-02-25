package com.udacity.popularmovies.common;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.udacity.popularmovies.network.MovieDbUrlFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Norbert Boros on 2018.02.25..
 */

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    private static final String POSTERS_DIRECTORY = "posters";

    public static File readPoster(Context context, String posterPath){
        try {
            return getPosterFile(context, posterPath);
        } catch(Exception e){
            /* Should not happen, just making sure the app does not crash */
            Log.e(TAG, "An error occurred while reading poster", e);
            return null;
        }
    }

    public static void savePoster(final Context context, final String posterPath){
        Runnable savePosterTask = new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] posterImage = downloadPoster(posterPath);
                    File posterFile = getPosterFile(context, posterPath);

                    if(posterFile != null && posterImage != null){
                        FileOutputStream fos = new FileOutputStream(posterFile);
                        fos.write(posterImage);
                        fos.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "An error occurred while saving poster", e);
                }
            }
        };

        AsyncTask.execute(savePosterTask);
    }

    private static File getPostersDirectory(Context context){
        File postersDir = new File(context.getFilesDir(), POSTERS_DIRECTORY);

        boolean success = false;

        if(!postersDir.exists()){
            success = postersDir.mkdir();
        }

        return success ? postersDir : null;
    }

    private static File getPosterFile(Context context, String posterPath){
        String posterFilename = posterPath.replace("/", "");

        File postersDir = getPostersDirectory(context);

        if(postersDir != null){
            return new File(postersDir.getAbsolutePath(), posterFilename);
        }

        return null;
    }

    private static byte[] downloadPoster(String posterPath){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(MovieDbUrlFactory.posterImage(posterPath))
                .build();

        try{
            Response response = client.newCall(request).execute();

            return response.body().bytes();
        } catch(IOException e){
            Log.e(TAG, "An error occurred while downloading poster", e);
            return null;
        }
    }

}
