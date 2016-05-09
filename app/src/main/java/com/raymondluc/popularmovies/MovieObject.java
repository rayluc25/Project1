package com.raymondluc.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rtluc on 5/6/2016.
 */
public class MovieObject implements Parcelable {
    public static final String EXTRA_KEY = "movie_key";

    //Object attributes
    String title = null;
    String releaseDate = null;
    String rating = null;
    String plot = null;
    String imageUrl = null;

    protected MovieObject(Parcel in) {
        title = in.readString();
        releaseDate = in.readString();
        rating = in.readString();
        plot = in.readString();
        imageUrl = in.readString();
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
        dest.writeString(releaseDate);
        dest.writeString(rating);
        dest.writeString(plot);
        dest.writeString(imageUrl);
    }
}
