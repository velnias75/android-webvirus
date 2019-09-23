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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MovieList extends ArrayList<IMovie> {

    private List<String> titles = null;

    public IMovie getByMovieId(long id) {

        for (int idx = 0; idx < size(); ++idx) {
            IMovie m = get(idx);
            if (m.id() == id) {
                return m;
            }
        }

        return get(0);
    }

    public List<String> titles() {

        if(titles == null) {
            titles = new ArrayList<>();
            for (int idx = 0; idx < size(); ++idx) {
                titles.add(get(idx).title());
            }
        }

        return Collections.unmodifiableList(titles);
    }

    public IMovie findByTitle(String text) {

        for (int idx = 0; idx < size(); ++idx) {

            IMovie m = get(idx);

            if (text.equalsIgnoreCase(m.title())) {
                return m;
            }
        }

        return null;
    }
}
