package com.tqz.business.utils;

public class Constant {

    /************** FOR MONGODB ****************/

    public static String MONGODB_DATABASE = "recommender";

    public static String MONGODB_USER_COLLECTION = "User";

    public static String MONGODB_PRODUCT_COLLECTION = "Product";

    public static String MONGODB_RATING_COLLECTION = "Rating";

    public static String MONGODB_AVERAGE_PRODUCTS_SCORE_COLLECTION = "AverageProducts";

    public static String MONGODB_PRODUCT_RECS_COLLECTION = "ProductRecs";

    public static String MONGODB_RATE_MORE_PRODUCTS_COLLECTION = "RateMoreProducts";

    public static String MONGODB_RATE_MORE_PRODUCTS_RECENTLY_COLLECTION = "RateMoreRecentlyProducts";

    public static String MONGODB_STREAM_RECS_COLLECTION = "StreamRecs";

    public static String MONGODB_USER_RECS_COLLECTION = "UserRecs";

    public static String MONGODB_ITEMCF_COLLECTION = "ItemCFProductRecs";

    public static String MONGODB_CONTENTBASED_COLLECTION = "ContentBasedProductRecs";

    /************** FOR PRODUCT RATING ******************/

    public static String PRODUCT_RATING_PREFIX = "PRODUCT_RATING_PREFIX";

    public static int REDIS_PRODUCT_RATING_QUEUE_SIZE = 40;
}
