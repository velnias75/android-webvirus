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
 *  Last modified 12.10.19 01:48 by heiko
 */

package de.rangun.webvirus.model.db;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AsyncAppDatabaseTask<R extends AsyncAppDatabaseTask.IMovieReceiver>
        extends AsyncTask<Object, Void, Movie> {

    public interface IMovieReceiver {
        void onMovieReceived(@Nullable Movie movie);
    }

    @Nonnull
    private final WeakReference<R> weakReceiver;

    @Nonnull
    final AppDatabase db;

    final long id;

    AsyncAppDatabaseTask(@Nonnull R receiver, @Nonnull AppDatabase db, long id) {

        weakReceiver = new WeakReference<>(receiver);
        this.db = db;
        this.id = id;
    }

    @Override
    protected void onPostExecute(Movie movie) {

        final R recv = weakReceiver.get();

        if(recv != null) recv.onMovieReceived(movie);
    }
}
