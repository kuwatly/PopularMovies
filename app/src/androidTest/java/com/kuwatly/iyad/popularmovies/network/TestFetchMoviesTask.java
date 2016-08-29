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

import android.annotation.TargetApi;
import android.test.AndroidTestCase;

import com.kuwatly.iyad.popularmovies.data.MovieContract;
import com.kuwatly.iyad.popularmovies.models.Movie;

public class TestFetchMoviesTask extends AndroidTestCase{
    static final Movie SAMPLE_MOVIE = Movie.getSampleMovie();
    @TargetApi(11)
    public void testAddMovie() {
        // start from a clean state
        getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{SAMPLE_MOVIE.getId()});

    }
}