package com.tqz.business.utils;

import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
public class Configure {

    public Configure() {
    }

    @Bean(name = "mongoClient")
    public MongoClient getMongoClient() {
        MongoClient mongoClient = new MongoClient(PropertiesFileUtils.MONGO_HOST, PropertiesFileUtils.MONGO_PORT);
        return mongoClient;
    }

    @Bean(name = "jedis")
    public Jedis getRedisClient() {
        Jedis jedis = new Jedis(PropertiesFileUtils.REDIS_HOST, PropertiesFileUtils.REDIS_PORT);
        return jedis;
    }
}
