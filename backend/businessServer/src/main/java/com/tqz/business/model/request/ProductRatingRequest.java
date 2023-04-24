package com.tqz.business.model.request;

public class ProductRatingRequest {

    private int userId;

    private int productId;

    private Double score;

    public ProductRatingRequest(int userId, int productId, Double score) {
        this.userId = userId;
        this.productId = productId;
        this.score = score;
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

    public Double getScore() {
//        return Double.parseDouble(String.format("%.2f", score / 2D));
        return Double.parseDouble(String.format("%.2f", score));
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
