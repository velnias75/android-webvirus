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
 *  Last modified 30.12.19 04:01 by heiko
 */

package de.rangun.webvirus.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import javax.annotation.Nonnull;

import de.rangun.webvirus.MainActivity;
import de.rangun.webvirus.R;
import de.rangun.webvirus.Toaster;
import de.rangun.webvirus.model.db.AppDatabase;
import de.rangun.webvirus.model.db.AsyncMarkerUpdateTask;
import de.rangun.webvirus.model.db.AsyncMovieFetcherTask;
import de.rangun.webvirus.model.db.Movie;
import de.rangun.webvirus.model.movie.IMovie;
import de.rangun.webvirus.widgets.CategoryTextView;

public final class MovieDetailsFragment extends Fragment
        implements AsyncMovieFetcherTask.IMovieReceiver {

    private final Toaster toaster;

    private IResumeListener listener;
    private boolean doUpdate = false;

    @NonNull
    private final BitmapMemCache bmc = new BitmapMemCache(3072);

    @Nonnull
    private final AppDatabase db;

    public MovieDetailsFragment(Toaster toaster, @Nonnull AppDatabase db) {
        this.toaster = toaster;
        this.db = db;
    }

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
        final Spinner mrk = fragmentLayout.findViewById(R.id.marker);

        mrk.setAdapter(new MarkerSpinnerAdapter(Objects.requireNonNull(getContext()),
                R.layout.marker_spinner_row, inflater));

        mrk.setSelection(1);

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        if(displayMetrics.widthPixels <= 720) {

            fragmentLayout.findViewById(R.id.openInDB).setVisibility(View.GONE);

            ((TextView)fragmentLayout.findViewById(R.id.m_duration)).
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
            ((TextView)fragmentLayout.findViewById(R.id.m_disc)).
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
            ((TextView)fragmentLayout.findViewById(R.id.languages)).
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0f);
            ((TextView)fragmentLayout.findViewById(R.id.filename)).
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0f);
            ((TextView)fragmentLayout.findViewById(R.id.m_abstract)).
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0f);
        }

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

        (new AsyncMovieFetcherTask<>(this, db, m.id())).execute();

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
        final Button oib = getView().findViewById(R.id.openInDB);
        final Button cpy = getView().findViewById(R.id.copyURL);
        final Spinner mrk = getView().findViewById(R.id.marker);

        StringBuilder sb = new StringBuilder();

        for(String l: Objects.requireNonNull(m.languages())) sb.append(l).append(", ");

        top250.setVisibility(m.top250() ? View.VISIBLE : View.INVISIBLE);
        mid.setText(MainActivity.makeIdString(m.id(), movieCount));
        tit.setText(m.title(), m.category());
        dus.setText(m.durationString());
        dis.setText(m.disc());
        lan.setText(sb.toString().substring(0, sb.toString().length() - 2));
        fin.setText(m.filename(getContext()));
        cat.setCategoryText(m.category());
        abs.setText(m.description(getContext()));

        mrk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(doUpdate) {
                    (new AsyncMarkerUpdateTask<>(MovieDetailsFragment.this,
                            db, m.id(), position)).execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        oib.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://rangun.de/db/?filter_ID=" + m.id()))));

        cpy.setOnClickListener(v -> {

            ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(getActivity()).
                    getSystemService(Context.CLIPBOARD_SERVICE);

            if(clipboard != null) {

                clipboard.setPrimaryClip(ClipData.newPlainText(m.title(),
                        "https://rangun.de/db/video/" + m.id()));

                toaster.show(Objects.requireNonNull(getContext()).
                                getResources().getString(R.string.url_copy_ok),
                        Color.BLACK);

            } else {
                toaster.show(Objects.requireNonNull(getContext()).getResources().getString(R.string.url_copy_fail),
                        Color.RED);
            }
        });

        try {
            cov.setImageUrl("https://rangun.de/db/omdb.php?cover-oid=" +
                    (m.tmdb_id() != null ? ("&tmdb_type=" + m.tmdb_type() +
                            "&tmdb_id=" + m.tmdb_id()) : "&fallback=" +
                            URLEncoder.encode(m.title(), "UTF-8")) +
                    (m.top250() ? "&top250=true" : ""), new ImageLoader(queue, bmc));
        } catch(UnsupportedEncodingException e) {
            cov.setImageUrl(null, null);
        }
    }

    @Override
    public void onMovieReceived(@Nullable Movie movie) {

        try {

            doUpdate = false;

            final Spinner mrk = Objects.requireNonNull(getView()).findViewById(R.id.marker);

            if (movie == null) {
                mrk.setSelection(1);
            } else {
                mrk.setSelection(movie.marker);
            }

        } finally {
            doUpdate = true;
        }
    }

    public void clearBitmapMemCache() { bmc.evictAll(); }
}
