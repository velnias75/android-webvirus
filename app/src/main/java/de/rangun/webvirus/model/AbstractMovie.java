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

    private final IMovie m;

    AbstractMovie(IMovie m) {
        this.m = m;
    }

    @Override
    public long id() {
        return m.id();
    }

    @Override
    public Long oid() {
        return m.oid();
    }

    @Override
    public String title() {
        return m.title();
    }

    @Override
    public long duration() {
        return m.duration();
    }

    @Override
    public String durationString() {
        return m.durationString();
    }

    @Override
    public List<String> languages() {
        return m.languages();
    }

    @Override
    public String disc() {
        return m.disc();
    }

    @Override
    public int category() {
        return m.category();
    }

    @Override
    public String filename() {
        return m.filename();
    }

    @Override
    public boolean omu() {
        return m.omu();
    }

    @Override
    public boolean top250() {
        return m.top250();
    }
}
