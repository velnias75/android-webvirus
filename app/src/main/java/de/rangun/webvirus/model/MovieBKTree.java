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

package de.rangun.webvirus.model;

import jregex.Matcher;
import jregex.Pattern;

public final class MovieBKTree extends BKTree<IMovie> {

    private final static Pattern idSearch = new Pattern("#(\\d+)");

    static boolean isSpecialSearch(String text) {
        return ('#' == text.charAt(0) && MovieBKTree.idSearch.matches(text));
    }

    static Long extractId(String text) {

        final Matcher idMatcher = MovieBKTree.idSearch.matcher(text);

        if(idMatcher.matches()) return Long.parseLong(idMatcher.group(1));

        return null;
    }

    @Override
    void add(IMovie item) {
        super.add(item);
    }

    public IMovie getByMovieId(long id) {
        return getByMovieId(id, true);
    }

    private IMovie getByMovieId(long id, boolean returnRoot) {

        for(IMovie m: this) {
            if (m.id() == id) {
                return m;
            }
        }

        return returnRoot ? getRootItem() : null;
    }

    public IMovie findByTitleOrId(String text) {

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
