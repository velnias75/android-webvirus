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
 *  Last modified 12.12.19 09:00 by heiko
 */

package de.rangun.webvirus.model.movie;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.rangun.webvirus.R;

final class Movie extends AbstractMovie {

    @NonNull
    private final MovieFactory.IMoviesAvailableListener cb;
    private final IMovieFilename fn;

    @Nullable
    private String dsc = null;

    public Movie(@NonNull MovieProxy m, String filename,
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

            final ConnectivityManager cm =
                    (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

            if(cm != null) {

                final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                if (activeNetwork != null && activeNetwork.isConnected()) {

                    try {

                        cb.fetchDescription(new StringRequest(Request.Method.GET,
                                "https://rangun.de/db/omdb.php?cover-oid=" +
                                        "&abstract=true" +
                                        (tmdb_id() != null ? ("&tmdb_type=" + tmdb_type() +
                                        "&tmdb_id=" + tmdb_id()) : "&fallback=" +
                                        URLEncoder.encode(title(), "UTF-8")),
                                response -> {
                                    dsc = response;
                                    cb.descriptionAvailable(dsc);
                                }, error -> Log.d("Movie",
                                "(description): error:" + error.getMessage())));

                        return ctx.getResources().getString(R.string.fetch_abstract);

                    } catch(UnsupportedEncodingException ex) {
                        return ctx.getResources().getString(R.string.no_abstract);
                    }

                } else dsc = ctx.getResources().getString(R.string.no_abstract);
            }
        }

        return dsc;
    }
}
