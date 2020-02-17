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
 *  Last modified 30.12.19 03:48 by heiko
 */

package de.rangun.webvirus.widgets;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import de.rangun.webvirus.R;

public final class CategoryTextView extends AppCompatTextView {

    private final Context ctx;

    public CategoryTextView(@NonNull Context ctx, AttributeSet atts) {
        super(ctx, atts);
        this.ctx = ctx;
    }


    public final void setCategoryText(int cat) {
        super.setText(catText(cat));
        setTextColorByCategory(cat);
    }

    public final void setText(CharSequence txt, int cat) {
        super.setText(txt);
        setTextColorByCategory(cat);
    }

    public final void setTextColorByCategory(int cat) {
        super.setTextColor(color(cat));
    }

    @NonNull
    private String catText(int cat) {
        switch (cat) {
            case 1:
                return getResources().getString(R.string.categoryFeature);
            case 2:
                return getResources().getString(R.string.categoryYouth);
            case 3:
                return getResources().getString(R.string.categoryDocumentary);
            case 4:
                return getResources().getString(R.string.categoryConcert);
            default:
                return getResources().getString(R.string.categoryAny);
        }
    }

    private int color(int cat) {
        switch (cat) {
            case 1:
                return ContextCompat.getColor(ctx, R.color.categoryFeature);
            case 2:
                return ContextCompat.getColor(ctx, R.color.categoryYouth);
            case 3:
                return ContextCompat.getColor(ctx, R.color.categoryDocumentary);
            case 4:
                return ContextCompat.getColor(ctx, R.color.categoryConcert);
            default:
                return ContextCompat.getColor(ctx, R.color.categoryAny);
        }
    }
}
