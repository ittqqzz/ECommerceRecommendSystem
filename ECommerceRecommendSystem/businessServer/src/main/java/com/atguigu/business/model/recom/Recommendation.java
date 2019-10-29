package com.atguigu.business.model.recom;

/**
 * 推荐项目的包装
 */
public class Recommendation {

    private int productId;

    private Double score;

    public Recommendation() {
    }

    public Recommendation(int productId, Double score) {
        this.productId = productId;
        this.score = score;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
