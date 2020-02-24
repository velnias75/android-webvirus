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
 *  Last modified 17.02.20 08:25 by heiko
 */

package de.rangun.webvirus.model.movie;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import de.rangun.webvirus.model.bktree.MovieBKTree;
import de.rangun.webvirus.net.GZipJsonObjectRequest;

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

    public final static class CallbackTransfer {
        String error = null;
        public MovieBKTree movies = null;
        public Long lid = null;
    }

    private final class _idCoverMapping implements Comparable<_idCoverMapping> {

        final Long mid;
        private final Long oid;
        private final Long tid;

        @SuppressWarnings("unused")
        private _idCoverMapping(Long mid, Long oid, Long tid) {
            this.mid = mid;
            this.oid = oid;
            this.tid = tid;
        }

        public Long id() { return tid != null ? tid : oid; }

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

    private static final String TAG = "MovieFactory";

    @Nullable
    private static MovieFactory _instance = null;

    private final static String URL = "https://rangun.de/db/bktree-json.php?order_by=ltitle";
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

    public CallbackTransfer fetchMovies(StringBuilder data) {

        try {

            final CallbackTransfer callbackTransfer = new CallbackTransfer();

            parseJSONObject(callbackTransfer, new JSONObject(new String(data.toString().
                    getBytes(StandardCharsets.UTF_8))));

            return callbackTransfer;

        } catch (JSONException ex) {
            //cb.error("sth went wrong");
        }

        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public void fetchMovies(@NonNull RequestQueue q, boolean silent, OutputStream gzipOut) {

        if(cb != null) {

            cb.loading(silent);

            final CallbackTransfer callbackTransfer = new CallbackTransfer();

            //noinspection unused
            GZipJsonObjectRequest<CallbackTransfer> jsonArrayRequest =
                    new GZipJsonObjectRequest<CallbackTransfer>(Request.Method.GET, URL,
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
                protected JSONObject customParse(JSONObject jsonObject, CallbackTransfer userParam) {
                    return parseJSONObject(callbackTransfer, jsonObject);
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

    private JSONObject parseJSONObject(@NonNull CallbackTransfer callbackTransfer,
                                      @NonNull JSONObject jsonObject) {

        callbackTransfer.movies = new MovieBKTree();

        ArrayList<_idCoverMapping> ids;

        try {
            ids = new ArrayList<>(jsonObject.getInt("size"));
        } catch(JSONException ex) {
            ids = new ArrayList<>();
        }

        try {
            build(callbackTransfer, jsonObject.getJSONObject("root"), ids);
        } catch(JSONException ex) {
            callbackTransfer.error = "JSONException: " + ex.getMessage();
        }

        Collections.sort(ids);

        for (int i = ids.size() - 1; i >= 0; --i) {

            final _idCoverMapping icm = ids.get(i);

            if (icm.id() != null) {
                callbackTransfer.lid = icm.mid;
                break;
            }
        }

        return jsonObject;
    }

    private void build(@NonNull CallbackTransfer callbackTransfer,
                       @NonNull JSONObject jsonObject,
                       @NonNull List<_idCoverMapping> ids) throws JSONException {
        buildRecursive(null, 0, callbackTransfer, jsonObject, ids);
    }

    private void buildRecursive(MovieBKTree.INode<IMovie> p, int d,
                                @NonNull CallbackTransfer callbackTransfer,
                                @NonNull JSONObject jsonObject,
                                @NonNull List<_idCoverMapping> ids) throws JSONException {

        final JSONObject item = jsonObject.getJSONObject("item");

        final long mid = item.getLong("id");
        final Long oid = item.isNull("oid") ? null : item.getLong("oid");
        final Long tid = item.isNull("tmdb_id") ? null : item.getLong("tmdb_id");

        final MovieBKTree.INode<IMovie> parent =
                callbackTransfer.movies.createNode(p, d, new MovieProxy(cb,
                        item.getInt("pos"),
                        mid,
                        item.getString("title"),
                        item.getLong("dur_sec"),
                        item.getString("languages"),
                        item.getString("disc"),
                        item.getInt("category"),
                        !item.isNull("filename") ? item.getString("filename") : null,
                        item.getBoolean("omu"),
                        item.getBoolean("top250"),
                        oid,
                        item.getString("tmdb_type"),
                        tid));

        ids.add(new _idCoverMapping(mid, oid, tid));

        if(!jsonObject.isNull("children")) {

            final JSONObject children = jsonObject.getJSONObject("children");
            final Iterator<String> keys = children.keys();

            while(keys.hasNext()) {

                String key = keys.next();

                if(children.get(key) instanceof JSONObject) {
                    buildRecursive(parent, Integer.parseInt(key), callbackTransfer,
                            (JSONObject)children.get(key), ids);
                }
            }
        }
    }
}
