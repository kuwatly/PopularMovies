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

package com.kuwatly.iyad.popularmovies.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.kuwatly.iyad.popularmovies.R;
import com.kuwatly.iyad.popularmovies.adapters.ImageAdapter;
import com.kuwatly.iyad.popularmovies.models.Movie;
import com.kuwatly.iyad.popularmovies.network.FetchMoviesTask;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {
    private ImageAdapter mImageAdapter;
    private ArrayList<Movie> movieList;
    private int sortBy;
    static final String STATE_SORT = "sortOption";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null ) {
            movieList = new ArrayList<>();
            sortBy=R.id.most_popular;
        }
        else {
            movieList = savedInstanceState.getParcelableArrayList("Movies");
            sortBy=savedInstanceState.getInt(STATE_SORT);
        }

        setContentView(R.layout.activity_main);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        mImageAdapter = new ImageAdapter(this);
        gridview.setAdapter(mImageAdapter);
        updateMovies();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Movie m = mImageAdapter.getItem(position);
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class)
                                                .putExtra(Intent.EXTRA_TEXT, m);
                                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id != sortBy) {
            sortBy = id;
            updateMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SORT, sortBy);
        outState.putParcelableArrayList("Movies", movieList);
        super.onSaveInstanceState(outState);
    }

    private void updateMovies() {
        if (isOnline()){
            FetchMoviesTask moviesTask = new FetchMoviesTask(mImageAdapter);
            moviesTask.execute(sortBy);
        }
        else {
            Toast.makeText(mImageAdapter.context,
                    "Unable to connect to Internet!",
                    Toast.LENGTH_LONG).show();
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
