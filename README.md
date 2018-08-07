

# Spring Cloud Feign 之日志输出

[第一章 Spring Cloud Feign 之初体验](https://www.jianshu.com/p/f9dce82021b5)已经对Feign有了初步了解，这章介绍下Feign的日志输出

在平时开发过程中少不了查看日志解决问题，一个好的框架日志输出是必不可少的，Feign也一样

### Feign日志输出说明

Feign的日志是以下部分组成

1、Feign的Level日志级别配置默认是:NONE，不要跟log日志级别混淆

日志级别枚举类 `Logger.Level`

`NONE` 不输出日志

`BASIC` 只有请求方法、URL、响应状态代码、执行时间

`HEADERS`基本信息以及请求和响应头

`FULL` 请求和响应 的heads、body、metadata，建议使用这个级别

2、log日志级别配置，默认是debug

使用指定Feign类会包名配置日志打印级别

此处使用spring logging配置 比如打印 UserFeign  logging.level.com.example.feign.UserFeign=debug

### Feign日志输出-Logger.Level.FULL+log debug级别

全局开启方式 使用spring java config 配置，注意该类一定要放到spring可以扫描到的包下

```java
/**
 * feign 配置
 * @author: sunshaoping
 * @date: Create by in 下午4:07 2018/8/7
 */
@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLevel() {
        return Logger.Level.FULL;
    }
}
```



application.properties 配置debug 日志输出级别

```properties
user.url=http://localhost:8080/user
logging.level.com.example.feign.UserFeign=debug
```

运行 `UserFeignTest.save`方法 可以看到以下保存user输出的请求和响应日志。

```log
2018-08-07 16:48:03.011 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] ---> POST http://localhost:8080/user HTTP/1.1
2018-08-07 16:48:03.011 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] Content-Type: application/json;charset=UTF-8
2018-08-07 16:48:03.011 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] Content-Length: 27
2018-08-07 16:48:03.011 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] 
2018-08-07 16:48:03.011 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] {"id":null,"name":"张三"}
2018-08-07 16:48:03.012 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] ---> END HTTP (27-byte body)
2018-08-07 16:48:03.041 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] <--- HTTP/1.1 200 (29ms)
2018-08-07 16:48:03.042 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] content-length: 0
2018-08-07 16:48:03.043 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] date: Tue, 07 Aug 2018 08:48:03 GMT
2018-08-07 16:48:03.043 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] 
2018-08-07 16:48:03.043 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] <--- END HTTP (0-byte body)

```

运行 `UserFeignTest.getUserByID`方法 可以看到以下查询user详情输出的请求和响应日志。

```
2018-08-07 16:48:03.045 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#getUserByID] ---> GET http://localhost:8080/user/5 HTTP/1.1
2018-08-07 16:48:03.046 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#getUserByID] ---> END HTTP (0-byte body)
2018-08-07 16:48:03.051 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#getUserByID] <--- HTTP/1.1 200 (4ms)
2018-08-07 16:48:03.051 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#getUserByID] content-type: application/json;charset=UTF-8
2018-08-07 16:48:03.051 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#getUserByID] date: Tue, 07 Aug 2018 08:48:03 GMT
2018-08-07 16:48:03.051 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#getUserByID] transfer-encoding: chunked
2018-08-07 16:48:03.051 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#getUserByID] 
2018-08-07 16:48:03.054 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#getUserByID] {"id":"5","name":"张三"}
2018-08-07 16:48:03.055 DEBUG 75661 --- [           main] com.example.feign.UserFeign              : [UserFeign#getUserByID] <--- END HTTP (26-byte body)

```

### 总结

此章节只介绍了feign自带的日志输出配置方式，下面章节将详细介绍其实现原理及自定义log 日志级别输出。

样例地址 [spring-cloud-feign](https://github.com/ssp1523/spring-cloud-feign/tree/Spring-Cloud-Feign%E4%B9%8B%E5%88%9D%E4%BD%93%E9%AA%8C)  分支 `Spring-Cloud-Feign之日志输出`，

## 写在最后

Spring Cloud Feign 系列持续更新中。。。。。欢迎关注

如发现哪些知识点有误或是没有看懂，欢迎在评论区提出，博主及时改正。

欢迎转载请注明出处。