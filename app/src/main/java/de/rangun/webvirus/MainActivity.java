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

import de.rangun.webvirus.fragments.SearchBarFragment;
import de.rangun.webvirus.model.BitmapMemCache;
import de.rangun.webvirus.model.IMovie;
import de.rangun.webvirus.model.IMoviesAvailable;
import de.rangun.webvirus.model.MovieFactory;
import de.rangun.webvirus.model.MovieList;

import static java.lang.Math.ceil;
import static java.lang.Math.log;

public class MainActivity extends AppCompatActivity implements IMoviesAvailable,
        SearchBarFragment.iface {

    private static final String TAG = "MainActivity";
    private static final double LN10 = log(10);

    private RequestQueue queue = null;
    private TextView status = null;
    private MovieList movies = null;
    private StringBuilder preZeros = null;
    private Long currentId = null;

    public MainActivity() {
        Log.d(TAG, "MainActivity()");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");

        if(savedInstanceState != null) {
            currentId = savedInstanceState.getLong("currentId", -1L);

            if (currentId == -1L) currentId = null;
        }

        queue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);

        final NetworkImageView cov = findViewById(R.id.cover);
        cov.setDefaultImageResId(R.drawable.nocover);

        if(movies == null) MovieFactory.instance(this).allMovies(queue);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        Log.d(TAG, "onSaveInstanceState()");
        if(currentId != null) outState.putLong("currentId", currentId);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        Log.d(TAG, "onRestoreInstanceState()");
        currentId = savedInstanceState.getLong("currentId", -1L);

        if(currentId == -1L) currentId = null;
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop(): cur: " + currentId);

        if (queue != null) {
            queue.cancelAll(MovieFactory.instance(this).tag());
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void updateMovieByTitle(String title) {
        if(movies != null) {
            updateMovie(movies.findByTitle(title));
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

        if(m == null) {
            status.setText(R.string.notfound);
            return;
        }

        Log.d(TAG, "updateMovie(): id=" + currentId + "; t=" + m.title());

        final View top250 = findViewById(R.id.top250);
        final TextView mid = findViewById(R.id.m_id);
        final TextView tit = findViewById(R.id.m_title);
        final TextView dus = findViewById(R.id.m_duration);
        final TextView dis = findViewById(R.id.m_disc);
        final TextView abs = findViewById(R.id.m_abstract);
        final NetworkImageView cov = findViewById(R.id.cover);
        final CustomAutoCompleteTextView srt = findViewById(R.id.searchTerm);

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
        abs.setText(m.description());

        final InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(srt.getWindowToken(), 0);

        if (m.oid() != null) {

            cov.setImageUrl("https://rangun.de/db/omdb.php?cover-oid=" + m.oid() +
                            (m.top250() ? "&top250=true" : ""),
                    new ImageLoader(queue, new BitmapMemCache()));
        } else {
            cov.setImageUrl(null, null);
        }

        Log.d(TAG, "updateMovie(): id=" + currentId + "; t=" + m.title() + " - updated");
    }

    @Override
    public void loading() {
        status.setText(R.string.loading);
    }

    @Override
    public void loaded(int num) {

        status.setText("" + num + " hirnlose Schrott- oder Rentnerfilme geladen.");

        preZeros = new StringBuilder();

        for (int i = 0; i < ceil(log(num) / LN10); ++i) {
            preZeros.append('0');
        }

        Log.d(TAG, "loaded(): cur: " + currentId);
        updateMovie(currentId != null ? currentId : 1L);
    }

    @Override
    public void movies(MovieList ml) {

        movies = ml;

        ((SearchBarFragment)getSupportFragmentManager().findFragmentById(R.id.searchBar)).
                populateCompleter(new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        movies.titles()));

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
