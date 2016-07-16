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


import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.kuwatly.iyad.popularmovies.BuildConfig;
import com.kuwatly.iyad.popularmovies.R;
import com.kuwatly.iyad.popularmovies.adapters.ImageAdapter;
import com.kuwatly.iyad.popularmovies.models.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchMoviesTask extends AsyncTask<Integer, Void, Movie[]> {
    // URL parameters
    private static final String MOVIE_BASE_URL =
        "http://api.themoviedb.org/3/discover/movie?";
    private static final String QUERY_PARAM = "sort_by";
    private String SORT_PARAM; // based on user sort (Top Rated or Most Popular)
    private static final String QUERY_PAGE_PARAM = "page";
    private static final String QUERY_NUMBER_PARAM = "1";
    private static final String APPID_PARAM = "api_key";

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

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    private ImageAdapter mImageAdapter;

    public FetchMoviesTask(ImageAdapter imageAdapter) {
        this.mImageAdapter = imageAdapter;
    }

    @Override
    protected Movie[] doInBackground(Integer... params) {
        if (params.length == 0) {
            return null;
        }
        int sortBy = params[0];

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        try {

            if (sortBy== R.id.most_popular)
                SORT_PARAM = "popularity.desc";
            else
                SORT_PARAM = "vote_average.desc";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, SORT_PARAM)
                    .appendQueryParameter(QUERY_PAGE_PARAM, QUERY_NUMBER_PARAM)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

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
            moviesJsonStr = buffer.toString();

        } catch (IOException e) {
            // If the code didn't successfully get the movies data, there's no point in attemping
            // to parse it.
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
        try {
            return getMoviesDataFromJson(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }



    private Movie[] getMoviesDataFromJson(String movieJsonStr)
            throws JSONException {

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray moviesArray = movieJson.getJSONArray(TMDB_LIST);

        Movie[] resultMovies = new Movie[TMDB_MAXPERPAGE];

        for (int i = 0; i < moviesArray.length(); i++) {

            Movie m = new Movie();
            // Get the JSON object representing a movie
            JSONObject movie = moviesArray.getJSONObject(i);

            m.setId(movie.getString(TMDB_ID));
            m.setOriginalTitle(movie.getString(TMDB_ORIGINAL_TITLE));
            m.setTitle(movie.getString(TMDB_TITLE));
            m.setOverview(movie.getString(TMDB_OVERVIEW));
            m.setPosterPath(TMDB_URL_PREFIX + movie.getString(TMDB_POSTER_PATH));
            m.setReleaseDate(movie.getString(TMDB_RELEASE_DATE));
            m.setVote_average(movie.getString(TMDB_VOTE_AVERAGE));

            resultMovies[i] = m;
        }

        return resultMovies;

    }


    @Override
    protected void onPostExecute(Movie[] result) {
        super.onPostExecute(result);
        if (result != null) {
            mImageAdapter.clearData();
            for(Movie movie : result) {
                mImageAdapter.addData(movie);
            }
            // refresh the View
            mImageAdapter.notifyDataSetChanged();

        }
        else
            Toast.makeText(mImageAdapter.context,
                    "Unable to retrieve movie data. Make sure you are connected to Internet!",
                    Toast.LENGTH_LONG).show();

    }
}