package com.atguigu.business.rest;

import com.atguigu.business.model.domain.Product;
import com.atguigu.business.model.recom.Recommendation;
import com.atguigu.business.model.request.*;
import com.atguigu.business.service.*;
import com.atguigu.business.model.domain.User;
import com.atguigu.business.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.apache.log4j.Logger;

import java.util.List;

@RequestMapping("/rest/product")
@Controller
public class ProductRestApi {

    private static Logger logger = Logger.getLogger(ProductRestApi.class.getName());

    @Autowired
    private RecommenderService recommenderService;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;
    @Autowired
    private RatingService ratingService;

    /**
     * 获取热门推荐
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/hot", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getHotProducts(@RequestParam("num") int num, Model model) {
        List<Recommendation> recommendations = recommenderService.getHotRecommendations(new HotRecommendationRequest(num));
        model.addAttribute("success", true);
        model.addAttribute("products", productService.getRecommendProducts(recommendations));
        return model;
    }

    /**
     * 获取打分最多的商品
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/rate", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getRateMoreProducts(@RequestParam("num") int num, Model model) {
        List<Recommendation> recommendations = recommenderService.getRateMoreRecommendations(new RateMoreRecommendationRequest(num));
        model.addAttribute("success", true);
        model.addAttribute("products", productService.getRecommendProducts(recommendations));
        return model;
    }

    // 基于物品的协同过滤
    @RequestMapping(value = "/itemcf/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getItemCFProducts(@PathVariable("id") int id, Model model) {
        List<Recommendation> recommendations = recommenderService.getItemCFRecommendations(new ItemCFRecommendationRequest(id));
        model.addAttribute("success", true);
        model.addAttribute("products", productService.getRecommendProducts(recommendations));
        return model;
    }

    // 基于内容的推荐
    @RequestMapping(value = "/contentbased/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getContentBasedProducts(@PathVariable("id") int id, Model model) {
        List<Recommendation> recommendations = recommenderService.getContentBasedRecommendations(new ContentBasedRecommendationRequest(id));
        model.addAttribute("success", true);
        model.addAttribute("products", productService.getRecommendProducts(recommendations));
        return model;
    }

    /**
     * 获取单个商品的信息
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/info/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getProductInfo(@PathVariable("id") int id, Model model) {
        model.addAttribute("success", true);
        model.addAttribute("product", productService.findByProductId(id));
        return model;
    }

    /**
     * 模糊查询商品
     *
     * @param query
     * @param model
     * @return
     */
    @RequestMapping(value = "/search", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getSearchProducts(@RequestParam("query") String query, Model model) {
        try {
            query = new String(query.getBytes("ISO-8859-1"), "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        List<Product> products = productService.findByProductName(query);
        model.addAttribute("success", true);
        model.addAttribute("products", products);
        return model;
    }

    @RequestMapping(value = "/rate/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model rateToProduct(@PathVariable("id") int id, @RequestParam("score") Double score, @RequestParam("username") String username, Model model) {
        User user = userService.findByUsername(username);
        ProductRatingRequest request = new ProductRatingRequest(user.getUserId(), id, score);
        boolean complete = ratingService.productRating(request);
        //埋点日志
        if (complete) {
            System.out.print("=========埋点=========");
            logger.info(Constant.PRODUCT_RATING_PREFIX + ":" + user.getUserId() + "|" + id + "|" + request.getScore() + "|" + System.currentTimeMillis() / 1000);

            // 之前采用的是 flume 采集日志文件发送到 kafka，我改为直接发送
            try {
                // 这里的分数被除以了 2，防止分数太大
                KafkaLogProducer.produceLog(user.getUserId() + "|" + id + "|" + request.getScore() + "|" + System.currentTimeMillis() / 1000);
            } catch (Exception e) {
                System.err.println(" ======  kafka 消息投递异常  ====== ");
            }
        }
        model.addAttribute("success", true);
        model.addAttribute("message", " 已完成评分！");
        return model;
    }

    // 离线推荐
    @RequestMapping(value = "/offline", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getOfflineProducts(@RequestParam("username") String username, @RequestParam("num") int num, Model model) {
        User user = userService.findByUsername(username);
        List<Recommendation> recommendations = recommenderService.getCollaborativeFilteringRecommendations(new UserRecommendationRequest(user.getUserId(), num));
        model.addAttribute("success", true);
        model.addAttribute("products", productService.getRecommendProducts(recommendations));
        return model;
    }

    // 实时推荐
    @RequestMapping(value = "/stream", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getStreamProducts(@RequestParam("username") String username, @RequestParam("num") int num, Model model) {
        User user = userService.findByUsername(username);
        List<Recommendation> recommendations = recommenderService.getStreamRecommendations(new UserRecommendationRequest(user.getUserId(), num));
        model.addAttribute("success", true);
        model.addAttribute("products", productService.getRecommendProducts(recommendations));
        return model;
    }
}
