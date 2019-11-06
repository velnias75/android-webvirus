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
 *  Last modified 30.09.19 10:30 by heiko
 */

package de.rangun.webvirus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings
public class autostart extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent rqIntent) {

        if(Intent.ACTION_BOOT_COMPLETED.equals(rqIntent.getAction())) {

            final AlarmManager alarmMgr =
                    (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            if(alarmMgr != null) {

                final Intent aIntent = new Intent(context, AlarmReceiver.class);
                final PendingIntent alarmIntent = PendingIntent.getBroadcast(context,
                        0, aIntent, 0);

                Log.d("autostart", "now periodically fetching movies");

                final long interval = AlarmManager.INTERVAL_HALF_DAY; //(1000 * 3600) * 6;

                alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + interval, interval,
                        alarmIntent);

            } else {
                Log.d("autostart", "NOT periodically fetching movies");
            }

            Log.d("autostart", "initial \"boot completed\" fetch");

            final Intent intent = new Intent(context, MovieFetcherService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }
}
