package com.example.feign;

import feign.RequestTemplate;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.netflix.feign.AnnotatedParameterProcessor;
import org.springframework.cloud.netflix.feign.annotation.PathVariableParameterProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;

/**
 * Feign全局配置
 */
@Configuration
public class FeignClientsConfig {

    @Bean
    AnnotatedParameterProcessor annotatedParameterProcessor(){
        return new PathVariableParameterProcessor();
    }

    /**
     * 错误解码
     */
    @Bean
    ErrorDecoder errorDecoder() {
        return new MyErrorDecoder();
    }

    /**
     * 自定义编码器
     *
     */
    @Bean
    Encoder encoder() {
        return new Encoder() {
            @Override
            public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
                System.out.println();
            }
        };
    }

    /**
     * 自定义解码器
     */
    @Bean
    Decoder decoder() {
        return (response, type) -> {
            System.out.println();
            return null;
        };
    }

}
