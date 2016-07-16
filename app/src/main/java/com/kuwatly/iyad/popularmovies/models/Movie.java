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

package com.kuwatly.iyad.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;


public class Movie implements Parcelable {

    String originalTitle;
    String posterPath;
    String overview;
    String releaseDate;
    String id;
    String title;
    String vote_average;

    public Movie() {
        this.originalTitle = "";
        this.posterPath = "";
        this.overview = "";
        this.releaseDate = "";
        this.id = "";
        this.title = "";
        this.vote_average = "";
    }

    private Movie(Parcel in) {
        originalTitle = in.readString();
        posterPath = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        id = in.readString();
        title = in.readString();
        vote_average = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(originalTitle);
        dest.writeString(posterPath);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(vote_average);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Title: ");
        sb.append(title);
        sb.append("\n");

        sb.append("id: ");
        sb.append(id);
        sb.append("\n");

        sb.append("originalTitle: ");
        sb.append(originalTitle);
        sb.append("\n");

        sb.append("posterPath: ");
        sb.append(posterPath);
        sb.append("\n");

        sb.append("releaseDate: ");
        sb.append(releaseDate);
        sb.append("\n");

        sb.append("overview: ");
        sb.append(overview);
        sb.append("\n");

        sb.append("vote_average: ");
        sb.append(vote_average);
        sb.append("\n");

        return sb.toString();

    }

    public final static Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };


    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }


    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }


    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVote_average() {
        return vote_average;
    }

    public void setVote_average(String vote_average) {
        this.vote_average = vote_average;
    }


}
