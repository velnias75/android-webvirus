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

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONException;
import org.json.JSONObject;

public final class MovieFactory {

    private static final String TAG = "MovieFactory";
    private static MovieFactory _instance = null;

    private final String URL = "https://rangun.de/db/movies-json.php";
    //private final String URL = "http://192.168.1.156/~heiko/db/movies-json.php";

    private IMoviesAvailable cb = null;

    private final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
            URL, null, response -> {

                MovieList movies = new MovieList();

                for (int i = 0; i < response.length(); ++i) {
                    try {
                        JSONObject item = response.getJSONObject(i);
                        movies.add(new MovieProxy(cb,
                                item.getLong("id"),
                                item.getString("title"),
                                item.getString("duration"),
                                item.getString("disc"),
                                item.getBoolean("top250"),
                                item.isNull("oid") ? null : item.getLong("oid")));
                    } catch (JSONException ex) {
                        cb.error("Error: " + ex.getLocalizedMessage());
                    }
                }

                cb.movies(movies);
                cb.loaded(movies.size());
            }, error -> cb.error("Error: " + error.getLocalizedMessage()));

    private MovieFactory(IMoviesAvailable cb) {
        Log.d(TAG, "MovieFactory constructed");
        this.cb = cb;
    }

    public static MovieFactory instance(IMoviesAvailable cb) {

        if (_instance == null) {
            _instance = new MovieFactory(cb);
        }

        return _instance;
    }

    public String tag() {
        return TAG;
    }

    public void allMovies(RequestQueue q) {
        Log.d(TAG, "allMovies()");

        cb.loading();
        jsonArrayRequest.setTag(TAG);
        q.add(jsonArrayRequest);
    }
}
