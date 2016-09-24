package com.mgodevelopment.restservice;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.mgodevelopment.restservice.data.User;
import com.mgodevelopment.restservice.webservices.WebServiceTask;
import com.mgodevelopment.restservice.webservices.WebServiceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private UserInfoTask mUserInfoTask = null;
    private UserEditTask mUserEditTask = null;
    private UserResetTask mUserResetTask = null;
    private UserDeleteTask mUserDeleteTask = null;

    private EditText mEmailText, mPasswordText, mNameText, mPhoneNumberText, mNoteText;

    private interface ConfirmationListener {
        void onConfirmation(boolean isConfirmed);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        showProgress(true);
        mUserInfoTask = new UserInfoTask();
        mUserInfoTask.execute();

    }

    private void showProgress(final boolean isShow) {

        findViewById(R.id.progress).setVisibility(isShow ? View.VISIBLE : View.GONE);
        findViewById(R.id.info_form).setVisibility(isShow ? View.VISIBLE : View.GONE);

    }

    private void initViews() {

        mEmailText = (EditText) findViewById(R.id.email);
        mPasswordText = (EditText) findViewById(R.id.password);
        mNameText = (EditText) findViewById(R.id.name);
        mPhoneNumberText = (EditText) findViewById(R.id.phoneNumber);
        mNoteText = (EditText) findViewById(R.id.note);

    }

    private void populateText() {

        User user = RESTServiceApplication.getInstance().getUser();
        mEmailText.setText(user.getEmail());
        mPasswordText.setText(user.getPassword());
        mNameText.setText(user.getName() == null ? "" : user.getName());
        mPhoneNumberText.setText(user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
        mNoteText.setText(user.getNote() == null ? "" : user.getNote());

    }

    private abstract class ActivityWebServiceTask extends WebServiceTask {

        public ActivityWebServiceTask(WebServiceTask webServiceTask) {
            super(MainActivity.this);
        }

        @Override
        public void hideProgress() {
            MainActivity.this.showProgress(false);
        }

        @Override
        public void performSuccessfulOperation() {
            populateText();
        }

        @Override
        public void showProgress() {
            MainActivity.this.showProgress(true);
        }

    }

    private class UserInfoTask extends ActivityWebServiceTask {

        public UserInfoTask() {
            super(mUserInfoTask);
        }

        public boolean performRequest() {

            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.ID, user.getId());
            contentValues.put(Constants.ACCESS_TOKEN,
                    RESTServiceApplication.getInstance().getAccessToken());

            JSONObject obj = WebServiceUtils.requestJSONObject(Constants.INFO_URL, WebServiceUtils.METHOD.GET, contentValues, null);
            if (!hasError(obj)) {

                JSONArray jsonArray = obj.optJSONArray(Constants.INFO);
                JSONObject jsonObject = jsonArray.optJSONObject(0);
                user.setName(jsonObject.optString(Constants.NAME));
                if (user.getName().equalsIgnoreCase("null"))
                    user.setName(null);
//                user.setPassword(jsonObject.optString(Constants.PASSWORD));
//                if (user.getPassword().equalsIgnoreCase("null"))
//                    user.setPassword(null);
                user.setPhoneNumber(jsonObject.optString(Constants.PHONE_NUMBER));
                if (user.getPhoneNumber().equalsIgnoreCase("null"))
                    user.setPhoneNumber(null);
                user.setNote(jsonObject.optString(Constants.NOTE));
                if (user.getNote().equalsIgnoreCase("null"))
                    user.setNote(null);
//                user.setEmail(jsonObject.optString(Constants.EMAIL));
//                if (user.getEmail().equalsIgnoreCase("null"))
//                    user.setEmail(null);

                user.setId(jsonObject.optLong(Constants.ID_INFO));
                return true;

            }

            return false;

        }

    }

    private class UserEditTask extends ActivityWebServiceTask {

        public UserEditTask() {
            super(mUserEditTask);
        }

        public boolean performRequest() {

            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.ID, user.getId());
            contentValues.put(Constants.NAME, mNameText.getText().toString());
            contentValues.put(Constants.PASSWORD, mPasswordText.getText().toString());
            contentValues.put(Constants.PHONE_NUMBER, mPhoneNumberText.getText().toString());
            contentValues.put(Constants.NOTE, mNoteText.getText().toString());

            ContentValues urlValues = new ContentValues();
            urlValues.put(Constants.ACCESS_TOKEN, RESTServiceApplication.getInstance().getAccessToken());
            JSONObject obj = WebServiceUtils.requestJSONObject(Constants.UPDATE_URL, WebServiceUtils.METHOD.POST, urlValues, contentValues);

            if (!hasError(obj)) {

                JSONArray jsonArray = obj.optJSONArray(Constants.INFO);
                JSONObject jsonObject = jsonArray.optJSONObject(0);
                user.setName(jsonObject.optString(Constants.NAME));
                user.setPhoneNumber(jsonObject.optString(Constants.PHONE_NUMBER));
                user.setNote(jsonObject.optString(Constants.NOTE));
                user.setPassword(jsonObject.optString(Constants.PASSWORD));
                return true;

            }

            return false;

        }

    }

    private class UserResetTask extends ActivityWebServiceTask {

        public UserResetTask() {
            super(mUserResetTask);
        }

        public boolean performRequest() {

            return true;

        }

    }

    private class UserDeleteTask extends ActivityWebServiceTask {

        public UserDeleteTask() {
            super(mUserDeleteTask);
        }

        public boolean performRequest() {

            return true;

        }

    }

}
