package com.tqz.business.rest;

import com.tqz.business.model.domain.Product;
import com.tqz.business.model.domain.User;
import com.tqz.business.model.recom.Recommendation;
import com.tqz.business.model.request.*;
import com.tqz.business.service.*;
import com.tqz.business.utils.Constant;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        List<Recommendation> recommendations = null;
        try {
            recommendations = recommenderService.getHotRecommendations(new HotRecommendationRequest(num));
            model.addAttribute("success", true);
            model.addAttribute("products", productService.getRecommendProducts(recommendations));
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("msg", e.getMessage());
        }
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
        List<Recommendation> recommendations = null;
        try {
            recommendations = recommenderService.getRateMoreRecommendations(new RateMoreRecommendationRequest(num));
            model.addAttribute("success", true);
            model.addAttribute("products", productService.getRecommendProducts(recommendations));
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("msg", e.getMessage());
        }
        return model;
    }

    /**
     * 基于物品的协同过滤
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/itemcf/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getItemCFProducts(@PathVariable("id") int id, Model model) {
        List<Recommendation> recommendations = null;
        try {
            recommendations = recommenderService.getItemCFRecommendations(new ItemCFRecommendationRequest(id));
            model.addAttribute("success", true);
            model.addAttribute("products", productService.getRecommendProducts(recommendations));
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("msg", e.getMessage());
        }
        return model;
    }

    /**
     * 基于内容的推荐
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/contentbased/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getContentBasedProducts(@PathVariable("id") int id, Model model) {
        List<Recommendation> recommendations = null;
        try {
            recommendations = recommenderService.getContentBasedRecommendations(new ContentBasedRecommendationRequest(id));
            model.addAttribute("success", true);
            model.addAttribute("products", productService.getRecommendProducts(recommendations));
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("msg", e.getMessage());
        }
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
        List<Product> products = null;
        try {
            products = productService.findByProductName(query);
            model.addAttribute("success", true);
            model.addAttribute("products", products);
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("extra", "可能是编码问题导致查询异常");
        }
        return model;
    }

    @RequestMapping(value = "/rate/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model rateToProduct(@PathVariable("id") int id, @RequestParam("score") Double score, @RequestParam("username") String username, Model model) {
        try {
            User user = userService.findByUsername(username);
            ProductRatingRequest request = new ProductRatingRequest(user.getUserId(), id, score);
            boolean complete = ratingService.productRating(request);
            // 埋点日志
            if (complete) {
                System.out.print("=========埋点=========");
                String text = user.getUserId() + "|" + id + "|" + request.getScore() + "|" + System.currentTimeMillis() / 1000;
                logger.info(Constant.PRODUCT_RATING_PREFIX + ":" + text);
                // 之前采用的是 flume 采集日志文件发送到 kafka，改为直接发送
                KafkaLogProducer.produceLog(text);
            }
            model.addAttribute("success", true);
            model.addAttribute("message", " 已完成评分！");
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("msg", e.getMessage());
        }
        return model;
    }

    /**
     * 离线推荐
     *
     * @param username
     * @param num
     * @param model
     * @return
     */
    @RequestMapping(value = "/offline", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getOfflineProducts(@RequestParam("username") String username, @RequestParam("num") int num, Model model) {
        User user = null;
        List<Recommendation> recommendations = null;
        try {
            user = userService.findByUsername(username);
            // 主动设置一个 admin 账号，避免offine数据获取失败
            if (user != null && user.getUsername().equals("admin")) {
                user.setUid(50130);
            }
            recommendations = recommenderService.getCollaborativeFilteringRecommendations(new UserRecommendationRequest(user.getUserId(), num));
            model.addAttribute("success", true);
            model.addAttribute("products", productService.getRecommendProducts(recommendations));
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("msg", e.getMessage());
        }
        return model;
    }

    /**
     * 实时推荐
     */
    @RequestMapping(value = "/stream", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getStreamProducts(@RequestParam("username") String username, @RequestParam("num") int num, Model model) {
        User user = null;
        List<Recommendation> recommendations = null;
        try {
            user = userService.findByUsername(username);
            recommendations = recommenderService.getStreamRecommendations(new UserRecommendationRequest(user.getUserId(), num));
            model.addAttribute("success", true);
            model.addAttribute("products", productService.getRecommendProducts(recommendations));
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("msg", e.getMessage());
        }
        return model;
    }
}
