package com.example.feign;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


@RequestMapping("user")
@RestController
public class UserController {

    private Map<String, User> userMap = new HashMap<>();

    private AtomicLong pk = new AtomicLong();

    @PostMapping
    public void save(@RequestBody User user) {
        String id = String.valueOf(pk.incrementAndGet());
        user.setId(id);
        userMap.put(id, user);
        System.out.println("保存成功");
    }

    @GetMapping("/{id}")
    public User getUserByID(@PathVariable("id") String id) {
        return userMap.get(id);
    }

    @GetMapping
    public List<User> findAll(@RequestHeader("token") String token) {
        System.out.println("请求头token信息：" + token);
        return new ArrayList<>(userMap.values());
    }
}
