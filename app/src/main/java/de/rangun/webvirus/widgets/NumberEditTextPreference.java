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
 *  Last modified 05.10.19 13:28 by heiko
 */

package de.rangun.webvirus.widgets;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

abstract class NumberEditTextPreference extends EditTextPreference {

    NumberEditTextPreference(Context context) { super(context); }

    NumberEditTextPreference(Context context, AttributeSet attrs) { super(context, attrs); }

    NumberEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public CharSequence getSummary() {

        if(super.getSummary() == null) return null;

        String summary = super.getSummary().toString();
        return String.format(summary, getText());
    }
}
