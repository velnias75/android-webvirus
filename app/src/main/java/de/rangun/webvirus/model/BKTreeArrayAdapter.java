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

public class BKTreeArrayAdapter extends ArrayAdapter<String> {

    private final BKTree<IMovie> bktree;
    private final List<String>  objects;

    private List<String> filtered;

    public BKTreeArrayAdapter(@NonNull Context context, @LayoutRes int resource,
                              @NonNull List<String> objects, BKTree<IMovie> bkt) {

        super(context, resource, objects);

        this.objects = objects;
        filtered = this.objects;
        this.bktree = bkt;
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

                    fr.values = new ArrayList<String>();

                    final String lowerConstraint = constraint.toString().toLowerCase();

                    final Set<String>  best = new TreeSet<>();
                    final List<String> near = bktree.Search(lowerConstraint,
                            lowerConstraint.length() >> 1);

                    for(String s: objects) {
                        if(s.toLowerCase().contains(lowerConstraint)) best.add(s);
                    }

                    near.removeAll(best);

                    //noinspection unchecked
                    ((List<String>)fr.values).addAll(best);
                    //noinspection unchecked
                    ((List<String>)fr.values).addAll(near);

                    //noinspection unchecked
                    fr.count = ((List<String>)fr.values).size();

                } else {
                    fr.values = objects;
                    fr.count  = objects.size();
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
