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
import androidx.annotation.Nullable;

import java.util.List;

import de.rangun.webvirus.R;

final class MovieProxy extends AbstractMovie {

    private final MovieFactory.IMoviesAvailableListener cb;

    @Nullable
    private IMovie movie = null;
    @Nullable
    private String filename;

    MovieProxy(MovieFactory.IMoviesAvailableListener cb, long id, String title, String dur_str,
               long dur_sec, @NonNull String languages, String disc, int category, @Nullable String filename,
               boolean omu, boolean top250, Long oid) {

        super(id, title, dur_str, dur_sec, languages, disc, category, omu, top250, oid);

        this.cb = cb;
        this.filename = filename;
    }

    @Override
    void clear() {
        super.clear();
        filename = null;
    }

    @Override
    public Long oid() { return movie == null ? super.oid() : movie.oid(); }

    @Override
    public String title() { return movie == null ? super.title() : movie.title(); }

    @Override
    public String durationString() {
        return movie == null ? super.durationString() : movie.durationString();
    }

    @Override
    public List<String> languages() { return movie == null ? super.languages() : movie.languages(); }

    @Override
    public String disc() { return movie == null ? super.disc() : movie.disc(); }

    @Nullable
    @Override
    public String filename(@NonNull Context ctx) {

        if(movie == null) {

            if(filename == null) filename = ctx.getResources().getString(R.string.no_filename);
            return filename;

        } else return movie.filename(ctx);
    }

    @Nullable
    @Override
    public String description(@NonNull Context ctx) {

        if(movie == null) {
            movie = new Movie(this, filename(ctx), cb);
            clear();
        }

        return movie.description(ctx);
    }
}
