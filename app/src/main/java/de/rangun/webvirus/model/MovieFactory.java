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
 *  Last modified 23.09.19 06:31 by heiko
 */

package de.rangun.webvirus.model;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public final class MovieFactory {

    public interface IMoviesAvailableListener {
        void loading(boolean silent);
        void loaded(int num, boolean silent);
        void movies(MovieBKTree movies, boolean silent);
        void error(String localizedMessage);
        void fetchDescription(StringRequest rq);
        void descriptionAvailable(String dsc);
    }

    private static final String TAG = "MovieFactory";
    private static MovieFactory _instance = null;
    private boolean silent = false;

    private final String URL = "https://rangun.de/db/movies-json.php";
    //private final String URL = "http://192.168.1.156/~heiko/db/movies-json.php";

    private IMoviesAvailableListener cb = null;

    private final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
            URL, null, response -> {

        MovieBKTree movies = new MovieBKTree();

        if (cb != null) {

            for (int i = 0; i < response.length(); ++i) {

                try {

                    final JSONObject item = response.getJSONObject(i);
                    final IMovie m = new MovieProxy(cb, new MovieProxy.
                            MovieParameters(item.getLong("id"),
                            item.getString("title"),
                            item.getString("duration"),
                            item.getLong("dur_sec"),
                            item.getString("languages"),
                            item.getString("disc"),
                            item.getInt("category"),
                            item.isNull("filename") ? null :item.getString("filename"),
                            item.getBoolean("omu"),
                            item.getBoolean("top250"),
                            item.isNull("oid") ? null : item.getLong("oid")));

                    movies.add(m);

                } catch (JSONException ex) {
                    cb.error(ex.getMessage());
                }
            }

            cb.movies(movies, silent);
            cb.loaded(movies.size(), silent);

        }

    }, error -> {
        if (cb != null && error != null && error.networkResponse != null) {
            cb.error("" + error.networkResponse.statusCode);
        }
    });

    private MovieFactory() {}

    public static MovieFactory instance() {
        if (_instance == null) _instance = new MovieFactory();
        return _instance;
    }

    public String tag() {
        return TAG;
    }

    public void setOnMoviesAvailableListener(IMoviesAvailableListener cb) {
        this.cb = cb;
    }

    public void fetchMovies(RequestQueue q, boolean silent) {

        this.silent = silent;

        if(cb != null) cb.loading(this.silent);

        jsonArrayRequest.setTag(TAG);
        jsonArrayRequest.setShouldCache(false);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        q.add(jsonArrayRequest);
    }
}
