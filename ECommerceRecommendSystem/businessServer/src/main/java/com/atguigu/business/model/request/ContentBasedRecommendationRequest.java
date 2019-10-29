package com.atguigu.business.model.request;

public class ContentBasedRecommendationRequest {

    private int id;

    public ContentBasedRecommendationRequest(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
