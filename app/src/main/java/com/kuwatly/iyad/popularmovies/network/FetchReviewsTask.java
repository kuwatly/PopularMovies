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

public class FetchReviewsTask extends AsyncTask<Long, Void, Void> {
    Context mContext;
    // URL parameters
    private static final String MOVIE_BASE_URL =
            "http://api.themoviedb.org/3/movie/";
    private static final String REVIEWS_METHOD = "reviews";
    private static final String APPID_PARAM = "api_key";

    // JSON Objects Names
    final String TMDB_RUNTIME = "runtime";

    final String TMDB_LIST = "results";
    final String TMDB_REVIEW_ID = "id";
    final String TMDB_REVIEW_AUTHOR = "author";
    final String TMDB_REVIEW_CONTENT = "content";
    final String TMDB_REVIEW_URL = "url";

    private String mMovieApiId; // movie ID in themoviedb
    private long movieSQLId;
    private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

    public FetchReviewsTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Long... params) {
        if (params.length == 0) {
            return null;
        }
        movieSQLId = params[0];


        // Will contain the raw JSON response as a string.
        String movieReviewsJsonStr = null;

        // First, check if the location with this city name exists in the db
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,// The content URI of the words table
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID, MovieContract.MovieEntry.COLUMN_COMPLETE_RECORD_FLAG},// The columns to return for each row
                "_ID=?",// Selection criteria
                new String[] {String.valueOf(movieSQLId)},// Selection criteria
                null); // The sort order for the returned rows

        boolean compelteRecordFlag = false;
        if (movieCursor.moveToFirst()) {
            //int movieIdIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            mMovieApiId = String.valueOf(movieCursor.getLong(0));
            compelteRecordFlag = movieCursor.getInt(1) > 0;
        }

        if (!compelteRecordFlag) {
            Uri builtUriMovieReviews = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(mMovieApiId)
                    .appendPath(REVIEWS_METHOD)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            try {
                URL urlMovieReviews = new URL(builtUriMovieReviews.toString());

                Log.i(LOG_TAG, "URL Reviews: " + builtUriMovieReviews.toString());

                movieReviewsJsonStr = processHTTP(urlMovieReviews);
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }


            try {

                getReviewsFromJson(movieReviewsJsonStr);


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

    private void getReviewsFromJson(String movieReviewsJsonStr)
            throws JSONException {
        if (null!=movieReviewsJsonStr) {
            JSONObject reviewJson = new JSONObject(movieReviewsJsonStr);
            JSONArray reviewsArray = reviewJson.getJSONArray(TMDB_LIST);
            Vector<ContentValues> cVVector = new Vector<>(reviewsArray.length());

            for (int i = 0; i < reviewsArray.length(); i++) {

                JSONObject reviewObject = reviewsArray.getJSONObject(i);
                ContentValues reviewValues = new ContentValues();

                reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_ID, reviewObject.getString(TMDB_REVIEW_ID));
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, reviewObject.getString(TMDB_REVIEW_AUTHOR));
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, reviewObject.getString(TMDB_REVIEW_CONTENT));
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_URL, reviewObject.getString(TMDB_REVIEW_URL));
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movieSQLId);
                cVVector.add(reviewValues);
            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                // Delete database contents before inserting
                mContext.getContentResolver().delete(MovieContract.ReviewEntry.CONTENT_URI,
                        MovieContract.ReviewEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{String.valueOf(movieSQLId)});

                inserted = mContext.getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, cvArray);
            }

            //Log.d(LOG_TAG, "FetchMovieTask Complete. " + inserted + " Inserted");
        }
    }
}