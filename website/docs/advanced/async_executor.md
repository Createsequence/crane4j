## 异步填充

在 crane4j 中，所有的填充操作都通过操作执行器 `BeanOperationExecutor` 完成触发，因此，你可以通过改变执行器支持异步填充，Crane4j 默认已经提供了一个异步执行器 `AsyncBeanOperationExecutor` 执行器 

:::tip

关于执行器，请参照 "[基本概念](./../user_guide/basic_concept.md)" 一节中执行器部分内容。

:::

## 1.启用执行器

### 1.1.在 Spring 环境

在 Spring 中，已经默认注册了一个异步执行器，它默认使用的线程池配置如下：

~~~java
ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
// 最大线程数与核心线程数皆为 cpu 核心数，不设置队列大小，且允许核心线程池超时
int processors = Runtime.getRuntime().availableProcessors();
executor.setCorePoolSize(processors);
executor.setMaxPoolSize(processors);
executor.setAllowCoreThreadTimeOut(true);
executor.setQueueCapacity(1);
// 当触发拒绝策略时，由主线程进行继续执行任务
executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
executor.setThreadNamePrefix("crane4j-async-executor");
~~~

如果你需要更换线程池，你可以通过配置类重新配置执行器，它将会覆盖 Crane4j 中的默认配置。

### 1.2.在非 Spring 环境

在非 Spring 环境中，你可以直接将其注册到 Crane4j 全局配置类中：

~~~java
// 创建线程池
int processNum = Runtime.getRuntime().availableProcessors();
ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
    processNum * 2, processNum * 2,
    0L, TimeUnit.MILLISECONDS,
    new LinkedBlockingQueue<>(10),
    new ThreadFactoryBuilder().setNameFormat("crane4j-thread-pool-executor-%d").build(),
    new ThreadPoolExecutor.CallerRunsPolicy()
);
// 创建异步操作执行器
AsyncBeanOperationExecutor operationExecutor = new AsyncBeanOperationExecutor(configuration, threadPoolExecutor);
// 指定每一批处理对象数
operationExecutor.setBatchSize(5);

// 将其注册到全局配置类
SimpleCrane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
configuration.getBeanOperationExecutorMap().put(operationExecutor.getClass().getSimpleName(), operationExecutor);
~~~

## 2.使用执行器

### 2.1.在自动填充时使用

在进行自动填充时，你可以在 `@AutoOperate` 注解中指定使用异步执行器：

~~~java
@AutoOperate(type = Foo.class, executor = "AsyncBeanOperationExecutor")
public List<Foo> getFoo(Integer type) {
    // do nothing
}
~~~

### 2.2.在手动填充时使用

你也可以在手动填充时使用：

~~~java
List<Foo> foos = fooService.list();
OperateTemplate template = SpringUtil.getBean(OperateTemplate.class);
AsyncBeanOperationExecutor operationExecutor = SpringUtil.getBean(AsyncBeanOperationExecutor.class);
OperateTemplate.execute(foos, operationExecutor, op -> true);
~~~

### 2.3.在操作者接口中使用

在操作者接口中，你可以在 `@Operator` 注解中指定要使用异步执行器：

~~~java
@Operator(executor = "AsyncBeanOperationExecutor") // 使用异步执行器
private interface OperatorInterface {
    @Assemble(key = "id", container = "test", props = @Mapping(ref = "name"))
    void operate(Collection<Map<String, Object>> targets);
}
~~~

## 3.批量大小

异步执行器执行的时候，将会先将操作按数据源容器分组，然后在前者的基础上，再根据指定的操作数量进行分组。

你可以通过 `setBatchSize` 调整执行器的每次向线程池提交任务时，一次任务中操作对象的数量。

比如：

~~~java
// 创建异步操作执行器
AsyncBeanOperationExecutor operationExecutor = new AsyncBeanOperationExecutor(configuration, threadPoolExecutor);
// 指定批量大小
operationExecutor.setBatchSize(5);
~~~

举个例子：

假设我们现在有 10 个待填充 Bean 对象，而每个 Bean 又各自通过 `nsetedBean` 嵌套一个 Bean 对象，即总共有 20 个 Bean。

现在，我们在 Bean 中分别基于 `id`、`key` 和 `code` 声明了三个装配操作，具体配置如下：

~~~java
@Data
private static class Bean {

    @Assemble(container = "container1", props = @Mapping("name"))
    private Integer id;
    private String name;
    
    @Assemble(container = "container2", props = @Mapping("value"))
    private Integer key;
    private String value;
    
    @Assemble(container = "container2", props = @Mapping("val"))
    private Integer code;
    private String val;
    
    @Disassemble(type = Bean.class)
    private Bean nsetedBean;
}
~~~

那么，当我们填充这 10 个 Bean 时，实际上总共需要完成 10 * (1 + 1) * 3 共 60 组操作。

为了保证尽可能减少查库次数，因此默认情况下，执行器会将 60 次填充按对应的数据源容器打包成两个任务提交给线程池完成：

- 查询 `container1`，然后完成全部基于 `container1` 的 40 组操作；
- 查询 `container2`，然后完成基于 `container1` 的 20 组操作；

上述这个逻辑的问题在于，当需要填充的对象越来越多，且需要映射的字段也越来越多时，反射读写字段消耗的时间也会越来越多，甚至可能会超过查库或 RPC 调用所消耗的时间。

此时，为了提高效率，你可以指定批量大小，将每一组操作再拆分为更细粒度的任务。比如，如果你可以指定批量大小为 20，那么第一个任务就会被拆成两份，此时实际上提交到线程池中的任务就是三个：

- 查询 `container1`，然后完成基于 `container1` 的 20 组操作；
- 查询 `container1`，然后完成基于 `container1` 的 20 组操作；
- 查询 `container2`，然后完成基于 `container1` 的 20 组操作；

:::tip

所有的执行器都支持设置批量大小，不过在同步执行时意义不大

:::