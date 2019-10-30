package com.tqz.business.service;

import com.tqz.business.model.domain.User;
import com.tqz.business.model.request.LoginUserRequest;
import com.tqz.business.model.request.RegisterUserRequest;
import com.tqz.business.utils.Constant;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UserService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> userCollection;

    private MongoCollection<Document> getUserCollection() {
        if (null == userCollection)
            userCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_USER_COLLECTION);
        return userCollection;
    }

    public boolean registerUser(RegisterUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setFirst(true);
        user.setTimestamp(System.currentTimeMillis());
        try {
            getUserCollection().insertOne(Document.parse(objectMapper.writeValueAsString(user)));
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User loginUser(LoginUserRequest request) {
        User user = findByUsername(request.getUsername());
        if (null == user) {
            return null;
        } else if (!user.passwordMatch(request.getPassword())) {
            return null;
        }
        return user;
    }

    private User documentToUser(Document document) {
        try {
            return objectMapper.readValue(JSON.serialize(document), User.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean checkUserExist(String username) {
        return null != findByUsername(username);
    }

    public User findByUsername(String username) {
        Document user = getUserCollection().find(new Document("username", username)).first();
        if (null == user || user.isEmpty())
            return null;
        return documentToUser(user);
    }

    public boolean updateUser(User user) {
        getUserCollection().updateOne(Filters.eq("userId", user.getUserId()), new Document().append("$set", new Document("first", user.isFirst())));
        getUserCollection().updateOne(Filters.eq("userId", user.getUserId()), new Document().append("$set", new Document("prefGenres", user.getPrefGenres())));
        return true;
    }

}
