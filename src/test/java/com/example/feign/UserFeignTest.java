package com.example.feign;

import com.netflix.hystrix.HystrixCommand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rx.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserFeignTest {

    @Autowired
    UserFeign userFeign;

    @Test
    public void save() {
        User user = new User();
        user.setName("张三4");
        Completable completable = userFeign.save(user);
        completable.subscribe(new Subscriber<Object>() {
            @Override
            public void onCompleted() {
                System.out.println("完成");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("异常：" + e);
            }

            @Override
            public void onNext(Object o) {
                System.out.println("数据" + o);
            }
        });
//        completable.subscribe(() -> System.out.println("保存成功"));
//        Throwable throwable = completable.get();
//        System.out.println("异常信息：" + throwable);

        sleep2();

    }

    private void printUser(Observable<User> user1) {

        user1.subscribe(new Observer<User>() {
            /**
             * 数据发射完成时执行与{@link #onError(Throwable)} 它俩只会执行其中一个方法
             */
            @Override
            public void onCompleted() {
                System.out.println("user处理完成");
            }

            /**
             * 发生错误时执行与{@link #onCompleted()} 它俩只会执行其中一个方法
             */
            @Override
            public void onError(Throwable e) {
                System.out.println("user出现异常" + e);
            }

            /**
             * 发射数据流 user
             */
            @Override
            public void onNext(User user) {
                System.out.println("返回数据：" + user);
            }
        });
        sleep2();

    }

    @Test
    public void getUserByID() {
        Observable<User> user = userFeign.getUserByID("1");
        printUser(user);
        printUser(user);
        sleep2();
    }

    @Test
    public void getUserByIDSingle() {
        Single<User> singleUser = userFeign.getUserByIDSingle("1");
        /*singleUser.subscribe(new Observer<User>() {
            @Override
            public void onCompleted() {
                System.out.println("执行完成");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("执行错误" + e);

            }

            @Override
            public void onNext(User user) {
                System.out.println("返回数据：" + user);
            }
        });*/
        singleUser.subscribe(System.out::println);
        sleep2();
    }

    @Test
    public void findAll() {
        HystrixCommand<List<User>> userList = userFeign.findAll();

        //直接获取结果，这块可以处理一些业务逻辑，最后需要userList数据调用execute方法
//        System.out.println(userList.execute());

//        Observable<List<User>> listObservable = userList.toObservable();
        Observable<List<User>> listObservable = userList.observe();//可订阅多次
        listObservable.subscribe(new Subscriber<List<User>>() {
            @Override
            public void onCompleted() {
                System.out.println("完成");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("异常:" + e);
            }

            @Override
            public void onNext(List<User> users) {
                System.out.println("返回结果:" + users);
            }
        });
        listObservable.subscribe(new Subscriber<List<User>>() {
            @Override
            public void onCompleted() {
                System.out.println("完成");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("异常:" + e);
            }

            @Override
            public void onNext(List<User> users) {
                System.out.println("返回结果:" + users);
            }
        });
        sleep2();

//        try {
//            //使用java Future
//            Future<List<User>> listFuture = userList.queue();
//            System.out.println(listFuture.get());
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
    }

    private void sleep2() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}