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
 *  Last modified 12.10.19 02:00 by heiko
 */

package de.rangun.webvirus.model.db;

import javax.annotation.Nonnull;

public final class AsyncMarkerUpdateTask<R extends AsyncAppDatabaseTask.IMovieReceiver>
        extends AsyncMovieFetcherTask<R> {

    private final int marker;

    public AsyncMarkerUpdateTask(@Nonnull R receiver, @Nonnull AppDatabase db, long id,
                                 int marker) {
        super(receiver, db, id);
        this.marker = marker;
    }

    @Override
    protected Movie doInBackground(Object... objects) {

        Movie m = super.doInBackground(objects);

        if(m != null) {

            if(m.marker != marker) {
                m.marker = marker;
                db.moviesDao().update(m);
            }

        } else {
            m = new Movie(id, marker);
            db.moviesDao().insert(m);
        }

        return m;
    }
}
