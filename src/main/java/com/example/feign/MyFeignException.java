package com.example.feign;

import feign.Response;

public class MyFeignException extends RuntimeException {
    private final String methodKey;
    private Response response;


    MyFeignException(String methodKey, Response response) {
        this.methodKey = methodKey;
        this.response = response;
    }


    public Response getResponse() {
        return response;
    }

    public String getMethodKey() {
        return methodKey;
    }
}