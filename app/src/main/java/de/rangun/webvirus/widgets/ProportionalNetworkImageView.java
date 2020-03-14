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
 *  Last modified 14.03.20 03:00 by heiko
 */

package de.rangun.webvirus.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

public final class ProportionalNetworkImageView extends NetworkImageView {

    public ProportionalNetworkImageView(Context context) {
        super(context);
    }

    public ProportionalNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProportionalNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final Drawable d = getDrawable();

        if(d != null) {

            final int ow = MeasureSpec.getSize(widthMeasureSpec);
            final int oh = MeasureSpec.getSize(heightMeasureSpec);
            final boolean isPortrait = oh >= ow;

            final int w = isPortrait ?
                    ow : oh * d.getIntrinsicWidth() / d.getIntrinsicHeight();
            final int h = isPortrait ?
                    w * d.getIntrinsicHeight() / d.getIntrinsicWidth() : oh;

            setMeasuredDimension(w, h);
        }

        else super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
