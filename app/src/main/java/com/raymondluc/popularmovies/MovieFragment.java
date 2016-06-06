package com.raymondluc.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.raymondluc.popularmovies.api.MovieObject;
import com.raymondluc.popularmovies.api.MoviesList;
import com.raymondluc.popularmovies.api.TheMovieDBAPI;
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
public class MovieFragment extends Fragment implements MovieAsyncQueryHandler.QueryCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    static final int COL_ID = 0;
    static final int COL_TITLE = 1;
    static final int COL_RELEASE_DATE = 2;
    static final int COL_MOVIE_ID = 3;
    static final int COL_POSTER_PATH = 4;
    static final int COL_VOTE_AVERAGE = 5;
    static final int COL_OVERVIEW = 6;
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW
    };

    private final String LOG_TAG = MovieFragment.class.getSimpleName();
    private Callbacks mCallbacks;
    private MovieAdapter movieAdapter;

    @Override
    public void onInsertComplete(boolean successful) {

    }

    @Override
    public void onDeleteComplete(boolean successful) {

    }

    @Override
    public void onQueryComplete(Cursor cursor) {
        ArrayList<MovieObject> movieArrayList = new ArrayList<>();
        if (cursor != null && cursor.moveToPosition(0)) {
            do {
                MovieObject movie = new MovieObject();
                movie.id = cursor.getString(COL_MOVIE_ID);
                movie.overview = cursor.getString(COL_OVERVIEW);
                movie.title = cursor.getString(COL_TITLE);
                movie.poster_path = cursor.getString(COL_POSTER_PATH);
                movie.vote_average = cursor.getString(COL_VOTE_AVERAGE);
                movie.release_date = cursor.getString(COL_RELEASE_DATE);
                movieArrayList.add(movie);
            } while (cursor.moveToNext());
            cursor.close();
        }
        movieAdapter.addList(movieArrayList);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(getString(R.string.pref_sort_key).equals(key)){
            fetchMovieData();
            Activity activity = getActivity();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortPref = preferences.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
            if (activity != null && activity instanceof AppCompatActivity) {
                if (sortPref.equals("0")) {
                    ((AppCompatActivity) activity).getSupportActionBar().setTitle(R.string.title_app_popular);
                } else if (sortPref.equals("1")) {
                    ((AppCompatActivity) activity).getSupportActionBar().setTitle(R.string.title_app_top_rated);
                } else {
                    ((AppCompatActivity) activity).getSupportActionBar().setTitle(R.string.title_app_favorites);
                }
            }
        }
    }


    public interface Callbacks {
        public void onMovieSelected(MovieObject movie);
    }

    public MovieFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainactivityfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            fetchMovieData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        movieAdapter = new MovieAdapter();
        recyclerView.setAdapter(movieAdapter);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        else recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchMovieData();
    }

    private void fetchMovieData() {
        final String BASE_URL = "http://api.themoviedb.org";

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortPref = preferences.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));

        String sortMethod = getString(R.string.pref_sort_default);
        // Construct the URL for the MovieDB query
        if(sortPref.equals("2")){
            //Get favorites from db, add to arraylist, add the arraylist to the adapter
            new MovieAsyncQueryHandler(getContext().getContentResolver(), this).startQuery(
                    0, null, MovieContract.MovieEntry.CONTENT_URI,MOVIE_COLUMNS, null, null, null);

        }
        else{
            if (sortPref.equals("0")){
                sortMethod = "popular";
            }
            else if(sortPref.equals("1")){
                sortMethod = "top_rated";
            }
            Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()).build();

            TheMovieDBAPI theMovieDBAPI = retrofit.create(TheMovieDBAPI.class);

            Call<MoviesList> call = theMovieDBAPI.getMovies(sortMethod, BuildConfig.THE_MOVIE_DB_API_KEY);
            call.enqueue(new Callback<MoviesList>() {
                @Override
                public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {
                    if(response.isSuccessful()){
                        movieAdapter.addList(response.body().results);
                    }
                }

                @Override
                public void onFailure(Call<MoviesList> call, Throwable t) {
                    Toast.makeText(getActivity(), "Error loading movie data", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

        final String LOG_TAG = MovieAdapter.class.getSimpleName();

        private ArrayList<MovieObject> mDataset;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_movie, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final String baseUrl = "http://image.tmdb.org/t/p/w185/";
            final MovieObject movie = mDataset.get(position);
            String url = baseUrl + movie.poster_path;
            Glide.with(holder.moviePoster.getContext()).load(url).fitCenter().into(holder.moviePoster);
            holder.moviePoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Ask the main activity to handle the event
                    ((Callbacks)getActivity()).onMovieSelected(movie);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataset != null ? mDataset.size() : 0;
        }

        public void addList(ArrayList<MovieObject> list) {
            mDataset = list;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            //There's only a single ImageView in the ViewHolder in this case
            public ImageView moviePoster;

            public ViewHolder(View v) {
                super(v);
                moviePoster = (ImageView) v.findViewById(R.id.grid_item_movie_imageview);
            }

        }
    }


}