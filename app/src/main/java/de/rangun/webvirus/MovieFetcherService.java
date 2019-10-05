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
 *  Last modified 02.10.19 13:57 by heiko
 */

package de.rangun.webvirus;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Objects;

import de.rangun.webvirus.model.IMovie;
import de.rangun.webvirus.model.MovieBKTree;
import de.rangun.webvirus.model.MovieFactory;

public final class MovieFetcherService extends Service
        implements MovieFactory.IMoviesAvailableListener {

    private static final String TAG = "MovieFetcherService";

    private static final String CHANNEL_DEFAULT = "de.rangun.webvirus.notifications.default";
    private static final String CHANNEL_HIGH = "de.rangun.webvirus.notifications.high";
    private static final String CHANNEL_LOW = "de.rangun.webvirus.notifications.low";
    private static final String CHANNEL_MIN = "de.rangun.webvirus.notifications.min";

    @Nullable
    private RequestQueue queue = null;
    private final IBinder binder = new MovieFetcherBinder();
    private MovieFactory.IMoviesAvailableListener listener;

    enum NOTIFICATION {

        NONE(),
        NEW(0, android.R.drawable.stat_notify_sync
        ),
        ERROR(1, android.R.drawable.stat_notify_error
        ),
        LOADED(2, android.R.drawable.stat_notify_sync, NotificationCompat.PRIORITY_MIN,
                CHANNEL_MIN, 10000L),
        NOTFOUND(3, android.R.drawable.stat_notify_error, NotificationCompat.PRIORITY_LOW,
                CHANNEL_LOW, 10000L);

        private final int id;
        private final int prio;
        private final int icon;
        private final String chan;
        private final Long duration;

        NOTIFICATION() {
            this(-1, android.R.drawable.stat_notify_sync,
                    NotificationCompat.PRIORITY_DEFAULT, CHANNEL_DEFAULT, null);
        }

// --Commented out by Inspection START (28.09.19 03:47):
//        NOTIFICATION(int id, int icon) {
//            this(id, icon, NotificationCompat.PRIORITY_DEFAULT, CHANNEL_DEFAULT, null);
//        }
// --Commented out by Inspection STOP (28.09.19 03:47)

        NOTIFICATION(int id, int icon) {
            this(id, icon, NotificationCompat.PRIORITY_HIGH, MovieFetcherService.CHANNEL_HIGH,
                    null);
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
        @NonNull
        MovieFetcherService getService() {
            return MovieFetcherService.this;
        }
    }

    @NonNull
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
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand");

        createNotificationChannel();

        getQueue();
        fetchMovies(true);

        startPeriodicFetch(intent);

        return START_REDELIVER_INTENT;
    }

    private void startPeriodicFetch(@NonNull Intent intent) {

        final AlarmManager alarmMgr =
                (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        if(alarmMgr != null) {
            final PendingIntent alarmIntent = PendingIntent.getService(this, 0,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Log.d(TAG, "periodically fetching movies");

            final long interval = AlarmManager.INTERVAL_HALF_DAY; //(1000 * 3600) * 6;

            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + interval, interval, alarmIntent);

        } else Log.d(TAG, "NOT periodically fetching movies");
    }

    @NonNull
    public RequestQueue getQueue() {
        if (queue == null) queue = Volley.newRequestQueue(this);
        return queue;
    }

    public void setOnMoviesAvailableListener(MovieFactory.IMoviesAvailableListener l) {
        listener = l;
    }

    @SuppressWarnings("deprecation")
    public void fetchMovies(boolean silent) {

        Log.d(TAG, "fetchMovies");

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        Objects.requireNonNull(MovieFactory.instance()).setOnMoviesAvailableListener(this);

        if(cm != null) {

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork != null && activeNetwork.isConnected()) {
                Objects.requireNonNull(MovieFactory.instance()).
                        fetchMovies(Objects.requireNonNull(getQueue()), silent);
            } else if(!silent) {
                error(getString(R.string.not_connected));
            }
        }
    }

    @Override
    public void loading(boolean silent) { if(listener != null) listener.loading(silent); }

    @Override
    public void loaded(int num, boolean silent) {
        if(listener != null) listener.loaded(num, silent);
    }

    @Override
    public void movies(@NonNull MovieBKTree movies, Long latestCoverId, boolean silent) {

        final SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        int lastMovieCount = sharedPrefs.getInt("lastMovieCount", 0);

        final int newMoviesSeen = sharedPrefs.getInt("newMoviesSeen", lastMovieCount);
        final int nm = movies.size() - newMoviesSeen;

        for(IMovie m: movies) m.setNewMovie(m.id() > newMoviesSeen);

        Log.d(TAG, "(before fetch) lastMovieCount=" + lastMovieCount);

        if(movies.size() > lastMovieCount) {

            final Long oid = Objects.requireNonNull(movies.getByMovieId(latestCoverId)).oid();
            final int lmc = movies.size() - lastMovieCount;

            lastMovieCount = movies.size();

            if(oid != null) {
                Objects.requireNonNull(getQueue()).add(new ImageRequest(
                        "https://rangun.de/db/omdb.php?cover-oid=" + oid,
                        bitmap -> notifyInternal(getString(R.string.new_movies,
                                lmc), NOTIFICATION.NEW,
                                bitmap, silent),
                        getResources().getDimensionPixelSize(android.R.dimen.
                                notification_large_icon_width),
                        getResources().getDimensionPixelSize(android.R.dimen.
                                notification_large_icon_height),
                        ImageView.ScaleType.FIT_START, Bitmap.Config.RGB_565,
                        error -> notifyInternal(getString(R.string.new_movies,
                                lmc),
                                NOTIFICATION.NEW, null, silent)));
            } else notifyInternal(getString(R.string.new_movies,
                    lmc),
                    NOTIFICATION.NEW, null, silent);

            sharedPrefs.edit().putInt("lastMovieCount", lastMovieCount).apply();

            Log.d(TAG, "(after fetch) lastMovieCount=" + lastMovieCount);

        } else Log.d(TAG, "(after fetch) lastMovieCount unchanged");

        if(listener != null) {
            listener.movies(movies, latestCoverId, silent);
            listener.newMoviesAvailable(nm);
        }
    }

    @Override
    public void newMoviesAvailable(int num) {}

    @Override
    public void unproxied(IMovie oldProxy, IMovie newInstance) {
        if(listener != null) listener.unproxied(oldProxy, newInstance);
    }

    @Override
    public void error(String localizedMessage) {
        if(listener != null) listener.error(localizedMessage);
    }

    @Override
    public void fetchDescription(@NonNull StringRequest rq) {
        Objects.requireNonNull(getQueue()).add(rq);
        if(listener != null) listener.fetchDescription(rq);
    }

    @Override
    public void descriptionAvailable(String dsc) {
        if(listener != null) listener.descriptionAvailable(dsc);
    }

    public void notify(String txt, @NonNull NOTIFICATION notification) {
        notifyInternal(txt, notification, null, false);
    }

    private void notifyInternal(String txt, @NonNull NOTIFICATION notification, Bitmap bm,
                                boolean tap) {

        final Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this,
                        notification.getChannel())
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSmallIcon(notification.getIcon())
                        .setContentTitle(txt)
                        .setPriority(notification.getPriority())
                        .setAutoCancel(true);

        if(notification.getDuration() != null) {
            builder.setTimeoutAfter(notification.getDuration());
        }

        if(tap) builder.setContentIntent(pendingIntent);

        if(bm != null) builder.setLargeIcon(bm);

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
