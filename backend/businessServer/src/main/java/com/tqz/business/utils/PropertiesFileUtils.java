package com.tqz.business.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesFileUtils {

    public static String KAFKA_HOST = null;
    public static Integer KAFKA_PORT = 9092;
    public static String KAFKA_TOPIC = null;

    public static String MONGO_HOST = null;
    public static Integer MONGO_PORT = 27017;

    public static String REDIS_HOST = null;
    public static Integer REDIS_PORT = 6379;

    static {
        Properties properties = new Properties();
        Resource resource = new ClassPathResource("recommend.properties");
        try {
            properties.load(new FileInputStream(resource.getFile()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("没有发现配置文件：recommend.properties");
        }
        KAFKA_HOST = properties.getProperty("kafka.host");
        KAFKA_PORT = Integer.parseInt(properties.getProperty("kafka.port"));
        KAFKA_TOPIC = properties.getProperty("kafka.topic");

        MONGO_HOST = properties.getProperty("mongo.host");
        MONGO_PORT = Integer.parseInt(properties.getProperty("mongo.port"));

        REDIS_HOST = properties.getProperty("redis.host");
        REDIS_PORT = Integer.parseInt(properties.getProperty("redis.port"));

    }
}
