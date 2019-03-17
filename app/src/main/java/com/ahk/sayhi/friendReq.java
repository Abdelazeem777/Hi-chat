package com.ahk.sayhi;

public class friendReq {
    private String requestType;

    public friendReq() {

    }
    public friendReq(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
}
