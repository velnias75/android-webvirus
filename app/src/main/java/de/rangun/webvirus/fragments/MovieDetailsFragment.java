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
 *  Last modified 02.10.19 12:23 by heiko
 */

package de.rangun.webvirus.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.Objects;

import de.rangun.webvirus.MainActivity;
import de.rangun.webvirus.R;
import de.rangun.webvirus.model.BitmapMemCache;
import de.rangun.webvirus.model.IMovie;
import de.rangun.webvirus.widgets.CategoryTextView;

public final class MovieDetailsFragment extends Fragment {

    private IResumeListener listener;

    public interface IResumeListener {
        void updateRequested(MovieDetailsFragment f);
    }

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        try {
            listener = (IResumeListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement IResumeListener");
        }
    }

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

    @Override
    public void onResume() {
        super.onResume();
        listener.updateRequested(this);
    }

    public final void setVisibility(int visibility) {

        View v = getView();

        if(v != null) v.setVisibility(visibility);
    }

    public final void setContents(@NonNull IMovie m, RequestQueue queue, int movieCount) {

        final View top250 = Objects.requireNonNull(getView()).findViewById(R.id.top250);
        final TextView mid = getView().findViewById(R.id.m_id);
        final CategoryTextView tit = getView().findViewById(R.id.title);
        final TextView dus = getView().findViewById(R.id.m_duration);
        final TextView dis = getView().findViewById(R.id.m_disc);
        final TextView lan = getView().findViewById(R.id.languages);
        final TextView fin = getView().findViewById(R.id.filename);
        final CategoryTextView cat = getView().findViewById(R.id.category);
        final TextView abs = getView().findViewById(R.id.m_abstract);
        final NetworkImageView cov = getView().findViewById(R.id.cover);
        final Button but = getView().findViewById(R.id.openInDB);

        StringBuilder sb = new StringBuilder();

        for(String l: Objects.requireNonNull(m.languages())) {
            sb.append(l);
            sb.append(", ");
        }

        top250.setVisibility(m.top250() ? View.VISIBLE : View.INVISIBLE);
        mid.setText(MainActivity.makeIdString(m.id(), movieCount));
        tit.setText(m.title(), m.category());
        dus.setText(m.durationString());
        dis.setText(m.disc());
        lan.setText(sb.toString().substring(0, sb.toString().length() - 2));
        fin.setText(m.filename(getContext()));
        cat.setCategoryText(m.category());
        abs.setText(m.description(getContext()));

        but.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://rangun.de/db/?filter_ID=" + m.id()))));

        if (m.oid() != null) {

            cov.setImageUrl("https://rangun.de/db/omdb.php?cover-oid=" + m.oid() +
                            (m.top250() ? "&top250=true" : ""),
                    new ImageLoader(queue, new BitmapMemCache(3072)));
        } else {
            cov.setImageUrl(null, null);
        }
    }
}
