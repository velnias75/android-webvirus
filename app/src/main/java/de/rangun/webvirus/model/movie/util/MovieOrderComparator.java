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
 *  Last modified 06.11.19 01:23 by heiko
 */

package de.rangun.webvirus.model.movie.util;

import java.io.Serializable;
import java.util.Comparator;

import de.rangun.webvirus.model.movie.IMovie;

public final class MovieOrderComparator implements Comparator<IMovie>, Serializable {

    private static final long serialVersionUID = 1697576161130028002L;

    @Override
    public int compare(IMovie o1, IMovie o2) {
        return Integer.compare(o1.pos(), o2.pos());
    }
}
