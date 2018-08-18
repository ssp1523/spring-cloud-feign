package com.example.feign;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserFeignTest {

    @Autowired
    UserFeign userFeign;

    @Test
    public void save() {
        User user = new User();
        user.setName("张三");
        userFeign.save(user);
        System.out.println("根据id查询");
        User user1 = userFeign.getUserByID("5");
        System.out.println(user1);
    }

    @Test
    public void getUserByID() {
        User user = userFeign.getUserByID("1");
        System.out.println(user);
    }

    @Test
    public void findAll() {
        List<User> userList = userFeign.findAll();
        System.out.println(userList);
    }
}