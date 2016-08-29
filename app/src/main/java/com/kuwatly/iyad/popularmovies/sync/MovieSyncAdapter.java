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

package com.kuwatly.iyad.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.kuwatly.iyad.popularmovies.BuildConfig;
import com.kuwatly.iyad.popularmovies.R;
import com.kuwatly.iyad.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;


public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final String MOVIE_BASE_URL =
            "http://api.themoviedb.org/3/discover/movie?";
    private static final String QUERY_PARAM = "sort_by";
    private String SORT_PARAM; // based on user sort (Top Rated or Most Popular)
    private static final String QUERY_PAGE_PARAM = "page";
    private static final String QUERY_NUMBER_PARAM = "1";
    private static final String APPID_PARAM = "api_key";

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        //Log.d(LOG_TAG, "onPerformSync Called.");
        // Delete database contents before inserting
        getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_FAVORITE_FLAG + "<>1", // don't delete favorite movies
                null);
        getContext().getContentResolver().delete(MovieContract.TrailerEntry.CONTENT_URI,
                null,
                null);
        getContext().getContentResolver().delete(MovieContract.ReviewEntry.CONTENT_URI,
                null,
                null);

        for (int categoryIterator=1;categoryIterator<=2;categoryIterator++) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;
            try {

                if (categoryIterator==1) SORT_PARAM = "popularity.desc";
                else SORT_PARAM = "vote_average.desc";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, SORT_PARAM)
                        .appendQueryParameter(QUERY_PAGE_PARAM, QUERY_NUMBER_PARAM)
                        .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                //Log.i(LOG_TAG, "URL: " + builtUri.toString());
                // Create the request to TheMovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // For better debuging experience, added new line.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }
                moviesJsonStr = buffer.toString();

            } catch (IOException e) {
                return;
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

            try {
                getMoviesDataFromJson(moviesJsonStr,categoryIterator);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
        return;
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }
    private void getMoviesDataFromJson(String movieJsonStr, int category)
            throws JSONException {

        // JSON Objects Names
        final String TMDB_LIST = "results";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_ADULT = "adult";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_GENRE_IDS = "genre_ids";
        final String TMDB_ID = "id";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_ORIGINAL_LANGUAGE = "original_language";
        final String TMDB_TITLE = "title";
        final String TMDB_BACKDROP_PATH = "backdrop_path";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_VOTE_COUNT = "vote_count";
        final String TMDB_VIDEO = "video";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_URL_PREFIX = "http://image.tmdb.org/t/p/w185";
        final int TMDB_MAXPERPAGE = 20;

        try {
            //Log.d(LOG_TAG, "JSON String " + movieJsonStr);
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray moviesArray = movieJson.getJSONArray(TMDB_LIST);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<>(moviesArray.length());

            for (int i = 0; i < moviesArray.length(); i++) {

                JSONObject movieObject = moviesArray.getJSONObject(i);
                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieObject.getString(TMDB_ID));
                movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, movieObject.getString(TMDB_ORIGINAL_TITLE));
                movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, TMDB_URL_PREFIX + movieObject.getString(TMDB_POSTER_PATH));
                movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movieObject.getString(TMDB_OVERVIEW));
                movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, Float.valueOf(movieObject.getString(TMDB_VOTE_AVERAGE)));
                movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movieObject.getString(TMDB_RELEASE_DATE));
                movieValues.put(MovieContract.MovieEntry.COLUMN_CATEGORY, category);
                //Log.d(LOG_TAG, "Movie inserted " + category);
                cVVector.add(movieValues);
            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);


                inserted = getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
            }

            //Log.d(LOG_TAG, "FetchMovieTask Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}