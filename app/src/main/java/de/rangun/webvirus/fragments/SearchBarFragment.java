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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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

        final CustomAutoCompleteTextView textView = fragmentLayout.findViewById(R.id.searchTerm);
        final Spinner spinner = fragmentLayout.findViewById(R.id.searchTermSpinner);
        final Button search = fragmentLayout.findViewById(R.id.search);

        textView.setDrawableClickListener(target -> {
            if (target == DrawableClickListener.DrawablePosition.RIGHT) {

                textView.setText(null);
                textView.requestFocus();

                final InputMethodManager imm = (InputMethodManager) Objects.
                        requireNonNull(getActivity()).getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if(imm != null) imm.showSoftInput(textView, 0);
            }
        });

        textView.setOnEditorActionListener((v, actionId, event) -> {

            if(EditorInfo.IME_ACTION_SEARCH == actionId) {
                listener.onUpdateMovieByTitleOrId(v.getText().toString(), this);
                return true;
            }

            return false;
        });

        search.setOnClickListener(v ->
                listener.onUpdateMovieByTitleOrId(textView.getText().toString(), this));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            private void setSelectedItem() {
                final Object item = spinner.getSelectedItem();
                if(item != null) {
                    textView.setText(item.toString());
                    textView.setSelection(item.toString().length());
                }
                textView.dismissDropDown();
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setSelectedItem();
            }
        });

        return fragmentLayout;
    }

    public final void populateCompleter(ArrayAdapter<String> adapter) {

        final Spinner spinner =
                Objects.requireNonNull(getView()).findViewById(R.id.searchTermSpinner);
        final CustomAutoCompleteTextView textView = getView().findViewById(R.id.searchTerm);

        spinner.setAdapter(adapter);
        textView.setAdapter(adapter);
    }

    public final void setEnabled(boolean b) {

        ViewGroup layout = (ViewGroup)getView();

        for(int i = 0; i < Objects.requireNonNull(layout).getChildCount(); ++i) {
            View child = layout.getChildAt(i);
            child.setEnabled(b);
        }

        layout.setEnabled(b);
    }

    public final void setText(@NonNull String title) {

        final CustomAutoCompleteTextView srt =
                Objects.requireNonNull(getView()).findViewById(R.id.searchTerm);

        srt.setText(title);
        srt.setSelection(title.length());
        srt.dismissDropDown();
    }

    public final void hideSoftKeyboard() {

        final CustomAutoCompleteTextView srt =
                Objects.requireNonNull(getView()).findViewById(R.id.searchTerm);
        final InputMethodManager imm =
                (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(
                Context.INPUT_METHOD_SERVICE);

        if(imm != null) imm.hideSoftInputFromWindow(srt.getWindowToken(), 0);
    }

    public final void setShowDropdown(boolean b) {
        final CustomAutoCompleteTextView textView = Objects.requireNonNull(getView()).
                findViewById(R.id.searchTerm);
        if(textView != null) textView.setDropDownHeight(b ?
                ConstraintLayout.LayoutParams.WRAP_CONTENT : 0);
    }
}
