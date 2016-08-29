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



public class Trailer implements Parcelable {
    String id;
    String key;
    String name;
    String site;
    String type;
    String movie_id;


    public Trailer() {
        this.id = "";
        this.key = "";
        this.name = "";
        this.site = "";
        this.type = "";
        this.movie_id = "";
    }

    public static Trailer getSampleTrailer(){
        Trailer r = new Trailer();
        r.setId("571c8dc4c3a36842aa000190");
        r.setKey("6as8ahAr1Uc");
        r.setName("Exclusive Sneak");
        r.setSite("YouTube");
        r.setType("Teaser/Trailers");
        r.setMovieID("305784");
        return r;

    }

    private Trailer(Parcel in) {
        id = in.readString();
        key = in.readString();
        name = in.readString();
        site = in.readString();
        type = in.readString();
        movie_id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(site);
        dest.writeString(type);
        dest.writeString(movie_id);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("id: ");
        sb.append(id);
        sb.append("\n");

        sb.append("key: ");
        sb.append(key);
        sb.append("\n");

        sb.append("name: ");
        sb.append(name);
        sb.append("\n");

        sb.append("site: ");
        sb.append(site);
        sb.append("\n");

        sb.append("type: ");
        sb.append(type);
        sb.append("\n");

        sb.append("movie_id: ");
        sb.append(movie_id);
        sb.append("\n");

        return sb.toString();

    }

    public final static Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel parcel) {
            return new Trailer(parcel);
        }

        @Override
        public Trailer[] newArray(int i) {
            return new Trailer[i];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMovieID() {
        return movie_id;
    }

    public void setMovieID(String movie_id) {
        this.movie_id = movie_id;
    }

    public String getTrailerUrl() {
        return "http://www.youtube.com/watch?v=" + key;
    }


}

