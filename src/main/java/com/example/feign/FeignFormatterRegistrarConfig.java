package com.example.feign;

import org.springframework.cloud.netflix.feign.FeignFormatterRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.FormatterRegistry;

/**
 * Feign 格式转换器
 * @author: sunshaoping
 * @date: Create by in 上午10:51 2018/9/15
 * @see ConversionService
 */
@Configuration
public class FeignFormatterRegistrarConfig implements FeignFormatterRegistrar {
    @Override
    public void registerFormatters(FormatterRegistry registry) {
        //字符串转换成Integer
        registry.addConverter(String.class, Integer.class, Integer::valueOf);
    }
}
