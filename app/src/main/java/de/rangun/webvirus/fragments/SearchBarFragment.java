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
 *  Last modified 23.09.19 11:59 by heiko
 */

package de.rangun.webvirus.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.rangun.webvirus.CustomAutoCompleteTextView;
import de.rangun.webvirus.DrawableClickListener;
import de.rangun.webvirus.R;

public class SearchBarFragment extends Fragment {

    private iface listener;

    public interface iface {
        void updateMovieByTitle(String title);
    }

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        try {
            listener = (iface)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement OnArticleSelectedListener");
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View fragmentLayout = inflater.inflate(R.layout.searchbarfragment, container,
                false);

        final CustomAutoCompleteTextView textView = fragmentLayout.findViewById(R.id.searchTerm);
        textView.setDrawableClickListener(target -> {
            if (target == DrawableClickListener.DrawablePosition.RIGHT) {
                textView.setText(null);
                textView.requestFocus();
                final InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(textView, 0);
            }
        });

        return fragmentLayout;
    }

    public void populateCompleter(ArrayAdapter<String> adapter) {

        final Spinner spinner = getView().findViewById(R.id.searchTermSpinner);
        final CustomAutoCompleteTextView textView = getView().findViewById(R.id.searchTerm);
        final Button search = getView().findViewById(R.id.search);

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinner.setAdapter(adapter);
        textView.setAdapter(adapter);

        search.setOnClickListener(v -> listener.updateMovieByTitle(textView.getText().toString()));

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
    }
}
