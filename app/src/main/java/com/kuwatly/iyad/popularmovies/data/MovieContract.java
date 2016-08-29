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

package com.kuwatly.iyad.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.kuwatly.iyad.popularmovies.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILER = "trailer";
    public static final String PATH_REVIEW = "review";

    public static final class MovieEntry implements BaseColumns {


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movie";

        // Movie if as returned by API
        public static final String COLUMN_MOVIE_ID = "movie_id";
        // Original title as returned by API
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        // Poster path as returned by API, to identify the image to be used
        public static final String COLUMN_POSTER_PATH =  "poster_path";
        // A plot synopsis (overview) as returned by API
        public static final String COLUMN_OVERVIEW =  "overview";
        // User rating out of 10 (vote average) as returned by API
        public static final String COLUMN_VOTE_AVERAGE =  "vote_average";
        // Release date as returned by API
        public static final String COLUMN_RELEASE_DATE =  "release_date";
        // Runtime as returned by API
        public static final String COLUMN_RUNTIME =  "runtime";
        // Flag to indicate that (runtime, reviews and trailers)
        // have been fetched for this record
        public static final String COLUMN_COMPLETE_RECORD_FLAG =  "complete_record_flag";
        // Favorite flag
        public static final String COLUMN_FAVORITE_FLAG =  "favorite_flag";
        // Category integer 1=Most Popular; 2=Top Rated
        public static final String COLUMN_CATEGORY =  "category";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }


    /* Inner class that defines the table contents of the trailers table */
    public static final class TrailerEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public static final String TABLE_NAME = "trailer";

        // ID as returned by API
        public static final String COLUMN_TRAILER_ID = "trailer_id";
        // Name as returned by API
        public static final String COLUMN_NAME= "name";
        // Key as returned by API, used to identify trailer in the site
        public static final String COLUMN_KEY= "key";
        // Site as returned by API ex. youtube
        public static final String COLUMN_SITE = "site";
        // Type as returned by API ex. Teaser/Trailers
        public static final String COLUMN_TYPE= "type";
        // Column with the foreign key into the movie table. (movie SQL id)
        public static final String COLUMN_MOVIE_ID= "movie_id";

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the reviews table */
    public static final class ReviewEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;


        public static final String TABLE_NAME = "review";

        // ID as returned by API
        public static final String COLUMN_REVIEW_ID = "review_id";
        // Author as returned by API
        public static final String COLUMN_AUTHOR = "author";
        // Content as returned by API
        public static final String COLUMN_CONTENT= "content";

        // URL as returned by API
        public static final String COLUMN_URL= "url";

        // Column with the foreign key into the movie table.

        public static final String COLUMN_MOVIE_ID= "movie_id";

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}