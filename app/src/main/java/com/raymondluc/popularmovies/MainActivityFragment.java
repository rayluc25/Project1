package com.raymondluc.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private MyAdapter movieAdapter;

    public MainActivityFragment() {
    }

    private void updateMovieData() {
        FetchMovieDataTask movieDataTask = new FetchMovieDataTask();
        movieDataTask.execute();
    }

    private ArrayList<String> getImageUrls(ArrayList<MovieObject> array){
        ArrayList<String> urlList = new ArrayList<String>();

        for(int i = 0; i < array.size(); i++){
            String string = array.get(i).imageUrl;
            urlList.add(string);
        }

        Log.v(LOG_TAG, "List of image URLs: " + urlList);
        return urlList;
    }

    private ArrayList<MovieObject> getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MDB_LIST = "results";
        final String MDB_TITLE = "original_title";
        final String MDB_RELEASE = "release_date";
        final String MDB_RATING = "vote_average";
        final String MDB_PLOT = "overview";
        final String MDB_IMAGE = "backdrop_path";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray(MDB_LIST);

        ArrayList<MovieObject> moviesArray = new ArrayList<MovieObject>();

        //For each movie in the JSON list, grab the information needed
        for (int i = 0; i < movieArray.length(); i++) {
            //Format of data is "Title, Release Date, Rating, Plot, Image"
            String title;
            String releaseDate;
            String rating;
            String plot;
            String imageTag;

            // Get the JSON object representing the movie
            JSONObject movieInformation = movieArray.getJSONObject(i);
            title = movieInformation.getString(MDB_TITLE);
            releaseDate = movieInformation.getString(MDB_RELEASE);
            rating = String.valueOf(movieInformation.getDouble(MDB_RATING));
            plot = movieInformation.getString(MDB_PLOT);
            imageTag = movieInformation.getString(MDB_IMAGE);

            //Create a movie object, then add it to the array list
            MovieObject movie = new MovieObject();

            //Place information into MovieObject, and add to moviesArray
            movie.title = title;
            movie.releaseDate = releaseDate;
            movie.rating = rating;
            movie.plot = plot;
            movie.imageUrl = imageTag;

            moviesArray.add(movie);
        }
        return moviesArray;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainactivityfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovieData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        movieAdapter = new MyAdapter();
        recyclerView.setAdapter(movieAdapter);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateMovieData();
    }

    public class FetchMovieDataTask extends AsyncTask<Void, Void, ArrayList<MovieObject>> {
        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

        //This is where we get the raw data from movieDB
        @Override
        protected ArrayList<MovieObject> doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;
            ArrayList<MovieObject> moviesArray = new ArrayList<MovieObject>();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortPref = preferences.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));

            try {
                String sortMethod = getString(R.string.pref_sort_default);
                // Construct the URL for the MovieDP query
                if (sortPref.equals("1")){
                    sortMethod = "popular";
                }
                else{
                    sortMethod = "top_rated";
                }
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + sortMethod + "?";
                final String APIKEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();
                URL url;
                try {
                    url = new URL(builtUri.toString());

                    Log.v(LOG_TAG, "Built URI: " + url);

                    // Create the request to TheMovieDB, and open connection

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        //Nothing to do.
                        movieJsonStr = null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        //Stream was empty. No point in parsing.
                        movieJsonStr = null;
                    }
                    movieJsonStr = buffer.toString();

                    Log.v(LOG_TAG, "Movie JSON String: " + movieJsonStr);

                    moviesArray = getMovieDataFromJson(movieJsonStr);

                } catch (MalformedURLException e) {
                    Log.e(LOG_TAG, "Could not build URL.");
                    url = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } catch (IOException e) {
                movieJsonStr = null;
                Log.e(LOG_TAG, "Could not build URL for query.");
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("MainActivityFragment", "Error closing stream", e);
                    }
                }
            }
            return moviesArray;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieObject> arrayList) {
            if (arrayList != null) {
                //Get image URLs and give dataset to the adapter
                movieAdapter.addList(arrayList);
            }
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        final String LOG_TAG = MyAdapter.class.getSimpleName();

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
            String url = baseUrl + movie.imageUrl;
            Glide.with(holder.moviePoster.getContext()).load(url).fitCenter().into(holder.moviePoster);
            holder.moviePoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Start a new activity
                    Intent detailIntent = new Intent(holder.moviePoster.getContext(), DetailActivity.class);
                    detailIntent.putExtra(MovieObject.EXTRA_KEY, movie);
                    startActivity(detailIntent);
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