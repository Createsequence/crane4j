# 示例：如何基于枚举进行填充

本示例将指导你如何使用枚举作为数据源，去配置一个填充操作，在这之前，请先确保你已经阅读过[快速开始](./../user_guide/getting_started/getting_started_abstract.md)，并且成功将 crane4j 引入你的项目。

## 1.准备

假设，现在你有一个 `Gender` 枚举：

~~~java
@Getter
@RequiredArgsConstructor
private enum Gender {
    FEMALE(0, "女", "female"), 
  	MALE(1, "男", "male");
  
    private final Integer code;
    private final String value;
}
~~~

然后，你有一个 `Student` 类，我们忽略其他无关的属性。其 genderCode 字段与 `Gender` 枚举的 `code` 字段对应：

~~~java
@Acccessor(chain = true)
@Data
public class Student {
  
  	// 其他属性......
  
  	private Integer genderCode;
  	private String genderName;
}
~~~

现在，你需要配置一个操作，根据 `genderCode` 找到对应的枚举，然后将枚举的 `value` 字段填充到 `Student` 类的 `genderName` 上面。

## 2.使用

你需要在目标类的 `genderCode` 字段上通过 `@AssembleEnum` 注解声明一个装配操作，并在里面指定将哪个枚举类作为数据源，并且指定在填充时字段需要以什么样的方式进行映射：

~~~java
@Acccessor(chain = true)
@Data
public class Student {
  	@AssembleEnum(
        type = Gender.class, // 指定数据源为 Gender 枚举类
        props = @Mapping(ref = "genderName"), // 将 value 映射到 genderName 字段上
        followTypeConfig = false, 
        enums = @ContainerEnum(
            key = "code", // key 为 Gender.code 字段值
            value = "value" // value 为 Gender.value 字段值
        )
    )
  	private Integer genderCode;
  	private String genderName;
}
~~~

此后通过不同的方式触发填充即可，这里我们选择直接基于 AOP 切面触发：

~~~java
@Component
public class ServiceImpl { // 你的 service 接口，确保 Spring 可以代理它
  	
  	@AutoOperate(type = Student.class) // 声明自动填充该方法的返回值
    public List<Student> getStudents(Collection<Integer> ids) {
      	return ids.stream() // 模拟返回数据
          .map(id -> {
            	int genderCode = id % 2; // 随机设置一个性别编码
            	return new Student().setGenderCode(genderCode);
          })
          .collect(Collectors.toList());
    }
}

// 测试
ServiceImpl service = SrpingUtil.getBean(ServiceImpl.class);
service.getStudents(Arrays.asList(1, 2)); 
// 执行结果为：[{genderCode=1, genderName="男"}, {genderCode=0, genderName="女"}]
~~~

:::tip

- 关于如何指定字段映射，具体内容可以参见：[字段映射](./../basic/property_mapping.md) 一节；
- 你可以基于更多的配置项和不同的声明方式来配置基于枚举的填充操作，具体内容可以参见：[枚举填充](./../basic/container/enum_container.md) 一节；
- 除了基于 AOP 自动触发，crane4j 还支持更多的触发方式，具体内容可以参见：[触发填充操作](./../basic/trigger_operation.md) 一节；

:::

## 3.组合式配置

上述写法被称为选项式，即所有的配置集成在一个注解中，作为不同的选项。

如果你希望数据源和操作的配置能够分开，那么你可以先将枚举的数据源配置分离到枚举上：

~~~java
@ContainerEnum(
    namespace = "gender" // 指定枚举容器的 namespace
    key = "code", // key 为 Gender.code 字段值
    value = "value" // value 为 Gender.value 字段值
)
@Getter
@RequiredArgsConstructor
private enum Gender {
    FEMALE(0, "女", "female"), 
  	MALE(1, "男", "male");
  
    private final Integer code;
    private final String value;
}
~~~

然后在 Student 中不必再进行数据源配置，直接引用即可：

~~~java
@Acccessor(chain = true)
@Data
public class Student {
  	@AssembleEnum(
        type = Gender.class, // 指定数据源为 Gender 枚举类
        props = @Mapping(ref = "genderName") // 将 value 映射到 genderName 字段上
    )
  	private Integer genderCode;
  	private String genderName;
}
~~~

其他部分与之前完全一样。

:::tip

更多内容请参见 [枚举数据源容器](./../basic/container/enum_container) 一节。

:::
