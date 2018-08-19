package com.example.feign;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class UserFeignFactory implements FallbackFactory<UserFeign> {

    private final UserFeignFallback userFeignFallback;

    public UserFeignFactory(UserFeignFallback userFeignFallback) {
        this.userFeignFallback = userFeignFallback;
    }

    @Override
    public UserFeign create(Throwable cause) {
        //打印下异常
//        cause.printStackTrace();
        return userFeignFallback;
    }
}
