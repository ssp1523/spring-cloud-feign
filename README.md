

# Spring Cloud Feign 之初体验

### Feign是什么

1、Feign使编写java http客户端更容易

2、Feign是一个Java到HTTP客户端绑定器

3、基于注解驱动，支持 [JAXRS-2.0](https://jax-rs-spec.java.net/nonav/2.0/apidocs/index.html)、spring mvc 注解系列、自带注解`@RequestLine`等等。

4、支持http客户端扩展，如:  Apache HttpClient、OKHTTP等主流的HTTP客户端。

### 为什么用Feign

1、可以与多种HTTP客户端集成

2、spring 已经完全集成feign

3、极大地简化了HTTP请求代码量

4、与ribbon负载均衡器、hystrix熔断器无缝集成。

### Feign使用例子

本项目使用spring boot+maven构建的，如果对于spring boot不了解的同学请先了解下。

话不多说，首先来个例子让大家体验下Feign的魅力

#### 代码示例

下面以User类为例，简单的保存、查询全部、根据id查询三个接口。

maven  pom.xml文件使用的相关依赖项

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.10.RELEASE</version>
    </parent>
    <groupId>com.example.feign</groupId>
    <artifactId>spring-cloud-feign</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <spring-cloud.version>Edgware.SR3</spring-cloud.version>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

spring boot 启动类

```java
/**
 * 启动类
 * @author: sunshaoping
 * @date: Create by in 上午10:47 2018/8/7
 */
@EnableFeignClients
@SpringBootApplication
public class FeignApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeignApplication.class, args);
    }
}

```

User实体类，数据传输和存储

```java
public class User {

    private String id;

    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

```



UserController类，处理HTTP请求，代码比较简单就不做说明了

```java
@RequestMapping("user")
@RestController
public class UserController {

    /**
     * 简单的Map存储User
     */
    private Map<String, User> userMap = new HashMap<>();

    /**
     * id自增主键
     */
    private AtomicLong pk = new AtomicLong();

    @PostMapping
    public void save(@RequestBody User user) {
        String id = String.valueOf(pk.incrementAndGet());
        user.setId(id);
        userMap.put(id, user);
        System.out.println("保存成功");
    }

    @GetMapping("/{id}")
    public User getUserByID(@PathVariable("id") String id) {
        return userMap.get(id);
    }

    @GetMapping
    public List<User> findAll(@RequestHeader("token") String token) {
        System.out.println("请求头token信息：" + token);
        return new ArrayList<>(userMap.values());
    }
}

```

启动`FeignApplication`，可以访问web服务了，默认端口8080控制会打印相关信息。

使用http客户端工具或浏览器访问已启动的web服务，本人使用的是postman

#### http请求访问接口

对此处比较熟的同学可以跳过，看下一节 `使用Feign调用http接口`

user保存接口POST  http://localhost:8080/user ，可以多保存几次，下面的列表就可以看的条数据了

请求头

Content-Type:application/json

请求体

```json
{
	"name":"张三"
}
```

user列表接口GET  http://localhost:8080/user

响应体

```json
[
    {
        "id": "1",
        "name": "张三"
    },
    {
        "id": "2",
        "name": "李四"
    },
    {
        "id": "3",
        "name": "王五"
    }
]
```

user根据id查询接口GET  http://localhost:8080/user/1

响应体

```json
{
    "id": "1",
    "name": "张三"
}
```



#### 使用Feign调用http接口

创建Feign接口类 

```java
@FeignClient(name = "user",url = "${user.url}")
public interface UserFeign {

    @PostMapping
    void save(User user);

    @GetMapping("/{id}")
    User getUserByID(@PathVariable("id") String id);

    @GetMapping
    List<User> findAll();
}

```



resources目录下 application.properties 文件配置

```properties
user.url=http://localhost:8080/user
```

创建UserFeignTest测试类，此处是基于 `@SpringBootTest`注解

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserFeignTest {

    @Autowired
    UserFeign userFeign;

    @Test
    public void save() {
        User user = new User();
        user.setName("张三");
        userFeign.save(user);
        User user1 = userFeign.getUserByID("1");
        System.out.println(user1);
    }

    @Test
    public void getUserByID() {
        userFeign.getUserByID("1");
    }

    @Test
    public void findAll() {
        List<User> userList = userFeign.findAll();
        System.out.println(userList);
    }
}
```



运行 `UserFeignTest.save` 方法会看到以下信息：

```java
User{id='1', name='张三'}
```

运行 `UserFeignTest.findAll` 就会返回User全部信息

#### 总结

到此Feign初体验结束。

我们会发现在`UserFeignTest`测试类的 `UserFeign userFeign `成员变量是spring 自动注入的，无论保存还是查询只要是调用`UserFeign`对应方法就可以了，对应开发者就像调用本地接口一样，大大的简化了http客户端请求的方式，当然 `UserFeignTest`类可以改成 `UserService`类。

样例地址 [spring-cloud-feign](https://github.com/ssp1523/spring-cloud-feign)

