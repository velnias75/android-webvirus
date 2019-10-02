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
 *  Last modified 02.10.19 03:55 by heiko
 */

package de.rangun.webvirus.fragments;

import androidx.fragment.app.Fragment;

import de.rangun.webvirus.model.IMovie;

public interface IMovieUpdateRequestListener {
    void onUpdateMovieByTitleOrId(String text, Fragment f);
    void onUpdateMovie(IMovie m, Fragment f);
    void onRequestNewMoviesUpdate(MovieListFragment f);
}
