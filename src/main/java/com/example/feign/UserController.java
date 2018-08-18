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

    /**
     * 简单的Map存储User
     */
    private Map<String, User> userMap = new HashMap<>();

    /**
     * id自增主键
     */
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

//        return userMap.get(id);
        throw new RuntimeException("服务端测试异常！");
    }

    @GetMapping
    public List<User> findAll() {
        return new ArrayList<>(userMap.values());
    }
}
