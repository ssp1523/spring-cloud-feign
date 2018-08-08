package com.example.feign;

import org.slf4j.Logger;

/**
 * info feign 日志
 * @author: sunshaoping
 * @date: Create by in 下午3:29 2018/8/8
 */
public class InfoFeignLogger extends feign.Logger {

    private final Logger logger;

    public InfoFeignLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(String.format(methodTag(configKey) + format, args));
        }
    }
}
