package com.flamebase.database.model.request;

/**
 * Created by efraespada on 08/07/2017.
 */

public class UpdateFromServer {

    private String method;
    private String path;
    private String content;
    private int len;
    private String token;
    private String os;

    public UpdateFromServer() {
        // nothing to do here
    }

    public UpdateFromServer(String method, String path, String content, int len, String token, String os) {
        this.method = method;
        this.path = path;
        this.content = content;
        this.len = len;
        this.token = token;
        this.os = os;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
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
}
