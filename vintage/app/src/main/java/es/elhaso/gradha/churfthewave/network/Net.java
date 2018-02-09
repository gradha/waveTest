package es.elhaso.gradha.churfthewave.network;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Net
{
    private static final String TAG = "Net";
    private static final String CONTENT_TYPE_FORM =
        "application/x-www-form-urlencoded";

    private static final String BASE_URL = "http://wave-recruit-test" +
        ".herokuapp.com";
    private static final String LOGIN_URL = BASE_URL + "/login";

    public static boolean login(@NonNull String user,
        @NonNull String password)
    {
        final URL url;
        try {
            url = new URL(LOGIN_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();

            return false;
        }
        final HttpURLConnection connection;
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

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "response code " + responseCode);

            InputStream input = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        return true;
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
}
