# 方法容器

方法容器指以实例方法或静态方法作为数据源的容器，它是我们在日常中最经常使用的容器之一。

和其他的容器不同，方法容器通常不直接创建使用，而是通过在目标方法上添加注解的方式，将该方法 “声明” 为一个方法容器。你可以直接将带有容器方法的类交由 crane4j 进行解析，而在 Spring 环境中，crane4j 将会通过后处理器自动解析并注册。

crane4j 在设计上参考了 Spring 处理监听器注解 `@EventListener` 的责任链机制，它基于注解处理器 `MethodContainerAnnotationProcessor` 和方法容器工厂链 `MethodContainerFactory` 实现了扫描和适配的功能，你可以通过添加自己的 `MethodContainerFactory` 实现从而扩展这部分功能。

## 1.声明容器

你可以直接在类或方法上添加 `@ContainerMethod` 注解，在 Spring 环境中，当项目启动后，会在后处理阶段扫描该方法，并将其注册为一个方法容器。

**声明在方法上**

```java
@ContainerMethod(
    namespace = "onoToOneMethod",
    resultType = Foo.class, resultKey = "id" // 返回的数据源对象类型为 Foo，并且需要按 id 分组
)
public Set<Foo> onoToOneMethod(List<String> args) {
    // do something
}
```

**声明在类上**

当你在类上声明时，你需要使用 `bindMethod` 和 `bindMethodParamTypes` 属性显式的进行方法绑定：

```java
// 父类
public class SuperClass {
    public Set<Foo> onoToOneMethod(List<String> args) {
        // do something
    }
}

// 子类
@ContainerMethod(
    namespace = "onoToOneMethod",
    resultType = Foo.class, resultKey = "id", // 返回的数据源对象类型为 Foo，并且需要按 id 分组
    bindMethod = "onoToOneMethod" // 指定要绑定的方法名称
    bindMethodParamTypes = List.class // 指定要绑定方法的参数类型
)
public class ChildClass extends SuperClass {}
```

此时，你可以在子类中绑定父类方法作为方法容器。

可以作为方法容器的方法需要**满足下述条件**：

+ **声明类**：不限制，你可以将注解声明在接口或抽象类上，如果声明在类父类或者父接口上，那么子类/实现类同样会获得此方法；
+ **方法类型**：不限制，方法可以是实例方法（包括接口或抽象类中的抽象方法）或静态方法；
+ **返回值类型**：方法必须有返回值，且返回值类型必须为 `Collection` 集合或 `Map` 集合（取决于 `@ContainerMethod#type` 属性）；
+ **参数类型**：可以是无参方法，若是有参方法，则首个参数必须为 `Collection` 类型（这类方法在调用时其他参数都是 `null`）；

常见的各种 `xxxByIds` 都是非常典型的方法。

:::tip

crane4j 将根据现有的条件自动查找最匹配的方法，因此 `bindMethodParamTypes` 并不总是必须的。

当没有多个重载方法时，你可以只填写方法名，而当有多个重载方法时，你只需要填写足以区分出两个方法的前部分参数即可（比如 a, b, c 与 a, c, d，只需要 a, c 即可确认 a, c, d）。

不过出于代码的可维护性考虑，还是推荐把参数类型写全。

:::

## 2.可选配置项

在 `@ContainerMethod` 注解中，提供了一些可选的配置项：

| API                    | 作用                                  | 类型                                             | 默认值                                                |
| ---------------------- | ------------------------------------- | ------------------------------------------------ | ----------------------------------------------------- |
| `namespace`            | 定义枚举容器的命名空间                | 任意字符串                                       | `Method#getName`                                      |
| `type`                 | 映射类型，表示如何对结果集按 key 分组 | `MappingType` 枚举                               | `MappingType.ONE_TO_ONE`，即结果总是与 key 一对一分组 |
| `duplicateStrategy`    | 当 key 出现重复值时的处理策略         | `DuplicateStrategy` 枚举                         | `DuplicateStrategy.ALERT`，出现重复值时直接抛异常     |
| `resultKey`            | 分组的 key 值                         | 方法返回的对象列表的 key                         | `"id"`                                                |
| `resultType`           | 返回值类型                            | 返回值参数类型（如果是集合，则为其中的元素类型） | 无，必填                                              |
| `bindMethod`           | 绑定方法的名称                        | 方法名                                           | 当注解声明在类上时必填，声明在方法上时不填            |
| `bindMethodParamTypes` | 绑定方法的参数类型                    | 方法参数类型                                     | 无，不填时默认获取首个符合条件的同名方法              |

## 3.对结果分组

这里需要强调一下 `@ContainerMethod#type` 属性，它用于指定如何对结果集按 key 分组，它通常与 `resultKey` 与 `resultType` 结合使用。

| 类型                      | 说明                                      | 分组结果                | 场景                                                         |
| ------------------------- | ----------------------------------------- | ----------------------- | ------------------------------------------------------------ |
| `MappingType.ONE_TO_ONE`  | 按 key 值一对一分组                       | `Map<key, value>`       | 默认                                                         |
| `MappingType.ONE_TO_MANY` | 按 key 值一对多分组                       | `Map<key, List<value>>` | 一个 key 对应多个值<br />比如一个 `classId` 对应多个 `Student` |
| `MappingType.NO_MAPPING`      | 返回值已经是分组后的 `Map` 集合，无需分组 | 原始的方法返回值        | 当返回值已经是 `Map` 时                                      |
| `MappingType.ORDER_OF_KEYS`        | 将输入的 key 值与结果按顺序合并           | `Map<key, value>`       | 方法的返回值是 String 或基础数据类型（及其包装类）的时候     |

下面是它们的一些使用场景，你可以参照着理解一下：

~~~java

// ========== MappingType.ONE_TO_ONE ==========

@ContainerMethod(
    namespace = "userName", type = MappingType.ONE_TO_ONE,
    resultType = User.class, resultKey = "deptId"
)
public List<User> listUserByIds(List<Integer> ids);  // 查询用户，并按用户 ID 一对一分组

// ========== MappingType.ONE_TO_MANY ==========

@ContainerMethod(
    namespace = "userName", type = MappingType.ONE_TO_MANY,
    resultType = User.class, resultKey = "deptId"
)
public List<User> listUserByDeptId(List<Integer> deptIds); // 查询用户，并按用户的所属部门 ID 一对多分组

// ========== MappingType.NO_MAPPING ==========

@ContainerMethod(namespace = "userName", type = MappingType.NO_MAPPING)
public Map<Integer, User> listUserMapByIds(List<Integer> ids); // 查询结果集已经分好组了

@ContainerMethod(namespace = "userName", type = MappingType.NO_MAPPING)
public Map<Integer, List<User>> listUserByDeptIds(List<Integer> deptIds);

// ========== MappingType.ORDER_OF_KEYS ==========

@ContainerMethod(namespace = "userName", type = MappingType.ORDER_OF_KEYS)
public String getUserNameById(Integer id);  // 查询结果集是 String 类型，无法获取 key 值，因此直接按顺序合并即可

@ContainerMethod(namespace = "userName", type = MappingType.ORDER_OF_KEYS)
public List<Integer> listUserAgeNameByIds(List<Integer> ids);
~~~

## 4.接受参数对象

有时候，我们要声明为数据源容器的方法会将对象作为查询参数，在 2.7.0 及以上版本，你可以通过下述方式来配置如何生成参数对象：

~~~java
@Assemble(
    container = "dict", props = @Mapping(src = "name", ref = "dictName"),
    keyType = DictItemQueryDTO.class, // 指定参数对象类型，该类必须有一个公开的无参构造方法
    keyDesc = "dictId:id, dictType:type", // 指定如何将属性值映射到参数对象
)
@Data
public class Foo {
    private Integer dictId;
  	private String dictType;
    private String dictName;
}

// 对应的参数对象
@Data
public class CustomerQueryDTO {
  private String id;
  private String type;
}

// 对应的接受参数对象的查询方法
@ContainerMethod(
    namespace = "onoToOneMethod", resultType = DictItemQueryVO.class,
  	type = MappingType.ORDER_OF_KEYS
)
public List<DictItemQueryDTO> listItemByIdsAndTypes(List<DictItemQueryDTO> dtos) {
    // do something
}
~~~

具体可参见 [声明装配操作](./../declare_assemble_operation.md) 中 “键的解析策略” 一节。

## 5.缓存

在 2.0 及以上版本，你可以在方法上添加 `@ContainerCache` 注解，使其具备缓存功能：

~~~java
@ContainerCache(
    expirationTime = 1000L, // 配置过期时间
    timeUnit = TimeUnit.SECONDS, // 指定过期时间单位
)
@ContainerMethod(
    namespace = "onoToOneMethod",
    resultType = Foo.class, resultKey = "id" // 返回的数据源对象类型为 Foo，并且需要按 id 分组
)
public Set<Foo> onoToOneMethod(List<String> args) {
    // do something
}
~~~

如果你的方法上同时声明了多个方法容器，那么它们都将具备缓存功能。

具体可参见后文 [缓存](./../../advanced/cache.md) 一节。

## 6.手动注册

手动注册一般只在你的目标类未被 Spring 管理，或者干脆项目没有使用 Spring 的时候会使用。

在 Spring 环境中，针对方法容器的扫描和注册是自动完成的。不过你也可以手动完成这个过程：

~~~java
// 从 Spring 容器中获取处理器和全局配置
@Autowried
private BeanMethodContainerRegistrar beanMethodContainerRegistrar;

// 基于 Foo 的实例方法创建方法容器
Foo foo = new Foo();
beanMethodContainerRegistrar.register(foo, foo.class);
~~~

如果你是在非 Spring 环境中，那么你需要先通过以下代码手动构建 `MethodContainerAnnotationProcessor` 实例，然后再手动注册：

~~~java
// 构建方法容器处理器
Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
MethodContainerAnnotationProcessor processor = ConfigurationUtil.createContainerMethodAnnotationProcessor(configuration);

// 从目标对象上解析方法容器
Foo foo = new Foo();
Collection<Container<Object>> containers = processor.process(foo, Foo.getClass());

// 将方法容器注册到全局配置对象中
containers.forEach(configuration::registerContainer);
~~~

## 7.选项式配置

在 2.2 及以上版本，你可以使用 `@AssembleMethod` 注解进行选项式风格的配置。通过在类或属性上添加 `@AssembleMethod` 注解，并指定要绑定的目标类中的指定方法。

在这种情况下，你可以快速的使用 spring 容器中的 bean 里面的方法、或任意类中的静态方法作为数据源容器。

比如：

```java
@RequiredArgsConstructor
@Data
private static class Foo {
    @AssembleMethod(
        targetType = FooService.class, // 填充数据源为 FooService#listByIds 方法
        method = @ContainerMethod(bindMethod = "listByIds", resultType = Foo.class, resultKey = "id"),
        props = {
            @Mapping("name"), // Item.name -> Item.name
            @Mapping("type") // Item.type -> Item.type
        }
    )
    private id;
    private String name;
    private String type;
}
```

出于降低理解成本的目的，这种配置方式直接复用了 `@ContainerMethod` 注解。

`@AssembleMethod` 注解提供了一些参数：

| API           | 作用                                              | 类型                                                         | 默认值                         |
| ------------- | ------------------------------------------------- | ------------------------------------------------------------ | ------------------------------ |
| `targetType`  | 指定调用类的类型                                  | 目标类                                                       | 无，与 `target` 二选一必填     |
| `target`      | 指定调用类的类型全限定名，或者容器中的 `beanName` | 调用类的全限定名字符串，如果在 Spring 容器中，则可以是 `beanName` | 无，与 `targetType` 二选一必填 |
| `method`      | 指定绑定方法                                      | `@ContainerMethod`                                           | 无，必填                       |
| `enableCache` | 是否启用缓存配置                                  | boolean                                                      | false                          |
| `cache`       | 指定缓存配置                                      | `@ContainerCache`                                            | 无                             |

此外，在选项式配置中，你同样可以通过在被 `@ContainerMethod` 注解绑定的方法上添加 `@ContainerCache` 注解的方式实现配置缓存。

不过，在 2.6.0 及以上版本，缓存配置同样集成到了 `@AssembleMethod` 中：

~~~java
@RequiredArgsConstructor
@Data
private static class Foo {
    @AssembleMethod(
        targetType = FooService.class,
        method = @ContainerMethod(bindMethod = "listByIds", resultType = Foo.class, resultKey = "id"),
        props = { @Mapping("name"), @Mapping("type") },
        enableCache = true,  // 启用缓存
        cache = @ContainerCache(expirationTime = 1000L, timeUnit = TimeUnit.SECONDS) // 设置缓存
    )
    private id;
    private String name;
    private String type;
}
~~~

需要注意的是，**如果目标方法上已经通过 `@ContainerCache` 或配置文件的方式配置缓存时，你在 `@AssembleMethod` 中的缓存配置将不会生效**，因为前者的优先级更高。
