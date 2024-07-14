# MybatisPlus 插件

`crane4j` 提供 `MybatisPlus` 的扩展组件，允许基于 `MybatisPlus` 的 `BaseMapper` 自动构建数据源容器，以便快速地实现查询关联数据并用于填充。

## 1.安装

在开始前，请先确保已经引入必要的 crane4j 配置，然后在此基础上，额外的引入下述依赖：

~~~xml
<!-- 引入 crane4j-extension-mybatis-plus -->
<dependency>
    <groupId>cn.crane4j</groupId>
    <artifactId>crane4j-extension-mybatis-plus</artifactId>
    <version>${last-version}</version>
</dependency>

<!-- 引入 mybatis-plus 依赖，若已有则可以跳过 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>${last-version}</version>
</dependency>
~~~

此后当项目启动时，将会自动加载相关的组件。

## 2.注册Mapper

在使用前，你需要通过自动注册、手动注册或懒加载的方式向 crane4j 注册 `BaseMapper` 接口。

### 2.1.懒加载

在 Spring 环境中，用户默认不需要进行额外的操作，`AssembleMpAnnotationHandler` 会在用户使用时根据 `beanName` 自动从 Spring 上下文中获得对应的 `Mapper`，并完成自动注册，即懒加载。

### 2.2.自动注册

在 Spring 环境中，用户也可以指定 `auto-register-mapper` 为 `true` 开启自动注册，相关配置如下：

~~~yml
crane4j:
 mybatis-plus:
  auto-register-mapper: true # 启动自动注册
  includes: xxxMapper, xxxMapper # 仅注册指定 Mapper
  excludes: xxxMapper, xxxMapper # 仅排除指定 Mapper
~~~

在开启自动注册的情况下，spring 上下文中任何符合 `includes` 与 `excludes` 规则的 `Mapper` 都会被注册。

### 2.3.手动注册

用户也可以获取 `AssembleMpAnnotationHandler` 进行手动注册：

~~~java
AssembleMpAnnotationHandler handler = SpringUtil.getBean(AssembleMpAnnotationHandler.class);
handler.registerRepository("xxxMapper", xxxMapper);
~~~

## 3.使用

用户可以使用 `@AssembleMp` 注解来配置以 `BaseMapper` 接口的查询方法作为数据源的装配操作。这个注解会被配置解析器中的专门的注解解析器 `AssembleMpAnnotationHandler` 解析为 `AssembleOperation`。

在使用时，由于可以同时指定**查询的条件字段**和**查询字段**，从而有四种情况：

- 根据默认主键查询全部字段；
- 根据默认主键查询指定字段；
- 根据指定外键查询全部字段；
- 根据指定外键查询指定字段；

假设我们有一个数据库表映射对象如下：

```java
@TableName("foo")
public class Foo {
    @TableId
    private Integer id;
    @TableField("user_name")
    private String userName;
    @TableField("user_age")
    private Integer userAge;
}
```

并且在 Spring 上下文中已经有了一个继承 `BaseMapper` 接口的 `FooMapper` bean，其 bean 名称默认为 `fooMapper`。

### 3.1.根据主键查询全部字段

```java
public class Foo {
    @AssembleMp(
        mapper = "fooMapper",
        props = @Mapping(src = "name", ref = "name")
    )
    private Integer id;
    private String name;
}
```

当执行装配时，数据源等同于基于 `id` 批量查询出来的 `Foo` 对象，SQL 为 `select * from foo where id in ?`。

### 3.2.根据主键查询指定字段

```java
@AssembleMp(
    mapper = "fooMapper",
    selects = {"userName", "userAge"} // 要查询的字段
)
private Integer id;
```

上述配置相当于使用 `QueryWrapper` 构建并执行了 `select user_name AS userName, user_age AS userAge, id from foo where id in ?` 这条 SQL，查询出的数据将按照 `Foo` 中配置的主键 `id` 进行分组。

**查询字段名为实体类中对应的属性名，构建 SQL 时会自动转换为查询 SQL**。

:::tip

默认情况下，`crane4j` 将使用被 `@TableId` 注解标记的属性作为主键。

:::

### 3.3.根据指定外键查询全部字段

```java
@AssembleMp(
    mapper = "fooMapper",
    where = "userName" // 查询的条件字段
)
private String name;
```

上述配置相当于使用 `QueryWrapper` 构建并执行了 `select * from foo where user_name in ?` 这条 SQL，查询出的数据将按照用户指定的 `userName` 属性进行分组。

### 3.4.根据指定外键查询指定字段

```java
@AssembleMp(
    mapper = "fooMapper",
    selects = {"userName", "userAge"}, // 要查询的字段
    where = "userName" // 查询的条件字段
)
private String name;
```

上述配置相当于使用 `QueryWrapper` 构建并执行了 `select user_age AS userAge, user_name AS userName from foo where user_name in ?` 这条 SQL，查询出的数据将按照用户指定的 `name` 属性进行分组。

:::tip

由于查询出的数据需要根据用户指定的外键字段进行分组，并与键值对应，因此如果用户指定了查询字段，但未包含该外键字段时，将自动在查询字段后面追加该外键字段。

:::

## 4.指定查询字段 SQL

通常情况下，建议用户始终使用**实体类中的属性名作为查询字段/查询外键**，`crane4j` 会借助 MP 的 `TableInfo` 将其转换为对应的表字段 SQL。

然而，有时确实需要自定义查询字段的情况，因此可以直接编写自定义 SQL 作为查询字段。

例如，假设有以下 `Bean`：

```java
@TableName("foo")
public class FooDO {
    @TableId
    private Integer id;
    @TableField("user_name")
    private String name;
    @TableField("user_age")
    private Integer age;
}
```

然后装配配置如下：

```java
public class FooVO {
    @AssembleMp(
        mapper = "fooMapper",
        selects = {"user_name AS name", "userAge AS age"}
    )
    private Integer id;
    private String name;
    private String age;
}
```

最终执行的 SQL 为：`select user_age AS age, user_name AS name from foo where user_name in ?`。

::: warning

需要注意的是，该查询是基于 `QueryWrapper` 完成的，因此在这种情况下，查询的表字段可能与用户的对象属性不一致，且无法自动设置别名。

:::

## 5.对结果分组

与方法容器类似，基于 MyBatisPlus 的查询也允许指定查询结果的一对一或一对多映射类型。例如：

```java
public class DeptEmpVO {
    @AssembleMp(
        mapper = "empMapper", where = "deptId", // 根据部门 id 查询员工集合
        mappingType = MappingType.ONE_TO_MANY, // 按部门 id 进行一对多映射
        handlerType = OneToManyAssembleOperationHandler.class, // 一对多映射处理器
        props = @Mapping(src = "name", ref = "deptNames") // 将指定部门下所有的员工名称映射到 empNames 集合
    )
    private Integer deptId;
    private List<String> empNames;
}
```

上述示例中，使用 `@AssembleMp` 注解指定在 `empMapper` 中根据部门 id 查询员工集合，然后按部门 id 进行一对多映射。最后，将员工集合中的员工名称映射到 `DeptEmpVO` 对象的 `empNames` 集合中。

这样，我们可以实现多对一的映射关系。

同样的，如果分组的时候 key 可能重复，那么你可以像使用 `@ContainerMethod` 配置数据源容器那样，通过 `duplicateStrategy` 属性来配置处理策略：

~~~java
public class DeptEmpVO {
    @AssembleMp(
        mapper = "empMapper", where = "deptId",
      	duplicateStrategy = DuplicateStrategy.DISCARD_OLD, // 如果 key 重复，那么默认使用新覆盖旧
        props = @Mapping(src = "name", ref = "deptNames")
    )
    private Integer deptId;
    private List<String> empNames;
}
~~~

## 6.多数据源

Crane4j 提供指定指定数据源的方法，但是考虑不同的项目中实现多数据源的方案并不相同，因此这里不提供具体的实现。

### 6.1.实现数据源切换器

如果你希望指定使用某个特定的数据源来完成查询，那么你需要实现 `AssembleMpAnnotationHandler.DataSourceSwitcher` 接口，在里面定义具体的切换数据源的逻辑：

~~~java
public class ExampleDataSourceSwitcher 
  	implements AssembleMpAnnotationHandler.DataSourceSwitcher {
  	
      @Override
      public void beforeInvoke(String dataSource) {
          // 在这里定义切换数据源的逻辑
      }
  
      @Override
      public void afterInvoke(String dataSource) {
          // 在这里定义完成查询后清理上下文的逻辑
      }
}
~~~

### 6.2.注册切换器

此后，你可以将该数据源切换器设置到 `AssembleMpAnnotationHandler` 中：

~~~java
public AssembleMpAnnotationHandler assembleMpAnnotationHandler(
    AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
    MethodInvokerContainerCreator methodInvokerContainerCreator, BeanFactory beanFactory) {
    AssembleMpAnnotationHandler handler = new AssembleMpAnnotationHandler(
        annotationFinder, globalConfiguration, methodInvokerContainerCreator
    );
    handler.setRepositoryTargetProvider(new MapperLazyLoader(beanFactory));
  
  	// 设置数据源切换器
    ExampleDataSourceSwitcher dss = new ExampleDataSourceSwitcher();
  	handler.setDataSourceSwitcher(dss);
  	
    return handler;
}
~~~

### 6.3.指定数据源

最后，在使用的时候直接在 `@AssembleMp` 中通过 `datasource` 指定要使用的数据源即可：

~~~java
@AssembleMp(
    mapper = "fooMapper",
    where = "userName",
  	datasource = "ds2" // 指定使用数据源
)
private String name;
~~~

:::warning

考虑到一些多数据源的切换可能会依赖 `ThreadLocal` 实现，如果确实如此，那么你使用异步填充时可能需要注意一下上下文切换的问题。

关于异步填充，具体请参见：[异步填充](./../advanced/async_executor.md)。

:::