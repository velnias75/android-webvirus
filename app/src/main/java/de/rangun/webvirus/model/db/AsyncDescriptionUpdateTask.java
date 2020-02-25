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
 *  Last modified 23.02.20 08:29 by heiko
 */

package de.rangun.webvirus.model.db;

import androidx.annotation.Nullable;

import javax.annotation.Nonnull;

public final class AsyncDescriptionUpdateTask<R extends AsyncAppDatabaseTask.IMovieReceiver>
        extends AsyncMovieFetcherTask<R> {

    private final String dsc;

    public AsyncDescriptionUpdateTask(@Nonnull R receiver, @Nonnull AppDatabase db, long id,
                                      @Nullable String dsc) {
        super(receiver, db, id);
        this.dsc = dsc;
    }

    @Override
    protected Movie doInBackground(Object... objects) {

        Movie m = super.doInBackground(objects);

        if(m != null) {

            if(dsc != null && !dsc.equals(m.dsc)) {
                m.dsc = dsc;
                db.moviesDao().update(m);
            }

        } else {
            m = new Movie(id, dsc, 1);
            db.moviesDao().insert(m);
        }

        return m;
    }
}
