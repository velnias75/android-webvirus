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
 *  Last modified 04.10.19 02:02 by heiko
 */

package de.rangun.webvirus.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

final class LanguageList {

    @Nullable
    @SuppressWarnings("SpellCheckingInspection")
    private static ArrayList<String> lingos = null;

    private final IntArraySet posList = new IntArraySet();

    LanguageList() {}

    LanguageList(@NonNull @SuppressWarnings("SpellCheckingInspection") String lingos) {
        this(lingos.split(", "));
    }

    LanguageList(@NonNull List<String> l) {
        this(l.toArray(new String[0]));
    }

    private LanguageList(@NonNull String[] l) {
        for(String s: l) posList.add(add(s));
    }

    @NonNull
    List<String> asList() {

        final TreeSet<String> r = new TreeSet<>();

        if(lingos != null) for(int i: posList.getAllItems()) r.add(lingos.get(i));

        return new ArrayList<>(r);
    }

    private int add(String s) {

        if(lingos == null) lingos = new ArrayList<>();

        if(!lingos.contains(s)) lingos.add(s);

        return lingos.indexOf(s);
    }
}
