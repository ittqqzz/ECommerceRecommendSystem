package com.atguigu.business.model.request;

public class UserRecommendationRequest {
    private int userId;

    private int sum;

    public UserRecommendationRequest(int userId, int sum) {
        this.userId = userId;
        this.sum = sum;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
