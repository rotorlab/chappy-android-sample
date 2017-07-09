package com.flamebase.database.model.request;

/**
 * Created by efraespada on 09/07/2017.
 */

public class RemoveListener {

    private String method;
    private String path;
    private String token;

    public RemoveListener() {
        // nothing to do here
    }

    public RemoveListener(String method, String path, String token) {
        this.method = method;
        this.path = path;
        this.token = token;
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
}
