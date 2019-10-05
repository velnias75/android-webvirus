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
 *  Last modified 05.10.19 01:24 by heiko
 */

package de.rangun.webvirus;

import android.app.Activity;
import android.app.Instrumentation;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoIntentTest {

    private final static String TEST_URI = "webvirus:2015";

    @Rule
    public IntentsTestRule<MainActivity> intentsTestRule =
            new IntentsTestRule<>(MainActivity.class);

    @Before
    public void stubAllExternalIntents() {
        intending(not(isInternal())).
                respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK,
                        null));
    }

    @Test
    public void pickContact_viewIsSet() {
        onView(withId(R.id.openInDB)).check(matches(withText(R.string.open_in_db)));
    }
}
