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
 *  Last modified 23.09.19 06:31 by heiko
 */

package de.rangun.webvirus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import de.rangun.webvirus.fragments.MovieDetailsFragment;
import de.rangun.webvirus.fragments.SearchBarFragment;
import de.rangun.webvirus.model.IMovie;
import de.rangun.webvirus.model.MovieBKTree;
import de.rangun.webvirus.model.MovieBKTreeAdapter;
import de.rangun.webvirus.model.MovieFactory;

import static java.lang.Math.ceil;
import static java.lang.Math.log;

public class MainActivity extends AppCompatActivity implements
        MovieFactory.OnMoviesAvailableListener,
        SearchBarFragment.OnMovieUpdateRequestListener {

    private static final String CHANNEL_DEFAULT = "de.rangun.webvirus.notifications.default";
    private static final String CHANNEL_HIGH = "de.rangun.webvirus.notifications.high";
    private static final String CHANNEL_LOW = "de.rangun.webvirus.notifications.low";
    private static final String CHANNEL_MIN = "de.rangun.webvirus.notifications.min";

    private static final String TAG = "MainActivity";
    private static final double LN10 = log(10);

    private enum NOTIFICATION {

        NONE(-1),
        ERROR(0, android.R.drawable.stat_notify_error, NotificationCompat.PRIORITY_HIGH,
                CHANNEL_HIGH),
        LOADED(1, android.R.drawable.stat_notify_sync, NotificationCompat.PRIORITY_MIN,
                CHANNEL_MIN, 10000L),
        NOTFOUND(2, android.R.drawable.stat_notify_error, NotificationCompat.PRIORITY_LOW,
                CHANNEL_LOW, 5000L);

        private final int id;
        private final int prio;
        private final int icon;
        private final String chan;
        private final Long duration;

        NOTIFICATION(int id) {
            this(id, android.R.drawable.stat_notify_sync,
                    NotificationCompat.PRIORITY_DEFAULT, CHANNEL_DEFAULT, null);
        }

// --Commented out by Inspection START (28.09.19 03:47):
//        NOTIFICATION(int id, int icon) {
//            this(id, icon, NotificationCompat.PRIORITY_DEFAULT, CHANNEL_DEFAULT, null);
//        }
// --Commented out by Inspection STOP (28.09.19 03:47)

        NOTIFICATION(int id, int icon, int prio, String chan) {
            this(id, icon, prio, chan, null);
        }

        NOTIFICATION(int id, int icon, int prio, String chan, Long duration) {
            this.id = id;
            this.prio = prio;
            this.icon = icon;
            this.chan = chan;
            this.duration = duration;
        }

        int getId() {
            return id;
        }

        int getPriority() {
            return prio;
        }

        int getIcon() {
            return icon;
        }

        String getChannel() {
            return chan;
        }

        Long getDuration() {
            return duration;
        }
    }

    private RequestQueue queue = null;
    private TextView status = null;
    private MovieBKTree movies = null;
    private StringBuilder preZeros = null;
    private Long currentId = null;

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            if(notificationManager != null) {

                final NotificationChannel channel1 = new NotificationChannel(CHANNEL_DEFAULT,
                        getString(R.string.notification_default_name),
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel1.setDescription(getString(R.string.notification_default_desc));
                notificationManager.createNotificationChannel(channel1);

                final NotificationChannel channel2 = new NotificationChannel(CHANNEL_HIGH,
                        getString(R.string.notification_high_name),
                        NotificationManager.IMPORTANCE_HIGH);
                channel2.setDescription(getString(R.string.notification_high_desc));
                notificationManager.createNotificationChannel(channel2);

                final NotificationChannel channel3 = new NotificationChannel(CHANNEL_MIN,
                        getString(R.string.notification_min_name),
                        NotificationManager.IMPORTANCE_MIN);
                channel3.setDescription(getString(R.string.notification_min_desc));
                notificationManager.createNotificationChannel(channel3);

                final NotificationChannel channel4 = new NotificationChannel(CHANNEL_LOW,
                        getString(R.string.notification_low_name),
                        NotificationManager.IMPORTANCE_LOW);
                channel4.setDescription(getString(R.string.notification_low_desc));
                notificationManager.createNotificationChannel(channel4);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            currentId = savedInstanceState.getLong("currentId", -1L);

            if (currentId == -1L) currentId = null;
        }

        setContentView(R.layout.activity_main);
        createNotificationChannel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbarmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_reload) {
            MovieFactory.instance().fetchMovies(queue);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        queue.stop();
        movies = null;

        super.onDestroy();
    }

    @Override
    protected void onResume() {

        super.onResume();

        status = findViewById(R.id.status);

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm != null) {

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork != null && activeNetwork.isConnected()) {

                if (queue == null) queue = Volley.newRequestQueue(this);

                if (movies == null) {
                    MovieFactory.instance().setOnMoviesAvailableListener(this);
                    MovieFactory.instance().fetchMovies(queue);
                }

            } else {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                setStatus(R.string.not_connected);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        if(currentId != null) outState.putLong("currentId", currentId);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        currentId = savedInstanceState.getLong("currentId", -1L);

        if(currentId == -1L) currentId = null;
    }

    @Override
    protected void onStop() {

        super.onStop();

        if (queue != null) {
            queue.cancelAll(MovieFactory.instance().tag());
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onUpdateMovieByTitleOrId(String text, SearchBarFragment sbf) {

        if(movies != null) {
            updateMovie(movies.findByTitleOrId(text));
        } else {
            sbf.hideSoftKeyboard();
        }
    }

    private void updateMovie(long id) {
        try {
            updateMovie(movies.getByMovieId(id));
        } catch (IndexOutOfBoundsException ex) {
            Log.d(TAG, "Exc: " + ex.getMessage());
        }
    }

    private void updateMovie(IMovie m) {

        final SearchBarFragment sbf =
                (SearchBarFragment) getSupportFragmentManager().
                        findFragmentById(R.id.searchBar);

        final MovieDetailsFragment mdf =
                (MovieDetailsFragment) getSupportFragmentManager().
                        findFragmentById(R.id.moviedetailsfragment);

        if(sbf != null) sbf.hideSoftKeyboard();

        if(m == null) {
            if(mdf != null) mdf.setVisibility(View.GONE);
            error(getString(R.string.notfound), NOTIFICATION.NOTFOUND);
            return;
        } else {
            if(mdf != null) mdf.setVisibility(View.VISIBLE);
            setStatus(getString(R.string.loaded, movies.size()));
        }

        currentId = m.id();

        if(mdf != null) mdf.setContents(m, queue, preZeros.toString());
        if(sbf != null) sbf.setText(m.title());
    }

    @Override
    public void loading() {
        setStatus(R.string.loading);
    }

    @Override
    public void loaded(int num) {

        setStatus(getString(R.string.loaded, num), NOTIFICATION.LOADED);

        preZeros = new StringBuilder();

        for (int i = 0; i < ceil(log(num) / LN10); ++i) preZeros.append('0');

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        updateMovie(currentId != null ? currentId : 1L);
    }

    @Override
    public void movies(MovieBKTree ml) {

        movies = ml;

        final SearchBarFragment sbf =
                (SearchBarFragment)getSupportFragmentManager().findFragmentById(R.id.searchBar);

        if(sbf != null) {

            final ArrayAdapter<String> adapter = new MovieBKTreeAdapter(this,
                    R.layout.searchsuggestions, movies);

            sbf.populateCompleter(adapter);
        }
    }

    @Override
    public void error(String msg) {
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        error(getString(R.string.network_error, msg), NOTIFICATION.ERROR);
    }

    private void error(String msg, NOTIFICATION notification) {
        setStatus(msg, Color.RED, notification);
    }

    private void setStatus(int resource) {
        setStatus(getString(resource), Color.GRAY, NOTIFICATION.NONE);
    }

    private void setStatus(String txt) {
        setStatus(txt, Color.GRAY, NOTIFICATION.NONE);
    }

    private void setStatus(String txt, NOTIFICATION notification) {
        setStatus(txt, Color.GRAY, notification);
    }

// --Commented out by Inspection START (28.09.19 03:48):
//    private void setStatus(int resource, NOTIFICATION notification) {
//        setStatus(getString(resource), Color.GRAY, notification);
//    }
// --Commented out by Inspection STOP (28.09.19 03:48)

    private void setStatus(String txt, int color, NOTIFICATION notification) {

        status.setTextColor(color);
        status.setText(Html.fromHtml(txt));

        if(NOTIFICATION.NONE != notification) {

            final NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this,
                    notification.getChannel()).
                            setSmallIcon(notification.getIcon())
                            .setContentText(txt)
                            .setPriority(notification.getPriority());

            if(notification.getDuration() != null) {
                builder.setTimeoutAfter(notification.getDuration());
            }

            final NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);
            notificationManager.notify(notification.getId(), builder.build());
        }
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
