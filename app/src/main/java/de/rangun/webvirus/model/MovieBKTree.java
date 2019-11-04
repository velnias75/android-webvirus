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
 *  Last modified 02.10.19 06:21 by heiko
 */

package de.rangun.webvirus.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import jregex.Matcher;
import jregex.Pattern;

public final class MovieBKTree extends BKTree<IMovie> {

    private final static Pattern idSearch = new Pattern("#(\\d+)");
    private final static StringBuilder lcSb = new StringBuilder(4096);

    static boolean isSpecialSearch(@NonNull String text) {
        return !text.isEmpty() && ('#' == text.charAt(0) && MovieBKTree.idSearch.matches(text));
    }

    @Nullable
    static Long extractId(String text) {

        final Matcher idMatcher = MovieBKTree.idSearch.matcher(text);

        if(idMatcher.matches()) return Long.parseLong(idMatcher.group(1));

        return null;
    }

    MovieBKTree() {}

    static synchronized String toLowerCase(char[] s) {

        lcSb.delete(0, lcSb.length());

        for(char c: s) lcSb.append(Character.toLowerCase(c));

        return lcSb.toString();
    }

    @Override
    protected char[] lowerCaseItem(IMovie item) {
        return lowerCaseWord(item.normalizedTitle());
    }

    @Override
    protected char[] lowerCaseWord(String word) {
        return MovieBKTree.toLowerCase(word.toCharArray()).toCharArray();
    }

    @Nullable
    public IMovie getByMovieId(long id) {
        return getByMovieId(id, true);
    }

    @Nullable
    private IMovie getByMovieId(long id, boolean returnRoot) {

        for(IMovie m: this) {
            if (m.id() == id) {
                return m;
            }
        }

        return returnRoot ? getRootItem() : null;
    }

    @Nullable
    public IMovie findByTitleOrId(@NonNull String text) {

        if(!isSpecialSearch(text)) {

            for(IMovie m: this) {
                if (text.equalsIgnoreCase(m.title())) {
                    return m;
                }
            }

        } else {

            final Long lid = extractId(text);

            if(lid != null) return getByMovieId(lid, false);
        }

        return null;
    }
}
