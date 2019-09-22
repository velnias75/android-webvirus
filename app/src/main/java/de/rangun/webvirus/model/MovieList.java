package de.rangun.webvirus.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MovieList extends ArrayList<IMovie> {

    private List<String> titles = null;

    public IMovie getByMovieId(long id) {

        for (int idx = 0; idx < size(); ++idx) {
            IMovie m = get(idx);
            if (m.id() == id) {
                return m;
            }
        }

        return get(0);
    }

    public List<String> titles() {

        if(titles == null) {
            titles = new ArrayList<>();
            for (int idx = 0; idx < size(); ++idx) {
                titles.add(get(idx).title());
            }
        }

        return Collections.unmodifiableList(titles);
    }

    public IMovie findByTitle(String text) {

        for (int idx = 0; idx < size(); ++idx) {

            IMovie m = get(idx);

            if (text.equals(m.title())) {
                return m;
            }
        }

        return null;
    }
}
