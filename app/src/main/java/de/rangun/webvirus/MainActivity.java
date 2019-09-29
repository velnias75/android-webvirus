/*
 * Copyright 2019 by Heiko Schäfer <heiko@rangun.de>
 *
 *  This file is part of android-webvirus.
 *
 *  android-webvirus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  android-webvirus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with android-webvirus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Last modified 23.09.19 06:31 by heiko
 */

package de.rangun.webvirus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.StringRequest;

import de.rangun.webvirus.MovieFetcherService.NOTIFICATION;
import de.rangun.webvirus.fragments.MovieDetailsFragment;
import de.rangun.webvirus.fragments.SearchBarFragment;
import de.rangun.webvirus.model.IMovie;
import de.rangun.webvirus.model.MovieBKTree;
import de.rangun.webvirus.model.MovieBKTreeAdapter;
import de.rangun.webvirus.model.MovieFactory;

import static java.lang.Math.ceil;
import static java.lang.Math.log;

public class MainActivity extends AppCompatActivity implements
        MovieFactory.OnMoviesAvailableListener,
        SearchBarFragment.OnMovieUpdateRequestListener {

    private static final String TAG = "MainActivity";
    private static final double LN10 = log(10);

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            MovieFetcherService.MovieFetcherBinder binder =
                    (MovieFetcherService.MovieFetcherBinder)service;

            mfs = binder.getService();
            mfs.setOnMoviesAvailableListener(MainActivity.this);
            mfs.fetchMovies();

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private boolean mBound = false;
    private StringBuilder preZeros = null;
    private Long currentId = null;
    private MovieFetcherService mfs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            currentId = savedInstanceState.getLong("currentId", -1L);

            if (currentId == -1L) currentId = null;
        }

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {

        super.onStart();

        Intent intent = new Intent(this, MovieFetcherService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbarmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_reload) {
            if(mBound) mfs.fetchMovies();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        if(currentId != null) outState.putLong("currentId", currentId);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        currentId = savedInstanceState.getLong("currentId", -1L);

        if(currentId == -1L) currentId = null;
    }

    @Override
    protected void onStop() {

        super.onStop();

        unbindService(connection);
        mBound = false;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onUpdateMovieByTitleOrId(String text, SearchBarFragment sbf) {

        if(mBound) {

            final MovieBKTree movies = mfs.getMovies();

            if (movies != null) {
                updateMovie(movies.findByTitleOrId(text));
            } else {
                sbf.hideSoftKeyboard();
            }
        }
    }

    private void updateMovie(long id) {

        if(mBound) {

            final MovieBKTree movies = mfs.getMovies();

            try {
                updateMovie(movies.getByMovieId(id));
            } catch (IndexOutOfBoundsException ex) {
                Log.d(TAG, "Exc: " + ex.getMessage());
            }
        }
    }

    private void updateMovie(IMovie m) {

        final SearchBarFragment sbf =
                (SearchBarFragment) getSupportFragmentManager().
                        findFragmentById(R.id.searchBar);

        final MovieDetailsFragment mdf =
                (MovieDetailsFragment) getSupportFragmentManager().
                        findFragmentById(R.id.moviedetailsfragment);

        if(sbf != null) sbf.hideSoftKeyboard();

        if(m == null) {
            if(mdf != null) mdf.setVisibility(View.GONE);
            error(getString(R.string.notfound),
                    NOTIFICATION.NOTFOUND);
            return;
        } else {

            if(mdf != null) mdf.setVisibility(View.VISIBLE);

            if(mBound) {
                final MovieBKTree movies = mfs.getMovies();
                setStatus(getString(R.string.loaded, movies.size()));
            }
        }

        currentId = m.id();

        if(mdf != null && mBound) mdf.setContents(m, mfs.getQueue(), preZeros.toString());

        if(sbf != null) sbf.setText(m.title());
    }

    @Override
    public void loading() {
        setStatus(R.string.loading);
    }

    @Override
    public void loaded(int num) {

        setStatus(getString(R.string.loaded, num), NOTIFICATION.LOADED);

        preZeros = new StringBuilder();

        for (int i = 0; i < ceil(log(num) / LN10); ++i) preZeros.append('0');

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        updateMovie(currentId != null ? currentId : 1L);
    }

    @Override
    public void movies(MovieBKTree m) {

        if(mBound) {

            final SearchBarFragment sbf =
                (SearchBarFragment)getSupportFragmentManager().findFragmentById(R.id.searchBar);

            if(sbf != null) {

                final ArrayAdapter<String> adapter = new MovieBKTreeAdapter(this,
                        R.layout.searchsuggestions, mfs.getMovies());

                sbf.populateCompleter(adapter);
            }
        }
    }

    @Override
    public void error(String msg) {
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        error(getString(R.string.network_error, msg), NOTIFICATION.ERROR);
    }

    private void error(String msg, NOTIFICATION notification) {
        setStatus(msg, Color.RED, notification);
    }

    private void setStatus(int resource) {
        setStatus(getString(resource), Color.GRAY, NOTIFICATION.NONE);
    }

    private void setStatus(String txt) {
        setStatus(txt, Color.GRAY, NOTIFICATION.NONE);
    }

    private void setStatus(String txt, NOTIFICATION notification) {
        setStatus(txt, Color.GRAY, notification);
    }

// --Commented out by Inspection START (28.09.19 03:48):
//    private void setStatus(int resource, NOTIFICATION notification) {
//        setStatus(getString(resource), Color.GRAY, notification);
//    }
// --Commented out by Inspection STOP (28.09.19 03:48)

    private void setStatus(String txt, int color, NOTIFICATION notification) {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                findViewById(R.id.custom_toast_container));

        TextView text = layout.findViewById(R.id.text);
        text.setText(Html.fromHtml(txt));
        text.setTextColor(color);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

        if(NOTIFICATION.NONE != notification) {
            if(mBound) mfs.notify(txt, notification);
        }
    }

    @Override
    public void fetchDescription(StringRequest rq) {}

    @Override
    public void descriptionAvailable(String dsc) {
        ((TextView)findViewById(R.id.m_abstract)).setText(dsc);
    }
}
