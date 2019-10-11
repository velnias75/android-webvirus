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
 *  Last modified 05.10.19 10:19 by heiko
 */

package de.rangun.webvirus.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import de.rangun.webvirus.BuildConfig;
import de.rangun.webvirus.R;

@SuppressWarnings("ConstantConditions")
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        final Preference p = getPreferenceScreen().findPreference("debugPrefs");

        if(p != null) p.setVisible("a_hirnlos".equals(BuildConfig.FLAVOR));
    }
}
