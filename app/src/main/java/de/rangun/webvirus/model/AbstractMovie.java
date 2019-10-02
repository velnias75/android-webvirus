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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

abstract class AbstractMovie implements IMovie {

    private final long id;
    private final String title;
    @Nullable
    private final String dur_str;
    private final long dur_sec;
    @Nullable
    private final List<String> languages;
    @Nullable
    private final String disc;
    private final int category;
    private final boolean omu;
    private final boolean top250;
    @Nullable
    private final Long oid;

    private AbstractMovie(long id, @Nullable String title, @Nullable String dur_str, long dur_sec,
                          @Nullable List<String> languages, @Nullable String disc, int category,
                          boolean omu, boolean top250, @Nullable Long oid) {
        this.id = id;
        this.title = title != null ? title : "(null)";
        this.dur_str = dur_str;
        this.dur_sec = dur_sec;
        this.languages = languages;
        this.disc = disc;
        this.category = category;
        this.omu = omu;
        this.top250 = top250;
        this.oid = oid;
    }

    AbstractMovie(long id, String title, String dur_str, long dur_sec, @NonNull String languages,
                  String disc, int category, boolean omu, boolean top250, Long oid) {
        this(id, title, dur_str, dur_sec, Arrays.asList(languages.split(", ")), disc,
                category, omu, top250, oid);
    }

    AbstractMovie(String title) {
        this(0L, title, null, 0L, new ArrayList<>(0),
                null, -1, false, false, null);
    }

    AbstractMovie(@NonNull IMovie m) {
        this(m.id(), m.title(), m.durationString(), m.duration(), m.languages(), m.disc(),
                m.category(), m.omu(), m.top250(), m.oid());
    }

    @Nullable
    @Override
    public Long oid() { return oid; }

    @Override
    public String title() { return title; }

    @Nullable
    @Override
    public String durationString() { return dur_str; }

    @Nullable
    @Override
    public List<String> languages() { return languages; }

    @Nullable
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
    public int compareTo(@NonNull IMovie o) {
        return title.compareTo(Objects.requireNonNull(o.title()));
    }

    @NonNull
    @Override
    public String toString() { return this.title; }
}
