package de.rangun.webvirus;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import de.rangun.webvirus.model.BitmapMemCache;
import de.rangun.webvirus.model.IMovie;
import de.rangun.webvirus.model.IMoviesAvailable;
import de.rangun.webvirus.model.MovieFactory;
import de.rangun.webvirus.model.MovieList;

import static java.lang.Math.ceil;
import static java.lang.Math.log;

public class MainActivity extends AppCompatActivity implements IMoviesAvailable {

    private static final String TAG = "MainActivity";
    private static final double LN10 = log(10);

    private RequestQueue queue = null;
    private TextView status = null;
    private MovieList movies = null;
    private StringBuilder preZeros = null;

    public MainActivity() {
        Log.d(TAG, "MainActivity()");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        queue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);

        final NetworkImageView cov = findViewById(R.id.cover);
        cov.setDefaultImageResId(R.drawable.nocover);

        MovieFactory.instance(this).allMovies(queue);

        final Button search = findViewById(R.id.search);
        search.setOnClickListener(v -> {

            final CustomAutoCompleteTextView textView = findViewById(R.id.searchTerm);

            if(movies != null) {
                updateMovie(movies.findByTitle(textView.getText().toString()));
            }
        });

        final CustomAutoCompleteTextView textView = findViewById(R.id.searchTerm);
        textView.setDrawableClickListener(target -> {
            if (target == DrawableClickListener.DrawablePosition.RIGHT) {
                textView.setText(null);
                textView.requestFocus();
                final InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(textView, 0);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (queue != null) {
            queue.cancelAll(MovieFactory.instance(this).tag());
        }
    }

    private void updateMovie(long id) {
        updateMovie(movies.getByMovieId(id));
    }

    private void updateMovie(IMovie m) {

        if(m == null) {
            status.setText(R.string.notfound);
            return;
        }

        final View top250 = findViewById(R.id.top250);
        final TextView mid = findViewById(R.id.m_id);
        final TextView tit = findViewById(R.id.m_title);
        final TextView dus = findViewById(R.id.m_duration);
        final TextView dis = findViewById(R.id.m_disc);
        final TextView abs = findViewById(R.id.m_abstract);
        final NetworkImageView cov = findViewById(R.id.cover);
        final CustomAutoCompleteTextView srt = findViewById(R.id.searchTerm);

        try {

            final String idNum = preZeros.toString() + m.id();

            top250.setVisibility(m.top250() ? View.VISIBLE : View.INVISIBLE);
            mid.setText(idNum.substring(idNum.length() - preZeros.length()));
            tit.setText(m.title());
            srt.setText(m.title());
            srt.setSelection(m.title().length());
            srt.dismissDropDown();
            dus.setText(m.durationString());
            dis.setText(m.disc());
            abs.setText(m.description());

            final InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(srt.getWindowToken(), 0);

            if (m.oid() != null) {

                cov.setImageUrl("https://rangun.de/db/omdb.php?cover-oid=" + m.oid() +
                                (m.top250() ? "&top250=true" : ""),
                        new ImageLoader(queue, new BitmapMemCache()));
            } else {
                cov.setImageUrl(null, null);
            }

        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Override
    public void loading() {
        status.setText(R.string.loading);
    }

    @Override
    public void loaded(int num) {

        status.setText("" + num + " hirnlose Schrott- oder Rentnerfilme geladen.");

        preZeros = new StringBuilder();

        for (int i = 0; i < ceil(log(num) / LN10); ++i) {
            preZeros.append('0');
        }

        updateMovie(1);
    }

    @Override
    public void movies(MovieList ml) {

        final Spinner spinner = findViewById(R.id.searchTermSpinner);
        final CustomAutoCompleteTextView textView = findViewById(R.id.searchTerm);

        movies = ml;

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, movies.titles());

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinner.setAdapter(adapter);
        textView.setAdapter(adapter);

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

    @Override
    public void error(String localizedMessage) {
        status.setText(localizedMessage);
    }

    @Override
    public void fetchDescription(StringRequest rq) {
        queue.add(rq);
    }

    @Override
    public void descriptionAvailable(String dsc) {
        ((TextView)findViewById(R.id.m_abstract)).setText(dsc);
    }
}
