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
 *  Last modified 05.11.19 11:18 by heiko
 */

package de.rangun.webvirus.model;

abstract class AbstractMovieFilename implements IMovieFilename {

    private final static String MP4 = ".mp4";

    private final String suf;
    private final String filename;

    AbstractMovieFilename(String filename) {

        if(filename != null && filename.endsWith(MP4)) {
            this.filename = filename.substring(0, filename.length() - 4);
        } else {
            this.filename = filename;
        }

        this.suf = filename != null && filename.endsWith(MP4) ? MP4 : null;

    }

    String suffix() { return suf != null ? suf : ""; }

    final String filename() { return filename; }
}
