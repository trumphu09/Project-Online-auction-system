package com.auction.client.service;

public interface ApiCallback {
    void onSuccess(String message);
    void onError(String error);
}
