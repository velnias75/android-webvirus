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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import de.rangun.webvirus.R;

final class Movie extends AbstractMovie {

    @NonNull
    private final StringRequest rq;
    @NonNull
    private final MovieFactory.IMoviesAvailableListener cb;
    private final String fn;
    @Nullable
    private String dsc = null;

    Movie(@NonNull MovieProxy m, String filename,
          @NonNull MovieFactory.IMoviesAvailableListener cb) throws IllegalArgumentException {

        super(m);

        this.cb = cb;
        this.fn = filename;

        rq = new StringRequest(Request.Method.GET,
                "https://rangun.de/db/omdb.php?cover-oid=" + oid() + "&abstract=true",
                response -> {
                    dsc = response;
                    cb.descriptionAvailable(dsc);
                }, error -> {});
    }

    @Override
    public String filename(Context ctx) { return fn; }

    @Override
    public String description(@NonNull Context ctx) {
        if(dsc == null) {

            if(oid() != null) {
                cb.fetchDescription(rq);
                return ctx.getResources().getString(R.string.fetch_abstract);
            } else {
                dsc = ctx.getResources().getString(R.string.no_abstract);
            }
        }

        return dsc;
    }
}
