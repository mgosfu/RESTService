package com.mgodevelopment.restservice.webservices;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mgodevelopment.restservice.Constants;
import com.mgodevelopment.restservice.R;
import com.mgodevelopment.restservice.utils.LogUtils;

import org.json.JSONObject;

/**
 * Created by Martin on 9/19/2016.
 */

public class WebServiceTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = WebServiceTask.class.getName();

    public abstract void showProgress();
    public abstract boolean performRequest();
    public abstract void performSuccessfulOperation();
    public abstract void hidePrograss();

    private String mMessage;
    private Context mContext;

    public WebServiceTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        showProgress();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        if (!WebServiceUtils.hasInternetConnection(mContext)) {

            mMessage = Constants.CONNECTION_MESSAGE;
            return false;

        } else {
            return performRequest();
        }

        return null;

    }

    @Override
    protected void onPostExecute(Boolean success) {

        hidePrograss();
        if (success) {
            performSuccessfulOperation();
        }

        if (mMessage != null && !mMessage.isEmpty()) {
            Toast.makeText(mContext, mMessage, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        hidePrograss();
    }

    public boolean hasError(JSONObject obj) {

        if (obj != null) {

            int status = obj.optInt(Constants.STATUS);
            LogUtils.log(TAG, "Response: " + obj.toString());
            mMessage = obj.optString(Constants.MESSAGE);

            if (status == Constants.STATUS_ERROR || status == Constants.STATUS_UNAUTHORIZED) {
                return true;
            } else {
                return false;
            }

        }

        mMessage = mContext.getString(R.string.error_url_not_found);
        return true;

    }

}
