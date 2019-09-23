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

import java.util.List;

final class MovieProxy implements IMovie {

    private final long id;
    private final Long oid;
    private final boolean top250;
    private IMovie movie = null;
    private final String title;
    private final String duration;
    private final String disc;
    private final IMoviesAvailable cb;

    public MovieProxy(IMoviesAvailable cb, long id, String title, String dur_str, String disc,
                      boolean top250, Long oid) {
        this.id = id;
        this.oid = oid;
        this.title = title;
        this.duration = dur_str;
        this.disc = disc;
        this.top250 = top250;
        this.cb = cb;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public Long oid() {
        return oid;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public long duration() {
        return 0;
    }

    @Override
    public String durationString() {
        return duration;
    }

    @Override
    public List<String> languages() {
        return null;
    }

    @Override
    public String disc() {
        return disc;
    }

    @Override
    public int category() {
        return 0;
    }

    @Override
    public String filename() {
        return null;
    }

    @Override
    public boolean omu() {
        return false;
    }

    @Override
    public boolean top250() {
        return top250;
    }

    @Override
    public String description() {

        if(movie == null) {
            movie = new Movie(this, cb);
        }

        return movie.description();
    }
}
