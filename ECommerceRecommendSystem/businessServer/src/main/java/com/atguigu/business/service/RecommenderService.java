package com.atguigu.business.service;

import com.atguigu.business.model.recom.Recommendation;
import com.atguigu.business.model.request.*;
import com.atguigu.business.utils.Constant;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommenderService {

    @Autowired
    private MongoClient mongoClient;

    public List<Recommendation> getHotRecommendations(HotRecommendationRequest request) {
        // 获取热门电影的条目
        MongoCollection<Document> rateMoreMoviesRecentlyCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_RATE_MORE_PRODUCTS_RECENTLY_COLLECTION);
        FindIterable<Document> documents = rateMoreMoviesRecentlyCollection.find().sort(Sorts.descending("yearmonth")).limit(request.getSum());

        List<Recommendation> recommendations = new ArrayList<>();
        for (Document document : documents) {
            recommendations.add(new Recommendation(document.getInteger("productId"), 0D));
        }
        System.out.println(recommendations);
        return recommendations;
    }

    public List<Recommendation> getRateMoreRecommendations(RateMoreRecommendationRequest request) {

        // 获取评分最多电影的条目
        MongoCollection<Document> rateMoreProductsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_RATE_MORE_PRODUCTS_COLLECTION);
        FindIterable<Document> documents = rateMoreProductsCollection.find().sort(Sorts.descending("count")).limit(request.getSum());

        List<Recommendation> recommendations = new ArrayList<>();
        for (Document document : documents) {
            recommendations.add(new Recommendation(document.getInteger("productId"), 0D));
        }
        return recommendations;
    }

    public List<Recommendation> getItemCFRecommendations(ItemCFRecommendationRequest request) {
        MongoCollection<Document> itemCFProductsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_ITEMCF_COLLECTION);
        Document document = itemCFProductsCollection.find(new Document("productId", request.getId())).first();

        System.out.println(document.get("recs"));
        List<Recommendation> recommendations = new ArrayList<>();
        ArrayList<Document> recs = document.get("recs", ArrayList.class);

        for (Document recDoc : recs) {
            recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
        }

        return recommendations;
    }

    public List<Recommendation> getContentBasedRecommendations(ContentBasedRecommendationRequest request) {
        MongoCollection<Document> contentBasedProductsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_CONTENTBASED_COLLECTION);
        Document document = contentBasedProductsCollection.find(new Document("productId", request.getId())).first();

        System.out.println(document.get("recs"));
        List<Recommendation> recommendations = new ArrayList<>();
        ArrayList<Document> recs = document.get("recs", ArrayList.class);

        for (Document recDoc : recs) {
            recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
        }

        return recommendations;
    }

    // user recs
    public List<Recommendation> getCollaborativeFilteringRecommendations(UserRecommendationRequest request) {
        MongoCollection<Document> userRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_USER_RECS_COLLECTION);
        Document document = userRecsCollection.find(new Document("userId", request.getUserId())).first();

        List<Recommendation> recommendations = new ArrayList<>();
        ArrayList<Document> recs = document.get("recs", ArrayList.class);

        for (Document recDoc : recs) {
            recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
        }

        return recommendations;
    }

    // user stream recs
    public List<Recommendation> getStreamRecommendations(UserRecommendationRequest request) {
        MongoCollection<Document> userRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_STREAM_RECS_COLLECTION);
        Document document = userRecsCollection.find(new Document("userId", request.getUserId())).first();

        List<Recommendation> recommendations = new ArrayList<>();
        ArrayList<Document> recs = document.get("recs", ArrayList.class);

        for (Document recDoc : recs) {
            recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
        }

        return recommendations;
    }
}