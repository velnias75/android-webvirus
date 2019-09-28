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
 *  Last modified 28.09.19 04:32 by heiko
 */

package de.rangun.webvirus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import de.rangun.webvirus.model.MovieBKTree;
import de.rangun.webvirus.model.MovieFactory;

public class MovieFetcherService extends Service implements MovieFactory.OnMoviesAvailableListener {

    private static final String TAG = "CheckMoviesService";

    private static final String CHANNEL_DEFAULT = "de.rangun.webvirus.notifications.default";
    private static final String CHANNEL_HIGH = "de.rangun.webvirus.notifications.high";
    private static final String CHANNEL_LOW = "de.rangun.webvirus.notifications.low";
    private static final String CHANNEL_MIN = "de.rangun.webvirus.notifications.min";

    private RequestQueue queue = null;
    private MovieBKTree movies = null;
    private final IBinder binder = new MovieFetcherBinder();
    private MovieFactory.OnMoviesAvailableListener listener;

    enum NOTIFICATION {

        NONE(-1),
        NEW(0, android.R.drawable.stat_notify_sync,
                NotificationCompat.PRIORITY_HIGH, CHANNEL_HIGH),
        ERROR(1, android.R.drawable.stat_notify_error, NotificationCompat.PRIORITY_HIGH,
                CHANNEL_HIGH),
        LOADED(2, android.R.drawable.stat_notify_sync, NotificationCompat.PRIORITY_MIN,
                CHANNEL_MIN, 10000L),
        NOTFOUND(3, android.R.drawable.stat_notify_error, NotificationCompat.PRIORITY_LOW,
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

    public class MovieFetcherBinder extends Binder {
        MovieFetcherService getService() {
            return MovieFetcherService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createNotificationChannel();
        return binder;
    }

    @Override
    public void onDestroy() {
        if (queue != null) queue.stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand");

        createNotificationChannel();

        getQueue();
        fetchMovies();

        return START_REDELIVER_INTENT;
    }

    public RequestQueue getQueue() {
        if (queue == null) queue = Volley.newRequestQueue(this);
        return queue;
    }

    public void setOnMoviesAvailableListener(MovieFactory.OnMoviesAvailableListener l) {
        listener = l;
    }

    public void fetchMovies() {
        Log.d(TAG, "fetchMovies");

        MovieFactory.instance().setOnMoviesAvailableListener(this);
        MovieFactory.instance().fetchMovies(getQueue());
    }

    public MovieBKTree getMovies() {
        return movies;
    }

    @Override
    public void loading() { if(listener != null) listener.loading(); }

    @Override
    public void loaded(int num) { if(listener != null) listener.loaded(num); }

    @Override
    public void movies(MovieBKTree movies) {

        this.movies = movies;

        final SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        int lastMovieCount = sharedPrefs.getInt("lastMovieCount", 0);
        Log.d(TAG, "(before fetch) lastMovieCount=" + lastMovieCount);

        if(movies.size() > lastMovieCount) {

            notify(getString(R.string.new_movies, movies.size() - lastMovieCount),
                    NOTIFICATION.NEW);

            lastMovieCount = movies.size();

            sharedPrefs.edit().putInt("lastMovieCount", lastMovieCount).apply();

            Log.d(TAG, "(after fetch) lastMovieCount=" + lastMovieCount);

        } else Log.d(TAG, "(after fetch) lastMovieCount unchanged");

        //sharedPrefs.edit().putInt("lastMovieCount", 3200).apply();

        if(listener != null) listener.movies(this.movies);
    }

    @Override
    public void error(String localizedMessage) {
        if(listener != null) listener.error(localizedMessage);
    }

    @Override
    public void fetchDescription(StringRequest rq) {
        getQueue().add(rq);

        if(listener != null) listener.fetchDescription(rq);
    }

    @Override
    public void descriptionAvailable(String dsc) {
        if(listener != null) listener.descriptionAvailable(dsc);
    }

    public void notify(String txt, NOTIFICATION notification) {

        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this,
                        notification.getChannel()).
                        setSmallIcon(notification.getIcon())
                        .setContentTitle(txt)
                        .setPriority(notification.getPriority());

        if(notification.getDuration() != null) {
            builder.setTimeoutAfter(notification.getDuration());
        }

        final NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        notificationManager.notify(notification.getId(), builder.build());
    }

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
}
