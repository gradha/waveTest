package es.elhaso.gradha.churfthewave.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.content.LocalBroadcastManager;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Poor man's greenbot's EventBus.
 */
public class PubSub
{
    private final @NonNull LocalBroadcastManager mBroadcastManager;

    // The login event is not really used very much, since the login is
    // centralized.
    public static final String LOGIN_EVENT = "LoginEvent";

    // The logout event may be sent any time from anywhere, for example if
    // the server decides our auth token has expired.
    public static final String LOGOUT_EVENT = "LogoutEvent";

    @Retention(SOURCE) @StringDef({LOGIN_EVENT, LOGOUT_EVENT,})

    public @interface Event {}


    public PubSub(@NonNull Context appContext)
    {
        mBroadcastManager = LocalBroadcastManager.getInstance(appContext);
    }

    /**
     * Registers a receiver for the specified filter.
     *
     * @param filterName Pass a constant identifying the filter.
     * @param receiver The code that will run when a broadcast is sent.
     */
    public void registerReceiver(@NonNull BroadcastReceiver receiver,
        @NonNull final String filterName)
    {
        assertNotNull(receiver);
        assertNotNull(filterName);
        assertTrue(filterName.length() > 0);

        mBroadcastManager.registerReceiver(receiver, new IntentFilter
            (filterName));
    }

    /**
     * Convenience wrapper for symmetry with registerReceiver().
     *
     * @param receiver The receiver you need to unregister.
     */
    public void unregisterReceiver(@NonNull BroadcastReceiver receiver)
    {
        mBroadcastManager.unregisterReceiver(receiver);
    }

    /// Builds a local broadcast for filterName and sends it without parameters
    public void sendBroadcast(@NonNull final @Event String filterName)
    {
        assertNotNull(filterName);
        assertTrue(filterName.length() > 0);

        Intent intent = new Intent(filterName);
        mBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Builds and sends a local broadcast with parameters.
     *
     * To target a specific filter you need to create the Intent with the String
     * registered by the receiver. Example:
     *
     * Intent intent = new Intent(FILTER_SOMETHING);
     * intent.putExtra("message", "This is my message!");
     */
    public void sendBroadcast(@NonNull final Intent intent)
    {
        assertNotNull(intent);

        mBroadcastManager.sendBroadcast(intent);
    }

}
