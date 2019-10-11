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
 *  Last modified 11.10.19 09:48 by heiko
 */

package de.rangun.webvirus.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;

import androidx.annotation.NonNull;

import de.rangun.webvirus.R;

final class MarkerSpinnerAdapter extends ArrayAdapter<String> implements SpinnerAdapter {

    private final LayoutInflater inflater;
    private final TypedArray imgs;

    public MarkerSpinnerAdapter(@NonNull Context context, int textViewResourceId,
                                LayoutInflater inflater) {

        super(context, textViewResourceId);

        this.imgs = context.getResources().obtainTypedArray(R.array.markers);
        this.inflater = inflater;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent);
    }

    @Override
    public int getCount() { return imgs.length(); }

    private View getCustomView(int position, ViewGroup parent) {

        final View row = inflater.inflate(R.layout.marker_spinner_row, parent, false);
        final ImageView icon = row.findViewById(R.id.marker);

        icon.setImageResource(imgs.getResourceId(position, -1));

        return row;
    }
}