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
 *  Last modified 03.10.19 03:54 by heiko
 */

package de.rangun.webvirus;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public final class Toaster {

    private final View layout;
    private final Context ctx;

    Toaster(AppCompatActivity activity) {

        layout = activity.getLayoutInflater().inflate(R.layout.custom_toast,
                activity.findViewById(R.id.custom_toast_container));

        ctx = activity.getApplicationContext();
    }

    public void show(String txt, int color) {

        final TextView text = layout.findViewById(R.id.text);

        text.setText(txt);
        text.setTextColor(color);

        Toast toast = new Toast(ctx);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);

        toast.show();
    }
}
