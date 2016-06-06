package com.raymondluc.popularmovies.api;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by rtluc on 5/29/2016.
 */
public interface TheMovieDBAPI {
    @GET("/3/movie/{path}")
    public Call<MoviesList> getMovies(@Path("path") String path, @Query("api_key") String key);

    @GET("/3/movie/{id}/videos")
    public Call<VideosList> getVideos(@Path("id") String path, @Query("api_key") String key);

    @GET("/3/movie/{id}/reviews")
    public Call<ReviewsList> getReviews(@Path("id") String path, @Query("api_key") String key);
}