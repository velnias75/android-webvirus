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
 *  Last modified 06.11.19 00:36 by heiko
 */

package de.rangun.webvirus.model.movie;

import android.content.Context;

import androidx.annotation.NonNull;

public final class DummyMovie extends AbstractMovie {

    private static final String DUMMY = "";

    public DummyMovie(String title) throws IllegalArgumentException { super(title); }

    @Override
    public boolean isDummy() { return true; }

    @NonNull
    @Override
    public String filename(@NonNull Context ctx) { return DUMMY; }

    @NonNull
    @Override
    public String description(@NonNull Context ctx) {
        return DUMMY;
    }
}
