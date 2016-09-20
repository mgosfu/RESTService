package com.mgodevelopment.restservice;

import android.app.Application;

import com.mgodevelopment.restservice.data.User;

/**
 * Created by Martin on 9/19/2016.
 */

public class RESTServiceApplication extends Application {

    private static RESTServiceApplication instance;
    private User user;
    private String accessToken;

    @Override
    public void onCreate() {

        super.onCreate();
        instance = this;
        user = new User();

    }

    public static RESTServiceApplication getInstance() {
        return instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
