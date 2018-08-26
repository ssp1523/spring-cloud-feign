package com.example.feign;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 记得写注释
 * @author: sunshaoping
 * @date: Create by in 下午9:33 2018/8/25
 */
@Configuration
public class UserFeignClientConfig {
    /**
     * 错误解码
     */
    @Bean
    ErrorDecoder errorDecoder() {
        return new MyErrorDecoder();
    }

}
