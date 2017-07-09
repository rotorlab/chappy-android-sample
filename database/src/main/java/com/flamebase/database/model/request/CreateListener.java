package com.flamebase.database.model.request;

/**
 * Created by efraespada on 08/07/2017.
 */

public class CreateListener {

    private String method;
    private String path;
    private String token;
    private String os;
    private String sha1;
    private int len;

    public CreateListener() {
        // nothing to do here
    }

    public CreateListener(String method, String path, String token, String os, String sha1, int len) {
        this.method = method;
        this.path = path;
        this.token = token;
        this.os = os;
        this.sha1 = sha1;
        this.len = len;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
}
