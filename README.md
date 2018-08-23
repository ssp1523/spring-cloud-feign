

# Spring Cloud Feign 之Decode和Encode

**环境信息: java 1.8、Spring boot 1.5.10.RELEASE、spring cloud-Edgware.SR3、maven 3.3+**



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

