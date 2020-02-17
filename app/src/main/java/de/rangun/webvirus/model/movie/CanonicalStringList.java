/*
 * Copyright 2019-2020 by Heiko Schäfer <heiko@rangun.de>
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
 *  Last modified 06.11.19 04:33 by heiko
 */

package de.rangun.webvirus.model.movie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

final class CanonicalStringList {

    @Nullable
    private static ArrayList<String> strings = null;

    private final IntArraySet posList = new IntArraySet();

    CanonicalStringList() {}

    CanonicalStringList(@NonNull String str) {
        this(str.split(", "));
    }

    CanonicalStringList(@NonNull List<String> l) {
        this(l.toArray(new String[0]));
    }

    private CanonicalStringList(@NonNull String[] l) {
        for(String s: l) posList.add(add(s));
    }

    @NonNull
    List<String> asList() {

        final TreeSet<String> r = new TreeSet<>();

        if(strings != null) for(int i: posList.getAllItems()) r.add(strings.get(i));

        return new ArrayList<>(r);
    }

    @SuppressFBWarnings
    private int add(String s) {

        if(strings == null) strings = new ArrayList<>();

        if(!strings.contains(s)) strings.add(s.intern());

        return strings.indexOf(s);
    }

    @NonNull
    @Override
    public String toString() { return posList.toString(); }
}
