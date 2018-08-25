

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

4、`application.properties(.yml)`配置文件全局默认配置，配置属性`feign.client.default-config`指定默认值(defult)，如何使用，[**application.properties(.yml)配置文件应用**]("application.properties(.yml)配置文件应用")小节讲解



5、`application.properties(.yml)`配置文件局部配置

```

```

### 第三种：feign.client.default-to-properties=false(默认true)

```java
private Logger.Level loggerLevel;//日志级别

private Integer connectTimeout;//连接超时时间 java.net.HttpURLConnection#getConnectTimeout()

private Integer readTimeout;//读取超时时间  java.net.HttpURLConnection#getReadTimeout()

private Class<Retryer> retryer;//重试接口实现类，默认实现 feign.Retryer.Default

private Class<ErrorDecoder> errorDecoder;//错误编码

private List<Class<RequestInterceptor>> requestInterceptors;//请求拦截器

private Boolean decode404;//是否开启404编码
```



全局和局部配置。这里的配置不是`application.properties(.ymlw)`内的配置。

那么`Feign`的配置有哪些呢，我们列举下

```
Decoder
Encoder
Contract
ErrorDecoder
Request.Options
Logger.Level
Retryer
```



### application.properties(.yml)配置文件应用

只需配置指定`defaultConfiguration`默认全局默认配置

```java
@EnableFeignClients(
        defaultConfiguration = FeignClientsConfig.class
)
```



### java config配置应用





### 总结

本章节讲了如下内容

`HystrixCommand`命令模式，这个功能是最全的，如果实际业务处理比较复杂的情况下可以使用该方式。

`Observable`观察者模式，`HystrixCommand`的变种，简化版的`HystrixCommand`

`Single`观察者模式，处理单个返回结果时可以使用，简化版的`Observable`

`Completable`观察者模式，处理一些没有返回数据，只需成功或失败通知的情况下使用。

样例地址 [spring-cloud-feign](https://github.com/ssp1523/spring-cloud-feign/tree/Spring-Cloud-Feign-%E4%B9%8BHystrix)  分支 `Spring-Cloud-Feign-之Hystrix`

## 写在最后

Spring Cloud Feign 系列持续更新中。。。。。欢迎关注

如发现哪些知识点有误或是没有看懂，请在评论区提出，博主及时改正。

欢迎转载请注明出处。

