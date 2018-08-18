package com.example.feign;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign全局配置
 */
@Configuration
public class FeignClientsConfig {

    /**
     * 错误解码
     */
    @Bean
    ErrorDecoder errorDecoder() {
        return new MyErrorDecoder();
    }


}
