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
 *  Last modified 02.10.19 10:45 by heiko
 */

package de.rangun.webvirus.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

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

    private final class CallbackTransfer {
        String error = null;
        MovieBKTree movies = null;
        Long lid = null;
    }

    private static final String TAG = "MovieFactory";
    @Nullable
    private static MovieFactory _instance = null;

    private final String URL = "https://rangun.de/db/movies-json.php";
    //private final String URL = "http://192.168.1.156/~heiko/db/movies-json.php";

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

    @SuppressWarnings("UnusedReturnValue")
    public void fetchMovies(@NonNull RequestQueue q, boolean silent) {

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

            }, callbackTransfer) {

                @Override
                protected JSONArray customParse(JSONArray array, CallbackTransfer userParam) {

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
                    }

                    callbackTransfer.movies = new MovieBKTree();
                    final ArrayList<_idCoverMapping> ids = new ArrayList<>(array.length());

                    for (int i = 0; i < array.length(); ++i) {

                        try {

                            final JSONObject item = array.getJSONObject(i);

                            final long mid = item.getLong("id");
                            final Long oid = item.isNull("oid") ? null :
                                    item.getLong("oid");

                            callbackTransfer.movies.add(new MovieProxy(cb, mid,
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

                        } catch (JSONException ex) {
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
            };

            Objects.requireNonNull(jsonArrayRequest).setTag(TAG);
            jsonArrayRequest.setShouldCache(false);
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                    5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            q.add(jsonArrayRequest);
        }
    }
}
