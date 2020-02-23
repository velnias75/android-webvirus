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
 *  Last modified 06.11.19 01:22 by heiko
 */

package de.rangun.webvirus.model.movie;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public interface IMovie extends Comparable<IMovie> {

    int pos();

    long id();

    @NonNull
    String title();

    @NonNull
    String normalizedTitle();

    long duration();

    @NonNull
    String durationString();

    List<String> languages();

    String disc();

    int category();

    @Nullable
    String filename(Context ctx);

    boolean omu();

    boolean top250();

    @Nullable
    Long oid();

    @NonNull
    String tmdb_type();

    @Nullable
    Long tmdb_id();

    @Nullable
    String description(Context ctx, MovieFactory.IMoviesAvailableListener l);

    boolean isDummy();

    boolean isNewMovie();

    void setNewMovie(boolean b);
}
