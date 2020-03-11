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
 *  Last modified 13.12.19 06:47 by heiko
 */

package de.rangun.webvirus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import de.rangun.webvirus.model.bktree.MovieBKTree;
import de.rangun.webvirus.model.movie.IMovie;
import de.rangun.webvirus.model.movie.MovieFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static com.android.volley.toolbox.Volley.newRequestQueue;

public final class MovieFetcherService extends Service
        implements MovieFactory.IMoviesAvailableListener {

    private static final String TAG = "MovieFetcherService";
    private static final String OFFLINE_FILENAME = "schrottfilme.json.gz";

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
        NEW(0, android.R.drawable.stat_notify_sync),
        ERROR(1, android.R.drawable.stat_notify_error),
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

        int getId() { return id; }

        int getPriority() { return prio; }

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

    private final static class OfflineFetchTask extends AsyncTask<File, Void,
            MovieFactory.CallbackTransfer> {

        private final MovieFactory.IMoviesAvailableListener listener;
        private final boolean silent;

        OfflineFetchTask(@NonNull MovieFactory.IMoviesAvailableListener listener,
                         boolean silent) {
            this.listener = listener;
            this.silent = silent;
        }

        @Override
        protected void onPreExecute() {
            listener.loading(silent);
        }

        @Override
        protected MovieFactory.CallbackTransfer doInBackground(File... files) {

            try {

                final FileInputStream fis = new FileInputStream(files[0]);
                final StringBuilder output = new StringBuilder();
                final GZIPInputStream gStream = new GZIPInputStream(fis);
                final InputStreamReader reader = new InputStreamReader(gStream,
                        StandardCharsets.UTF_8);
                final BufferedReader in = new BufferedReader(reader, 65536);

                String read;

                while((read = in.readLine()) != null) { output.append(read); }

                reader.close();
                in.close();
                gStream.close();
                fis.close();

                return Objects.requireNonNull(MovieFactory.instance()).fetchMovies(output);

            } catch(IOException ex) {
                Log.w(TAG, ex);
            }

            return null;
        }

        @Override
        protected void onPostExecute(MovieFactory.CallbackTransfer callbackTransfer) {

            if(callbackTransfer != null) {
                listener.movies(callbackTransfer.movies, callbackTransfer.lid, silent);
                listener.loaded(callbackTransfer.movies.size(), silent);
            }
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
    public boolean onUnbind(Intent intent) {
        return true; // to keep volley in an sane state
    }

    @Override
    public void onDestroy() {
        destroyQueue();
    }

    @NonNull
    public RequestQueue getQueue() {
        if (queue == null) queue = newRequestQueue(this);
        return queue;
    }

    private void destroyQueue() {
        if (queue != null) queue.stop();
        queue = null;
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand");

        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        startForeground(1, (new NotificationCompat.Builder(this,
                MovieFetcherService.CHANNEL_LOW))
                .setContentText(getString(R.string.fetching))
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build());

        fetchMovies(true);

        return START_REDELIVER_INTENT;
    }

    public void setOnMoviesAvailableListener(MovieFactory.IMoviesAvailableListener l) {
        listener = l;
    }

    @SuppressFBWarnings
    public void fetchMovies(boolean silent) {

        Log.d(TAG, "fetchMovies: trying...");

        final ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm != null) {

            final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            final File staleFile = new File(getExternalFilesDir(null),
                    "schrottfilme.gz");

            if(staleFile.exists()) {
                if(staleFile.delete()) {
                    Log.d(TAG,"file deleted: " + staleFile.getAbsolutePath());
                } else {
                    Log.d(TAG,"file NOT deleted: " + staleFile.getAbsolutePath());
                }
            }

            FileOutputStream fos = null;
            final File file = new File(getExternalFilesDir(null), OFFLINE_FILENAME);

            if(activeNetwork != null && activeNetwork.isConnected()) {

                Log.d(TAG, "fetchMovies: ok, now lets go to the MovieFactory");

                try {
                    fos = new FileOutputStream(file);
                    Log.d(TAG, "writing offline data to: " + file.getAbsolutePath());
                } catch(FileNotFoundException ex) {
                    Log.w(TAG, ex);
                }

                Objects.requireNonNull(MovieFactory.instance()).setOnMoviesAvailableListener(this);
                Objects.requireNonNull(MovieFactory.instance()).
                        fetchMovies(Objects.requireNonNull(getQueue()), silent, fos);

            } else {

                if(file.exists() && file.length() > 0) {
                    (new OfflineFetchTask(this, silent)).execute(file);
                } else if(!silent) {
                    error(getString(R.string.not_connected));
                } else Log.d(TAG, "NOT connected yet, hoping for first periodic fetch");
            }

        } else Log.d(TAG, "NO ConnectivityManager!");
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

        final int newMoviesSeen = sharedPrefs.getInt("newMoviesSeen", 0);
        final int nm = movies.size() - newMoviesSeen;

        for(IMovie m: movies) m.setNewMovie(m.id() > newMoviesSeen);

        Log.d(TAG, "(before fetch) lastMovieCount=" + lastMovieCount);

        if(movies.size() > lastMovieCount) {

            final IMovie latestCoverMovie = movies.getByMovieId(latestCoverId);
            final Long   tid = latestCoverMovie != null ? latestCoverMovie.tmdb_id() : null;
            final String ttp = latestCoverMovie != null ? latestCoverMovie.tmdb_type() : null;
            final int    lmc = movies.size() - lastMovieCount;

            lastMovieCount = movies.size();

            final Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("requestCode", MainActivity.SHOW_NEW_MOVIES_REQUEST);
            final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(intent);

            final PendingIntent pendingIntent =
                    stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            if(tid != null) {

                Objects.requireNonNull(getQueue()).add(new ImageRequest(
                        "https://rangun.de/db/omdb.php?cover-oid=&tmdb_type=" + ttp +
                                "&tmdb_id=" + tid,
                        bitmap -> notifyInternal(getString(R.string.new_movies, lmc),
                                NOTIFICATION.NEW,
                                bitmap, silent ? null : pendingIntent),
                        getResources().getDimensionPixelSize(android.R.dimen.
                                notification_large_icon_width),
                        getResources().getDimensionPixelSize(android.R.dimen.
                                notification_large_icon_height),
                        ImageView.ScaleType.FIT_START, Bitmap.Config.RGB_565,
                        error -> notifyInternal(getString(R.string.new_movies,
                                lmc),
                                NOTIFICATION.NEW, null, null)));
            } else notifyInternal(getString(R.string.new_movies, lmc),
                    NOTIFICATION.NEW, null, silent ? null : pendingIntent);

            sharedPrefs.edit().putInt("lastMovieCount", lastMovieCount).apply();

            Log.d(TAG, "(after fetch) lastMovieCount=" + lastMovieCount);

        } else Log.d(TAG, "(after fetch) lastMovieCount unchanged");

        if(listener != null) {
            listener.movies(movies, latestCoverId, silent);
            listener.newMoviesAvailable(nm);
        }

        if(silent) stopForeground(true);
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
        notifyInternal(txt, notification, null, null);
    }

    private Notification notificationBuilder(String txt, @NonNull NOTIFICATION notification,
                                             Bitmap bm, PendingIntent tapIntent) {

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

        if(tapIntent != null) builder.setContentIntent(tapIntent);

        if(bm != null) builder.setLargeIcon(bm);

        if(notification.getPriority() == NotificationCompat.PRIORITY_HIGH) {
            final Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    getPackageName() + "/" + R.raw.aargh_catherine);

            builder.setDefaults(NotificationCompat.DEFAULT_ALL
                    &~NotificationCompat.DEFAULT_SOUND);

            builder.setSound(alarmSound, AudioManager.STREAM_ALARM);
        }

        return builder.build();
    }

    private void notifyInternal(String txt, @NonNull NOTIFICATION notification, Bitmap bm,
                                PendingIntent tapIntent) {

        final NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        notificationManager.notify(notification.getId(),
                notificationBuilder(txt, notification, bm, tapIntent));
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

                final Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        getPackageName() + "/" + R.raw.aargh_catherine);
                final AudioAttributes aatt = (new AudioAttributes.Builder()).
                        setUsage(AudioAttributes.USAGE_NOTIFICATION).
                        build();

                channel2.setDescription(getString(R.string.notification_high_desc));
                channel2.setSound(alarmSound, aatt);
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
