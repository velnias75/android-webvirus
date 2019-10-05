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
 *  Last modified 02.10.19 11:15 by heiko
 */

package de.rangun.webvirus.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import de.rangun.webvirus.R;
import de.rangun.webvirus.widgets.CustomAutoCompleteTextView;
import de.rangun.webvirus.widgets.DrawableClickListener;

public final class SearchBarFragment extends Fragment {

    private IMovieUpdateRequestListener listener;

    private CustomAutoCompleteTextView srt;

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        try {
            listener = (IMovieUpdateRequestListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement IMovieUpdateRequestListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View fragmentLayout = inflater.inflate(R.layout.searchbarfragment, container,
                false);

        final Button search = fragmentLayout.findViewById(R.id.search);

        srt = fragmentLayout.findViewById(R.id.searchTerm);

        if(srt == null) throw new IllegalStateException("serachTerm field is missing");

        srt.setDrawableClickListener(target -> {

            if(target == DrawableClickListener.DrawablePosition.RIGHT) {

                srt.setText(null);
                srt.requestFocus();

                final InputMethodManager imm = (InputMethodManager) Objects.
                        requireNonNull(getActivity()).getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if(imm != null) imm.showSoftInput(srt, 0);
            }
        });

        srt.setOnEditorActionListener((v, actionId, event) -> {

            if(EditorInfo.IME_ACTION_SEARCH == actionId) {
                listener.onUpdateMovieByTitleOrId(v.getText().toString(), this);
                return true;
            }

            return false;
        });

        search.setOnClickListener(v ->
                listener.onUpdateMovieByTitleOrId(srt.getText().toString(), this));

        return fragmentLayout;
    }

    public final void populateCompleter(ArrayAdapter<String> adapter) { srt.setAdapter(adapter); }

    public final void setEnabled(boolean b) {

        final ViewGroup layout = (ViewGroup)getView();

        for(int i = 0; i < Objects.requireNonNull(layout).getChildCount(); ++i) {
            View child = layout.getChildAt(i);
            child.setEnabled(b);
        }

        layout.setEnabled(b);
    }

    public final void setText(@NonNull String title) {
        srt.setText(title);
        srt.setSelection(title.length());
        srt.dismissDropDown();
    }

    public final String getText() { return srt.getText().toString(); }

    public final int getThreshold() { return srt.getThreshold(); }

    public final void hideSoftKeyboard() {

        final InputMethodManager imm =
                (InputMethodManager)Objects.requireNonNull(getContext()).
                        getSystemService(Context.INPUT_METHOD_SERVICE);

        if(imm != null) imm.hideSoftInputFromWindow(srt.getWindowToken(), 0);
    }

    public final void setShowDropdown(boolean b) {
        if(srt != null) srt.setDropDownHeight(b ? ConstraintLayout.LayoutParams.WRAP_CONTENT : 0);
    }
}
