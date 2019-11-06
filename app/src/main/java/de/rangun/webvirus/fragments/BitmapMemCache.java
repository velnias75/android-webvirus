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
 *  Last modified 07.10.19 04:37 by heiko
 */

package de.rangun.webvirus.fragments;

import android.graphics.Bitmap;
import android.util.LruCache;

import androidx.annotation.NonNull;

import com.android.volley.toolbox.ImageLoader;

final class BitmapMemCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

    public BitmapMemCache(int sizeInKiloBytes) { super(sizeInKiloBytes); }

    @Override
    protected int sizeOf(String key, @NonNull Bitmap bitmap) {
        return (bitmap.getByteCount() >> 10);
    }

    public Bitmap getBitmap(String key) {
        return get(key);
    }

    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}