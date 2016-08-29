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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kuwatly.iyad.popularmovies.R;
import com.kuwatly.iyad.popularmovies.adapters.ReviewListAdapter;
import com.kuwatly.iyad.popularmovies.adapters.TrailerListAdapter;
import com.kuwatly.iyad.popularmovies.data.MovieContract;
import com.kuwatly.iyad.popularmovies.models.Review;
import com.kuwatly.iyad.popularmovies.models.Trailer;
import com.kuwatly.iyad.popularmovies.network.FetchMovieDetailsTask;
import com.kuwatly.iyad.popularmovies.network.FetchReviewsTask;
import com.kuwatly.iyad.popularmovies.network.FetchTrailersTask;
import com.kuwatly.iyad.popularmovies.utils.Utility;
import com.kuwatly.iyad.popularmovies.views.SquaredImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DetailFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        TrailerListAdapter.Callbacks,
        ReviewListAdapter.Callbacks {

    @BindView(R.id.detail_image)
    SquaredImageView detailImage;
    @BindView(R.id.detail_original_title)
    TextView detailOriginalTitle;
    @BindView(R.id.detail_release_date)
    TextView detailReleaseDate;
    @BindView(R.id.detail_vote_average)
    RatingBar detailVoteAverage;
    @BindView(R.id.detail_overview)
    TextView detailOverview;
    @BindView(R.id.detail_runtime)
    TextView detailRuntime;
    @BindView(R.id.trailer_list)
    RecyclerView mRecyclerViewForTrailers;
    @BindView(R.id.review_list)
    RecyclerView mRecyclerViewForReviews;
    @BindView(R.id.imgBtnFavorite)
    ImageButton detailFavortie;

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String DETAIL_URI = "URI";
    private Uri mUri;
    private static final String MOVIE_KEY = "saved_movie";
    private Long mMovieID;
    private TrailerListAdapter mTrailerListAdapter;
    private ReviewListAdapter mReviewListAdapter;
    public String mTrailer_YouTube = "NO TRAILER";
    public static final String POSITION = "position";
    public static final String MOVIE_ID = "movieId";

    private ShareActionProvider mShareActionProvider;
    private Unbinder unbinder;

    private static final int DETAIL_LOADER = 0;
    private static final int TRAILER_LOADER = 1;
    private static final int REVIEW_LOADER = 2;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RUNTIME,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_COMPLETE_RECORD_FLAG,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_FAVORITE_FLAG

    };

    private static final String[] TRAILER_COLUMNS = {
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry._ID,
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry.COLUMN_TRAILER_ID,
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry.COLUMN_NAME,
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry.COLUMN_KEY,
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry.COLUMN_SITE,
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry.COLUMN_TYPE,
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry.COLUMN_MOVIE_ID,

    };

    private static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry._ID,
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry.COLUMN_REVIEW_ID,
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry.COLUMN_AUTHOR,
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry.COLUMN_CONTENT,
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry.COLUMN_URL,
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry.COLUMN_MOVIE_ID

    };
    public static final int COL_MID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_ORIGINAL_TITLE = 2;
    public static final int COL_POSTER_PATH = 3;
    public static final int COL_OVERVIEW = 4;
    public static final int COL_VOTE_AVERAGE = 5;
    public static final int COL_RELEASE_DATE = 6;
    public static final int COL_RUNTIME = 7;
    public static final int COL_COMPLETE_RECORD_FLAG = 8;
    public static final int COL_FAVORITE_FLAG = 9;

    public static final int COL_TID = 0;
    public static final int COL_TRAILER_ID = 1;
    public static final int COL_NAME = 2;
    public static final int COL_KEY = 3;
    public static final int COL_SITE = 4;
    public static final int COL_TYPE = 5;
    public static final int COL_TRAILER_MOVIE_ID = 6;

    public static final int COL_RID = 0;
    public static final int COL_REVIEW_ID = 1;
    public static final int COL_AUTHOR = 2;
    public static final int COL_CONTENT = 3;
    public static final int COL_URL = 4;
    public static final int COL_REVIEW_MOVIE_ID = 5;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("DEBUG", "OnCreate of DetailFragment");

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        detailFavortie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageButton btn = (ImageButton) v;

                if (btn.isSelected())
                    btn.setSelected(false);
                else
                    btn.setSelected(true);
                final boolean favorite = btn.isSelected();
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {

                        ContentValues movieValues = new ContentValues();
                        movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE_FLAG, favorite);
                        long update = getContext().getContentResolver().update(
                                MovieContract.MovieEntry.CONTENT_URI,
                                movieValues,
                                "_Id=?",
                                new String[]{String.valueOf(mMovieID)}
                        );

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        if (favorite) {
                            detailFavortie.setImageResource(android.R.drawable.btn_star_big_on);
                        } else {
                            detailFavortie.setImageResource(android.R.drawable.btn_star_big_off);
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewForTrailers.setLayoutManager(layoutManager);
        mTrailerListAdapter = new TrailerListAdapter(new ArrayList<Trailer>(), this);
        mRecyclerViewForTrailers.setAdapter(mTrailerListAdapter);
        mRecyclerViewForTrailers.setNestedScrollingEnabled(false);

        mReviewListAdapter = new ReviewListAdapter(new ArrayList<Review>(), this);
        mRecyclerViewForReviews.setAdapter(mReviewListAdapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTrailer_YouTube);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DETAIL_LOADER:
                if (null != mUri) {
                    mMovieID = ContentUris.parseId(mUri);
                    updateMovieDetails();

                    return new CursorLoader(
                            getActivity(),
                            MovieContract.MovieEntry.CONTENT_URI,
                            DETAIL_COLUMNS,
                            MovieContract.MovieEntry.TABLE_NAME + "._ID=?",
                            new String[]{String.valueOf(mMovieID)},
                            null
                    );
                }
                break;
            case TRAILER_LOADER:
                if (null != mUri) {
                    mMovieID = ContentUris.parseId(mUri);
                    updateMovieTrailers();
                    return new CursorLoader(
                            getActivity(),
                            MovieContract.TrailerEntry.CONTENT_URI,
                            TRAILER_COLUMNS,
                            MovieContract.TrailerEntry.TABLE_NAME + ".movie_id=?",
                            new String[]{String.valueOf(mMovieID)},
                            null
                    );
                }

                break;
            case REVIEW_LOADER:
                if (null != mUri) {
                    mMovieID = ContentUris.parseId(mUri);
                    updateMovieReviews();

                    return new CursorLoader(
                            getActivity(),
                            MovieContract.ReviewEntry.CONTENT_URI,
                            REVIEW_COLUMNS,
                            MovieContract.ReviewEntry.TABLE_NAME + ".movie_id=?",
                            new String[]{String.valueOf(mMovieID)},
                            null
                    );
                }
                break;
            default:
                break;
        }

        return null;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case DETAIL_LOADER:
                //Log.v(LOG_TAG, "In onLoadFinished");
                if (data != null && !data.isClosed()&& data.moveToFirst()) {
                    Picasso.with(getActivity())
                            .load(data.getString(COL_POSTER_PATH).replace("w185", "w780")) //w780 retrives highest quality image
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.error)
                            .fit()
                            .tag(getActivity())
                            .into(detailImage);

                    detailOriginalTitle.setText(data.getString(COL_ORIGINAL_TITLE));
                    detailReleaseDate.setText(data.getString(COL_RELEASE_DATE));
                    detailVoteAverage.setRating(
                            Float.valueOf(data.getFloat(COL_VOTE_AVERAGE)) / 2);
                    detailOverview.setText(data.getString(COL_OVERVIEW));
                    detailRuntime.setText(data.getString(COL_RUNTIME));
                    if (data.getInt(COL_FAVORITE_FLAG) > 0) {
                        detailFavortie.setImageResource(android.R.drawable.btn_star_big_on);
                        detailFavortie.setSelected(true);
                    } else {
                        detailFavortie.setImageResource(android.R.drawable.btn_star_big_off);
                        detailFavortie.setSelected(false);
                    }

                }
                break;
            case TRAILER_LOADER:
                //Log.v(LOG_TAG, "In onLoadFinished");
                if (data != null && !data.isClosed() && data.moveToFirst()) {
                    ArrayList<Trailer> trailers = new ArrayList<>();
                    while (data.isAfterLast() == false) {
                        Trailer t = new Trailer();
                        t.setId(data.getString(COL_TRAILER_ID));
                        t.setName(data.getString(COL_NAME));
                        t.setKey(data.getString(COL_KEY));
                        t.setSite(data.getString(COL_SITE));
                        t.setType(data.getString(COL_TYPE));
                        t.setMovieID(String.valueOf(mMovieID));
                        trailers.add(t);
                        data.moveToNext();
                        if (trailers.size()==1) {
                            mTrailer_YouTube = t.getTrailerUrl();
                            if (mShareActionProvider != null) {
                                mShareActionProvider.setShareIntent(createShareForecastIntent());
                            }
                        }
                    }
                    mTrailerListAdapter.add(trailers);
                }
                break;
            case REVIEW_LOADER:
                //Log.v(LOG_TAG, "In onLoadFinished");
                if (data != null && !data.isClosed() && data.moveToFirst()) {
                    ArrayList<Review> reviews = new ArrayList<>();
                    while (data.isAfterLast() == false) {
                        Review r = new Review();
                        r.setId(data.getString(COL_RID));
                        r.setAuthor(data.getString(COL_AUTHOR));
                        r.setContent(data.getString(COL_CONTENT));
                        r.setURL(data.getString(COL_URL));
                        r.setMovieID(String.valueOf(mMovieID));
                        reviews.add(r);
                        data.moveToNext();
                    }
                    mReviewListAdapter.add(reviews);
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onResume() {
        Log.e("DEBUG", "onResume of DetailFragment");
        super.onResume();
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);

    }

    private void updateMovieDetails() {
        if (Utility.isOnline(getActivity())) {
            FetchMovieDetailsTask movieTask = new FetchMovieDetailsTask(getActivity());
            movieTask.execute(mMovieID);
        } else {

            Toast.makeText(getActivity(),
                    "Unable to connect to Internet!",
                    Toast.LENGTH_LONG).show();
        }

    }

    private void updateMovieTrailers() {
        if (Utility.isOnline(getActivity())) {
            FetchTrailersTask trailerTask = new FetchTrailersTask(getActivity());
            trailerTask.execute(mMovieID);
        } else {

            Toast.makeText(getActivity(),
                    "Unable to connect to Internet!",
                    Toast.LENGTH_LONG).show();
        }

    }

    private void updateMovieReviews() {
        if (Utility.isOnline(getActivity())) {
            FetchReviewsTask reviewTask = new FetchReviewsTask(getActivity());
            reviewTask.execute(mMovieID);
        } else {

            Toast.makeText(getActivity(),
                    "Unable to connect to Internet!",
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void watch(Trailer trailer, int position) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getTrailerUrl())));
    }

    @Override
    public void read(Review review, int position) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(review.getURL())));
    }

}


