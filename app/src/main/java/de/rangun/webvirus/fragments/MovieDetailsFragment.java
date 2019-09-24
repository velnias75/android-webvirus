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
 *  Last modified 24.09.19 07:46 by heiko
 */

package de.rangun.webvirus.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import de.rangun.webvirus.R;
import de.rangun.webvirus.model.BitmapMemCache;
import de.rangun.webvirus.model.IMovie;

public class MovieDetailsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View fragmentLayout = inflater.inflate(R.layout.moviedetailsfragment, container,
                false);

        final NetworkImageView cov = fragmentLayout.findViewById(R.id.cover);
        cov.setDefaultImageResId(R.drawable.nocover);
        cov.setImageUrl(null, null);

        return fragmentLayout;
    }

    public final void setVisibility(int visibility) {
        getView().setVisibility(visibility);
    }

    public final void setContents(IMovie m, RequestQueue queue, String preZeros) {

        final View top250 = getView().findViewById(R.id.top250);
        final TextView mid = getView().findViewById(R.id.m_id);
        final TextView tit = getView().findViewById(R.id.title);
        final TextView dus = getView().findViewById(R.id.m_duration);
        final TextView dis = getView().findViewById(R.id.m_disc);
        final TextView abs = getView().findViewById(R.id.m_abstract);
        final NetworkImageView cov = getView().findViewById(R.id.cover);

        final String idNum = preZeros + m.id();

        top250.setVisibility(m.top250() ? View.VISIBLE : View.INVISIBLE);
        mid.setText(idNum.substring(idNum.length() - preZeros.length()));
        tit.setText(m.title());
        dus.setText(m.durationString());
        dis.setText(m.disc());
        abs.setText(m.description(getContext()));

        if (m.oid() != null) {

            cov.setImageUrl("https://rangun.de/db/omdb.php?cover-oid=" + m.oid() +
                            (m.top250() ? "&top250=true" : ""),
                    new ImageLoader(queue, new BitmapMemCache()));
        } else {
            cov.setImageUrl(null, null);
        }
    }
}
