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

package de.rangun.webvirus.model;

import android.graphics.Bitmap;
import android.util.LruCache;

import androidx.annotation.NonNull;

import com.android.volley.toolbox.ImageLoader;

 public final class BitmapMemCache extends LruCache<String, Bitmap>
         implements ImageLoader.ImageCache {

    public BitmapMemCache() {
        this((int) (Runtime.getRuntime().maxMemory() >> 10) >> 3);
    }

    private BitmapMemCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }

    @Override
    protected int sizeOf(String key, @NonNull Bitmap bitmap) {
        return (bitmap.getByteCount() >> 10);
    }

// --Commented out by Inspection START (28.09.19 00:23):
//    public boolean contains(String key) {
//        return get(key) != null;
//    }
// --Commented out by Inspection STOP (28.09.19 00:23)

    public Bitmap getBitmap(String key) {
        return get(key);
    }

    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}