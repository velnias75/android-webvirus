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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

abstract class AbstractMovie implements IMovie {

    private final static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss",
            Locale.GERMANY);

    private final static BiMap<Integer, String> discMap = HashBiMap.create();

    private final long id;
    private final String title;
    private final MergedStringList languages;
    private final int category;
    private final boolean omu;
    private final boolean top250;
    private final long dur_sec;

    @NonNull
    private final Integer discId;

    private boolean newMovie = false;

    @Nullable
    private final Long oid;

    private AbstractMovie(long id, @NonNull String title, long dur_sec,
                          @NonNull MergedStringList languages, @NonNull String disc, int category,
                          boolean omu, boolean top250, @Nullable Long oid)
            throws IllegalArgumentException {

        this.id = id;
        this.title = title.intern();
        this.dur_sec = dur_sec;
        this.languages = languages;
        this.category = category;
        this.omu = omu;
        this.top250 = top250;
        this.oid = oid;
        this.discId = getIdForDisc(disc);

        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    AbstractMovie(long id, @NonNull String title, long dur_sec, @NonNull String languages,
                  String disc, int category, boolean omu,boolean top250, Long oid)
            throws IllegalArgumentException {
        this(id, title, dur_sec, new MergedStringList(languages), disc, category, omu,
                top250, oid);
    }

    AbstractMovie(@NonNull String title) throws IllegalArgumentException {
        this(0L, title, 0L, new MergedStringList(), "", -1,
                false, false, null);
    }

    AbstractMovie(@NonNull IMovie m) throws IllegalArgumentException {
        this(m.id(), m.title(), m.duration(), new MergedStringList(m.languages()), m.disc(),
                m.category(), m.omu(), m.top250(), m.oid());
    }

    @Nullable
    @Override
    public Long oid() { return oid; }

    @Override
    public String title() { return title; }

    @Override
    public String durationString() { return df.format(new Date(dur_sec * 1000)); }

    @Override
    public List<String> languages() { return languages.asList(); }

    @Override
    public String disc() { return discMap.get(discId); }

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

    @NonNull
    private Integer getIdForDisc(String disc) throws IllegalArgumentException {

        final Integer did;

        if(discMap.containsValue(disc)) {
            did = discMap.inverse().get(disc);
        } else {
            did = discMap.size();
            discMap.put(did, disc.intern());
        }

        if(did != null) {
            return did;
        }

        throw new IllegalArgumentException("\"" + disc + "\" not found in discMap");
    }

    @Override
    public int compareTo(@NonNull IMovie o) {
        return title.compareTo(o.title());
    }

    @NonNull
    @Override
    public String toString() { return this.title; }
}
