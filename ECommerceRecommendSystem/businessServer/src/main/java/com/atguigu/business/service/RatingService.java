package com.atguigu.business.service;

import com.atguigu.business.model.domain.Rating;
import com.atguigu.business.model.request.ProductRatingRequest;
import com.atguigu.business.utils.Constant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.io.IOException;

@Service
public class RatingService {

    @Autowired
    private MongoClient mongoClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Jedis jedis;

    private MongoCollection<Document> ratingCollection;

    private MongoCollection<Document> getRatingCollection() {
        if (null == ratingCollection)
            ratingCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_RATING_COLLECTION);
        return ratingCollection;
    }

    private Rating documentToRating(Document document) {
        Rating rating = null;
        try {
            rating = objectMapper.readValue(JSON.serialize(document), Rating.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rating;

    }

    public boolean productRating(ProductRatingRequest request) {
        Rating rating = new Rating(request.getUserId(), request.getProductId(), request.getScore());
        updateRedis(rating);
        if (ratingExist(rating.getUserId(), rating.getUserId())) {
            return updateRating(rating);
        } else {
            return newRating(rating);
        }
    }

    private void updateRedis(Rating rating) {
        if (jedis.exists("userId:" + rating.getUserId()) && jedis.llen("userId:" + rating.getUserId()) >= Constant.REDIS_PRODUCT_RATING_QUEUE_SIZE) {
            jedis.rpop("userId:" + rating.getUserId());
        }
        jedis.lpush("userId:" + rating.getUserId(), rating.getProductId() + ":" + rating.getScore());
    }

    private boolean newRating(Rating rating) {
        try {
            getRatingCollection().insertOne(Document.parse(objectMapper.writeValueAsString(rating)));
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean ratingExist(int userId, int productId) {
        return null != findRating(userId, productId);
    }

    private boolean updateRating(Rating rating) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append("userId", rating.getUserId());
        basicDBObject.append("productId", rating.getProductId());
        getRatingCollection().updateOne(basicDBObject,
                new Document().append("$set", new Document("score", rating.getScore())));
        return true;
    }

    private Rating findRating(int userId, int productId) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append("userId", userId);
        basicDBObject.append("productId", productId);
        FindIterable<Document> documents = getRatingCollection().find(basicDBObject);
        if (documents.first() == null)
            return null;
        return documentToRating(documents.first());
    }

}
