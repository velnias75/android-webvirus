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
 *  Last modified 06.11.19 01:43 by heiko
 */

package de.rangun.webvirus.model.movie;

import androidx.annotation.NonNull;

import de.rangun.webvirus.model.movie.util.TitleNormalizer;

final class NormalizedTitleFactory {

    private static NormalizedTitleFactory instance = null;

    private NormalizedTitleFactory() {}

    static NormalizedTitleFactory instance() {

        if(instance == null) instance = new NormalizedTitleFactory();

        return instance;
    }

    public INormalizedTitle createNormalizedTitle(@NonNull IMovie movie) {

        final String mt = movie.title();
        final String nt = TitleNormalizer.normalize(mt);

        if(mt.contains(nt)) {
            if(mt.equals(nt)) {
                return new NormalizedTitleEqual(movie);
            } else {
                final int idx = mt.indexOf(nt);
                return new NormalizedTitlePosition(movie, idx, idx + nt.length());
            }
        }

        return new NormalizedTitleString(nt.intern());
    }
}
