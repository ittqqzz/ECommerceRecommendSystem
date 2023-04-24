package com.tqz.business.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class User {

    @JsonIgnore
    private String _id;

    private int userId;

    private String username;

    private String password;

    private boolean first;

    private long timestamp;

    private List<String> prefGenres = new ArrayList<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.userId = username.hashCode();
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean passwordMatch(String password) {
        return this.password.compareTo(password) == 0;
    }

    public int getUserId() {
        return userId;
    }

    public void setUid(int uid) {
        this.userId = uid;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public List<String> getPrefGenres() {
        return prefGenres;
    }

    public void setPrefGenres(List<String> prefGenres) {
        this.prefGenres = prefGenres;
    }
}