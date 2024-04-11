# 示例：如何基于方法填充

本示例将指导你如何配置使用方法作为数据源，去配置一个填充操作，在这之前，请先确保你已经阅读过[快速开始](./../user_guide/getting_started/getting_started_abstract.md)，并且成功将 crane4j 引入你的项目。

## 1.准备

**数据源**

假设，现在你有一个 `UserDAO` 接口：

~~~java
@Component("userDAO")
public class UserDAO {
  
  	public List<User> listUsers(List<Integer> ids) {
      	// do something
    }
}
~~~

该方法将返回一个 `User` 对象，它包含下述字段

~~~java
@Data
public class User {
    private Integer id;
  	private String name;
  	private String address;
  	private Integer age;
}
~~~

**目标**

而现在，你有一个方法 `listUseInfo`，它返回一个 `UserInfo` 对象：

~~~java
@Component
public class UserService {
  
  	public List<UserInfo> listUserInfos(List<Integer> ids) {
      	// do something
    }
}

@Data
public UserInfo {
    // 其他属性......
  
    private Integer userId;
  	private String userName;
  	private String userAddress;
  	private Integer userAge;
}
~~~

你需要根据 `userId` 找到对应的 `User` 对象，然后将相关的属性映射到 `UserInfo` 里面。

## 2.使用

首先，你需要在 `UserDAO.listUsers` 方法上添加 `@ContainerMhetod` 注解，将其配置为一个数据源：

~~~java
@Component
public class UserDAO {
  
  	@ContainerMethod(
      	namespace = "user", // 表明该数据源的命名空间为 user
      	resultType = User.class, // 该方法的返回值为 User
      	resultKey = "id"， // 将 User 列表按 id 分组
      	type = MappingType.ONE_TO_ONE // 映射类型为一对一，即一个 UserInfo 对象对应一个 User 对象
    )
  	public List<User> listUsers(List<Integer> ids) {
      	// do something
    }
}
~~~

接着，你需要在 `UserInfo` 中基于该数据源配置一个填充操作：

~~~java
@Data
public UserInfo {
    // 其他属性......
  
  	@Assemble(
      	container = "user", // 引用配置好的数据源,
      	props = { // 配置属性映射
          	@Mapping(src = "name", ref = "userName"),
            @Mapping(src = "address", ref = "userAddress"),
            @Mapping(src = "age", ref = "userAge")
        }
    )
    private Integer userId;
  	private String userName;
  	private String userAddress;
  	private Integer userAge;
}
~~~

最后，我们选择一个方式触发填充即可，这里我们选择直接基于 AOP 对 `UserService.listUserInfos` 的返回值进行自动填充：

~~~java
@Component
public class UserService {
  
  	@AutoOperate(type = UserInfo.class)
  	public List<UserInfo> listUserInfos(List<Integer> ids) {
      	// do something
    }
}
~~~

:::tip

- 关于如何指定字段映射，具体内容可以参见：[字段映射](./../basic/property_mapping.md) 一节；
- 你可以基于更多的配置项和不同的声明方式来配置基于方法的填充操作，具体内容可以参见：[方法数据源容器](./../basic/container/method_container.md) 一节；
- 除了基于 AOP 自动触发，crane4j 还支持更多的触发方式，具体内容可以参见：[触发填充操作](./../basic/trigger_operation.md) 一节；

:::

## 3.选项式配置

这种先配置数据源，再配置操作的配置风格被称为组合式配置。你也可以按照选项式风格，将所有的配置都集中到一个注解里：

~~~java
@Data
public UserInfo {
    // 其他属性......
  
  	@AssembleMethod(
      	target = "userDAO", // 引用目标对象，可以是 beanName 或者类的全限定名
      	method = @ContainerMethod(
          	bindMethod = "listUsers", // 绑定 listUsers 方法
            resultType = User.class, // 该方法的返回值为 User
            resultKey = "id"， // 将 User 列表按 id 分组
            type = MappingType.ONE_TO_ONE // 映射类型为一对一，即一个 UserInfo 对象对应一个 User 对象
        )
      	props = { // 配置属性映射
          	@Mapping(src = "name", ref = "userName"),
            @Mapping(src = "address", ref = "userAddress"),
            @Mapping(src = "age", ref = "userAge")
        }
    )
    private Integer userId;
  	private String userName;
  	private String userAddress;
  	private Integer userAge;
}
~~~

其他配置完全不变。
