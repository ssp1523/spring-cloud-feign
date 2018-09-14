

# Spring Cloud Feign 之Hystrix

环境信息: java 1.8、Spring boot 1.5.10.RELEASE、spring cloud-Edgware.SR3、maven 3.3+

本章节只针对`Hystrix`在`Feign`中的简单实用，和一些简单的源码分析，更详细的请参考[GitHub官网](https://github.com/Netflix/Hystrix/wiki)

Fallback章节已经简单的介绍了Hystrix的熔断保护使用，这章将介绍`HystrixCommand`、`Observable`、`Single`、`Completable`这四种异步HTTP请求方式。

那么`Hystrix`为什么可以支持者四种异步HTTP请求方式，其实很简单通过一段源码就可以看出

`HystrixDelegatingContract.parseAndValidatateMetadata`方法的四个`if else`判断分别对应四个类

```java
package feign.hystrix;

import static feign.Util.resolveLastTypeParameter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.netflix.hystrix.HystrixCommand;

import feign.Contract;
import feign.MethodMetadata;
import rx.Completable;
import rx.Observable;
import rx.Single;

/**
 * This special cases methods that return {@link HystrixCommand}, {@link Observable}, or {@link Single} so that they
 * are decoded properly.
 * 
 * <p>For example, {@literal HystrixCommand<Foo>} and {@literal Observable<Foo>} will decode {@code Foo}.
 */
// Visible for use in custom Hystrix invocation handlers
public final class HystrixDelegatingContract implements Contract {

 
  private final Contract delegate;

  public HystrixDelegatingContract(Contract delegate) {
    this.delegate = delegate;
  }

  @Override
  public List<MethodMetadata> parseAndValidatateMetadata(Class<?> targetType) {
    List<MethodMetadata> metadatas = this.delegate.parseAndValidatateMetadata(targetType);

    for (MethodMetadata metadata : metadatas) {
      Type type = metadata.returnType();

      if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(HystrixCommand.class)) {
        Type actualType = resolveLastTypeParameter(type, HystrixCommand.class);
        metadata.returnType(actualType);
      } else if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(Observable.class)) {
        Type actualType = resolveLastTypeParameter(type, Observable.class);
        metadata.returnType(actualType);
      } else if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(Single.class)) {
        Type actualType = resolveLastTypeParameter(type, Single.class);
        metadata.returnType(actualType);
      } else if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(Completable.class)) {
        metadata.returnType(void.class);
      }
    }

    return metadatas;
  }
}
```

这里使用了`委派模式`，具体工作给委派对象，实际就是`SpringMvcContract`，它负责处理spring MVC 注解方式声明的Feign接口，所以需要`SpringMvcContract`。

### HystrixCommand

Hystrix命令模式，在这里不做深度剖析有兴趣的同学可以到[GitHub官网查看](https://github.com/Netflix/Hystrix/wiki)，只针对对在Feign中是如何使用的。

其实在Feign中使用很简单，`HystrixCommand`是一个泛型的抽象类，只需在返回类型指定泛型的类型即可

如：`HystrixCommand<List<User>>`指定`List<User>`泛型即可

`UserFeign.findAll`:

```java
@GetMapping
HystrixCommand<List<User>> findAll();
```

是不是很简单,那么`HystrixCommand`如何使用呢，下面讲一下两个常用方法

#### HystrixCommand.execute 直接获取请求结果

这种方式跟`java.util.concurrent.Future.get()`方法使用方式是一样的。

启动`FeignApplication`服务提供方

运行单元测试`UserFeignTest.save`保存两条数据。

`UserFeignTest.findAll` ，直接将结果打印

```java
	@Test
    public void findAll() {
        HystrixCommand<List<User>> userList = userFeign.findAll();
        //直接获取结果，这块可以处理一些业务逻辑，最后需要userList数据调用execute方法
        System.out.println(userList.execute());
    }
```

```json
[User{id='1', name='张三1'}, User{id='2', name='张三2'}]
```

#### HystrixCommand.observe或toObservable 观察者模式(订阅发布)

observe() 是 `Hot observables ` ，toObservable()是  `Cold  observables` 

##### Hot observables

Hot observable 不管有没有订阅者订阅，他们(发布者/生产者)创建后就开发发射数据流。

##### Cold observables

只有当有订阅者订阅的时候， Cold Observable 才开始执行发射数据流的代码。

更多请参考[Reactive Execution](https://github.com/Netflix/Hystrix/wiki/How-To-Use#Reactive-Execution) 或 [RxJava 驯服数据流之 hot & cold Observable](https://blog.csdn.net/jdsjlzx/article/details/51839090)

```java
Observable<List<User>> listObservable = userList.observe();
Observable<List<User>> listObservable = userList.toObservable();
```

接下来讲下 `Observable`使用

### Observable观察者模式(订阅发布)

Feign接口的方法返回类型也可以是`Observable<T>`泛型类型可以是任意类型，我们这里以`Observable<User>`为例

> 由于`Hystrix`是异步，会出现主线程结束后`Hystrix`线程同时杀死，导致结果无法打印，在订阅之后添加`TimeUnit.SECONDS.sleep(2)`使主线程睡眠2秒钟。

`UserFeign.getUserByID`,其他内容省略

```java
@GetMapping("/{id}")
Observable<User> getUserByID(@PathVariable("id") String id);
```

如果对结果集处理，只需订阅`Observable<User>`

```java
Observable<User> user = userFeign.getUserByID("1");
user.subscribe(new Observer<User>() {
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
```

运行单元测试`UserFeignTest.getUserByID`,输出结果

```java
返回数据：User{id='1', name='张三'}
user处理完成
```

### Single

`Observable`的订阅者`Observer.onNext`方法可能执行多次，最后执行`Observer.onCompleted`或`Observer.onError`(发生异常的情况下),其实在平时开发过程中大多数情况下`Observer.onNext`只需执行一次就可以获取到全部结果集，`Single`的订阅者`Observer.onNext`方法只会执行一次就完成了。接下来简单介绍下`Single`的使用

`UserFeign.getUserByIDSingle` 很简单返回类型指定`Single`泛型是`User`即可:

```java
@GetMapping("/{id}")
Single<User> getUserByIDSingle(@PathVariable("id") String id);
```

运行单元测试`UserFeignTest.getUserByIDSingle`

```java
Single<User> singleUser = userFeign.getUserByIDSingle("1");
singleUser.subscribe(new Observer<User>() {
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
});
```
输出结果：
```java
返回数据：User{id='1', name='张三'}
执行完成
```

`Single`订阅者`Observer.onNext`方法只会执行一次，其实`Observer.onCompleted`方法没有什么作用，再加上我们有的时候不需要处理错误`Observer.onError`，这样我们就可以简化代码了

简化后的代码：

```java
Single<User> singleUser = userFeign.getUserByIDSingle("1");
singleUser.subscribe(System.out::println);
```

### Completable

`Completable`HTTP客户端只需知道请求成功或失败，不需要返回的数据时使用。

这里以保存为例`UserFeign.save`

单元测试`UserFeignTest.save`

单元测试`UserFeignTest.save `完成处理

```java
User user = new User();
user.setName("张三4");
Completable completable = userFeign.save(user);
completable.subscribe(() -> System.out.println("保存成功"));
```

单元测试`UserFeignTest.save`同步请求阻塞，异常处理，如果没有异常`throwable==null`

```java
        User user = new User();
        user.setName("张三4");
        Completable completable = userFeign.save(user);
//        completable.subscribe(() -> System.out.println("保存成功"));
        Throwable throwable = completable.get();
        System.out.println("异常信息："+throwable);
```





### 总结

本章节讲了如下内容

Spring Cloud Feign HTTP请求异常`Fallback`容错机制，它是基于Hystrix实现的，所以要通过配置参数`feign.hystrix.enabled=true`开启该功能，及其两种实现方式。

`Fallback`工厂方式引出了`ErrorDecoder`错误解码自定义处理，有三种方式，可根据实际请求选择，举一反三其他自定义配置也可以通过这种方式实现如：Decoder、Encoder、Logger(第二、三章有介绍)。

> 如果开启的`Hystrix`就不要用feign的超时配置了,单位是毫秒
>
> ```properties
> feign.client.config.defalut.connect-timeout=10000
> ```
>
> `defalut`是默认配置名称，可以使用`feign.client.default-config`替换自定义名称
>
> ```properties
> feign.client.default-config=my-config
> feign.client.config.my-config.connect-timeout=10000
> ```
>
> 请使用如下属性配置超时时间，单位毫秒
>
> ```properties
> hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=20000
> ```



样例地址 [spring-cloud-feign](https://github.com/ssp1523/spring-cloud-feign/tree/Spring-Cloud-Feign-%E4%B9%8Bfallback)  分支 `Spring-Cloud-Feign-之fallback`

## 写在最后

Spring Cloud Feign 系列持续更新中。。。。。欢迎关注

如发现哪些知识点有误或是没有看懂，请在评论区提出，博主及时改正。

欢迎转载请注明出处。

