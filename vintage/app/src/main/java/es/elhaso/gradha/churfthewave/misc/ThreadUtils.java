package es.elhaso.gradha.churfthewave.misc;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Little snippets of code related to thread control or performance.
 *
 * Put the uppercase methods at the beginning of a method you want to make sure
 * always runs in a specific execution thread, to validate that no long action
 * is done blocking the user interface.
 */
public class ThreadUtils
{
    /**
     * Simple debug method to make sure we are running on the UI thread.
     */
    @UiThread public static void BLOCK_UI()
    {
        assertTrue(Looper.myLooper() == Looper.getMainLooper());
    }

    /**
     * Simple debug method to make sure we are running on the UI thread.
     */
    @WorkerThread public static void DONT_BLOCK_UI()
    {
        assertTrue(Looper.myLooper() != Looper.getMainLooper());
    }

    /**
     * Runs a specific piece of code on the main UI after a while.
     */
    public static void postDelayedOnUI(long delayMillis, @NonNull Runnable r)
    {
        assertNotNull(r);
        assertTrue(delayMillis >= 0);
        Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(r, delayMillis);
    }

    /**
     * Runs code on the UI thread now, or later if we are in a background
     * thread.
     */
    public static void runOnUi(@NonNull Runnable r)
    {
        assertNotNull(r);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            postDelayedOnUI(0, r);
        }
    }

    /**
     * Runs code on a background thread now, or later if we are in the UI
     * thread.
     *
     * The code is run in parallel to other threads.
     */
    public static void runInBackground(@NonNull Runnable r)
    {
        assertNotNull(r);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            r.run();
        } else {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(r);
        }
    }

    /**
     * Runs code always in a different background thread.
     *
     * This is guaranteed to not block the current thread.
     */
    public static void runInSeparateThread(@NonNull Runnable r)
    {
        assertNotNull(r);
        AsyncTask.THREAD_POOL_EXECUTOR.execute(r);
    }

    /// Converts a nanosecond to second.
    public static double nanoToSeconds(long t)
    {
        return (double) t / 1_000_000_000.0;
    }

    public static long secondsToNano(double seconds)
    {
        return (long) (seconds * 1_000_000_000.0);
    }

    public static long millisToNano(long ms)
    {
        return ms * 1_000_000;
    }

    /**
     * Returns the number of seconds between two nanosecond measurements.
     *
     * The difference is formatted in seconds with three decimal digits. See
     * http://stackoverflow.com/a/20293994/172690 for the format suggestion.
     *
     * You pass long values storing nanoseconds since boot, usually returned by
     * the System.nanoTime() method.
     */
    @NonNull public static String secondDifference(long t1, long t2)
    {
        assertTrue(t2 >= t1);
        final double dif = nanoToSeconds(t2 - t1);
        return String.format("%1$.3f", dif);
    }

    /**
     * Builds a background serial thread for running code on it.
     *
     * The returned handler will be infinitely associated to a thread you can't
     * safely quit, so it is a memory leak if you stop using it. Use this only
     * for threads that have to live forever.
     *
     * Based on http://stackoverflow.com/a/32751785/172690.
     */
    @NonNull public static Handler createInfiniteThread()
    {
        HandlerThread handlerThread = new HandlerThread(
            "HT" + System.currentTimeMillis()
        );
        handlerThread.setPriority(Thread.MIN_PRIORITY);

        // Start the Handler Thread
        // The thread will block (using the looper) until it
        // receives a new message
        handlerThread.start();

        // Create a Message Handler which you can use to
        // post and process messages
        // The same Handler can also be used to post a Runnable which will get
        // executed on handlerThread
        return new Handler(handlerThread.getLooper());
        // TODO: How is the handler thead destroyed?
    }

    /**
     * Checks if the activity is dying in certain weird conditions.
     *
     * This wrapper exists to allow accessing the API 17 isDestroyed() method.
     *
     * @return True if the activity is null, dying, or already finished.
     */
    public static boolean isDying(@Nullable Activity activity)
    {
        if (null == activity) {

            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) {

                return true;
            }
        }

        return activity.isFinishing();
    }
}
