# FAQ

以下是一些常见问题与对应的解决方案。如果当你遇到问题时，可以先尝试在这里寻找解决方案。

## 填充不生效？

- 确认在 `@AssembleXXX` 注解中正确配置了 `container` 与 `prop` / `props` 属性；
- 确认操作涉及的属性存在，且都有相应的 `setter` 和 `getter` 方法；
- 确认目标对象对应的 key 属性值不为空；
- 确认指定的数据源容器确实有根据 key 值列表返回非空集合；
- 确认通过 `BeanOperationParser` 解析类后，得到的 `BeanOperations` 中的 `AssembleOperation` 列表中有该 key 属性对应的操作配置；

当确认上述步骤皆无问题后，你可以尝试在源码中 `cn.crane4j.core.executor.handler.AbstractAssembleOperationHandler` 类的 `doProcess` 方法中添加断点：

+ 如果未进入断点，则说明该操作配置未能生效，请重新确认上述原因；
+ 进入 `collectToEntities`：在这一步，确认你填充的对象是否都已经被收集到，且 key 值被正确地获取；
+ 进入 `getSourcesFromContainer`：在这一步，确认通过上述 key 值能够正确的从数据源获取到数据；
+ 进入 `getTheAssociatedSource`：在这一步，确认待填充的对象可以通过 key 值获得相应的数据源对象；
+ 进入 `completeMapping`：在这一步，确认 crane4j 是否按你的配置正确的将数据源对象的属性值映射到待填充的对象上；

如果仍然无法解决，可以在 issues 中或者相关交流群中反馈。

## 如何实现嵌套填充？

在需要嵌套填充的属性上添加 `@Disassemble` 注解即可，具体参见 [填充嵌套对象](./../basic/declare_disassemble_operation.md)。

## 如何实现级联填充？

参见 [示例：如何级联填充](./../use_case/example_fill_relations.md) 一节。

## 如何处理一对多的情况？

通过 `@AssembleXXX` 注解中的 `handler` 或 `handlerType` 属性指定装配处理器为一对多装配处理器 `OneToManyAssembleOperationHandler` 类型或名称（在 Spring 中即为 bean 名称）即可。

具体参见 [一对多&多对多](./../basic/assemble_operation_handler.md) 中一对多装配一节。

## 键字段可以是按分隔符拼接的字符串吗？

通过 `@AssembleXXX` 注解中的 `handler` 或 `handlerType` 属性指定装配处理器为一对多装配处理器 `ManyToManyAssembleOperationHandler` 的类型或名称（在 Spring 中即为 bean 名称）即可。

具体参见 [一对多&多对多](./../basic/assemble_operation_handler.md) 中多对多装配一节。

## 键字段可以是集合或数组吗？

同上，通过 `@AssembleXXX` 注解中的 `handler` 或 `handlerType` 属性指定装配处理器为一对多装配处理器 `ManyToManyAssembleOperationHandler` 的类型或名称（在 Spring 中即为 bean 名称）即可。

具体参见 [一对多&多对多](./../basic/assemble_operation_handler.md) 中多对多装配一节。

## 为什么使用异步执行器的时候报错？

默认情况下，并没有注册异步操作执行器 `AsyncBeanOperationExecutor`，用户需要自行创建后再将其注册到全局配置中。

具体参见 [基本概念](./../user_guide/basic_concept.md) 中异步执行器一节。

## 怎么刷新容器的数据 ？

- 如果容器是 `ConstantContainer` ，直接通过 `get` 方法获取缓存的 `Map` 集合后直接修改即可；
- 获取 `Crane4jGlobalConfiguration` 或 `ContainerManager` 后，通过 `registerContainer` 使用命名空间相同的容器对旧容器进行覆盖；

## 怎么忽略掉某些字段不进行填充？

参见 [示例：如何在执行时排除某一些操作](./../use_case/example_exclude_operation.md) 一节。

## 为什么 `@ContainerMethod` 注解不生效？

如果是非 Spring 环境，则需要手动的通过 `ContainerMethodAnnotationProcessor` 扫描指定类并向全局配置注册扫描获取的方法容器。

如果是 Spring 环境，请确保：

- 容器中存在 `BeanMethodContainerRegistrar` 后处理器；
- 被注解的方法所在类被 Spring 扫描，且容器中存在对应的 bean；
- 被注解的方法所在类在 `BeanMethodContainerRegistrar` 后处理器初始化后才加载；

## 为什么 `@AutoOperate` 注解不生效？

如果是非 Spring 环境，则需要手动的通过 `MethodArgumentAutoOperateSupport` 和 `MethodResultAutoOperateSupport` 拦截方法调用。

如果是 Spring 环境，请确保：

- 开启了 `SpringAOP` 功能；
- 容器中存在 `MethodResultAutoOperateAdvisor` 或 `MethodArgumentAutoOperateAdvisor` 通知器；
- 被注解的方法所在类被 Spring 扫描、容器中存在对应的 bean 且被 Spring 代理；

## 为什么引了 guava 和 hutool ？

不想要重复造轮子，有些组件直接使用成熟的开源库比自己再写一套更可靠。

此外，`crane4j` 仅在有限的地方使用了这些工具类库：

- `guava` ：使用了缓存组件 `Cache` 与用于构造 `WeakConcurrentMap` 的  `MapMaker`；
- `hutool`：使用了类型转换取组件 `Convert`，如果没用到 `HutoolConverterManager` 可以在依赖中排除；

## 支持 jdk9+ / springboot3 吗？

支持。

## 容器可以做一些自定义的初始化/销毁操作吗？

实现 `Container.Lifecycle` 接口即可，具体参见 [容器的生命周期](./../advanced/callback_of_component.md)。

## 可以支持同时根据多个 key 字段填充数据吗？

可以，具体参照 [示例：如何通过多个Key关联数据](./../use_case/example_multi_key.md)。

## 启动应用报错 “No ServletContext set”

在 2.4.0 及更早的版本中，当你在 web 环境中通过 `@EnableCrane4j` 注解引入框架后，启动项目有可能会出现 “No ServletContext set” 问题，关于该问题的解决方案参见：[在启动类添加 `@EnableCrane4j` 注解后，启动应用报错 “No ServletContext set”](https://github.com/opengoofy/crane4j/issues/126)。

你可以直接升级 crane4j 到 2.5.0 或更高版本解决这个问题。

## 可以多线程填充吗？

你可以通过在手动或自动装配时指定使用异步执行器来实现多线程填充的效果，具体参见：

- [基本概念-操作执行器](./basic_concept)；
- [触发填充操作-指定执行器](./../basic/trigger_operation.md)；