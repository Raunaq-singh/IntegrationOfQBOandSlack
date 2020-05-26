package com.intuit.developer.helloworld.classes;

import org.springframework.stereotype.Component;

@Component
public class credentialsClass {
    String realmID;
    String accessToken;
    String refreshToken;

    public String getRealmID() {
        return realmID;
    }

    public void setRealmID(String realmID) {
        this.realmID = realmID;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}