package com.raymondluc.popularmovies;

/**
 * Created by rtluc on 5/28/2016.
 */

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.raymondluc.popularmovies.api.MovieObject;
import com.raymondluc.popularmovies.api.ReviewObject;
import com.raymondluc.popularmovies.api.ReviewsList;
import com.raymondluc.popularmovies.api.TheMovieDBAPI;
import com.raymondluc.popularmovies.api.VideoObject;
import com.raymondluc.popularmovies.api.VideosList;
import com.raymondluc.popularmovies.data.MovieAsyncQueryHandler;
import com.raymondluc.popularmovies.data.MovieContract;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements MovieAsyncQueryHandler.QueryCallback{

    private RecyclerView mRecyclerView;
    private DetailAdapter mReviewAdapter;
    private Boolean mFavorite;



    public static DetailFragment newInstance(MovieObject movie) {
        //Build the detail fragment with the MovieObject data
        Bundle args = new Bundle();
        args.putParcelable(MovieObject.EXTRA_KEY, movie);

        DetailFragment df = new DetailFragment();
        df.setArguments(args);
        return df;
    }

    public DetailFragment() {
    }

    private void fetchReviewData(String movieId) {
        final String BASE_URL = "http://api.themoviedb.org";

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();

        TheMovieDBAPI theMovieDBAPI = retrofit.create(TheMovieDBAPI.class);

        Call<ReviewsList> call = theMovieDBAPI.getReviews(movieId, BuildConfig.THE_MOVIE_DB_API_KEY);
        call.enqueue(new Callback<ReviewsList>() {
            @Override
            public void onResponse(Call<ReviewsList> call, Response<ReviewsList> response) {
                if(response.isSuccessful()){
                    mReviewAdapter.addReviewList(response.body().results);
                }

            }

            @Override
            public void onFailure(Call<ReviewsList> call, Throwable t) {
                Toast.makeText(getActivity(), "Error loading review data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchVideoData(String movieId) {
        final String BASE_URL = "http://api.themoviedb.org";

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();

        TheMovieDBAPI theMovieDBAPI = retrofit.create(TheMovieDBAPI.class);

        Call<VideosList> call = theMovieDBAPI.getVideos(movieId, BuildConfig.THE_MOVIE_DB_API_KEY);
        call.enqueue(new Callback<VideosList>() {
            @Override
            public void onResponse(Call<VideosList> call, Response<VideosList> response) {
                if(response.isSuccessful()){
                    mReviewAdapter.addVideoList(response.body().results);
                }
            }

            @Override
            public void onFailure(Call<VideosList> call, Throwable t) {
                Toast.makeText(getActivity(), "Error loading video data", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final String baseUrl = "http://image.tmdb.org/t/p/w185/";
        String movieId = null;

        //Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        //Retrieve the MovieObject passed with the intent from previous Activity if there is one
        if(getArguments() != null) {
            final MovieObject movie = getArguments().getParcelable(MovieObject.EXTRA_KEY);
            //Set the attributes from MovieObject to the respective Views in this fragment if the available
            ((TextView) rootView.findViewById(R.id.detail_title)).setText(movie.title);
            ((TextView) rootView.findViewById(R.id.detail_release)).setText(movie.release_date);
            ((TextView) rootView.findViewById(R.id.detail_rating)).setText("Rating: " + movie.vote_average);
            ((TextView) rootView.findViewById(R.id.detail_plot)).setText(movie.overview);
            String imageUrl = baseUrl + movie.poster_path;

            Glide.with(this).load(imageUrl).into((ImageView) rootView.findViewById(R.id.detail_image));

            //Set up the favorite button
            Boolean isFavorite = savedInstanceState != null ? savedInstanceState.getBoolean("extra_favorite") : null;
            final Button mFavoriteButton = (Button) rootView.findViewById(R.id.detail_favorite);
            if (isFavorite != null) {
                (rootView.findViewById(R.id.detail_favorite)).setEnabled(true);
                ((Button) rootView.findViewById(R.id.detail_favorite)).setText(isFavorite ? getString(R.string.detail_add_favorite) : getString(R.string.detail_remove_favorite));
            }
            else{
                //Start a query to see if it's a favorite movie
                new MovieAsyncQueryHandler(getContext().getContentResolver(), this).startQuery(
                        0,
                        null,
                        MovieContract.MovieEntry.CONTENT_URI,
                        null,
                        MovieContract.MovieEntry.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(movie.id)},
                        null);
            }

            mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //If it's not yet a favorite, insert it to favorites
                    if(mFavorite == null || !mFavorite){
                        ContentValues movieValues = new ContentValues();

                        movieValues.put(MovieContract.MovieEntry.COLUMN_ID, movie.id);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.overview);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.poster_path);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.release_date);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.title);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.vote_average);

                        new MovieAsyncQueryHandler(getContext().getContentResolver(), DetailFragment.this)
                                .startInsert(
                                        0,
                                        null,
                                        MovieContract.MovieEntry.CONTENT_URI,
                                        movieValues
                                );
                    }
                    //else, remove it from favorites
                    else{
                        new MovieAsyncQueryHandler(getContext().getContentResolver(), DetailFragment.this)
                                .startDelete(
                                        0,
                                        null,
                                        MovieContract.MovieEntry.CONTENT_URI,
                                        MovieContract.MovieEntry.COLUMN_ID + "= ?", new String[]{String.valueOf(movie.id)}
                                );
                    }
                }
            });

            //Grab the id of the movie from MovieObject, make retrofit calls to get reviews and trailers
            mReviewAdapter = new DetailAdapter();
            movieId = movie.id;
            fetchVideoData(movieId);
            fetchReviewData(movieId);

            //Hook up the recycler view to this fragment
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.detail_reviews);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(mReviewAdapter);

        }

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFavorite != null) {
            outState.putBoolean("extra_favorite", mFavorite);
        }
    }

    @Override
    public void onInsertComplete(boolean successful) {
        //Change button presentation
        final Button mFavoriteButton = (Button) getView().findViewById(R.id.detail_favorite);
        if(successful)
        mFavoriteButton.setText(R.string.detail_remove_favorite);
        else mFavoriteButton.setText(R.string.detail_add_favorite);
        mFavorite = successful;
        mFavoriteButton.setEnabled(true);
    }

    @Override
    public void onDeleteComplete(boolean successful) {
        //Change button presentation
        final Button mFavoriteButton = (Button) getView().findViewById(R.id.detail_favorite);
        if(successful)
        mFavoriteButton.setText(R.string.detail_add_favorite);
        else mFavoriteButton.setText(R.string.detail_remove_favorite);
        mFavorite = !successful;
        mFavoriteButton.setEnabled(true);
    }

    @Override
    public void onQueryComplete(Cursor cursor) {
        Button mFavoriteButton = (Button) getView().findViewById(R.id.detail_favorite);
        mFavoriteButton.setEnabled(true);
        if (cursor != null && cursor.moveToFirst()){
            mFavoriteButton.setText(getString(R.string.detail_remove_favorite));
            mFavorite = true;
        }
        else{
            mFavoriteButton.setText(getString(R.string.detail_add_favorite));
            mFavorite = false;
        }
        if(cursor !=null){
            cursor.close();
        }
    }

    public class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private static final int TYPE_VIDEO = 0;
        private static final int TYPE_REVIEW = 1;

        private  ArrayList<ReviewObject> mReviewData;
        private ArrayList<VideoObject> mVideoData;

        public DetailAdapter(){
            mReviewData = new ArrayList<>();
            mVideoData = new ArrayList<>();
        }

        //Is it a review or a trailer?
        @Override
        public int getItemViewType(int position) {
            if (position > mVideoData.size() - 1) {
                return TYPE_REVIEW;
            }
            else {
                return TYPE_VIDEO;
            }
        }

        //Create the necessary viewholders
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return viewType == TYPE_VIDEO ?
                    new VideoHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false)) :
                    new ReviewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false));
        }

        //Bind the data to the viewholders
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof ReviewHolder) {
                ReviewObject review = mReviewData.get(position - mVideoData.size());
                ((ReviewHolder) holder).mAuthorView.setText(review.author);
                ((ReviewHolder) holder).mReviewView.setText(review.content);
            }
            //Else, bind data to a TrailerHolder
            else if (holder instanceof VideoHolder){
                final VideoObject video = mVideoData.get(position);
                ((VideoHolder) holder).mVideoView.setText(video.name);
                ((VideoHolder) holder).mVideoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Launch in YouTube or browser
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://www.youtube.com/watch?v=" + video.key));
                        startActivity(intent);
                    }
                });
            }
        }

        //Should return how many reviews and trailers there are
        //Return the number of ViewHolders that Recycler View needs
        @Override
        public int getItemCount() {
            return mReviewData.size() + mVideoData.size();
        }

        public void addReviewList(ArrayList<ReviewObject> list) {
            mReviewData = list;
            notifyDataSetChanged();
        }

        public void addVideoList(ArrayList<VideoObject> list) {
            mVideoData = list;
            notifyDataSetChanged();
        }

        private class ReviewHolder extends RecyclerView.ViewHolder {
            private TextView mReviewView;
            private TextView mAuthorView;

            public ReviewHolder(View itemView) {
                super(itemView);

                mReviewView = (TextView) itemView.findViewById(R.id.item_review);
                mAuthorView = (TextView) itemView.findViewById(R.id.item_author);
            }
        }

        private class VideoHolder extends RecyclerView.ViewHolder{
            public TextView mVideoView;

            public VideoHolder(View itemView){
                super(itemView);

                mVideoView = (TextView) itemView.findViewById(R.id.item_video);
            }
        }
    }
}
