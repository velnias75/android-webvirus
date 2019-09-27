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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public final class MovieFactory {

    public interface OnMoviesAvailableListener {
        void loading();
        void loaded(int num);
        void movies(MovieBKTree movies);
        void error(String localizedMessage);
        void fetchDescription(StringRequest rq);
        void descriptionAvailable(String dsc);
    }

    private static final String TAG = "MovieFactory";
    private static MovieFactory _instance = null;

    private final String URL = "https://rangun.de/db/movies-json.php";
    //private final String URL = "http://192.168.1.156/~heiko/db/movies-json.php";

    private OnMoviesAvailableListener cb = null;

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

                    movies.Add(m);

                } catch (JSONException ex) {
                    cb.error(ex.getMessage());
                }
            }

            cb.movies(movies);
            cb.loaded(movies.size());
        }

    }, error -> {
        if (cb != null) cb.error("" + error.networkResponse.statusCode);
    });

    private MovieFactory() {}

    public static MovieFactory instance() {
        if (_instance == null) _instance = new MovieFactory();
        return _instance;
    }

    public String tag() {
        return TAG;
    }

    public void setOnMoviesAvailableListener(OnMoviesAvailableListener cb) {
        this.cb = cb;
    }

    public void fetchMovies(RequestQueue q) {

        if(cb != null) cb.loading();

        jsonArrayRequest.setTag(TAG);
        jsonArrayRequest.setShouldCache(false);
        q.add(jsonArrayRequest);
    }
}
