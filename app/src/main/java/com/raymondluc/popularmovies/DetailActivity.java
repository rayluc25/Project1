package com.raymondluc.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //If the fragment doesn't have a saved instance
        if (savedInstanceState == null) {
            //Create a new fragment, set arguments as the parcelable MovieObject
            Fragment fragment = new DetailActivityFragment();
            fragment.setArguments(getIntent().getExtras());
            //Create the fragment in the DetailActivity
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailActivityFragment extends Fragment {

        public DetailActivityFragment() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final String baseUrl = "http://image.tmdb.org/t/p/w185/";

            //Inflate the layout for this fragment
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            //Retrieve the MovieObject passed with the intent from previous Activity
            MovieObject movie = getArguments().getParcelable(MovieObject.EXTRA_KEY);
            //Set the attributes from MovieObject to the respective Views in this fragment if the available
                ((TextView)rootView.findViewById(R.id.detail_title)).setText(movie.title);
                ((TextView)rootView.findViewById(R.id.detail_release)).setText(movie.releaseDate);
                ((TextView)rootView.findViewById(R.id.detail_rating)).setText("Rating: " + movie.rating);
                ((TextView)rootView.findViewById(R.id.detail_plot)).setText(movie.plot);
                String imageUrl = baseUrl + movie.imageUrl;

                Glide.with(this).load(imageUrl).into((ImageView)rootView.findViewById(R.id.detail_image));


            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }
    }

}
