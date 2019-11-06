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
 *  Last modified 05.11.19 14:24 by heiko
 */

package de.rangun.webvirus.model.movie;

import androidx.annotation.NonNull;

final class MovieFilenameByTitle extends AbstractMovieFilename {

    private final IMovie movie;
    private final String pre;
    private final String suf;

    MovieFilenameByTitle(IMovie movie, @NonNull String filename, int start, int end) {

        super(filename);

        this.movie = movie;
        this.pre = start != 0 ? filename().substring(0, start) : null;
        this.suf = !"".equals(super.suffix()) ? super.suffix() : filename.substring(end);
    }

    @Override
    public String fileName() {
        return (pre != null ? pre : "") + movie.title() + suffix();
    }

    @Override
    String suffix() { return this.suf; }
}
