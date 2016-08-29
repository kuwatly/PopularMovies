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


public class Review implements Parcelable {
    String id;
    String author;
    String content;
    String url;
    String movie_id;


    public Review() {
        this.id = "";
        this.author = "";
        this.content = "";
        this.url = "";
        this.movie_id = "";
    }

    public static Review getSampleReview(){
        Review r = new Review();
        r.setId("56f4f0bd9251417a440017bd");
        r.setAuthor("Rahul Gupta");
        r.setContent("Awesome movie. Best Action sequence.\\r\\n\\r\\n**Slow in the first half**");
        r.setURL("https://www.themoviedb.org/review/56f4f0bd9251417a440017bd");
        r.setMovieID("305784");
        return r;

    }
    private Review(Parcel in) {
        id = in.readString();
        author = in.readString();
        content = in.readString();
        url = in.readString();
        movie_id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(author);
        dest.writeString(content);
        dest.writeString(url);
        dest.writeString(movie_id);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("id: ");
        sb.append(id);
        sb.append("\n");

        sb.append("author: ");
        sb.append(author);
        sb.append("\n");

        sb.append("content: ");
        sb.append(content);
        sb.append("\n");

        sb.append("url: ");
        sb.append(url);
        sb.append("\n");

        sb.append("movie_id: ");
        sb.append(movie_id);
        sb.append("\n");

        return sb.toString();

    }

    public final static Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel parcel) {
            return new Review(parcel);
        }

        @Override
        public Review[] newArray(int i) {
            return new Review[i];
        }
    };

    public String getId() {
        return id;
    }


    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getURL() {
        return url;
    }

    public String getMovieID() {
        return movie_id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setMovieID(String movie_id) {
        this.movie_id = movie_id;
    }


}
