

# Spring Cloud Feign 之Fallback

在网络请求时，可能会出现异常请求，如果还想再异常情况下使系统可用，那么就需要容错处理，比如:网络请求超时时给用户提示“稍后重试”或使用本地快照数据等等。

Spring Cloud Feign就是通过`Fallback`实现的，有两种方式：

1、`@FeignClient.fallback = UserFeignFallback.class`指定一个实现Feign接口的实现类。

2、`@FeignClient.fallbackFactory = UserFeignFactory.class`指定一个实现`FallbackFactory<T>`工厂接口类

因为`Fallback`是通过`Hystrix`实现的， 所以需要开启`Hystrix`，spring boot `application.properties`文件配置`feign.hystrix.enabled=true`，这样就开启了`Fallback`

### Fallback-实现Feign接口

`UserFeignFallback`回调实现，由spring创建使用`@Component`(其他的注册也可以)注解

> `HystrixTargeter.targetWithFallback`方法实现了`@FeignClient.fallback`处理逻辑，通过源码可以知道`UserFeignFallback`回调类是从Spring容器中获取的，所以`UserFeignFallback`由spring创建。

`UserFeign`配置:

```java
package com.example.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "user",url = "${user.url}",fallback = UserFeignFallback.class
        /*fallbackFactory = UserFeignFactory.class*/)
public interface UserFeign {

    @PostMapping
    void save(User user);

    @GetMapping("/{id}")
    User getUserByID(@PathVariable("id") String id);

    @GetMapping
    List<User> findAll();
}
```

`UserFeignFallback`类：

```java
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
```

为了模拟回调失败服务提供方，抛出500错误。

```java
 @GetMapping("/{id}")
    public User getUserByID(@PathVariable("id") String id) {

//        return userMap.get(id);
        throw new RuntimeException("服务端测试异常！");
    }
```

运行单元测试`UserFeignTest.getUserByID`控制台输出结果:


```java
2018-08-18 11:47:59.800  INFO 8660 --- [ hystrix-user-1] com.example.feign.UserFeign              : [UserFeign#getUserByID] ---> GET http://localhost:8080/user/1 HTTP/1.1
2018-08-18 11:47:59.800  INFO 8660 --- [ hystrix-user-1] com.example.feign.UserFeign              : [UserFeign#getUserByID] ---> END HTTP (0-byte body)
2018-08-18 11:47:59.828  INFO 8660 --- [ hystrix-user-1] com.example.feign.UserFeign              : [UserFeign#getUserByID] <--- HTTP/1.1 500 (27ms)
2018-08-18 11:47:59.828  INFO 8660 --- [ hystrix-user-1] com.example.feign.UserFeign              : [UserFeign#getUserByID] connection: close
2018-08-18 11:47:59.828  INFO 8660 --- [ hystrix-user-1] com.example.feign.UserFeign              : [UserFeign#getUserByID] content-type: application/json;charset=UTF-8
2018-08-18 11:47:59.828  INFO 8660 --- [ hystrix-user-1] com.example.feign.UserFeign              : [UserFeign#getUserByID] date: Sat, 18 Aug 2018 03:47:59 GMT
2018-08-18 11:47:59.828  INFO 8660 --- [ hystrix-user-1] com.example.feign.UserFeign              : [UserFeign#getUserByID] transfer-encoding: chunked
2018-08-18 11:47:59.828  INFO 8660 --- [ hystrix-user-1] com.example.feign.UserFeign              : [UserFeign#getUserByID] 
2018-08-18 11:47:59.829  INFO 8660 --- [ hystrix-user-1] com.example.feign.UserFeign              : [UserFeign#getUserByID] {"timestamp":1534564079825,"status":500,"error":"Internal Server Error","exception":"java.lang.RuntimeException","message":"服务端测试异常！","path":"/user/1"}
2018-08-18 11:47:59.829  INFO 8660 --- [ hystrix-user-1] com.example.feign.UserFeign              : [UserFeign#getUserByID] <--- END HTTP (167-byte body)
User{id='100', name='fallback 回调用户'}
```

服务提供方抛出的500错误代码，但是客户端程序还可以正常运行输出了`UserFeignFallback.getUserByID`方法返回的结果。

### FallbackFactory<T>工厂

上面的实现方式简单，但是获取不到HTTP请求错误状态码和信息 ，这时就可以使用工厂模式来实现`Fallback`

同样工厂实现类也要交由spring管理，同时结合`UserFeignFallback`使用，这里需要注意的`create`方法返回值类型一定要实现Feign接口，否则会报错。

`UserFeignFactory`只做了打印异常处理：

```java
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
        cause.printStackTrace();
        return userFeignFallback;
    }
}
```

UserFeign:

```java
package com.example.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "user", url = "${user.url}",
/*fallback = UserFeignFallback.class*/
        fallbackFactory = UserFeignFactory.class)
public interface UserFeign {

    @PostMapping
    void save(User user);

    @GetMapping("/{id}")
    User getUserByID(@PathVariable("id") String id);

    @GetMapping
    List<User> findAll();
}
```

运行单元测试`UserFeignTest.getUserByID`可以看到控制台打印的异常`feign.FeignException`更多信息省略。

`ErrorDecoder`接口处理请求错误信息，默认实现`ErrorDecoder.Default`抛出`FeignException`异常

> `FeignException.status` 方法返回HTTP状态码，`FallbackFactory.create`默认情况下可以强制转换成`FeignException`异常这样就可以获取到HTTP状态码了。

### 自定义ErrorDecoder

#### 第一种:`application.properties`

全局配置,通过`application.properties`配置文件

```properties
feign.client.default-config=my-config
feign.client.config.my-config.error-decoder=com.example.feign.MyErrorDecoder
```

错误解码实现类MyErrorDecoder

```java
package com.example.feign;

import feign.Response;
import feign.codec.ErrorDecoder;

public class MyErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return new MyFeignException(methodKey,response);
    }
}
```

自定义异常MyFeignException

```java
package com.example.feign;

import feign.Response;

public class MyFeignException extends RuntimeException {
    private final String methodKey;
    private Response response;


    MyFeignException(String methodKey, Response response) {
        this.methodKey = methodKey;
        this.response = response;
    }


    public Response getResponse() {
        return response;
    }

    public String getMethodKey() {
        return methodKey;
    }
}
```

#### 第二种:`@EnableFeignClients`

全局配置,`@EnableFeignClients.defaultConfiguration`注解

```java
package com.example;

import com.example.feign.FeignClientsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * 启动类
 *
 * @author: sunshaoping
 * @date: Create by in 上午10:47 2018/8/7
 */
@EnableFeignClients(
        defaultConfiguration = FeignClientsConfig.class
)
@SpringBootApplication
public class FeignApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeignApplication.class, args);
    }


}
```

#### 第三种:`@FeignClient`

作用范围是Feign接口，`@FeignClient.configuration` 注解



```java
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
```



### 总结

本章节讲了如下内容

Spring Cloud Feign HTTP请求异常`Fallback`容错机制，它是基于Hystrix实现的，所以要通过配置参数`feign.hystrix.enabled=true`开启该功能，及其两种实现方式。

`Fallback`工厂方式引出了`ErrorDecoder`错误解码自定义处理，有三种方式，可根据实际请求选择。



样例地址 [spring-cloud-feign](https://github.com/ssp1523/spring-cloud-feign/tree/Spring-Cloud-Feign%E4%B9%8B%E6%97%A5%E5%BF%97%E8%87%AA%E5%AE%9A%E4%B9%89%E6%89%A9%E5%B1%95)  分支 `Spring-Cloud-Feign之日志自定义扩展`，

## 写在最后

Spring Cloud Feign 系列持续更新中。。。。。欢迎关注

如发现哪些知识点有误或是没有看懂，欢迎在评论区提出，博主及时改正。

欢迎转载请注明出处。

