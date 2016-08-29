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

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.kuwatly.iyad.popularmovies.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {

    static final long TEST_MOVIE = 305784L;

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues creatMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, TEST_MOVIE);
        movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, "The Transcendental Object at the End of Time");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/lsPBx1PZqZ87ZXc5JYFRVKAm6Bq.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "An audio-visual journey through the mind of Terence McKenna.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 10);
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2014-11-16");

        return movieValues;
    }
    static ContentValues creatTrailerValues(long movieRowId) {
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, movieRowId);
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_ID, "5473176f9251414b4b000043");
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_NAME, "TRAILER: The Transcendental Object At The End Of Time (Terence McKenna Movie)");
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_KEY, "2Der6EhLocU");
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_SITE, "YouTube");
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_TYPE, "Trailer");


        return trailerValues;
    }
    static ContentValues creatReviewValues(long movieRowId) {
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movieRowId);
        trailerValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_ID, "56f4f0bd9251417a440017bd");
        trailerValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, "Rahul Gupta");
        trailerValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, "Awesome moview. Best Action sequence.\\r\\n\\r\\n**Slow in the first half**");


        return trailerValues;
    }


    static long insertSampleMovieValues(Context context) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.creatMovieValues();

        long movieRowId;
        movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Sample Movie Values", movieRowId != -1);

        return testValues.getAsLong(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}