package es.elhaso.gradha.churfthewave.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.elhaso.gradha.churfthewave.misc.ThreadUtils;

import static es.elhaso.gradha.churfthewave.misc.ThreadUtils.DONT_BLOCK_UI;

public class Net
{
    private static final String TAG = "Net";
    private static final String AUTHORIZATION_HEADER = "authorization";
    private static final String CONTENT_TYPE_FORM =
        "application/x-www-form-urlencoded";

    private static final String BASE_URL = "http://wave-recruit-test" + "" +
        ".herokuapp.com";
    private static final String LOGIN_URL = BASE_URL + "/login";
    private static final String USERS_URL = BASE_URL + "/getUsers";

    /**
     * Requests an authentication token from the server for the given params.
     *
     * @return The non null authentication token or any kind of exception.
     */
    @WorkerThread public static NetSyncResult<String> login(@NonNull String
        user,
        @NonNull String password)
    {
        DONT_BLOCK_UI();

        final URL url;
        try {
            url = new URL(LOGIN_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();

            return new NetSyncResult<>(e);
        }
        final HttpURLConnection connection;
        final String body;
        final int responseCode;

        try {
            HashMap<String, String> params = new HashMap<>(2);
            params.put("user", user);
            params.put("password", password);

            byte[] postData = getDataString(params).getBytes("utf-8");
            int postDataLength = postData.length;

            // https://stackoverflow.com/a/15555952/172690
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", CONTENT_TYPE_FORM);
            connection.setRequestProperty("Content-Length", Integer.toString
                (postDataLength));

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(connection
                .getOutputStream());
            outputStream.write(postData);

            responseCode = connection.getResponseCode();
            Log.d(TAG, "response code " + responseCode);

            InputStream inputStream = (isValidResponse(responseCode) ?
                connection.getInputStream() : connection.getErrorStream());
            body = getBodyAsString(inputStream);
            inputStream.close();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed hard " + e.getMessage());

            return new NetSyncResult<>(e);
        }

        final JSONObject json;
        try {
            json = new JSONObject(body);
            Log.d(TAG, "Got " + json);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Couldn't convert body to JSON: " + body);
            return new NetSyncResult<>(e);
        }

        if (!isValidResponse(responseCode)) {
            final String message = "Server response out of range " +
                responseCode;
            Log.e(TAG, message);
            // TODO: Attempt to extract error for user ui.

            return new NetSyncResult<>(new Exception(message));
        }

        final JSONAuthResult result = JSONAuthResult.from(json);
        if (null == result) {
            return new NetSyncResult<>(new JSONException("Didn't find token"));
        } else {
            return new NetSyncResult<>(result.token);
        }
    }

    @WorkerThread public static NetSyncResult<List<JSONUser>> getUsers
        (@NonNull String token)
    {
        DONT_BLOCK_UI();

        final URL url;
        try {
            url = new URL(USERS_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();

            return new NetSyncResult<>(e);
        }

        final HttpURLConnection connection;
        final String body;
        final int responseCode;

        try {
            // https://stackoverflow.com/a/15555952/172690
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", CONTENT_TYPE_FORM);
            connection.setRequestProperty(AUTHORIZATION_HEADER, token);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);

            responseCode = connection.getResponseCode();
            Log.d(TAG, "response code " + responseCode);

            InputStream inputStream = (isValidResponse(responseCode) ?
                connection.getInputStream() : connection.getErrorStream());
            body = getBodyAsString(inputStream);
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed hard " + e.getMessage());

            return new NetSyncResult<>(e);
        }

        final JSONArray json;
        try {
            json = new JSONArray(body);
            Log.d(TAG, "Got " + json.length() + " users");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Couldn't convert body to JSON: " + body);
            return new NetSyncResult<>(e);
        }

        if (!isValidResponse(responseCode)) {
            final String message = "Server response out of range " +
                responseCode;
            Log.e(TAG, message);
            // TODO: Attempt to extract error for user ui.

            return new NetSyncResult<>(new Exception(message));
        }

        List<JSONUser> result = new ArrayList<>(json.length());
        for (int f = 0; f < json.length(); f++) {
            JSONObject userJson = json.optJSONObject(f);
            if (null == userJson) {
                return new NetSyncResult<>(new Exception("Error parsing " +
                    "users"));
            }

            JSONUser user = JSONUser.from(userJson);
            if (null == user) {
                return new NetSyncResult<>(new Exception("Error parsing " +
                    "users"));
            }

            result.add(user);
        }

        return new NetSyncResult<>(result);
    }

    public interface OnBitmapLoadedCallback
    {
        /**
         * The callback will always be called in a background thread.
         *
         * @param bitmap Null if the bitmap could not be loaded.
         */
        @WorkerThread void onBitmapLoaded(@NonNull URL url,
            @Nullable Bitmap bitmap);
    }

    /**
     * Fetches an image at the specified URL.
     *
     * TODO: Cancel download.
     *
     * @param strongCallback The callback will be called with null or the
     * bitmap image always in a background thread.
     */
    @AnyThread public static void getBitmap(final @NonNull URL url,
        @NonNull final OnBitmapLoadedCallback strongCallback)
    {
        final HttpURLConnection connection;
        final WeakReference<OnBitmapLoadedCallback> weakCallback = new
            WeakReference<>(strongCallback);

        final Runnable errorRunnable = new Runnable()
        {
            @Override public void run()
            {
                DONT_BLOCK_UI();
                Log.e(TAG, "Error getBitmap for " + url);
                OnBitmapLoadedCallback callback = weakCallback.get();
                if (null != callback) {
                    callback.onBitmapLoaded(url, null);
                }
            }
        };

        try {
            // https://stackoverflow.com/a/15555952/172690
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", CONTENT_TYPE_FORM);
            connection.setUseCaches(true);
            connection.setDoInput(true);
            connection.setDoOutput(false);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error setting image download");
            ThreadUtils.runInBackground(errorRunnable);
            return;
        }

        ThreadUtils.runInBackground(new Runnable()
        {
            @Override public void run()
            {
                try {
                    readData();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error setting image download");
                    errorRunnable.run();
                }
            }

            @WorkerThread private void readData() throws IOException
            {
                final int responseCode = connection.getResponseCode();
                Log.d(TAG, "response code " + responseCode);

                if (!isValidResponse(responseCode)) {
                    errorRunnable.run();
                    return;
                }

                InputStream input = connection.getInputStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(input);

                OnBitmapLoadedCallback callback = weakCallback.get();
                if (null != callback) {
                    callback.onBitmapLoaded(url, bitmap);
                }
            }
        });
    }

    //region Generic helpers

    static private boolean isValidResponse(int code)
    {
        return (code >= 200 && code < 300);
    }

    /**
     * Builds POST form url encoded. Stolen from
     * https://stackoverflow.com/a/40576153/172690
     */
    static private String getDataString(HashMap<String, String> params)
        throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) first = false;
            else result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    /**
     * Converts an input stream into a string, or raises an exception.
     *
     * Stolen from https://stackoverflow.com/a/35446009/172690.
     */
    static private @NonNull String getBodyAsString(@NonNull InputStream
        inputStream) throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        // StandardCharsets.UTF_8.name() > JDK 7
        return result.toString("UTF-8");

    }

    //endregion Generic helpers
}
