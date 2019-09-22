package de.rangun.webvirus.model;

import com.android.volley.toolbox.StringRequest;

public interface IMoviesAvailable {
    void loading();

    void loaded(int num);

    void movies(MovieList movies);

    void error(String localizedMessage);

    void fetchDescription(StringRequest rq);

    void descriptionAvailable(String dsc);
}
