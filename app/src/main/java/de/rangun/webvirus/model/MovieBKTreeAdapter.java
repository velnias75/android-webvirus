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
 *  Last modified 25.09.19 07:54 by heiko
 */

package de.rangun.webvirus.model;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MovieBKTreeAdapter extends ArrayAdapter<String> {

    private final MovieBKTree movies;
    private final List<String> titles;

    private List<String> filtered;

    public MovieBKTreeAdapter(@NonNull Context context, @LayoutRes int resource,
                              MovieBKTree movies) {

        super(context, resource);

        this.movies = movies;
        this.titles = this.movies.titles();
        filtered = this.titles;
    }

    @Override
    public int getCount() {
        return filtered.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return filtered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public Filter getFilter() {

        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                final FilterResults fr = new FilterResults();

                if(constraint != null) {

                    final String lowerConstraint = constraint.toString().toLowerCase();

                    final Set<String>  best = new TreeSet<>();
                    final List<String> near = movies.Search(lowerConstraint,
                            lowerConstraint.length() >> 1);

                    for(String s: titles) {
                        if(s.toLowerCase().contains(lowerConstraint)) best.add(s);
                    }

                    near.removeAll(best);

                    fr.values = new ArrayList<String>(best.size() + near.size());

                    //noinspection unchecked
                    ((List<String>)fr.values).addAll(best);
                    //noinspection unchecked
                    ((List<String>)fr.values).addAll(near);

                    //noinspection unchecked
                    ((ArrayList<String>)fr.values).trimToSize();

                    //noinspection unchecked
                    fr.count = ((List<String>)fr.values).size();

                } else {
                    fr.values = titles;
                    fr.count  = titles.size();
                }

                return fr;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                filtered = (List<String>)results.values;
                notifyDataSetChanged();
            }
        };
    }
}
