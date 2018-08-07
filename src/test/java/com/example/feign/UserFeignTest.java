package com.example.feign;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserFeignTest {

    @Autowired
    UserFeign userFeign;

    @Test
    public void save() {
        User user = new User();
        user.setName("ssp");
        userFeign.save(user);
        User user1 = userFeign.getUserByID("1");
        System.out.println(user1);
    }

    @Test
    public void getUserByID() {
        userFeign.getUserByID("1");
    }

    @Test
    public void findAll() {
        List<User> userList = userFeign.findAll();
        System.out.println(userList);
    }
}