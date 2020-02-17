/*
 * Copyright 2019-2020 by Heiko Schäfer <heiko@rangun.de>
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
 *  Last modified 06.11.19 01:40 by heiko
 */

package de.rangun.webvirus;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.codec.language.ColognePhonetic;
import org.apache.commons.codec.language.Soundex;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.rangun.webvirus.model.bktree.MovieBKTree;
import de.rangun.webvirus.model.movie.DummyMovie;
import de.rangun.webvirus.model.movie.IMovie;
import de.rangun.webvirus.model.movie.util.TitleNormalizer;
import de.rangun.webvirus.widgets.CategoryTextView;
import jregex.Matcher;
import jregex.Pattern;
import jregex.PatternSyntaxException;
import jregex.REFlags;

final class MovieBKTreeAdapter extends ArrayAdapter<String> {

    public interface IFilterResultListener {
        void onFilterResultAvailable(List<IMovie> result);
    }

    private final static ColognePhonetic cp = new ColognePhonetic();
    private final static Soundex se = new Soundex();

    private final static Pattern rexSearch = new Pattern("/([^/]+)/",
            REFlags.IGNORE_CASE|REFlags.UNICODE);

    private final IFilterResultListener listener;
    private final MovieBKTree movies;

    private List<IMovie> filtered;

    @Nullable
    private Integer separatorPos = null;

    public MovieBKTreeAdapter(@NonNull Context context, MovieBKTree movies,
                              IFilterResultListener listener) {

        super(context, R.layout.searchsuggestions, R.id.title);

        this.movies = movies;
        filtered = this.movies.asList();
        this.listener = listener;
    }

    @Override
    public boolean isEnabled(int position) {

        if(separatorPos != null && separatorPos == position) {
            return false;
        }

        return super.isEnabled(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {

        final View v = super.getView(position, convertView, parent);
        final ImageView iv = v.findViewById(R.id.icon);
        final CategoryTextView tv = v.findViewById(R.id.title);

        iv.setVisibility(View.GONE);

        if(separatorPos != null && position == separatorPos) {
            tv.setTextColor(Color.GRAY);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        } else {
            tv.setTextColorByCategory(filtered.get(position).category());
            tv.setGravity(Gravity.START);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }

        return v;
    }

    @Override
    public int getCount() { return filtered != null ? filtered.size() : 0; }

    @NonNull
    @Override
    public String getItem(int position) { return filtered.get(position).title(); }

    @Override
    public long getItemId(int position) { return position; }

    @NonNull
    @Override
    public Filter getFilter() {

        return new Filter() {

            @NonNull
            @Override
            protected FilterResults performFiltering(@Nullable CharSequence constraint) {

                final FilterResults fr = new FilterResults();

                if(constraint != null && constraint.length() > 0) {

                    final String  lowerConstraint = constraint.toString().toLowerCase();
                    final boolean isSpecialSearch = MovieBKTree.isSpecialSearch(lowerConstraint);
                    final String normalConstraint = TitleNormalizer.normalize(lowerConstraint);

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
                            movies.search(normalConstraint,normalConstraint.length() >> 1) :
                            new ArrayList<>();

                    if(rexConstraint == null) {

                        for (IMovie s : movies) {
                            if((isSpecialSearch && lid != null && lid.equals(s.id()))
                                    || MovieBKTree.toLowerCase(s.title().toCharArray()).
                                    contains(lowerConstraint)) {
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
                            Matcher rxMatcher = rexConstraint.
                                    matcher(MovieBKTree.toLowerCase(s.title().toCharArray()));
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
                    if(((ArrayList<IMovie>) fr.values).isEmpty()) {

                        final String cps = cp.colognePhonetic(lowerConstraint);
                        final String ses = noThrowSoundex(lowerConstraint);

                        boolean firstCologne = false;

                        for(IMovie m: movies) {

                            if(cps.equals(cp.colognePhonetic(m.title().toLowerCase())) ||
                                    (!ses.isEmpty() &&
                                            noThrowSoundex(m.title()).equalsIgnoreCase(ses))) {

                                if(!firstCologne) {
                                    //noinspection unchecked
                                    ((List<IMovie>)fr.values).add(new DummyMovie(getContext().
                                            getResources().getString(R.string.colognesuggests)));
                                    separatorPos = 0;
                                    firstCologne = true;
                                }

                                //noinspection unchecked
                                ((List<IMovie>)fr.values).add(m);
                            }
                        }
                    }

                    //noinspection unchecked
                    fr.count = ((List<IMovie>)fr.values).size();

                } else {
                    separatorPos = null;
                    fr.values = null;
                    fr.count  = 0;
                }

                return fr;
            }

            @NonNull
            private String noThrowSoundex(@NonNull String str) {
                try {
                    return se.soundex(str);
                } catch(IllegalArgumentException ex) {
                    return "";
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, @NonNull FilterResults results) {

                //noinspection unchecked
                filtered = (List<IMovie>)results.values;

                notifyDataSetChanged();

                if(listener != null) {
                    listener.onFilterResultAvailable(filtered != null ? filtered :
                            new ArrayList<>());
                }
            }
        };
    }
}
