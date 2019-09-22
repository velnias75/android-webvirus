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
