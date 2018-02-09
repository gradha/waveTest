package es.elhaso.gradha.churfthewave.logic;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;

public class ImageCache
{
    private final Object mLock = new Object[0];
    private final HashMap<URL, SoftReference<Bitmap>> mCache = new HashMap<>();

    public @Nullable Bitmap get(@Nullable URL key)
    {
        if (null == key) {
            return null;
        }

        synchronized (mLock) {
            final SoftReference<Bitmap> reference = mCache.get(key);

            return (null == reference ? null : reference.get());
        }
    }

    public void put(@Nullable URL key,
        @Nullable Bitmap bitmap)
    {
        if (null == key) {
            return;
        }

        synchronized (mLock) {
            mCache.put(key, new SoftReference<>(bitmap));
        }
    }

    public void clear()
    {
        synchronized (mLock) {
            mCache.clear();
        }
    }
}
