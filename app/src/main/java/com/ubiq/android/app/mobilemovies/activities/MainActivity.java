package com.ubiq.android.app.mobilemovies.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ubiq.android.app.mobilemovies.fragments.DetailActivityFragment;
import com.ubiq.android.app.mobilemovies.fragments.MovieGridFragment;
import com.ubiq.android.app.mobilemovies.R;
import com.ubiq.android.app.mobilemovies.utils.Utils;

public class MainActivity extends AppCompatActivity implements MovieGridFragment.Callback {
    private final static String LOG_TAG             = MainActivity.class.getSimpleName();
    private final        String DETAIL_FRAGMENT_TAG = "DF";
    private boolean             mTwoPane            = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Prepare the internal directory for saving favorite movies
        // This must be done prior to setting the content view
        Utils.initFileDirectory(getApplicationContext());
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                FragmentManager     fragmentManager     = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DetailActivityFragment fragment = new DetailActivityFragment();
                fragmentTransaction.add(R.id.movie_detail_container, fragment, DETAIL_FRAGMENT_TAG);
                fragmentTransaction.hide(fragment);
                fragmentTransaction.commit();
            }
        } else {
            mTwoPane = false;
            ActionBar actionBar = getSupportActionBar ();
            if (actionBar != null) actionBar.setElevation(0f);
        }
        Log.v(LOG_TAG, "***mTwoPane=" + mTwoPane);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.v(LOG_TAG, "Launching SettingsActivity Intent");
            Intent settingsActivityIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivityIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(int position) {

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            Fragment oldFragment = fm.findFragmentById(R.id.movie_detail_container);
            if (oldFragment != null) ft.remove(oldFragment);

            Bundle arguments = new Bundle();
            arguments.putInt(Utils.MOVIE_KEY, position);
            Fragment newFragment = new DetailActivityFragment();
            newFragment.setArguments(arguments);

            ft.add(R.id.movie_detail_container, newFragment, DETAIL_FRAGMENT_TAG);
            ft.show(newFragment);
            ft.commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(Utils.MOVIE_KEY, position);
            startActivity(intent);
        }
        Log.v(LOG_TAG, "***mTwoPane=" + mTwoPane);
   }
}
