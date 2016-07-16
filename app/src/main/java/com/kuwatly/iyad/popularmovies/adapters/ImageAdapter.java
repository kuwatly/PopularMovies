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

package com.kuwatly.iyad.popularmovies.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kuwatly.iyad.popularmovies.R;
import com.kuwatly.iyad.popularmovies.models.Movie;
import com.kuwatly.iyad.popularmovies.views.SquaredImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends BaseAdapter {
    public final Context context;
    private List<Movie> movies = new ArrayList<>();

    public ImageAdapter(Context context) {
        this.context = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridItem = convertView;
        Holder holder;
        if (gridItem == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            gridItem = inflater.inflate(R.layout.grid_item, parent, false);
            holder = new Holder();
            holder.imageItem = (SquaredImageView) gridItem.findViewById(R.id.item_image);
            gridItem.setTag(holder);
        } else {
            holder = (Holder) gridItem.getTag();
        }
        Movie m = getItem(position);

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context)
                .load(m.getPosterPath())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .fit()
                .tag(context)
                .into(holder.imageItem);

        return gridItem;
    }

    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Movie getItem(int position) {
        return movies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addData(Movie m) {
        // add the data
        movies.add(m);

    }

    public void clearData() {
        // clear the data
        movies.clear();
    }

    static class Holder {
        SquaredImageView imageItem;
    }
}