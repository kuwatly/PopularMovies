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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.kuwatly.iyad.popularmovies.R;
import com.kuwatly.iyad.popularmovies.models.Movie;
import com.kuwatly.iyad.popularmovies.views.SquaredImageView;
import com.squareup.picasso.Picasso;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    public static class DetailFragment  extends Fragment {

        public DetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                Movie movieParcelableObject = (Movie) intent.getParcelableExtra(intent.EXTRA_TEXT);
                String imageURL = movieParcelableObject.getPosterPath();

                SquaredImageView detailImage =
                        (SquaredImageView) rootView.findViewById(R.id.detail_image);
                TextView detailOriginalTitle =
                        (TextView) rootView.findViewById(R.id.detail_original_title);
                TextView detailReleaseDate =
                        (TextView) rootView.findViewById(R.id.detail_release_date);
                RatingBar detailVoteAverage =
                        (RatingBar) rootView.findViewById(R.id.detail_vote_average);
                TextView detailOverview =
                        (TextView) rootView.findViewById(R.id.detail_overview);

                Picasso.with(getActivity()) //
                        .load(imageURL.replace("w185","w780")) //w780 retrives highest quality image
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error)
                        .fit()
                        .tag(getActivity())
                        .into(detailImage);
                detailOriginalTitle.setText(movieParcelableObject.getOriginalTitle());
                detailReleaseDate.setText(movieParcelableObject.getReleaseDate());
                // Rating is based of 10, divide by two to represent it with 5 stars rating
                detailVoteAverage.setRating(
                        Float.valueOf(movieParcelableObject.getVote_average())/2);
                detailOverview.setText(movieParcelableObject.getOverview());
            }

            return rootView;
        }
    }
}