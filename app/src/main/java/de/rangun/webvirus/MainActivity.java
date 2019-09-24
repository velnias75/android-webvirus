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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import de.rangun.webvirus.fragments.MovieDetailsFragment;
import de.rangun.webvirus.fragments.SearchBarFragment;
import de.rangun.webvirus.model.BitmapMemCache;
import de.rangun.webvirus.model.IMovie;
import de.rangun.webvirus.model.MovieFactory;
import de.rangun.webvirus.model.MovieList;

import static java.lang.Math.ceil;
import static java.lang.Math.log;

public class MainActivity extends AppCompatActivity implements
        MovieFactory.OnMoviesAvailableListener,
        SearchBarFragment.OnMovieUpdateRequestListener {

    private static final String TAG = "MainActivity";
    private static final double LN10 = log(10);

    private RequestQueue queue = null;
    private TextView status = null;
    private MovieList movies = null;
    private StringBuilder preZeros = null;
    private Long currentId = null;

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
    protected void onDestroy() {

        queue.stop();
        movies = null;

        super.onDestroy();
    }

    @Override
    protected void onResume() {

        super.onResume();

        status = findViewById(R.id.status);

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if(activeNetwork != null && activeNetwork.isConnected()) {

            if (queue == null) queue = Volley.newRequestQueue(this);

            if (movies == null) {
                MovieFactory.instance().setOnMoviesAvailableListener(this);
                MovieFactory.instance().fetchMovies(queue);
            }

        } else {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            status.setText(R.string.not_connected);
        }
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

        if (queue != null) {
            queue.cancelAll(MovieFactory.instance().tag());
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onUpdateMovieByTitle(String title) {

        if(movies != null) {
            updateMovie(movies.findByTitle(title));
        } else {
            hideSoftKeyboard();
        }
    }

    private void updateMovie(long id) {
        try {
            updateMovie(movies.getByMovieId(id));
        } catch (IndexOutOfBoundsException ex) {
            Log.d(TAG, "Exc: " + ex.getMessage());
        }
    }

    private void updateMovie(IMovie m) {

        final SearchBarFragment sbf = hideSoftKeyboard();
        final MovieDetailsFragment mdf =
                (MovieDetailsFragment) getSupportFragmentManager().
                        findFragmentById(R.id.moviedetailsfragment);

        if(m == null) {
            mdf.getView().setVisibility(View.GONE);
            status.setText(R.string.notfound);
            return;
        } else {
            mdf.getView().setVisibility(View.VISIBLE);
            status.setText(getString(R.string.loaded, movies.size()));
        }

        final View top250 = mdf.getView().findViewById(R.id.top250);
        final TextView mid = mdf.getView().findViewById(R.id.m_id);
        final TextView tit = mdf.getView().findViewById(R.id.title);
        final TextView dus = mdf.getView().findViewById(R.id.m_duration);
        final TextView dis = mdf.getView().findViewById(R.id.m_disc);
        final TextView abs = mdf.getView().findViewById(R.id.m_abstract);
        final NetworkImageView cov = mdf.getView().findViewById(R.id.cover);
        final CustomAutoCompleteTextView srt = sbf.getView().findViewById(R.id.searchTerm);

        currentId = m.id();

        final String idNum = preZeros.toString() + m.id();

        top250.setVisibility(m.top250() ? View.VISIBLE : View.INVISIBLE);
        mid.setText(idNum.substring(idNum.length() - preZeros.length()));
        tit.setText(m.title());
        srt.setText(m.title());
        srt.setSelection(m.title().length());
        srt.dismissDropDown();
        dus.setText(m.durationString());
        dis.setText(m.disc());
        abs.setText(m.description(this));

        if (m.oid() != null) {

            cov.setImageUrl("https://rangun.de/db/omdb.php?cover-oid=" + m.oid() +
                            (m.top250() ? "&top250=true" : ""),
                    new ImageLoader(queue, new BitmapMemCache()));
        } else {
            cov.setImageUrl(null, null);
        }
    }

    private SearchBarFragment hideSoftKeyboard() {

        final SearchBarFragment sbf =
                (SearchBarFragment) getSupportFragmentManager().
                        findFragmentById(R.id.searchBar);

        final CustomAutoCompleteTextView srt = sbf.getView().findViewById(R.id.searchTerm);
        final InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if(imm != null) imm.hideSoftInputFromWindow(srt.getWindowToken(), 0);

        return sbf;
    }

    @Override
    public void loading() {
        status.setText(R.string.loading);
    }

    @Override
    public void loaded(int num) {

        status.setText(getString(R.string.loaded, num));

        preZeros = new StringBuilder();

        for (int i = 0; i < ceil(log(num) / LN10); ++i) {
            preZeros.append('0');
        }

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        updateMovie(currentId != null ? currentId : 1L);
    }

    @Override
    public void movies(MovieList ml) {

        movies = ml;

        final SearchBarFragment sbf =
                (SearchBarFragment)getSupportFragmentManager().findFragmentById(R.id.searchBar);

        if(sbf != null) {
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, movies.titles());

            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

            sbf.populateCompleter(adapter);
        }

    }

    @Override
    public void error(String localizedMessage) {
        status.setText(localizedMessage);
    }

    @Override
    public void fetchDescription(StringRequest rq) {
        queue.add(rq);
    }

    @Override
    public void descriptionAvailable(String dsc) {
        ((TextView)findViewById(R.id.m_abstract)).setText(dsc);
    }
}
