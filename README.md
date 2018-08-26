

# Spring Cloud Feign 之自定义配置

**环境信息: java 1.8、Spring boot 1.5.10.RELEASE、spring cloud-Edgware.SR3、maven 3.3+**

使用`Feign`默认配置可能不能满足需求，这时就需要我们实现自己的`Feign`配置，如下几种配置

`application.properties(.yml)`全局和局部(针对单个Feign接口)，包含以下配置

spring `java config`全局配置和局部(针对单个Feign接口)

## <span id="priority">application.properties(.yml)配置文件和java config的优先级</span>


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

###  application.properties(.yml)配置文件应用

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



### java config配置应用

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

参考**application.properties(.yml)配置文件应用**

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



### 总结

本章节介绍了如何进行`Feign`自定义配置包括全局和局部、`application.properties`配置文件和`java config`配置，及其优先级配置。

样例地址 [spring-cloud-feign](https://github.com/ssp1523/spring-cloud-feign/tree/Spring-Cloud-Feign-%E4%B9%8B%E8%87%AA%E5%AE%9A%E4%B9%89%E9%85%8D%E7%BD%AE)  分支 ` Spring-Cloud-Feign-之自定义配置`

## 写在最后

Spring Cloud Feign 系列持续更新中。。。。。欢迎关注

如发现哪些知识点有误或是没有看懂，请在评论区指出，博主及时改正。

欢迎转载请注明出处。

