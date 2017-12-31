package com.flamebase.database.model.request;

/**
 * Created by efraespada on 08/07/2017.
 */

public class UpdateToServer {

    private String method;
    private String path;
    private String token;
    private String os;
    private String differences;
    private int len;
    private boolean clean;

    public UpdateToServer() {
        // nothing to do here
    }

    public UpdateToServer(String method, String path, String token, String os, String differences, int len, boolean clean) {
        this.method = method;
        this.path = path;
        this.token = token;
        this.os = os;
        this.differences = differences;
        this.len = len;
        this.clean = clean;
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

    public String getDifferences() {
        return differences;
    }

    public void setDifferences(String differences) {
        this.differences = differences;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public boolean isClean() {
        return clean;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
