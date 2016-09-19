package com.mgodevelopment.restservice.webservices;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.mgodevelopment.restservice.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Martin on 9/18/2016.
 */
public class WebServiceUtils {

    private static final String TAG = WebServiceUtils.class.getName();

    public enum METHOD {
        POST, GET, DELETE
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues headerValues, boolean hasAuthorization) {
        return requestJSONObject(serviceUrl, method, headerValues, null, null, hasAuthorization);
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues urlValues, ContentValues bodyValues) {
        return requestJSONObject(serviceUrl, method, null, urlValues, bodyValues, false);
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues headerValues, ContentValues urlValues,
                                               ContentValues bodyValues, boolean hasAuthorization) {

        HttpURLConnection urlConnection = null;
        try {
            if (urlValues != null) {
                serviceUrl = addParametersToURL(serviceUrl, urlValues);
            }

            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
            urlConnection.setRequestMethod(method.toString());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            if (hasAuthorization) {
                addBasicAuthentication(urlConnection);
            }

            if (headerValues != null) {
                Uri.Builder builder = new Uri.Builder();
                for (String key : headerValues.keySet()) {
                    builder.appendQueryParameter(key, headerValues.getAsString(key));
                }

                String query = builder.build().getEncodedQuery();
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
            }

        } catch (MalformedURLException e) {
            Log.d(TAG, e.getMessage());
        } catch (SocketTimeoutException e) {
            Log.d(TAG, e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;

    }

}
