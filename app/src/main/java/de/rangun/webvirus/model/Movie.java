package de.rangun.webvirus.model;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

final class Movie extends AbstractMovie {

    private final StringRequest rq;
    private final IMoviesAvailable cb;
    private String dsc = null;

    public Movie(IMovie m, IMoviesAvailable cb) {

        super(m);

        this.cb = cb;

        rq = new StringRequest(Request.Method.GET,
                "https://rangun.de/db/omdb.php?cover-oid=" + oid() + "&abstract=true",
                response -> {
                    dsc = response;
                    cb.descriptionAvailable(dsc);
                }, error -> {

                });
    }

    @Override
    public String description() {
        if(dsc == null) {
            if(oid() != null) {
                cb.fetchDescription(rq);
                return "Hole Kurzbeschreibung von OMDB …";
            } else {
                dsc = "Aufgrund fehlender Intelligenz kann diese App keine Kurzbeschreibung erfinden.";
            }
        }

        return dsc;
    }
}
