/*
 * Copyright 2019-2020 by Heiko Schäfer <heiko@rangun.de>
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
 *  Last modified 06.11.19 01:21 by heiko
 */

package de.rangun.webvirus.model.movie;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

import de.rangun.webvirus.R;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

final class MovieProxy extends AbstractMovie {

    public interface IMovieProxyObserver {
        void unproxied(IMovie oldProxy, IMovie newInstance);
    }

    private final MovieFactory.IMoviesAvailableListener cb;
    private final IMovieProxyObserver observer;

    @Nullable
    private WeakReference<IMovie> movie = null;

    @Nullable
    private final IMovieFilename filename;

    public MovieProxy(MovieFactory.IMoviesAvailableListener cb, int pos, long id, String title,
               long dur_sec, @NonNull String languages, String disc, int category,
               @Nullable String filename, boolean omu, boolean top250, Long oid,
                      @NonNull String tmdb_type, @Nullable Long tmdb_id)
            throws IllegalArgumentException {

        super(pos, id, title, dur_sec, languages, disc, category, omu, top250, oid,
                tmdb_type, tmdb_id);

        this.cb = cb;
        this.observer = cb;
        this.filename = MovieFilenameFactory.instance().createFilename(this, filename);
    }

    @Override
    @SuppressFBWarnings
    public Long oid() { return movie == null || movie.get() == null ? super.oid() :
            movie.get().oid(); }

    @NonNull
    @Override
    @SuppressFBWarnings
    public String title() {
        return movie == null || movie.get() == null ? super.title() : movie.get().title(); }

    @NonNull
    @Override
    @SuppressFBWarnings
    public String durationString() {
        return movie == null || movie.get() == null ? super.durationString() :
                movie.get().durationString();
    }

    @Override
    @SuppressFBWarnings
    public List<String> languages() {
        return movie == null || movie.get() == null ? super.languages() : movie.get().languages();
    }

    @Override
    @SuppressFBWarnings
    public String disc() {
        return movie == null || movie.get() == null ? super.disc() : movie.get().disc();
    }

    @Nullable
    @Override
    @SuppressFBWarnings
    public String filename(@NonNull Context ctx) {
        return movie == null || movie.get() == null ? (filename != null ? filename.fileName() :
                ctx.getResources().getString(R.string.no_filename)) : movie.get().filename(ctx);
    }

    @Nullable
    @Override
    public String description(@NonNull Context ctx, MovieFactory.IMoviesAvailableListener l) {

        if(movie == null || movie.get() == null) {

            movie = new WeakReference<>(new Movie(this, filename(ctx), cb));
            final IMovie mg = movie.get();

            if(mg != null) {

                final String dsc = mg.description(ctx, l);

                if (observer != null) observer.unproxied(this, mg);

                return dsc;
            }
        }

        return ctx.getString(R.string.no_abstract);
    }
}
