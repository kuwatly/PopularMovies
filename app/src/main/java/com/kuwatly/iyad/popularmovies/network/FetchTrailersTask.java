/*
 * Copyright (C) 2016 Iyad Kuwatly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kuwatly.iyad.popularmovies.network;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.kuwatly.iyad.popularmovies.BuildConfig;
import com.kuwatly.iyad.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

public class FetchTrailersTask extends AsyncTask<Long, Void, Void> {
    Context mContext;
    // URL parameters
    private static final String MOVIE_BASE_URL =
            "http://api.themoviedb.org/3/movie/";
    private static final String TRAILERS_METHOD = "videos";
    private static final String APPID_PARAM = "api_key";

    final String TMDB_LIST = "results";
    final String TMDB_TRAILER_ID = "id";
    final String TMDB_TRAILER_NAME = "name";
    final String TMDB_TRAILER_KEY = "key";
    final String TMDB_TRAILER_SITE = "site";
    final String TMDB_TRAILER_SIZE = "size";
    final String TMDB_TRAILER_TYPE = "type";


    private String mMovieApiId; // movie ID in themoviedb
    private long movieSQLId;
    private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

    public FetchTrailersTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Long... params) {
        if (params.length == 0) {
            return null;
        }
        movieSQLId = params[0];


        String movieTrailersJsonStr = null;

        // First, check if the location with this city name exists in the db
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,// The content URI of the words table
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID, MovieContract.MovieEntry.COLUMN_COMPLETE_RECORD_FLAG},// The columns to return for each row
                "_ID=?",// Selection criteria
                new String[]{String.valueOf(movieSQLId)},// Selection criteria
                null); // The sort order for the returned rows

        boolean compelteRecordFlag = false;
        if (movieCursor.moveToFirst()) {
            mMovieApiId = String.valueOf(movieCursor.getLong(0));
            compelteRecordFlag = movieCursor.getInt(1) > 0;
        }

        if (!compelteRecordFlag) {
            Uri builtUriMovieTrailers = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(mMovieApiId)
                    .appendPath(TRAILERS_METHOD)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();


            try {
                URL urlMovieTrailers = new URL(builtUriMovieTrailers.toString());

                Log.i(LOG_TAG, "URL Trailers: " + builtUriMovieTrailers.toString());

                movieTrailersJsonStr = processHTTP(urlMovieTrailers);
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }


            try {

                getTrailersFromJson(movieTrailersJsonStr);


            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
        return null;
    }

    private String processHTTP(URL url) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            // Create the request to TheMovieDB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // For better debuging experience, added new line.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            return buffer.toString();

        } catch (IOException e) {
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }

        }
    }

    private void getTrailersFromJson(String movieTrailersJsonStr)
            throws JSONException {
        if (null != movieTrailersJsonStr) {
            JSONObject movieJson = new JSONObject(movieTrailersJsonStr);
            JSONArray trailersArray = movieJson.getJSONArray(TMDB_LIST);
            Vector<ContentValues> cVVector = new Vector<>(trailersArray.length());

            for (int i = 0; i < trailersArray.length(); i++) {

                JSONObject movieObject = trailersArray.getJSONObject(i);
                ContentValues trailerValues = new ContentValues();

                trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_ID, movieObject.getString(TMDB_TRAILER_ID));
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_NAME, movieObject.getString(TMDB_TRAILER_NAME));
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_KEY, movieObject.getString(TMDB_TRAILER_KEY));
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_SITE, movieObject.getString(TMDB_TRAILER_SITE));
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_TYPE, movieObject.getString(TMDB_TRAILER_TYPE));
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, movieSQLId);
                cVVector.add(trailerValues);
            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                // Delete database contents before inserting
                mContext.getContentResolver().delete(MovieContract.TrailerEntry.CONTENT_URI,
                        MovieContract.TrailerEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{String.valueOf(movieSQLId)});

                inserted = mContext.getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchMovieTask Complete. " + inserted + " Inserted");

        }
    }
}