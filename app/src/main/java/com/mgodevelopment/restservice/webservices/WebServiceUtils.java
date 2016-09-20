package com.mgodevelopment.restservice.webservices;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Base64;

import com.mgodevelopment.restservice.Constants;
import com.mgodevelopment.restservice.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
                serviceUrl = addParametersToUrl(serviceUrl, urlValues);
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

            if (bodyValues != null) {

                JSONObject jsonObject = new JSONObject();

                for (String key : bodyValues.keySet()) {
                    jsonObject.put(key, bodyValues.getAsString(key));
                }

                String str = jsonObject.toString();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
                osw.write(str);
                osw.flush();
                osw.close();

            }

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                LogUtils.log(TAG, "Unauthorized Access!");
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                LogUtils.log(TAG, "URL Response Error");
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONObject(convertInputStreamToString(in));

        } catch (MalformedURLException e) {
            LogUtils.log(TAG, "MalformedURLException in requestJSONObject: " + e.getMessage());
        } catch (SocketTimeoutException e) {
            LogUtils.log(TAG, "SocketTimeoutException in requestJSONObject: " + e.getMessage());
        } catch (IOException e) {
            LogUtils.log(TAG, "IOException in requestJSONObject: " + e.getMessage());
        } catch (JSONException e) {
            LogUtils.log(TAG, "JSONException in requestJSONObject: " + e.getMessage());
        } catch (Exception e) {
            LogUtils.log(TAG, "Exception in requestJSONObject: " + e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;

    }

    private static String convertInputStreamToString(InputStream inputStream) {

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String responseText;

        try {
            while ((responseText = bufferedReader.readLine()) != null) {
                stringBuilder.append(responseText);
            }
        } catch (IOException e) {
            LogUtils.log(TAG, "IOException in convertInputStreamToString: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LogUtils.log(TAG, "Exception in convertInputStreamToString: " + e.getMessage());
        }

        return stringBuilder.toString();

    }

    private static String addParametersToUrl(String url, ContentValues urlValues) {

        StringBuffer stringBuffer = new StringBuffer(url);
        stringBuffer.append("?");
        for (String key : urlValues.keySet()) {

            stringBuffer.append(key);
            stringBuffer.append("=");
            stringBuffer.append(urlValues.getAsString(key));
            stringBuffer.append("&");

        }

        stringBuffer.replace(stringBuffer.length() - 1, stringBuffer.length() - 1, "");
        return stringBuffer.toString();

    }

    private static void addBasicAuthentication(HttpURLConnection urlConnection) {

        final String basicAuth = "Basic " + Base64.encodeToString((Constants.APP_KEY +
                ":" + Constants.APP_SECRET).getBytes(), Base64.NO_WRAP);
        urlConnection.setRequestProperty(Constants.AUTHORIZATION, basicAuth);

    }

    public static boolean hasInternetConnection(Context context) {

        ConnectivityManager connectivityManager = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager != null &&
                connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();

    }

}
