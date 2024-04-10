[English](https://github.com/opengoofy/crane4j/blob/dev/README-EN.md) | [中文](https://github.com/opengoofy/crane4j/blob/dev/README.md)

<img src="https://user-images.githubusercontent.com/49221670/221162632-95465432-f2df-4286-a53a-af59d70b1958.png" alt="image-20230220150040070" style="zoom: 80%;" />

![codecov](https://img.shields.io/badge/license-Apache--2.0-green) [![codecov](https://codecov.io/gh/opengoofy/crane4j/branch/dev/graph/badge.svg?token=CF2Q60Q0VH)](https://codecov.io/gh/opengoofy/crane4j) [![star](https://gitee.com/opengoofy/crane4j/badge/star.svg?theme=dark)](https://gitee.com/opengoofy/crane4j/stargazers) ![stars](https://img.shields.io/github/stars/Createsequence/crane4j) ![maven-central](https://img.shields.io/github/v/release/Createsequence/crane4j?include_prereleases)

# Crane4j

一个简单易用的数据映射框架，通过简单的注解配置快速根据外键/编码值填充相关字段，支持字典，枚举，方法等多种数据源。 

![image-20230810233647099](http://img.xiajibagao.top/image-20230810233647099.png)

## 它是什么？

在日常的开发工作中，我们经常面临着繁琐的数据组装任务：**根据一个对象的某个属性值，获取相关联的数据，并将其映射到另一个对象的属性中**。这种需求经常涉及到字典项、配置项、枚举常量，甚至需要进行关联数据库表的查询。这样的数据填充任务往往耗费大量时间和精力，而且容易产生重复的样板代码，让人感到心烦。

`crane4j` 旨在为了解决这种烦恼而生，它是一套注解驱动的数据填充框架。通过简单的注解配置，`crane4j` 可以优雅高效地完成不同数据源、不同类型、不同命名的字段填充任务，让你专注于核心业务逻辑而不再被繁琐的数据组装工作所困扰。

## 它有哪些特性？

- **多样的数据源支持**：支持枚举、键值对缓存和方法作为数据源，也可通过简单的自定义扩展兼容更多类型的数据源，并提供对所有数据源的缓存支持；
- **强大的字段映射能力**：通过注解即可完成不同类型字段的自动映射转换，还支持包括模板、排序、分组和嵌套对象填充等功能；
- **高度可扩展**：用户可以自由替换所有主要组件，结合 Spring 的依赖注入可实现轻松优雅的自定义扩展；
- **丰富的可选功能**：提供额外的自动填充方法返回值和方法入参参数，多线程填充，自定义注解和表达式，数据库框架插件等可选功能；
- **开箱即用**：简单配置即可与 spring/springboot 快速集成，也支持在非 spring 环境中使用；

## 文档

项目文档 document：[GitHub](https://opengoofy.github.io/crane4j/#/) / [Gitee](https://createsequence.gitee.io/crane4j-doc/#/)

## 快速开始

**引入依赖**

~~~xml
<dependency>
    <groupId>cn.crane4j</groupId>
    <artifactId>crane4j-spring-boot-starter</artifactId>
    <version>${last-version}</version>
</dependency>
~~~

**声明填充操作**

通过在类、字段或 getter 方法上添加注解，即可基于上文配置的数据源声明填充操作，支持一对一、一对多甚至多对多的属性映射：

~~~java
@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public static class Foo {

    // 根据从 getter 方法获取的值，从指定的数据源中获取对应的对象，然后将其name映射到当前的name中
    @Assemble(container = "method", prop = "name")
    public Integer getId() {
        // return value
    }
    private String name;

    // 根据 gender 获得对应的枚举对象，然后将其 name 属性映射到 genderName 中
    @AssembleEnum(type = Gender.class, prop = "name:genderName")
    private Integer gender;
    private String genderName;
    
    // 根据部门 ID 查询员工列表，并取列表中员工对象的名称映射到 empNames
    @AssembleMethod(
        handlerType = OneToManyAssembleHandler.class, targetType = empService.class,
        method = @ContainerMethod(bindMethod = "listByDeptId", resultType = Emp.class),
        prop = "name:empNames"
    )
    private Integer deptId;
    private List<String> empNames;
    
    // 根据常量值获取对应的常量名称，并映射到 letterName
    @AssembleConstant(type = LetterConstant.class)
    private String letter;
    private String letterName;

    // 将自己的 name 属性映射到 fooName
    @Assemble(prop = "name:fooName")
    private String fooName;
    
    // 将 phone 字段根据自定义策略进行处理后回填
    @AssembleKey(mapper = "phone_number_desensitization")
    private String phone;
    
    // 填充嵌套对象
    @Disassemble(type = Foo.class)
    private List<Foo> nestedBeans;
}
~~~

**执行填充**

在方法上添加 `@AutoOperate` 注解即可自动对方法返回值进行填充：

~~~java
@AutoOperate(type = Foo.class)
public List<Foo> doOperate() {
    // return something
}
~~~

填充前：

~~~json
{
    "gender": 1,
    "deptId": 10086,
    "letter": "a",
    "phone": "12456782563",
    "nestedBeans": [
        {
            "gender": 1,
            "deptId": 10086,
            "letter": "a",
            "phone": "12456782563"
        }
    ]
}
~~~

填充后：

~~~java
{
    "name": "foo1",
    "gender": 1,
    "genderName": "男",
    "deptId": 10086,
    "empNames": [
        "张三",
        "李四",
        "王五"
    ],
    "letter": "a",
    "letterName": "A",
    "fooName": "foo1",
    "phone": "124****2563",
    "nestedBeans": [
        {
            "name": "foo2",
            "gender": 1,
            "genderName": "男",
            "deptId": 10086,
            "empNames": [
                "张三",
                "李四",
                "王五"
            ],
            "letter": "a",
            "letterName": "A",
            "fooName": "foo2",
            "phone": "124****2563"
        }
    ]
}
~~~

这就是在 springboot 环境中使用 `crane4j` 的最简单步骤，更多玩法请参见官方文档。

## 友情链接 & 最佳实践

- [[ hippo4j \]](https://gitee.com/agentart/hippo4j)：强大的动态线程池框架，附带监控报警功能；
- [[[continew-admin]](https://gitee.com/continew/continew-admin)：持续迭代优化的前后端分离中后台管理系统框架，开箱即用，持续提供舒适的开发体验；

## 参与贡献和技术支持

如果在使用中遇到了问题、发现了 bug ，又或者是有什么好点子，欢迎提出你的 issues ，或者[加入社区交流群](https://opengoofy.github.io/crane4j/#/other/%E8%81%94%E7%B3%BB%E4%BD%9C%E8%80%85.html) 讨论！

若无法访问连接，或者微信群二维码失效，也可以联系作者加群：

![联系作者](https://foruda.gitee.com/images/1678072903420592910/c0dbb802_5714667.png)

## 鸣谢

感谢 JetBrains 提供的 Licenses ！

![JetBrains Logo (Main) logo](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)

