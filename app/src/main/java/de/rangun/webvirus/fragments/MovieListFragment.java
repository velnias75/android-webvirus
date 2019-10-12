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
 *  Last modified 02.10.19 13:13 by heiko
 */

package de.rangun.webvirus.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.rangun.webvirus.MainActivity;
import de.rangun.webvirus.R;
import de.rangun.webvirus.model.IMovie;
import de.rangun.webvirus.model.db.AppDatabase;
import de.rangun.webvirus.model.db.Movie;
import de.rangun.webvirus.widgets.CategoryTextView;
import de.rangun.webvirus.widgets.MarkerImageView;

public final class MovieListFragment extends ListFragment {

    private final boolean newMovies;

    public MovieListFragment(boolean newMovies) {

        super();
        this.newMovies = newMovies;
    }

    public static class Adapter extends ArrayAdapter<IMovie> {

        private final static class MarkedMovie {

            final IMovie m;
            int marker;

            MarkedMovie(IMovie m) {
                this.m = m;
                this.marker = 1;
            }
        }

        private final List<MarkedMovie> movies;
        private final int movieCount;
        private final boolean newMovies;

        private final int pad2;
        private final int pad8;

        final static class _fetchAll extends AsyncTask<Void, Void, List<Movie>> {

            @NonNull
            private final Adapter adapter;

            @NonNull
            private final AppDatabase db;

            @NonNull
            private final List<MarkedMovie> mm;

            _fetchAll(@NonNull Adapter adapter, @NonNull AppDatabase db,
                      @NonNull List<MarkedMovie> mm) {

                this.adapter = adapter;
                this.db = db;
                this.mm = mm;
            }

            @Override
            protected List<Movie> doInBackground(Void... voids) {
                return db.moviesDao().fetchAll();
            }

            @Override
            protected void onPostExecute(List<Movie> movies) {

                for(Movie m: movies) {
                    for(MarkedMovie markedMovie: mm) {
                        if(markedMovie.m.id() == m.id) markedMovie.marker = m.marker;
                    }
                }

                adapter.notifyDataSetChanged();
            }
        }

        public Adapter(@NonNull Context context, @NonNull List<IMovie> movies, int movieCount,
                       boolean newMovies, @NonNull AppDatabase db) {

            super(context, R.layout.searchsuggestions, R.id.title, movies);

            this.movieCount = movieCount;
            this.newMovies = newMovies;

            this.movies = new ArrayList<>(movies.size());

            for(IMovie m: movies) this.movies.add(new MarkedMovie(m));

            (new _fetchAll(this, db, this.movies)).execute();

            pad2 = (int)(2 * context.getResources().getDisplayMetrics().density + 0.5f);
            pad8 = (int)(8 * context.getResources().getDisplayMetrics().density + 0.5f);
        }

        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            final MarkedMovie m = movies.get(position);
            final View v = super.getView(position, convertView, parent);

            MarkerImageView iv = v.findViewById(R.id.icon);

            if(newMovies) {
                iv.setVisibility(View.GONE);
            } else {
                iv.setMarker(m.marker);
            }

            final CategoryTextView tv = v.findViewById(R.id.title);
            final int c = Color.parseColor(position % 2 == 0 ? "#CCCCCC" : "#BBBBBB");

            tv.setPadding(pad8, pad2, pad8, pad2);
            tv.setText(MainActivity.makeIdString(m.m.id(), movieCount) + " – " + m.m.title());
            tv.setTextColorByCategory(m.m.category());

            v.setBackgroundColor(c);

            if(m.m.isNewMovie() && !newMovies) tv.setTypeface(tv.getTypeface(),
                    Typeface.BOLD_ITALIC);

            return v;
        }
    }

    private IMovieUpdateRequestListener listener;

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        try {
            listener = (IMovieUpdateRequestListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement IMovieUpdateRequestListener");
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.movielistfragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(newMovies) listener.onRequestNewMoviesUpdate(this);
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        if(listener != null) {
            listener.onUpdateMovie(((IMovie) Objects.
                    requireNonNull(getListAdapter()).getItem(position)), this);
        }
    }
}
