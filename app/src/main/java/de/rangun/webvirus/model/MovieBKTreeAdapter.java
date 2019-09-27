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
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.rangun.webvirus.R;
import de.rangun.webvirus.widgets.CategoryTextView;
import jregex.Matcher;
import jregex.Pattern;
import jregex.PatternSyntaxException;
import jregex.REFlags;

public final class MovieBKTreeAdapter extends ArrayAdapter<String> {

    private final static Pattern rexSearch = new Pattern("/([^/]+)/",
            REFlags.IGNORE_CASE|REFlags.UNICODE);

    private final MovieBKTree movies;
    private List<IMovie> filtered;
    private Integer separatorPos = null;

    public MovieBKTreeAdapter(@NonNull Context context, @LayoutRes int resource,
                              MovieBKTree movies) {

        super(context, resource);

        this.movies = movies;
        filtered = this.movies.asList();
    }

    @Override
    public boolean isEnabled(int position) {

        if(separatorPos != null && separatorPos == position) {
            return false;
        }

        return super.isEnabled(position);
    }

    @NotNull
    @Override
    public View getView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {

        final View v = super.getView(position, convertView, parent);
        final CategoryTextView tv = (CategoryTextView)v;

        if(separatorPos != null && position == separatorPos) {
            tv.setTextColor(Color.GRAY);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        } else {
            tv.setTextColorByCategory(filtered.get(position).category());
            tv.setGravity(Gravity.START);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }

        return v;
    }

    @Override
    public int getCount() {
        return filtered != null ? filtered.size() : 0;
    }

    @Override
    public String getItem(int position) {
        return filtered.get(position).toString();
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

                    final String  lowerConstraint = constraint.toString().toLowerCase();
                    final boolean isSpecialSearch = MovieBKTree.isSpecialSearch(lowerConstraint);

                    Pattern rexConstraint = null;

                    final Matcher rexMatcher = rexSearch.matcher(lowerConstraint);
                    final Long lid = isSpecialSearch ?
                            MovieBKTree.extractId(lowerConstraint) : null;

                    if(rexMatcher.matches()) {
                        try {
                            rexConstraint = new Pattern(".*(" + rexMatcher.group(1) +
                                    ").*");
                        } catch(PatternSyntaxException ex) {
                            Log.i(getClass().getName(),
                                    "provided regular expression pattern", ex);
                        }
                    }

                    final Set<IMovie>  best = rexConstraint == null ? new TreeSet<>() :
                            new LinkedHashSet<>();
                    final List<IMovie> near = (rexConstraint == null && !isSpecialSearch) ?
                            movies.Search(lowerConstraint,lowerConstraint.length() >> 1) :
                            new ArrayList<>();

                    if(rexConstraint == null) {

                        for (IMovie s : movies) {
                            if((isSpecialSearch && lid != null && lid.equals(s.id()))
                                    || s.title().toLowerCase().contains(lowerConstraint)) {
                                best.add(s);
                            }
                        }

                        near.removeAll(best);

                        separatorPos = (near.isEmpty() || isSpecialSearch) ? null : best.size();

                    } else {

                        separatorPos = 0;

                        best.add(new DummyMovie(getContext().getResources().
                                getString(R.string.rexsuggests)));

                        final Set<IMovie> aux = new TreeSet<>();

                        for (IMovie s : movies) {
                            Matcher rxMatcher = rexConstraint.matcher(s.title().toLowerCase());
                            if(rxMatcher.matches()) aux.add(s);
                        }

                        best.addAll(aux);
                    }

                    fr.values = new ArrayList<String>(best.size() + near.size() + 1);

                    //noinspection unchecked
                    ((List<IMovie>)fr.values).addAll(best);

                    if(!near.isEmpty() && rexConstraint == null && !isSpecialSearch) //noinspection unchecked
                        ((List<IMovie>)fr.values).add(new DummyMovie(getContext().
                            getResources().getString(R.string.bksuggests)));

                    //noinspection unchecked
                    ((List<IMovie>)fr.values).addAll(near);

                    //noinspection unchecked
                    ((ArrayList<IMovie>)fr.values).trimToSize();

                    //noinspection unchecked
                    fr.count = ((List<IMovie>)fr.values).size();

                } else {
                    separatorPos = null;
                    fr.values = null;
                    fr.count  = 0;
                }

                return fr;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                filtered = (List<IMovie>)results.values;
                notifyDataSetChanged();
            }
        };
    }
}
