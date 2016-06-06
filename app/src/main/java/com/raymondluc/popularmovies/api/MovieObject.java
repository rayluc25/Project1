package com.raymondluc.popularmovies.api;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rtluc on 5/6/2016.
 */
public class MovieObject implements Parcelable {
    public static final String EXTRA_KEY = "movie_key";

    //Object attributes
    public String title = null;
    public String release_date = null;
    public String vote_average = null;
    public String overview = null;
    public String poster_path = null;
    public String id = null;

    protected MovieObject(Parcel in) {
        title = in.readString();
        release_date = in.readString();
        vote_average = in.readString();
        overview = in.readString();
        poster_path = in.readString();
        id = in.readString();
    }

    public static final Creator<MovieObject> CREATOR = new Creator<MovieObject>() {
        @Override
        public MovieObject createFromParcel(Parcel in) {
            return new MovieObject(in);
        }

        @Override
        public MovieObject[] newArray(int size) {
            return new MovieObject[size];
        }
    };

    public MovieObject() {

    }

    //Default constructor
    public void MovieObject(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(release_date);
        dest.writeString(vote_average);
        dest.writeString(overview);
        dest.writeString(poster_path);
        dest.writeString(id);
    }
}
