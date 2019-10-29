package com.atguigu.business.model.request;

public class ProductRecommendationRequest {
    private int productId;

    private int sum;

    public ProductRecommendationRequest(int productId, int sum) {
        this.productId = productId;
        this.sum = sum;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}
