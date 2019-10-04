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
 *  Last modified 02.10.19 10:16 by heiko
 */

package de.rangun.webvirus.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

abstract class AbstractMovie implements IMovie {

    private final long id;
    private final String dur_str;
    private final String disc;
    private final String title;
    private final LanguageList languages;
    private final int category;
    private final boolean omu;
    private final boolean top250;
    private final long dur_sec;
    private boolean newMovie = false;

    @Nullable
    private final Long oid;

    private AbstractMovie(long id, @NonNull String title, @NonNull String dur_str, long dur_sec,
                          @NonNull LanguageList languages, @NonNull String disc, int category,
                          boolean omu, boolean top250, @Nullable Long oid) {
        this.id = id;
        this.title = title;
        this.dur_str = dur_str;
        this.dur_sec = dur_sec;
        this.languages = languages;
        this.disc = disc;
        this.category = category;
        this.omu = omu;
        this.top250 = top250;
        this.oid = oid;
    }

    AbstractMovie(long id, @NonNull String title, String dur_str, long dur_sec,
                  @NonNull String languages, String disc, int category, boolean omu,
                  boolean top250, Long oid) {
        this(id, title, dur_str, dur_sec, new LanguageList(languages), disc, category, omu, top250,
                oid);
    }

    AbstractMovie(@NonNull String title) {
        this(0L, title, "", 0L, new LanguageList(), "", -1,
                false, false, null);
    }

    AbstractMovie(@NonNull IMovie m) {
        this(m.id(), m.title(), m.durationString(), m.duration(),
                new LanguageList(Objects.requireNonNull(m.languages())), m.disc(), m.category(),
                m.omu(), m.top250(), m.oid());
    }

    @Nullable
    @Override
    public Long oid() { return oid; }

    @Override
    public String title() { return title; }

    @Override
    public String durationString() { return dur_str; }

    @Override
    public List<String> languages() { return languages.asList(); }

    @Override
    public String disc() { return disc; }

    @Override
    public long id() { return id; }

    @Override
    public long duration() { return dur_sec; }

    @Override
    public int category() { return category; }

    @Override
    public boolean omu() { return omu; }

    @Override
    public boolean top250() { return top250; }

    @Override
    public boolean isDummy() { return false; }

    @Override
    public boolean isNewMovie() { return newMovie; }

    @Override
    public void setNewMovie(boolean newMovie) {
        this.newMovie = newMovie;
    }

    @Override
    public int compareTo(@NonNull IMovie o) {
        return title.compareTo(o.title());
    }

    @NonNull
    @Override
    public String toString() { return this.title; }
}
