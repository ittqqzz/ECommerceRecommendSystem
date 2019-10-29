package com.atguigu.business.model.request;

public class RateMoreRecommendationRequest {

    private int sum;

    public RateMoreRecommendationRequest(int sum) {
        this.sum = sum;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }
}
