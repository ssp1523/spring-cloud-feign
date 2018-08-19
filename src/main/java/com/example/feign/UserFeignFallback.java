package com.example.feign;

import com.netflix.hystrix.HystrixCommand;
import org.springframework.stereotype.Component;
import rx.Completable;
import rx.Observable;
import rx.Single;

import java.util.List;

@Component
public class UserFeignFallback implements UserFeign {

    @Override
    public Completable save(User user) {

        return Completable.complete();
    }

    @Override
    public Single<User> getUserByIDSingle(String id) {
        return getUserByID(id).toSingle();
    }

    @Override
    public Observable<User> getUserByID(String id) {

        return Observable.create(subscriber -> {
            User user = new User();
            user.setId("100");
            user.setName("fallback 回调用户");
            subscriber.onNext(user);
            subscriber.onCompleted();
        });
    }

    @Override
    public HystrixCommand<List<User>> findAll() {
        return null;
    }
}
