package com.example.image;

@FunctionalInterface
public interface ImageRequesterResponse {

    // callback when received newPhone
    void receivedNewPhoto(Photo newPhoto);
}
