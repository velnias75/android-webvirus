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
 *  Last modified 05.11.19 10:16 by heiko
 */

package de.rangun.webvirus.model;

class MovieFilenameFactory {

    private static MovieFilenameFactory ourInstance = null;

    private MovieFilenameFactory() {}

    static MovieFilenameFactory instance() {

        if(ourInstance == null) ourInstance = new MovieFilenameFactory();

        return ourInstance;
    }

    public IMovieFilename createFilename(IMovie movie, String filename) {

        if(filename != null) {

            final int idx = filename.indexOf(movie.title());

            if(idx != -1) return new MovieFilenameByTitle(movie, filename, idx,
                    idx + movie.title().length());
        }

        return new MovieFileNameString(filename);
    }
}
