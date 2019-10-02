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
 *  Last modified 01.10.19 09:14 by heiko
 */

package de.rangun.webvirus.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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
import de.rangun.webvirus.widgets.CategoryTextView;

public class MovieListFragment extends ListFragment {

    public static class Adapter extends ArrayAdapter<IMovie> {

        private final List<IMovie> movies;
        private final int movieCount;

        public Adapter(@NonNull Context context, @NonNull List<IMovie> movies, int movieCount) {
            super(context, R.layout.searchsuggestions, movies);
            this.movies = movies;
            this.movieCount = movieCount;
        }

        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            final IMovie m = movies.get(position);
            final View v = super.getView(position, convertView, parent);
            final CategoryTextView tv = (CategoryTextView)v;
            final int pad2 = (int)(2 * getContext().getResources().getDisplayMetrics().density +
                    0.5f);
            final int pad8 = (int)(8 * getContext().getResources().getDisplayMetrics().density +
                    0.5f);

            tv.setPadding(pad8, pad2, pad8, pad2);
            tv.setText(MainActivity.makeIdString(m.id(), movieCount) + " – " + m.title());
            tv.setTextColorByCategory(m.category());
            tv.setBackgroundColor(Color.parseColor(position % 2 == 0 ? "#CCCCCC" : "#BBBBBB"));

            return v;
        }
    }

    private ArrayList<IMovie> movies = new ArrayList<>();
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
        return inflater.inflate(R.layout.movielistfragment, container,
                false);
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        if(listener != null) {
            listener.onUpdateMovie(((IMovie) Objects.requireNonNull(getListAdapter()).getItem(position)), this);
        }
    }
}
