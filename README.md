[TOC]



# Spring Cloud Feign 之自定义配置

**环境信息: java 1.8、Spring boot 1.5.10.RELEASE、spring cloud-Edgware.SR3、maven 3.3+**

使用`Feign`默认配置可能不能满足需求，这时就需要我们实现自己的`Feign`配置，如下几种配置

`application.properties(.yml)`全局和局部(针对单个Feign接口)，包含以下配置

spring `java config`全局配置和局部(针对单个Feign接口)

## application.properties(.yml)配置文件和java config的优先级


下面代码就是处理配置使之生效,`FeignClientFactoryBean#configureFeign` :

```java
protected void configureFeign(FeignContext context, Feign.Builder builder) {
  //配置文件，以feign.client开头
   FeignClientProperties properties = applicationContext.getBean(FeignClientProperties.class);
   if (properties != null) {
      if (properties.isDefaultToProperties()) {
          //使用java config 配置
         configureUsingConfiguration(context, builder);
//
          configureUsingProperties(properties.getConfig().get(properties.getDefaultConfig()), builder);
         configureUsingProperties(properties.getConfig().get(this.name), builder);
      } else {
         configureUsingProperties(properties.getConfig().get(properties.getDefaultConfig()), builder);
         configureUsingProperties(properties.getConfig().get(this.name), builder);
         configureUsingConfiguration(context, builder);
      }
   } else {
      configureUsingConfiguration(context, builder);
   }
}
```

所有配置都是单个属性覆盖，如果对 `Spring boot`配置优先级有所了解

### 第一种：配置文件无配置

使用 `java config` 配置，优先级有低到高进行单个配置覆盖

1、`FeignClientsConfiguration` Spring Cloud Feign 全局默认配置。

2、`@EnableFeignClients#defaultConfiguration`  自定义全局默认配置。

3、`FeignClient#configuration` 单个`Feign`接口局部配置。



### 第二种：feign.client.default-to-properties=true(默认true)

 `java config` 和`application.properties(.yml)`配置，优先级有低到高进行单个配置覆盖

1、`FeignClientsConfiguration` Spring Cloud Feign 全局默认配置。

2、`@EnableFeignClients#defaultConfiguration`  自定义全局默认配置。

3、`FeignClient#configuration` 单个`Feign`接口局部配置。

4、`application.properties(.yml)`配置文件全局默认配置，配置属性`feign.client.default-config`指定默认值(defult)，如何使用，在[**application.properties(.yml)配置文件应用**](#priority)小节讲解

5、`application.properties(.yml)`配置文件局部配置，指定`@FeignClient#name`局部配置。

### 第三种：feign.client.default-to-properties=false(默认true)

 `java config` 和`application.properties(.yml)`配置，优先级有低到高进行单个配置覆盖

1、`application.properties(.yml)`配置文件全局默认配置，配置属性`feign.client.default-config`指定默认值(defult)，如何使用，在[**application.properties(.yml)配置文件应用**](#priority)小节讲解

2、`application.properties(.yml)`配置文件局部配置，指定`@FeignClient#name`局部配置。

3、`FeignClientsConfiguration` Spring Cloud Feign 全局默认配置。

4、`@EnableFeignClients#defaultConfiguration`  自定义全局默认配置。

5、`FeignClient#configuration` 单个`Feign`接口局部配置。

##  application.properties(.yml)配置文件应用

支持以下配置项:

```java
private Logger.Level loggerLevel;//日志级别

private Integer connectTimeout;//连接超时时间 java.net.HttpURLConnection#getConnectTimeout()，如果使用Hystrix，该配置无效

private Integer readTimeout;//读取超时时间  java.net.HttpURLConnection#getReadTimeout()，如果使用Hystrix，该配置无效

private Class<Retryer> retryer;//重试接口实现类，默认实现 feign.Retryer.Default

private Class<ErrorDecoder> errorDecoder;//错误编码

private List<Class<RequestInterceptor>> requestInterceptors;//请求拦截器

private Boolean decode404;//是否开启404编码
```

#### 使用全局默认配置名称:defalut

```properties
feign.client.config.defalut.error-decoder=com.example.feign.MyErrorDecoder
feign.client.config.defalut.logger-level=full
...
```

#### 修改全局默认配置名称为:my-config

```properties
feign.client.default-config=my-config
feign.client.config.my-config.error-decoder=com.example.feign.MyErrorDecoder
feign.client.config.my-config.logger-level=full
```

#### 局部配置，`@FeignClient#name=user`

```properties
feign.client.config.user.error-decoder=com.example.feign.MyErrorDecoder
feign.client.config.user.logger-level=full
```



## java config配置应用

可配置的接口或类，通过`@EnableFeignClients#defaultConfiguration全局默认`和`@FeignClient#configuration`局部`Feign`接口配置：

全局：

```java
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

局部：

```java
@FeignClient(name = "user", url = "${user.url}",
        configuration = UserFeignClientConfig.class
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
```

具体配置项如下，如何配置可以参考`FeignClientsConfiguration`类：

`Logger.Level`：日志级别
`Retryer`:重试机制
`ErrorDecoder`：错误解码器
`Request.Options`:

参考[**application.properties(.yml)配置文件应用**](#application.properties(.yml)配置文件应用)

```java
private final int connectTimeoutMillis;// connectTimeout配置项
private final int readTimeoutMillis;// readTimeout配置项
```

`RequestInterceptor`：请求拦截器

`Contract`:处理`Feign`接口注解，Spring Cloud Feign 使用`SpringMvcContract` 实现，处理Spring mvc 注解，也就是我们为什么可以用Spring mvc 注解的原因。
`Client`：Http客户端接口，默认是`Client.Default`，但是我们是不使用它的默认实现，Spring Cloud Feign为我们提供了okhttp3和ApacheHttpClient两种实现方式，只需使用maven引入以下两个中的一个依赖即可,版本自由选择。

```xml
<!--feign 集成httpclient-->
<dependency>
    <groupId>com.netflix.feign</groupId>
    <artifactId>feign-httpclient</artifactId>
    <version>8.3.0</version>
</dependency>
```

```xml
<!--feign集成okhttp-->
<dependency>
    <groupId>com.netflix.feign</groupId>
    <artifactId>feign-okhttp</artifactId>
    <version>8.18.0</version>
</dependency>
```

`Encoder`：编码器，将一个对象转换成http请求体中， Spring Cloud Feign 使用  `SpringEncoder`

`Decoder`：解码器， 将一个http响应转换成一个对象，Spring Cloud Feign 使用  `ResponseEntityDecoder`

`FeignLoggerFactory`：日志工厂参考[Spring Cloud Feign 之日志自定义扩展](https://www.jianshu.com/p/ae369d38c8b2)

`Feign.Builder`：`Feign`接口构建类，覆盖默认`Feign.Builder`，比如：`HystrixFeign.Builder`

`FeignContext`管理了所有的`java config` 配置

```java
/**
 * A factory that creates instances of feign classes. It creates a Spring
 * ApplicationContext per client name, and extracts the beans that it needs from there.
 *
 * @author Spencer Gibb
 * @author Dave Syer
 */
public class FeignContext extends NamedContextFactory<FeignClientSpecification> {

   public FeignContext() {
      super(FeignClientsConfiguration.class, "feign", "feign.client.name");
   }

}
```

## 自定义类型转换注册FeignFormatterRegistrar

`Spring`提供了一个接口`ConversionService`可以将任意类型转换成指定类型，如果`String`-`>Integer`

当然这些转换器需要实现一些`Spring`提供的类型转换接口，如：`Converter`(转换器)，`ConverterFactory`(转换工厂)，`Formatter`(格式化)等等。

我们来添加一个简单的转换器，将`String`-`>Integer`

```java
package com.example.feign;

import org.springframework.cloud.netflix.feign.FeignFormatterRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;

/**
 * Feign 格式转换器
 * @author: sunshaoping
 * @date: Create by in 上午10:51 2018/9/15
 * @see ConversionService
 */
@Configuration
public class FeignFormatterRegistrarConfig implements FeignFormatterRegistrar {
    @Override
    public void registerFormatters(FormatterRegistry registry) {
        //字符串转换成Integer
		registry.addConverter(String.class, Integer.class, Integer::valueOf);//lambda表达式
    }
}
```

更多请参考[Spring 官方文档](https://docs.spring.io/spring/docs/4.3.20.BUILD-SNAPSHOT/spring-framework-reference/htmlsingle/#core-convert)

## 自定义方法参数注解 AnnotatedParameterProcessor

Demo中的`UserFeign#getUserByID`方法就使用了方法参数注解`@PathVariable`，这个注解就是通过实现`AnnotatedParameterProcessor`接口实现的

```java
public class PathVariableParameterProcessor implements AnnotatedParameterProcessor {

   private static final Class<PathVariable> ANNOTATION = PathVariable.class;

   @Override
   public Class<? extends Annotation> getAnnotationType() {
      return ANNOTATION;
   }

   @Override
   public boolean processArgument(AnnotatedParameterContext context, Annotation annotation, Method method) {
      String name = ANNOTATION.cast(annotation).value();
      checkState(emptyToNull(name) != null,
            "PathVariable annotation was empty on param %s.", context.getParameterIndex());
      context.setParameterName(name);

      MethodMetadata data = context.getMethodMetadata();
      String varName = '{' + name + '}';
      if (!data.template().url().contains(varName)
            && !searchMapValues(data.template().queries(), varName)
            && !searchMapValues(data.template().headers(), varName)) {
         data.formParams().add(name);
      }
      return true;
   }

   private <K, V> boolean searchMapValues(Map<K, Collection<V>> map, V search) {
      Collection<Collection<V>> values = map.values();
      if (values == null) {
         return false;
      }
      for (Collection<V> entry : values) {
         if (entry.contains(search)) {
            return true;
         }
      }
      return false;
   }
}
```

通过上面源码我们也可以实现自己的方法参数注解，这里就不做演示了，说明下注意事项和注册方式。

#### 注册方式

很简单就行普通的java 对象注册到Spring容器一样将实现类使用Spring相关注解 `@Configuration`、`@Bean`等等

```java
@Bean
AnnotatedParameterProcessor annotatedParameterProcessor(){
    return new PathVariableParameterProcessor();
}
```

#### 注意事项

如果自定义实现`AnnotatedParameterProcessor`接口，Spring Cloud Feign 默认方法参数注解将失效，通过部分源码可以知：

```java
public class SpringMvcContract extends Contract.BaseContract
		implements ResourceLoaderAware {
    //spring mvc 注解处理类的构造器
    public SpringMvcContract(
          List<AnnotatedParameterProcessor> annotatedParameterProcessors,
          ConversionService conversionService) {
       Assert.notNull(annotatedParameterProcessors,
             "Parameter processors can not be null.");
       Assert.notNull(conversionService, "ConversionService can not be null.");

       List<AnnotatedParameterProcessor> processors;
       if (!annotatedParameterProcessors.isEmpty()) {
          processors = new ArrayList<>(annotatedParameterProcessors);
       }
       else {
         //当前annotatedParameterProcessors 为空时使用默认
          processors = getDefaultAnnotatedArgumentsProcessors();
       }
       this.annotatedArgumentProcessors = toAnnotatedArgumentProcessorMap(processors);
       this.conversionService = conversionService;
       this.expander = new ConvertingExpander(conversionService);
    }

    ...
    private List<AnnotatedParameterProcessor> getDefaultAnnotatedArgumentsProcessors() {

        List<AnnotatedParameterProcessor> annotatedArgumentResolvers = new ArrayList<>();

        annotatedArgumentResolvers.add(new PathVariableParameterProcessor());
        annotatedArgumentResolvers.add(new RequestParamParameterProcessor());
        annotatedArgumentResolvers.add(new RequestHeaderParameterProcessor());

        return annotatedArgumentResolvers;
    }
}

```



## 总结

本章节介绍了如何进行`Feign`自定义配置包括全局和局部、`application.properties`配置文件和`java config`配置，及其优先级配置。

样例地址 [spring-cloud-feign](https://github.com/ssp1523/spring-cloud-feign/tree/Spring-Cloud-Feign-%E4%B9%8B%E8%87%AA%E5%AE%9A%E4%B9%89%E9%85%8D%E7%BD%AE)  分支 ` Spring-Cloud-Feign-之自定义配置`

# 写在最后

Spring Cloud Feign 系列持续更新中。。。。。欢迎关注

如发现哪些知识点有误或是没有看懂，请在评论区指出，博主及时改正。

欢迎转载请注明出处。

