package com.atguigu.kafkastream;/**
 * Copyright (c) 2018-2028 尚硅谷 All Rights Reserved
 * <p>
 * Project: ECommerceRecommendSystem
 * Package: com.atguigu.kafkastream
 * Version: 1.0
 * <p>
 * Created by wushengran on 2019/4/28 15:08
 */

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

/**
 * @ClassName: LogProcessor
 * @Description:
 * @Author: wushengran on 2019/4/28 15:08
 * @Version: 1.0
 */
public class LogProcessor implements Processor<byte[], byte[]> {
    private ProcessorContext context;

    @Override
    public void init(ProcessorContext processorContext) {
        this.context = processorContext;
    }

    @Override
    public void process(byte[] dummy, byte[] line) {
        // 核心处理流程
        String input = new String(line);
        // 提取数据，以固定前缀过滤日志信息
        if (input.contains("PRODUCT_RATING_PREFIX:")) {
            System.out.println("product rating data coming! " + input);
            input = input.split("PRODUCT_RATING_PREFIX:")[1].trim();
            context.forward("logProcessor".getBytes(), input.getBytes());
        }
    }

    @Override
    public void punctuate(long l) {

    }

    @Override
    public void close() {

    }
}
