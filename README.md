

# Spring Cloud Feign 之日志输出

[第二章 Spring Cloud Feign 之日志输出](https://www.jianshu.com/p/415943eca709)已经对Feign自带的日志输出说明，与外部HTTP接口交互时需要记录一些请求和响应日志来排查问题，虽然Feign支持但它的日志是Debug级别，并不符合我们在生产中使用INFO级别日志要求，所以这章介绍下自定义日志输出。

### 分析Spring Cloud Feign 默认日志

首先看下spring cloud feign 对日志输出的处理

```java
package org.springframework.cloud.netflix.feign;

import feign.Logger;

/**
 * Allows an application to use a custom Feign {@link Logger}.
 *
 * @author Venil Noronha
 */
public interface FeignLoggerFactory {

   /**
    * Factory method to provide a {@link Logger} for a given {@link Class}.
    *
    * @param type the {@link Class} for which a {@link Logger} instance is to be created
    * @return a {@link Logger} instance
    */
   public Logger create(Class<?> type);

}
```

通过源码我们知道spring cloud feign 已经对feign的日志输出这块做了扩展，当然feign本身也可以，这里针对spring cloud feign 进行讲解。

`FeignLoggerFactory`是Feign日志工厂接口类，`DefaultFeignLoggerFactory`是它的默认实现，spring cloud feign 就是用的 `DefaultFeignLoggerFactory`

```java
@Override
public Logger create(Class<?> type) {
   return this.logger != null ? this.logger : new Slf4jLogger(type);
}
```

默认日志工厂使用`Slf4jLogger`日志，然后我们看下源码

```java
package feign.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import feign.Request;
import feign.Response;

public class Slf4jLogger extends feign.Logger {

  private final Logger logger;

  public Slf4jLogger() {
    this(feign.Logger.class);
  }

  public Slf4jLogger(Class<?> clazz) {
    this(LoggerFactory.getLogger(clazz));
  }

  public Slf4jLogger(String name) {
    this(LoggerFactory.getLogger(name));
  }

  Slf4jLogger(Logger logger) {
    this.logger = logger;
  }

  @Override
  protected void logRequest(String configKey, Level logLevel, Request request) {
    if (logger.isDebugEnabled()) {
      super.logRequest(configKey, logLevel, request);
    }
  }
    
  @Override
  protected Response logAndRebufferResponse(String configKey, Level logLevel, 
                                      Response response,long elapsedTime) throws IOException {
    if (logger.isDebugEnabled()) {
      return super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
    }
    return response;
  }

  @Override
  protected void log(String configKey, String format, Object... args) {
    // Not using SLF4J's support for parameterized messages (even though it would be more efficient) because it would
    // require the incoming message formats to be SLF4J-specific.
    if (logger.isDebugEnabled()) {
      logger.debug(String.format(methodTag(configKey) + format, args));
    }
  }
}
```

看到这里就知道为什么Spring Cloud Feign 日志输出的是Debug级别日志了。

### 自定Spring Cloud Feign日志输出

参考`DefaultFeignLoggerFactory`类实现自己的日志工厂实现类。

场景说明：将原有的debug级别，修改成info级别

第一步:实现`FeignLoggerFactory`工厂接口,`InfoFeignLoggerFactory` 是`FeignConfig`静态内部类

```java
/**
 * feign info 日志工厂
 */
public static class InfoFeignLoggerFactory implements FeignLoggerFactory {

    @Override
    public Logger create(Class<?> type) {
        return new InfoFeignLogger(LoggerFactory.getLogger(type));
    }
}
```

第二部: 继承`feign.Logger`实现info级别日志输出，`InfoFeignLogger`使用`slf4j`日志工具，此处只是简单的实现了info级别日志输出，如果想效率更高请参考`Slf4jLogger`添加一些日记级别判断

```java
/**
 * info feign 日志
 * @author: sunshaoping
 * @date: Create by in 下午3:29 2018/8/8
 */
public class InfoFeignLogger extends feign.Logger {

    private final Logger logger;

    public InfoFeignLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(String.format(methodTag(configKey) + format, args));
        }
    }
}
```

第三部:日志工厂`InfoFeignLoggerFactory`注册到spring 容器中，使用spring java Config

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

    @Bean
    FeignLoggerFactory infoFeignLoggerFactory() {
        return new InfoFeignLoggerFactory();
    }

}
```

这样就实现了自定义的日志打印了，简单吧，让我们看看效果，是不是变成INFO日志级别了。

```java
2018-08-08 15:45:39.261  INFO 3514 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] ---> POST http://localhost:8080/user HTTP/1.1
2018-08-08 15:45:39.262  INFO 3514 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] Content-Type: application/json;charset=UTF-8
2018-08-08 15:45:39.262  INFO 3514 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] Content-Length: 27
2018-08-08 15:45:39.262  INFO 3514 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] 
2018-08-08 15:45:39.262  INFO 3514 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] {"id":null,"name":"张三"}
2018-08-08 15:45:39.263  INFO 3514 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] ---> END HTTP (27-byte body)
2018-08-08 15:45:39.691  INFO 3514 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] <--- HTTP/1.1 200 (427ms)
2018-08-08 15:45:39.691  INFO 3514 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] content-length: 0
2018-08-08 15:45:39.691  INFO 3514 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] date: Wed, 08 Aug 2018 07:45:39 GMT
2018-08-08 15:45:39.691  INFO 3514 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] 
2018-08-08 15:45:39.692  INFO 3514 --- [           main] com.example.feign.UserFeign              : [UserFeign#save] <--- END HTTP (0-byte body)

```

细心的同学可能会发现，为什么我们的`FeignLoggerFactory`就起作用了，其实这是spring 强大的条件装配功能

当`FeignLoggerFactory`不存在时才加载默认`DefaultFeignLoggerFactory`,如果对spring 条件装配感兴趣的同学可以看下[官网](https://docs.spring.io/spring-boot/docs/2.0.4.RELEASE/reference/htmlsingle/#boot-features-condition-annotations)，spring boot对spring条件装配扩展了很多注解。

```java
@Bean
@ConditionalOnMissingBean(FeignLoggerFactory.class)
public FeignLoggerFactory feignLoggerFactory() {
   return new DefaultFeignLoggerFactory(logger);
}
```

### 总结

此章节只介绍了feign自带的日志输出配置方式，下面章节将详细介绍其实现原理及自定义log 日志级别输出。

样例地址 [spring-cloud-feign](https://github.com/ssp1523/spring-cloud-feign/tree/Spring-Cloud-Feign%E4%B9%8B%E5%88%9D%E4%BD%93%E9%AA%8C)  分支 `Spring-Cloud-Feign之日志输出`，

## 写在最后

Spring Cloud Feign 系列持续更新中。。。。。欢迎关注

如发现哪些知识点有误或是没有看懂，欢迎在评论区提出，博主及时改正。

欢迎转载请注明出处。