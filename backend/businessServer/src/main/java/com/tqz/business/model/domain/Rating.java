package com.tqz.business.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class Rating {

    @JsonIgnore
    private String _id;

    private int userId;

    private int productId;

    private double score;

    private long timestamp;

    public Rating() {
    }

    public Rating(int userId, int productId, double score) {
        this.userId = userId;
        this.productId = productId;
        this.score = score;
        this.timestamp = new Date().getTime();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
}