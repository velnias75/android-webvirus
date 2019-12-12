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
 *  Last modified 06.11.19 01:09 by heiko
 */

package de.rangun.webvirus.model.movie;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import de.rangun.webvirus.model.bktree.MovieBKTree;
import de.rangun.webvirus.net.GZipJsonArrayRequest;

public final class MovieFactory {

    public interface IMoviesAvailableListener extends MovieProxy.IMovieProxyObserver {
        void loading(boolean silent);
        void loaded(int num, boolean silent);
        void movies(MovieBKTree movies, Long latestCoverId, boolean silent);
        void error(String message);
        void fetchDescription(StringRequest rq);
        void descriptionAvailable(String dsc);
        void newMoviesAvailable(int num);
    }

    private final static class CallbackTransfer {
        String error = null;
        MovieBKTree movies = null;
        Long lid = null;
    }

    private static final String TAG = "MovieFactory";

    @Nullable
    private static MovieFactory _instance = null;

    private final static String URL = "https://rangun.de/db/movies-json.php?order_by=ltitle";
    //private final static String URL = "http://192.168.1.156/~heiko/db/movies-json.php";

    @Nullable
    private IMoviesAvailableListener cb = null;

    private MovieFactory() {}

    public static MovieFactory instance() {
        if(_instance == null) _instance = new MovieFactory();
        return _instance;
    }

    public void setOnMoviesAvailableListener(IMoviesAvailableListener cb) {
        this.cb = cb;
    }

    public void fetchMovies(StringBuilder data, boolean silent) {

        if(cb != null) {

            cb.loading(silent);

            try {

                final CallbackTransfer callbackTransfer = new CallbackTransfer();

                parseJSONArray(callbackTransfer, new JSONArray(new String(data.toString().
                        getBytes(StandardCharsets.UTF_8))));

                cb.movies(callbackTransfer.movies, callbackTransfer.lid, silent);
                cb.loaded(callbackTransfer.movies.size(), silent);

            } catch (JSONException ex) {
                cb.error("sth went wrong");
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public void fetchMovies(@NonNull RequestQueue q, boolean silent, OutputStream gzipOut) {

        if(cb != null) {

            cb.loading(silent);

            final CallbackTransfer callbackTransfer = new CallbackTransfer();

            //noinspection unused
            GZipJsonArrayRequest<CallbackTransfer> jsonArrayRequest =
                    new GZipJsonArrayRequest<CallbackTransfer>(Request.Method.GET, URL,
                    null, response -> {

                if (callbackTransfer.error == null) {
                    cb.movies(callbackTransfer.movies, callbackTransfer.lid, silent);
                    cb.loaded(callbackTransfer.movies.size(), silent);
                } else {
                    cb.error(callbackTransfer.error);
                }

            }, error -> {

                if (cb != null && error != null && error.networkResponse != null) {
                    cb.error("" + error.networkResponse.statusCode);
                }

            }, callbackTransfer, gzipOut) {

                @Override
                protected JSONArray customParse(JSONArray array, CallbackTransfer userParam) {
                    return parseJSONArray(callbackTransfer, array);
                }
            };

            Log.d(TAG, "now REALLY fetching movies");

            Objects.requireNonNull(jsonArrayRequest).setTag(TAG);
            jsonArrayRequest.setShouldCache(false);
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                    5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            q.add(jsonArrayRequest);

        } else Log.d(TAG, "NO IMoviesAvailableListener registered");
    }

    private JSONArray parseJSONArray(@NonNull CallbackTransfer callbackTransfer,
                                     @NonNull JSONArray array) {

        final class _idCoverMapping implements Comparable<_idCoverMapping> {

            final Long mid;
            final Long oid;

            @SuppressWarnings("unused")
            private _idCoverMapping(Long mid, Long oid) {
                this.mid = mid;
                this.oid = oid;
            }

            @Override
            public int compareTo(_idCoverMapping o) {
                return mid.compareTo(o.mid);
            }

            @Override
            public boolean equals(Object obj) {

                if(obj == null) return false;
                if(getClass() != obj.getClass()) return false;

                final _idCoverMapping other = (_idCoverMapping)obj;
                return Objects.equals(this.mid, other.mid);
            }

            @Override
            public int hashCode() { return mid.hashCode(); }
        }

        callbackTransfer.movies = new MovieBKTree();
        final ArrayList<_idCoverMapping> ids = new ArrayList<>(array.length());

        for(int i = 0; i < array.length(); ++i) {

            try {

                final JSONObject item = array.getJSONObject(i);

                final long mid = item.getLong("id");
                final Long oid = item.isNull("oid") ? null :
                        item.getLong("oid");

                callbackTransfer.movies.add(new MovieProxy(cb,
                        i, mid,
                        item.getString("title"),
                        item.getLong("dur_sec"),
                        item.getString("languages"),
                        item.getString("disc"),
                        item.getInt("category"),
                        !item.isNull("filename") ?
                                item.getString("filename") : null,
                        item.getBoolean("omu"),
                        item.getBoolean("top250"),
                        oid));

                ids.add(new _idCoverMapping(mid, oid));

            } catch(JSONException ex) {
                callbackTransfer.error = "JSONException: " + ex.getMessage();
            }
        }

        Collections.sort(ids);

        for (int i = ids.size() - 1; i >= 0; --i) {

            final _idCoverMapping icm = ids.get(i);

            if (icm.oid != null) {
                callbackTransfer.lid = icm.mid;
                break;
            }
        }

        return array;
    }
}
