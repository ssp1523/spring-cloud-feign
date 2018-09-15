package com.example.feign;

import feign.RequestTemplate;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.web.WebMvcRegistrations;
import org.springframework.boot.autoconfigure.web.WebMvcRegistrationsAdapter;
import org.springframework.cloud.netflix.feign.AnnotatedParameterProcessor;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.feign.annotation.PathVariableParameterProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Type;

/**
 * Feign全局配置
 */
@Configuration
public class FeignClientsConfig {


    @Bean
    public WebMvcRegistrations feignWebRegistrations() {
        return new WebMvcRegistrationsAdapter() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new FeignRequestMappingHandlerMapping();
            }
        };
    }

    private static class FeignRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
        @Override
        protected boolean isHandler(Class<?> beanType) {
            return super.isHandler(beanType) &&
                    !AnnotatedElementUtils.hasAnnotation(beanType, FeignClient.class);
        }
    }

    @Bean
    AnnotatedParameterProcessor annotatedParameterProcessor() {
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
