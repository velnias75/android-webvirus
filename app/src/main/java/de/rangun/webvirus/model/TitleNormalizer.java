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
 *  Last modified 03.11.19 07:15 by heiko
 */

package de.rangun.webvirus.model;

import androidx.annotation.NonNull;

import jregex.Pattern;
import jregex.Replacer;

final class TitleNormalizer {



    private final static Replacer richReplacer =
            new Replacer(
                    new Pattern("(\\[[^\\]]*\\])|" +
                            "([^\\p{InLatin-1Supplement}\\p{InBasicLatin}]*)|" +
                            "(Original mit Untertitel)"),
                    "", false);

    private final static Replacer msReplacer =
            new Replacer(new Pattern("[\\s]+"), " ", false);

    public static String normalize(@NonNull String str) {
        return msReplacer.replace(richReplacer.replace(str)).trim();
    }
}
