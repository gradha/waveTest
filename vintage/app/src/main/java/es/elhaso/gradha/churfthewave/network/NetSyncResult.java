package es.elhaso.gradha.churfthewave.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Wraps complex network return values preserving the response and/or
 * exception.
 */
public class NetSyncResult<T>
{
    public @Nullable Exception exception;
    public @Nullable T value;

    /**
     * Creates a result from a successful call.
     */
    public NetSyncResult(@Nullable T validValue)
    {
        this.value = validValue;
        this.exception = null;
    }

    /**
     * Creates a result from a most likely a totally failed result.
     */
    public NetSyncResult(@NonNull Exception e)
    {
        this.exception = e;
        this.value = null;
    }

    @Override public String toString()
    {
        return "NetSyncResult{" + (null != exception ? "exception=" +
            exception : "value=" + value) + '}';
    }
}
