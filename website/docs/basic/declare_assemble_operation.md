# 声明一个装配操作

在 crane4j 中，装配操作指一个 “根据 A 的 key 值拿到 B，再把 B 的属性映射到 A” 的操作，在代码上，它对应一个在类或类属性上声明的 `@Assemble` 注解。

一个装配操作包含三个必要的属性：

+ key 字段：被注解的属性，或 `@Assemble#key` 属性指向的属性；
+ 数据源容器：即 `@Assemble#container` 属性指向的数据源容器；
+ 字段映射：即通过 `@Assemble#props` 属性对应的字段映射配置；

具体内容可以参考 “[基本概念](./../user_guide/basic_concept.md)” 一节。

## 1.声明装配操作

所有的 `@Assemble` 注解都支持在类或类的属性上使用（其实也支持在方法使用，后续会介绍）。

### 1.1.在属性上声明

你可以使用 `@Assemble` 在某个 key 字段上声明一个装配操作：

```java
public class Student {
    @Assemble(container = "student", props = @Mapping(src = "studentName", ref = "name"))
    private Integer id;
    private String name;
}
```

上述配置表示：

1. 使用 `id` 字段的值；
2. 从命名空间为 `student` 的数据源容器中获取数据源对象；
3. 获取与 `id` 字段值对应的数据，将数据源对象中的 `studentName` 字段值映射到 `name` 字段上；

:::tip

注意，默认情况下，即使你的 key 字段（即上文的 id 字段）为 null，cranej4 依然会认为它是一个有效值，会使用 null 尝试查出一个对应的数据。

如果你希望过滤掉为 key 字段值为 null 的操作，你可以：

- 获取 `OneToOneAssembleOperationHandler` 后，调用 `setIgnoreNullKey` 方法设置过滤 null 值；
- 在 springboot 环境，直接在配置文件里面添加 `crane4j.ignore-null-key-when-assembling`；
- 在 2.6.0 及更高版本，使用 `@ConditionOnPropertyNotNull` 条件注解。具体可参见 [设置操作触发条件](./operation_condition.md) 一节。

:::

### 1.2.在方法上声明

在**有返回值的无参方法**上声明也是允许的，这种情况下 crane4j 会认为其为一个 fluent 风格的 getter 方法。比如：

```java
public class Student {
    private String name;
    
    // 标准 getter 方法
    @Assemble(container = "student", props = @Mapping(src = "studentName", ref = "name"))
    public Integer getId() {
        // return student id.
    }
    
    // fluent 风格的 getter 方法
    @Assemble(container = "student", props = @Mapping(src = "studentName", ref = "name"))
    public Integer id() {
        // return student id.
    }
}
```

### 1.3.在类上声明

不过，你也可以在类上声明，此时你需要显式的指定该操作要绑定的 **key 字段**或**有返回值的无参方法**。比如：

```java
// 直接声明
@Assemble(
    key = "id", // 手动指定绑定到 id 字段上，此外，你可以绑定到一个 getter 方法上
    container = "student", 
    props = @Mapping(src = "studentName", ref = "name")
)
public class Student {
    private Integer id;
    private String name;
}
```

在无法直接修改父类的情况下，可以在类上添加注解来声明装配操作。

此外，在父类或父类的属性上声明的操作，也会被子类所继承。

:::tip

`key` 属性默认支持使用 `xx.xx.xx` 格式的链式操作符访问嵌套对象的属性；

:::

### 1.4.重复声明

你可以在同一个键上声明多次装配操作：

```java
public class UserVO {
    @Assemble(
        id = "assemble_user_role", // 为了便于区分，你可以额外的为其指定一个 id
    	container = "user_role", props = @Mapping(src = "role", ref = "role")
    )
    @Assemble(
        id = "assemble_user",
        container = "user", props = @Mapping(src = "name", ref = "name")
    )
    private Integer id;
    private String name;
    private String role;
}
```

一般情况下，crane4j 会自动帮你合并在同一次操作中对相同数据源容器的调用，所以不用担心重复查询数据库。

:::tip

在默认情况，id 与 key 字段同名，不同的操作配置可以具备相同的 id。

`id` 属性不是必须的，它主要是作为一个区分标识，便于后续用户为其绑定条件，或在回调方法中获取。关于此部分内容，具体可参见 [设置操作触发条件](./operation_condition.md) 与 [组件的回调接口](./../advanced/callback_of_component.md) 一节。

:::

## 2.显式指定key类型

在一些情况下，你的数据源容器接受的 key 类型，可能与你实际的 key 类型并不相同。

在 2.2 及以上版本，你可以在注解中通过 `keyType`  强制将 key 的类型转为指定类型，从而保证它与数据源接受的参数类型一致。比如：

~~~java
// 容器对应的key类型为Integer集合
Containers.<Integer>forLambda("foo", ids -> ids.stream().collector(Collectors.toMap(Function::identify, Function::identify)));

// 带填充对象的key类型为String
public class Foo {
    @Assemble(
        container = "foo", props = @Mapping(src = "a", ref = "b"),
        keyType = Integer.class // 指定 key 类型强制转为 Integer
    )
    private String id;
    private String b;
}
~~~

在上述代码中，我们的数据源容器 `foo` 仅接受 `Integer` 类型的参数，而 `foo.id` 的类型却是 `String`，因此通过 `keyType` 指定其需要在执行时将 key 转为 `Integer`。

:::tip

从 2.2 版本开始，`crane4j` 提供了用于在序列化过程中进行填充的 `jackson` 插件，`jackson` 插件默认从 JsonNode 中拿到的所有 key 值都为 `String` 类型，因此当使用此插件时，若你的数据源容器接受的参数类型不是 `String`，则必须手动显式的在注解配置中指定 key 的类型。

:::

## 3.键的解析策略

在 2.7.0 及以上版本，你可以通过指定 `keyResolver`  ，或者基于操作处理器 `AssembleOperationHandler` 通过  `keyDesc` 中配置一些附加信息来调整键值的解析策略。

关于后一种场景，可以直接参见 [装配操作处理器](./assemble_operation_handler.md) 一节。

### 3.1.将属性值合成为参数对象

当你使用一对一或一对多装配处理器时，你可以通过 keyDesc 配置属性映射，以便动态生成一个参数对象作为 key 值：

~~~java
@Assemble(
    container = "foo",
    keyType = FooQueryDTO.class, // 指定参数对象类型，该类必须有一个公开的无参构造方法
    keyDesc = "id:prop1, name:prop2", // 指定如何将属性值映射到参数对象
    props = @Mapping("name")
  )
@Data
public class Foo {
  private String id;
  private String name;
}

// 参数对象
@Data
public class FooQueryDTO {
  private String prop1;
  private String prop2;
}
~~~

-   将注解配置在类而非属性或方法上；
-   不指定该操作绑定的 key 字段，并将 `keyType` 设置为你要生成的参数对象类型
-   接着在 `keyDesc` 中指定如何进行属性映射，其中：
    -   `keyDesc` 的格式为 “源属性名1:目标属性名1, 源属性名2:目标属性名2……”；
    -   如果你的源属性名与目标属性名一致，则也可以写为“属性名1, 属性名2……”；
    -   当如果你不填写 `keyDesc` ，那 crane4j 将尝试对所有的同名字段进行拷贝；

此后，当执行时， crane4j 将根据你的配置，尝试通过无参构造器创建一个参数对象，随后将目标对象的属性映射到参数对象中，最后再将参数对象作为本次调用的 Key 值。

:::tip

该功能基于 `ReflectiveBeanKeyResolver` 实现，不过默认的一对一与一对多装配处理器默认内置了该解析，因此你只需要通过 `keyDesc` 进行配置即可。

该功能通常配合方法容器使用，具体内容可参见 [方法容器](./container/method_container.md) 一节。

:::

### 3.2.将字符串按分隔符拆分

当你使用多对多参数处理器 `ManyToManyAssembleHandler` 进行多对多批量装配时，你可以在 `keyDesc` 属性值中指定分隔符：

```java
@Data
public class Foo {
  
  @Assemble(
    container = "foo",
    handlerType = ManyToManyAssembleHandler.class, // 使用多对多参数处理器
    keyDesc = ",", // 指定使用 “，” 作为分隔符
    keyType = Integer.class, // 指定将分割出的每个 key 都转为 Integer 类型
    props = @Mapping("name")
  )
  private String ids;
  private String name;
}
```

在默认情况下，总是使用 “`,`” 符号作为分隔符。

:::tip

该功能基于 `ReflectiveSeparablePropertyKeyResolver` 实现，不过默认的多对多装配处理器默认内置了该解析，因此你只需要通过 `keyDesc` 进行配置即可。

:::

### 3.3.为键值指定别名

在一些情况下，我们可能需要依次获取对象的某几个属性值，并将首个非空值作为实际的 key 值，类似下述代码：

~~~java
Bean bean = new Bean();
Integer id = bean.getId(); // 默认取 id 属性值
if (Objects.isNull(id)) { // 若 id 属性为空，则取 userId 属性
  	id = bean.getUserId();
}
if (Objects.isNull(id)) { // 若 userId 属性为空，则取 key 属性
  	id = bean.getKey();
}
return id;
~~~

我们可以通过指定键值解析器为 `ReflectiveAliasPropertyKeyResolver` 来实现类似的效果：

~~~java
@Data
public class Foo {
    @Assemble(
        container = "user",
        props = @Mapping("name"),
        keyResolver = ReflectiveAliasPropertyKeyResolver.class, // 指定键值解析器
        keyDesc = "id, userId, userKey" // 指定关联三个属性
    )
    private String id;
    private String userId;
    private String userKey;
    private String name;
}
~~~

### 3.4.自定义解析器

如果默认的解析器不符合你的要求，你也可以实现 `KeyResolver` 接口，并指定使用你自己的解析器。

在 Spring 环境下，你可以直接将其交给 Spring 管理，crane4j 将会优先从 Spring 容器中获取。此外，你也可以提供一个无参构造方法，在无法直接从 Spring 容器获取的情况下，crane4j 将会通过反射创建一个实例。

此外，如果你的 `KeyResolver` 在执行的过程中涉及到大量的逻辑判断，那么我们推荐参考 `ReflectiveAliasPropertyKeyResolver` 将其设置为多例 Bean，并且通过缓存等机制避免运行时的额外判断逻辑。

## 4.属性映射策略

在默认情况下，当我们指定要将数据源对象的 a 属性映射到目标对象的 b 属性时，仅当 a 的属性值不为 `null` 才会对 b 属性进行赋值。在 `2.1.0` 及以上版本， 该行为可以通过在指定属性映射策略改变。

### 4.1.指定策略

比如，如果我们希望仅当目标对象的 b 属性值为 `null` 时，才允许将数据源对象的 a 属性值映射过来：

~~~java
public class Foo {
    @Assemble(
        container = "foo", props = @Mapping(src = "a", ref = "b"),
        propertyMappingStrategy = "ReferenceMappingStrategy" // 指定属性映射策略
    )
    private String id;
    private String b;
}
~~~

默认提供了三种策略：

+ `OverwriteMappingStrategy`：覆写策略，不论 `src` 对应的属性值是不是 `null` ，都强制覆写 `ref` 对应的目标属性；

+ `OverwriteNotNullMappingStrategy`：非空时覆写策略，仅当 `src` 对应的属性值不为 `null` 时，强制覆写 `ref` 对应的目标属性。

	当不指定策略时，将默认使用它作为实际的映射策略；

+ `ReferenceMappingStrategy`：空值引用策略，仅当 `ref` 对应的目标属性为 `null` 时，才获取 `src` 的属性值；

### 4.2.自定义策略

你可以通过实现 `PropertyMappingStrategy` 接口创建自定义策略。

在 Spring 环境中，你只需要将自定义策略交由 Spring 容器管理即可，项目启动后 crane4j 会自动注册。

而在非 Spring 环境中，你可以直接将其注册到 `SimpleCrane4jGlobalConfiguration` 中：

~~~java
SimpleCrane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
PropertyMappingStrategy customStrategy = CustomPropertyMappingStrategy();
configuration.addPropertyMappingStrategy(customStrategy);
~~~

## 5.表达式支持

在 Spring 环境中，你可以在 `@Assemble` 注解的 `container` 属性里引用一些配置文件属性：

```java
public class Foo {
    @Assemble(container = "${custom.namespace}")
    private String name;
    private String alias;
}
```

或者使用 SpEL 表达式：：

```java
public class Foo {
    @Assemble(container = "${custom.namespace} + '$$defaultProvider'")
    private String name;
    private String alias;
}
```

用法与 Spring 提供的 `@Value` 一致。