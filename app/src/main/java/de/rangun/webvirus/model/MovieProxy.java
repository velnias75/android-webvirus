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

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import de.rangun.webvirus.R;

final class MovieProxy implements IMovie {

    static class MovieParameters {

        private final long id;
        private final String title;
        private final String dur_str;
        private final long dur_sec;
        private final List<String> languages;
        private final String disc;
        private final int category;
        private final String filename;
        private final boolean omu;
        private final boolean top250;
        private final Long oid;

        MovieParameters(long id, String title, String dur_str, long dur_sec,
                        String languages, String disc, int category, String filename, boolean omu,
                        boolean top250, Long oid) {

            this.id = id;
            this.title = title;
            this.dur_str = dur_str;
            this.dur_sec = dur_sec;
            this.languages = Arrays.asList(languages.split(", "));
            this.disc = disc;
            this.category = category;
            this.filename = filename;
            this.omu = omu;
            this.top250 = top250;
            this.oid = oid;
        }

        long getId() {
            return id;
        }

        String getTitle() {
            return title;
        }

        String getDur_str() {
            return dur_str;
        }

        List<String> getLanguages() { return languages; }

        long getDur_sec() { return dur_sec; }

        String getDisc() {
            return disc;
        }

        int getCategory() { return category; }

        String getFilename(Context ctx) {
            return filename != null ? filename : ctx.getResources().getString(R.string.no_filename);
        }

        boolean isOmu() { return omu; }

        boolean isTop250() {
            return top250;
        }

        Long getOid() {
            return oid;
        }
    }

    private final MovieFactory.IMoviesAvailableListener cb;
    private final MovieParameters mp;
    private IMovie movie = null;

    MovieProxy(MovieFactory.IMoviesAvailableListener cb, MovieParameters mp) {
        this.mp = mp;
        this.cb = cb;
    }

    @Override
    public long id() {
        return mp.getId();
    }

    @Override
    public Long oid() {
        return mp.getOid();
    }

    @Override
    public String title() {
        return mp.getTitle();
    }

    @Override
    public long duration() { return mp.getDur_sec(); }

    @Override
    public String durationString() {
        return mp.getDur_str();
    }

    @Override
    public List<String> languages() {
        return mp.getLanguages();
    }

    @Override
    public String disc() {
        return mp.getDisc();
    }

    @Override
    public int category() { return mp.getCategory(); }

    @Override
    public String filename(Context ctx) { return mp.getFilename(ctx); }

    @Override
    public boolean omu() {
        return mp.isOmu();
    }

    @Override
    public boolean top250() {
        return mp.isTop250();
    }

    @Override
    public String description(Context ctx) {

        if(movie == null) {
            movie = new Movie(mp, cb);
        }

        return movie.description(ctx);
    }

    @Override
    public int compareTo(IMovie o) {
        return title().compareTo(o.title());
    }

    @NonNull
    @Override
    public String toString() {
        return title();
    }
}
