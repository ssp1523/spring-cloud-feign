package com.example.feign;

import feign.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.FeignLoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * feign 配置
 * @author: sunshaoping
 * @date: Create by in 下午4:07 2018/8/7
 */
@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLevel() {
        return Logger.Level.FULL;
    }


    @Bean
    FeignLoggerFactory infoFeignLoggerFactory() {
        return new InfoFeignLoggerFactory();
    }

    /**
     * feign info 日志工厂
     */
    public static class InfoFeignLoggerFactory implements FeignLoggerFactory {

        @Override
        public Logger create(Class<?> type) {
            return new InfoFeignLogger(LoggerFactory.getLogger(type));
        }
    }



}
