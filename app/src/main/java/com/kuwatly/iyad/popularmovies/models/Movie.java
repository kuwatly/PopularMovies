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

    String id;
    String originalTitle;
    String posterPath;
    String overview;
    Float vote_average;
    String releaseDate;
    String runtime;

    public Movie() {
        this.id = "";
        this.originalTitle = "";
        this.posterPath = "";
        this.overview = "";
        this.vote_average=0.0F;
        this.releaseDate = "";
        this.runtime="";

    }

    public static Movie getSampleMovie(){
        Movie m = new Movie();
        m.setId("305784");
        m.setOriginalTitle("The Transcendental Object at the End of Time");
        m.setPosterPath("/lsPBx1PZqZ87ZXc5JYFRVKAm6Bq.jpg");
        m.setOverview("An audio-visual journey through the mind of Terence McKenna.");
        m.setVote_average(9.5F);
        m.setReleaseDate("2014-11-16");
        m.setRuntime("120m");
        return m;

    }
    private Movie(Parcel in) {
        id = in.readString();
        originalTitle = in.readString();
        posterPath = in.readString();
        overview = in.readString();
        vote_average = in.readFloat();
        releaseDate = in.readString();
        runtime = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(originalTitle);
        dest.writeString(posterPath);
        dest.writeString(overview);
        dest.writeFloat(vote_average);
        dest.writeString(releaseDate);
        dest.writeString(runtime);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("id: ");
        sb.append(id);
        sb.append("\n");

        sb.append("originalTitle: ");
        sb.append(originalTitle);
        sb.append("\n");

        sb.append("posterPath: ");
        sb.append(posterPath);
        sb.append("\n");

        sb.append("overview: ");
        sb.append(overview);
        sb.append("\n");

        sb.append("vote_average: ");
        sb.append(vote_average);
        sb.append("\n");

        sb.append("releaseDate: ");
        sb.append(releaseDate);
        sb.append("\n");

        sb.append("runtime: ");
        sb.append(runtime);
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


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }
    public void setOriginalTitle(String orginalTitle) {
        this.originalTitle = orginalTitle;
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

    public Float getVote_average() {
        return vote_average;
    }
    public void setVote_average(Float vote_average) {
        this.vote_average = vote_average;
    }

    public String getReleaseDate() {
        return releaseDate;
    }
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getRuntime() {
        return runtime;
    }
    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }
}
