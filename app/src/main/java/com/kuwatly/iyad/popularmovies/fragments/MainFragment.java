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

package com.kuwatly.iyad.popularmovies.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.kuwatly.iyad.popularmovies.R;
import com.kuwatly.iyad.popularmovies.adapters.GridAdapter;
import com.kuwatly.iyad.popularmovies.data.MovieContract;
import com.kuwatly.iyad.popularmovies.sync.MovieSyncAdapter;
import com.kuwatly.iyad.popularmovies.utils.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class MainFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIE_LOADER = 0;
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    public interface Callback {
        void onItemSelected(Uri movieUri);
    }

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_RUNTIME,
            MovieContract.MovieEntry.COLUMN_CATEGORY
    };

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_ORIGINAL_TITLE = 2;
    public static final int COL_POSTER_PATH = 3;
    public static final int COL_OVERVIEW = 4;
    public static final int COL_VOTE_AVERAGE = 5;
    public static final int COL_RELEASE_DATE = 6;
    public static final int COL_RUNTIME = 7;
    public static final int COL_CATEGORY = 8;


    @BindView(R.id.gridview)
    GridView gridview;
    private Unbinder unbinder;
    private GridAdapter mGridAdapter;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Utility.setPreferredSortedBy(getActivity(),item.getTitle().toString());
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }

        super.onSaveInstanceState(outState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Cursor cur = getActivity().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null, null, null, null);
        mGridAdapter = new GridAdapter(getActivity(), cur, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        unbinder = ButterKnife.bind(this, rootView);


        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(MovieContract.MovieEntry.buildMovieUri(cursor.getLong(COL_ID))
                            );
                }
                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        gridview.setAdapter(mGridAdapter);

        return rootView;
    }


    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }



    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void updateMovies() {
        if (Utility.isOnline(getActivity())) {
            MovieSyncAdapter.syncImmediately(getActivity());
        } else {

            Toast.makeText(getActivity(),
                    "Unable to connect to Internet!",
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortBy = Utility.getPreferredSortedBy(getActivity());
        String sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " ASC";
        if (sortBy.compareTo(this.getString(R.string.most_popular))==0) {
            Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
            return new CursorLoader(getActivity(),
                    movieUri,
                    MOVIE_COLUMNS,
                    MovieContract.MovieEntry.COLUMN_CATEGORY + "='1'",
                    null,
                    sortOrder);
        }

        else if (sortBy.compareTo(this.getString(R.string.top_rated))==0) {
            Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
            return new CursorLoader(getActivity(),
                    movieUri,
                    MOVIE_COLUMNS,
                    MovieContract.MovieEntry.COLUMN_CATEGORY + "='2'",
                    null,
                    sortOrder);
        }
        else {// favorite
            Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
            return new CursorLoader(getActivity(),
                    movieUri,
                    MOVIE_COLUMNS,
                    MovieContract.MovieEntry.COLUMN_FAVORITE_FLAG + "='1'",
                    null,
                    sortOrder);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!cursor.moveToFirst()) {
            return;
        }
        else {
            mGridAdapter.swapCursor(cursor);
            if (mPosition != GridView.INVALID_POSITION) {
                gridview.setSelection(mPosition);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mGridAdapter.swapCursor(null);
    }

}

