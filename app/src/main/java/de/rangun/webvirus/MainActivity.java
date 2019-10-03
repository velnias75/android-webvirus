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
 *  Last modified 02.10.19 13:52 by heiko
 */

package de.rangun.webvirus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.toolbox.StringRequest;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.rangun.webvirus.MovieFetcherService.NOTIFICATION;
import de.rangun.webvirus.fragments.IMovieUpdateRequestListener;
import de.rangun.webvirus.fragments.MovieDetailsFragment;
import de.rangun.webvirus.fragments.MovieListFragment;
import de.rangun.webvirus.fragments.SearchBarFragment;
import de.rangun.webvirus.model.IMovie;
import de.rangun.webvirus.model.MovieBKTree;
import de.rangun.webvirus.model.MovieBKTreeAdapter;
import de.rangun.webvirus.model.MovieFactory;

import static java.lang.Math.ceil;
import static java.lang.Math.log;

public class MainActivity extends AppCompatActivity implements
        MovieFactory.IMoviesAvailableListener,
        IMovieUpdateRequestListener,
        MovieBKTreeAdapter.IFilterResultListener,
        MovieDetailsFragment.IResumeListener {

    private static final String TAG = "MainActivity";
    private static final double LN10 = log(10);

    private final class MoviePagerAdapter extends FragmentPagerAdapter {

        private int pages = 2;

        MoviePagerAdapter(@NonNull FragmentManager fm) { super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT); }

        @Override
        @NotNull
        public Fragment getItem(int position) {

            if(position == 0) {
                return new MovieDetailsFragment();
            } else if(position == 1) {
                return new MovieListFragment(false);
            } else {
                return new MovieListFragment(true);
            }
        }

        @Override
        public int getCount() { return pages; }

        void setHasNewMovies() {
            pages = 3;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CharSequence getPageTitle(int position) {

            if(position == 0) {
                return getString(R.string.tab_details);
            } else if(position == 1) {
                return getString(R.string.tab_list);
            } else {
                return getString(R.string.tab_new);
            }
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            final MovieFetcherService.MovieFetcherBinder binder =
                    (MovieFetcherService.MovieFetcherBinder)service;

            mfs = binder.getService();
            mfs.setOnMoviesAvailableListener(MainActivity.this);
            mfs.fetchMovies(false);

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private boolean mBound = false;
    private ArrayList<IMovie> newMoviesList;

    @Nullable
    private Long currentId = null;
    private MovieFetcherService mfs;

    @Nullable
    private MovieBKTree movies = null;

    private ViewPager pager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            currentId = savedInstanceState.getLong("currentId", -1L);

            if (currentId == -1L) currentId = null;
        }

        setContentView(R.layout.activity_main);

        pager = findViewById(R.id.pager);

        final PagerAdapter pagerAdaper = new MoviePagerAdapter(getSupportFragmentManager());

        pager.setAdapter(pagerAdaper);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                final SearchBarFragment sbf = (SearchBarFragment) getSupportFragmentManager().
                                findFragmentById(R.id.searchBar);

                if(sbf != null) sbf.setShowDropdown(position == 0);
            }
        });

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);
    }

    @Override
    protected void onStart() {

        super.onStart();

        final Intent intent = new Intent(this, MovieFetcherService.class);
        if(!bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            Log.d(TAG, "Couldn't bind to MovieFetcherService");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbarmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_reload) {
            if(mBound) mfs.fetchMovies(false);
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

        if(currentId != null) {
            final SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(this);
            sharedPrefs.edit().putLong("lastMovieIdSeen", currentId).apply();
        }
    }

    @Override
    public void onBackPressed() {
        if(pager.getCurrentItem() == 0) {
            moveTaskToBack(true);
        } else {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onUpdateMovieByTitleOrId(@NonNull String text, @NonNull Fragment f) {
        onUpdateMovie(movies != null ? movies.findByTitleOrId(text) : null, f);
    }

    @Override
    public void onUpdateMovie(IMovie m, @NonNull Fragment f) {

        if(mBound) {

            if (movies != null) {
                updateMovie(m, (MovieDetailsFragment)getSupportFragmentManager().
                        findFragmentByTag("android:switcher:" + R.id.pager + ":0"));
            } else if(f instanceof SearchBarFragment) {
                ((SearchBarFragment)f).hideSoftKeyboard();
            }
        }
    }

    private void updateMovie(long id, MovieDetailsFragment mdf) {

        if(mBound) {

            try {
                updateMovie(Objects.requireNonNull(movies).getByMovieId(id), mdf);
            } catch (IndexOutOfBoundsException ex) {
                Log.d(TAG, "Exc: " + ex.getMessage());
            }
        }
    }

    private void updateMovie(@Nullable IMovie m, MovieDetailsFragment mdf) {

        final SearchBarFragment sbf =
                (SearchBarFragment) getSupportFragmentManager().findFragmentById(R.id.searchBar);

        if(sbf != null) sbf.hideSoftKeyboard();

        pager.setCurrentItem(0);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        if(m == null) {

            if(mdf != null) mdf.setVisibility(View.GONE);

            error(getString(R.string.notfound),
                    NOTIFICATION.NOTFOUND);
            return;

        } else if(mdf != null) mdf.setVisibility(View.VISIBLE);

        currentId = m.id();

        if(mdf != null && mBound) mdf.setContents(m, mfs.getQueue(),
                Objects.requireNonNull(movies).size());

        if(sbf != null) sbf.setText(Objects.requireNonNull(m.title()));
    }

    @Override
    public void updateRequested(MovieDetailsFragment f) {
        if(movies != null) {

            final SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(this);

            updateMovie(currentId != null ? currentId :
                    sharedPrefs.getLong("lastMovieIdSeen", 1L), f);
        }
    }

    @Override
    public void loading(boolean silent) {
        if(!silent) setStatus();
    }

    @Override
    public void loaded(int num, boolean silent) {

        setStatus(getString(R.string.loaded, num), NOTIFICATION.LOADED);
        if(mBound && !silent) setStatus(getString(R.string.loaded, Objects.requireNonNull(movies).size()));

        updateRequested((MovieDetailsFragment)getSupportFragmentManager().
                findFragmentByTag("android:switcher:" + R.id.pager + ":0"));
    }

    public static String makeIdString(long num, int base) {

        final StringBuilder preZeros = new StringBuilder();
        final int count = (int)ceil(log(base) / LN10);

        for(int i = 0; i < count; ++i) preZeros.append('0');

        preZeros.append(num);

        return "#" + preZeros.toString().substring(preZeros.length() - count);
    }

    @Override
    public void movies(@NonNull MovieBKTree m, Long lid, boolean silent) {

        if(mBound) {

            movies = new MovieBKTree(m);

            Log.d(TAG, "latest Cover id=" + lid);

            final SearchBarFragment sbf =
                (SearchBarFragment)getSupportFragmentManager().findFragmentById(R.id.searchBar);

            if(sbf != null) {

                final ArrayAdapter<String> adapter = new MovieBKTreeAdapter(this,
                        movies, this);

                sbf.populateCompleter(adapter);
            }
        }
    }

    @Override
    public void newMoviesAvailable(int num) {

        if(num > 0 && movies != null) {

            ((MoviePagerAdapter) Objects.requireNonNull(pager.getAdapter())).setHasNewMovies();

            newMoviesList = new ArrayList<>(movies.size());

            for(IMovie m : movies) if (m.isNewMovie()) newMoviesList.add(m);

            newMoviesList.trimToSize();
            Collections.sort(newMoviesList);
        }
    }

    @Override
    public void onRequestNewMoviesUpdate(MovieListFragment f) {

        List<IMovie> nml = newMoviesList != null ? newMoviesList : new ArrayList<>();

        f.setListAdapter(new MovieListFragment.Adapter(this, nml,
                Objects.requireNonNull(movies).size(), true));
    }

    @Override
    public void onFilterResultAvailable(List<IMovie> result) {

        final MovieListFragment mlf = (MovieListFragment)getSupportFragmentManager().
                findFragmentByTag("android:switcher:" + R.id.pager + ":1");

        if(mlf != null) mlf.setListAdapter(new MovieListFragment.Adapter(this,
                    filteredOrAllMovies(result), Objects.requireNonNull(movies).size(),
                false));
    }

    private List<IMovie> filteredOrAllMovies(List<IMovie> input) {

        final ArrayList<IMovie> r = new ArrayList<>(Objects.requireNonNull(movies).size());

        if(!input.isEmpty()) {
            for(IMovie m: input) if(!m.isDummy()) r.add(m);
        } else {
            for(IMovie m: movies) r.add(m);
            Collections.sort(r);
        }

        r.trimToSize();

        return r;
    }

    @Override
    public void error(String msg) {
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        error(getString(R.string.network_error, msg), NOTIFICATION.ERROR);
    }

    private void error(String msg, @NonNull NOTIFICATION notification) {
        setStatus(msg, Color.RED, notification);
    }

    private void setStatus() {
        setStatus(getString(R.string.loading), Color.GRAY, NOTIFICATION.NONE);
    }

    private void setStatus(String txt) {
        setStatus(txt, Color.GRAY, NOTIFICATION.NONE);
    }

    private void setStatus(String txt, @NonNull NOTIFICATION notification) {
        setStatus(txt, Color.GRAY, notification);
    }

// --Commented out by Inspection START (28.09.19 03:48):
//    private void setStatus(int resource, NOTIFICATION notification) {
//        setStatus(getString(resource), Color.GRAY, notification);
//    }
// --Commented out by Inspection STOP (28.09.19 03:48)

    private void setStatus(String txt, int color, @NonNull NOTIFICATION notification) {

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
