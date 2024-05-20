# Jackson 插件

在 2.2 及以上版本，crane4j 提供了基于 Jackson 的扩展插件。启用后，用户可以在通过 `ObjectMapper` 将对象序列化为 JSON 字符串的时候进行字段填充，该填充完全兼容已有的各项配置。

并且，由于 JsonNode 的特殊性，该填充可以在属性映射时“凭空”为对象添加原本不具备的属性，在默认使用 Jackson 进行序列化的 SpringMVC 应用中，引入该扩展会带来不少的便利。

## 1.安装

在开始前，请先确保已经引入必要的 crane4j 配置，然后在此基础上，额外的引入下述依赖：

~~~xml
<!-- 引入 crane4j-extension-jackson -->
<dependency>
    <groupId>cn.crane4j</groupId>
    <artifactId>crane4j-extension-jackson</artifactId>
    <version>${last-version}</version>
</dependency>

<!-- 引入 jackson 依赖，若已有则可以跳过 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>${last-version}</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>${last-version}</version>
</dependency>
~~~

此后当项目启动时，将会自动加载相关的组件。

## 2.启用插件

此扩展插件基于 `ObjectMapper` 的 `Module` 机制实现，因此不管在哪个环境使用，最终都需要将其注册到待使用的 `ObjectMapper` 实例中。

### 2.1.在 Spring 环境

当 Spring 容器有且仅有一个可用的 `ObjectMapper` 时，在项目启动后，会自动将对应的模块注册到该实例中，后续直接使用即可。

若 Spring 容器中**存在多个 `ObjectMapper`**，则需要按下述方式进行手动配置：

~~~java
// 在 Spring 环境中，请将 module 注册到你需要的 ObjectMapper 实例中
@Autowried
private JsonNodeAutoOperateModule jsonNodeAutoOperateModule;
@Autowried
private ObjectMapper objectMapper;

private void init() {
    objectMapper.registerModule(jsonNodeAutoOperateModule);
}
~~~

当使用注册有 `JsonNodeAutoOperateModule` 的 `ObjectMapper` 序列化对象时，自动填充将会生效。

### 2.2.在非 Spring 环境

若你在非 Spring 环境中，则需要按如下方式配置 `ObjectMapper`：

~~~java
// 准备一个 ObjectMapper
ObjectMapper objectMapper = new ObjectMapper();

// 创建全局配置类，需要使用 JsonNodePropertyOperator 保证默认的属性操作者
SimpleCrane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create(
    SimpleAnnotationFinder.INSTANCE, SimpleConverterManager.INSTANCE,
    new JsonNodePropertyOperator(new JacksonJsonNodeAssistant(objectMapper), new ReflectivePropertyOperator())
);
// 基于上述配置创建一个JsonNodeAutoOperateModule
JsonNodeAutoOperateModule autoOperateModule = new JsonNodeAutoOperateModule(
    new ClassBasedAutoOperateAnnotatedElementResolver(configuration, new OgnlExpressionEvaluator(), OgnlExpressionContext::new), objectMapper, SimpleAnnotationFinder.INSTANCE);

// 将 JsonNodeAutoOperateModule 注册到 ObjectMapper
objectMapper.registerModule(autoOperateModule);
~~~

## 2.使用

相比起普通的填充操作，仅需要实体类上添加 `@AutoOperate` 注解即可，除此之外不需要进行额外的配置：

~~~java
@AutoOperate(type = Foo.class) // 声明该对象序列化时需要进行自动填充
@Data // 使用 lombk 生成 setter 和 getter 方法
private static class Foo {
    @Assemble(
        container = "test", // 指定 key 类型为 Integer
        props = @Mapping(ref = "userName"),
        keyType = Integer.class
    )
    private Integer userId;
}
~~~

然后，我们可以使用上文配置的序列化器对其进行序列化：

~~~java
Foo foo = new Foo(1);
String json = objectMapper.writeValueAsString(foo);
System.out.println(json);
// {"user_id":1, "user_name": "name1"}
~~~

我们可以注意到，由于 `JsonNode` 数据结构的特殊性，因此在序列化过程中，你可以通过 `props` **动态的增添或替换 JSON 对象中的属性**。

比如在上文，原本的实体类 `Foo` 中并没有 `user_name` 属性，但是序列化后 crane4j 为你动态加上的这个字段。

:::warning

由于序列化为 `JsonNode` 的时候会将所有的 `key` 值转为字符串类型，因此当你的 `Container` 接受的 `key` 不为字符串时，你需要通过 `keyType` 显示指定 `key` 值的类型。

:::

**填充 `Controller`**

在 SpringMVC 中，由于默认使用了 Jackson 作为 JSON 库，因此你可以非常丝滑的接入，比如：

~~~java
@ResponseBody
@GetMapping("/{id}")
public Foo getFoo(@Pathvariable Integer id) {
    return new Foo(1);
}

// {"user_id":1, "user_name": "name1"}
~~~

当在在 `Controller` 上添加 `@RestConroller` 后注解，或者在方法上添加  `@ResponseBody` 注解后，crane4j 将会在 Spring 把你的返回值序列化为 JSON 对象时自动填充相关数据。

:::warning

前提是保证插件确实注册到该 `ObjectMapper`，当你的项目中存在多个 `ObjectMapper` 时，可能需要手动向 `ObjectMapper` 注册 json 插件，具体参见上文的安装步骤。

:::

## 3.可选配置

由于序列化过程中的填充也算是一种自动填充，因此和基于方法的返回值或入参自动填充一样，它也支持基于 `@AutoOperate` 注解的一些配置项。

比如：

~~~java
@AutoOperate(
    type = Foo.class, 
    excludes = "ex", // 排除 ex 分组的操作
    executorType = OrderedBeanOperationExecutor.class // 当存在多个操作时，进行异步填充
)
@Data
private static class Foo {
    @Assemble(
        container = "test", // 指定 key 类型为 Integer
        props = @Mapping(ref = "userName"),
        keyType = Integer.class
    )
    private Integer userId;
}
~~~

具体配置可以参见“自动填充”一节。