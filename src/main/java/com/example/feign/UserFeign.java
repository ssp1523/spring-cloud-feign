package com.example.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "user", url = "${user.url}",
/*fallback = UserFeignFallback.class*/
        decode404 = true,
        fallbackFactory = UserFeignFactory.class,
        configuration = FeignClientsConfig.class
)
public interface UserFeign {

    @PostMapping
    void save(User user);

    @GetMapping("/{id}")
    User getUserByID(@PathVariable("id") String id);

    @GetMapping
    List<User> findAll();
}
