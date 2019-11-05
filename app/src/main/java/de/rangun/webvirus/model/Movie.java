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
 *  Last modified 01.10.19 02:31 by heiko
 */

package de.rangun.webvirus.model;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import de.rangun.webvirus.R;

final class Movie extends AbstractMovie {

    @NonNull
    private final MovieFactory.IMoviesAvailableListener cb;
    private final IMovieFilename fn;

    @Nullable
    private String dsc = null;

    Movie(@NonNull MovieProxy m, String filename,
          @NonNull MovieFactory.IMoviesAvailableListener cb) throws IllegalArgumentException {

        super(m);

        this.cb = cb;
        this.fn = MovieFilenameFactory.instance().createFilename(this, filename);
    }

    @Override
    public String filename(@NonNull Context ctx) { return fn.fileName(); }

    @Override
    public String description(@NonNull Context ctx) {

        if(dsc == null) {

            Long oid = oid();

            if(oid != null) {

                cb.fetchDescription(new StringRequest(Request.Method.GET,
                        "https://rangun.de/db/omdb.php?cover-oid=" + oid + "&abstract=true",
                        response -> {
                            dsc = response;
                            cb.descriptionAvailable(dsc);
                        }, error -> Log.d("Movie",
                        "(description): error:" + error.getMessage())));

                return ctx.getResources().getString(R.string.fetch_abstract);

            } else dsc = ctx.getResources().getString(R.string.no_abstract);
        }

        return dsc;
    }
}
