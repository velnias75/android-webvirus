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
 *  Last modified 12.10.19 06:16 by heiko
 */

package de.rangun.webvirus.widgets;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import de.rangun.webvirus.R;

public final class MarkerImageView extends AppCompatImageView {

    public MarkerImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMarker(int marker) {

        switch(marker) {
            case 0:
                setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.heart));
                break;
            case 2:
                setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.human_child));
                break;
            case 3:
                setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.face_woman));
                break;
            default:
                setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.bone));
                break;
        }
    }
}
