package com.example.feign;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserFeignFallback implements UserFeign {

    @Override
    public void save(User user) {

    }

    @Override
    public User getUserByID(String id) {
        User user = new User();
        user.setId("100");
        user.setName("fallback 回调用户");
        return user;
    }

    @Override
    public List<User> findAll() {
        return null;
    }
}
