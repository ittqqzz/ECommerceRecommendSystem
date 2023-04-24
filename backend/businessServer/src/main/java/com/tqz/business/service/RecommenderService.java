package com.tqz.business.service;

import com.tqz.business.model.recom.Recommendation;
import com.tqz.business.model.request.*;
import com.tqz.business.utils.Constant;
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

    /**
     * 获取最近的热门商品
     *
     * @param request
     * @return
     */
    public List<Recommendation> getHotRecommendations(HotRecommendationRequest request) {
        MongoCollection<Document> rateMoreMoviesRecentlyCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_RATE_MORE_PRODUCTS_RECENTLY_COLLECTION);
        FindIterable<Document> documents = rateMoreMoviesRecentlyCollection.find().sort(Sorts.descending("yearmonth")).limit(request.getSum());
        List<Recommendation> recommendations = new ArrayList<>();
        if (documents != null) {
            for (Document document : documents) {
                recommendations.add(new Recommendation(document.getInteger("productId"), 0D));
            }
        }
        return recommendations;
    }

    /**
     * 获取评分最高的商品
     *
     * @param request
     * @return
     */
    public List<Recommendation> getRateMoreRecommendations(RateMoreRecommendationRequest request) {
        MongoCollection<Document> rateMoreProductsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_RATE_MORE_PRODUCTS_COLLECTION);
        FindIterable<Document> documents = rateMoreProductsCollection.find().sort(Sorts.descending("count")).limit(request.getSum());
        List<Recommendation> recommendations = new ArrayList<>();
        if (documents != null) {
            for (Document document : documents) {
                recommendations.add(new Recommendation(document.getInteger("productId"), 0D));
            }
        }
        return recommendations;
    }

    /**
     * 获取基于物品推荐的相似商品
     *
     * @param request 传入要检索的商品的 productId，可以查询出 productId 的相似商品
     * @return
     */
    public List<Recommendation> getItemCFRecommendations(ItemCFRecommendationRequest request) {
        MongoCollection<Document> itemCFProductsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_ITEMCF_COLLECTION);
        Document document = itemCFProductsCollection.find(new Document("productId", request.getId())).first();
        List<Recommendation> recommendations = new ArrayList<>();
        ArrayList<Document> recs = document.get("recs", ArrayList.class);
        if (document != null) {
            for (Document recDoc : recs) {
                recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
            }
        }
        return recommendations;
    }

    /**
     * 获取基于内容推荐的相似商品
     *
     * @param request
     * @return
     */
    public List<Recommendation> getContentBasedRecommendations(ContentBasedRecommendationRequest request) {
        MongoCollection<Document> contentBasedProductsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_CONTENTBASED_COLLECTION);
        Document document = contentBasedProductsCollection.find(new Document("productId", request.getId())).first();
        List<Recommendation> recommendations = new ArrayList<>();
        ArrayList<Document> recs = document.get("recs", ArrayList.class);
        if (document != null) {
            for (Document recDoc : recs) {
                recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
            }
        }
        return recommendations;
    }

    /**
     * 获取基于用户的协同过滤推荐列表
     *
     * @param request
     * @return
     */
    public List<Recommendation> getCollaborativeFilteringRecommendations(UserRecommendationRequest request) {
        MongoCollection<Document> userRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_USER_RECS_COLLECTION);
        Document document = userRecsCollection.find(new Document("userId", request.getUserId())).first();
        List<Recommendation> recommendations = new ArrayList<>();
        if (document != null) {
            ArrayList<Document> recs = document.get("recs", ArrayList.class);
            for (Document recDoc : recs) {
                recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
            }
        }
        return recommendations;
    }

    /**
     * 获取用户评分操作后的实时推荐列表
     *
     * @param request
     * @return
     */
    public List<Recommendation> getStreamRecommendations(UserRecommendationRequest request) {
        MongoCollection<Document> userRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_STREAM_RECS_COLLECTION);
        Document document = userRecsCollection.find(new Document("userId", request.getUserId())).first();
        List<Recommendation> recommendations = new ArrayList<>();
        if (document != null) {
            ArrayList<Document> recs = document.get("recs", ArrayList.class);
            for (Document recDoc : recs) {
                recommendations.add(new Recommendation(recDoc.getInteger("productId"), recDoc.getDouble("score")));
            }
        }
        return recommendations;
    }
}