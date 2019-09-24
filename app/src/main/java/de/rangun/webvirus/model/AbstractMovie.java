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

abstract class AbstractMovie implements IMovie {

    private final MovieProxy.MovieParameters m;

    AbstractMovie(MovieProxy.MovieParameters m) {
        this.m = m;
    }

    @Override
    public long id() {
        return m.getId();
    }

    @Override
    public Long oid() {
        return m.getOid();
    }

    @Override
    public String title() {
        return m.getTitle();
    }

    @Override
    public long duration() { return m.getDur_sec(); }

    @Override
    public String durationString() {
        return m.getDur_str();
    }

    @Override
    public List<String> languages() {
        throw new IllegalStateException("languages() not implemented yet");
    }

    @Override
    public String disc() {
        return m.getDisc();
    }

    @Override
    public int category() { return m.getCategory(); }

    @Override
    public String filename() { return m.getFilename(); }

    @Override
    public boolean omu() { return m.isOmu(); }

    @Override
    public boolean top250() {
        return m.isTop250();
    }
}
