package de.rangun.webvirus.model;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

public class BitmapMemCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

    public BitmapMemCache() {
        this((int) (Runtime.getRuntime().maxMemory() >> 10) >> 3);
    }

    private BitmapMemCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }

    @Override
    protected int sizeOf(String key, Bitmap bitmap) {
        return (bitmap.getByteCount() >> 10);
    }

    public boolean contains(String key) {
        return get(key) != null;
    }

    public Bitmap getBitmap(String key) {
        return get(key);
    }

    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}