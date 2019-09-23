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
