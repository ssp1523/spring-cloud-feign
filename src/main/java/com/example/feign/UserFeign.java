package com.example.feign;

import com.netflix.hystrix.HystrixCommand;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import rx.Completable;
import rx.Observable;
import rx.Single;

import java.util.List;

@FeignClient(name = "user", url = "${user.url}",
/*fallback = UserFeignFallback.class*/
        decode404 = true,
        fallbackFactory = UserFeignFactory.class
//        configuration = FeignClientsConfig.class
)
public interface UserFeign {

    @PostMapping
    Completable save(User user);

    @GetMapping("/{id}")
    Single<User> getUserByIDSingle(@PathVariable("id") String id);

    @GetMapping("/{id}")
    Observable<User> getUserByID(@PathVariable("id") String id);

    @GetMapping
    HystrixCommand<List<User>> findAll();


}
