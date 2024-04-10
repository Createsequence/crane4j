# 示例: 如何填充一个复杂对象

你了解了如何基于自动填充，在 `Controller` 方法返回后进行简单的填充。

在本文的例子中，你可以结合一个填充起来比较麻烦的的订单列表查询的例子，进一步的了解：

+ **选项式风格配置与组合式风格配置的使用方式与差异**；
+ **如何使用组合注解抽离并简化复杂配置**；

## 1.原始代码

我们先给出不使用 crane4j 前的原始代码，后续将会围绕其作出修改。

### 1.1.实体类与数据结构

我们有实体类 `Order` 和 `Item` 如下：

~~~java
@Data // 使用 lombok 生成 getter/setter 方法
public class Order {
    private Integer id;
    private String orderType;
    private Integer customerId;
    private String customerName;
    private String customerType;
    private List<Item> items;
}

@Data // 使用 lombok 生成 getter/setter 方法
public class Item {
    private id;
    private String name;
    private String type;
}
~~~

不过在最开始的时候我们仅有少量的必要信息：

~~~json
{
    "id": 123,
    "orderType": "new_order",
    "customerId": 123,
    "items": [
        {
            "id": 1
        },
        {
            "id": 2
        }
    ]
}
~~~

其他内容都需要通过关联查询获取。

### 1.2.实现代码

由于查询到所需的订单数据后，我们还需要关联查询出其他的信息，因此该接口实行如下：

~~~java
public List<Oder> listOrder(List<Integer> ids) {
    // 查询订单列表
    List<Order> orders = orderService.selectList(ids);

    // 1、填充订单类型
    Map<String, OrderType> orderTypeMap = Stream.of(OrderType.values())
        .collect(Collectors.toMap(OrderType::getCode, e -> e));
    orders.forEach(order -> {
        String orderTypeCode = foo.getOrderType(); // 根据订单类型编码获取对应的名称
        OrderType orderType = orderTypeMap.get(orderTypeCode);
        if (Objects.nonNull(orderType)) {
            foo.setOrderType(orderType.getName());
        }
    });

    // 2、填充关联客户信息
    Set<Integer> customerIds = orders.stream()
        .map(Order::getCustomerId)
        .collect(Collectors.toList());
    List<Customer> customers = customerService.selectListByIds(customerIds);
    Map<Integer, Customer> customerMap = customers.stream().
        .collect(Collectors.toMap(Customer::getId, e -> e));
    orders.forEach(order -> {
        Integer customerId = foo.getCustomerId(); // 根据客户Id获取对应的客户名称、客户类型
        Customer customer = orderTypeMap.get(customerId);
        if (Objects.nonNull(customer)) {
            foo.setCustomerName(customer.getName());
            foo.setCustomerType(customer.getType());
        }
    });

    // 3、填充关联商品信息
    Set<Integer> itemIds = orders.stream()
        .map(Order::getItems)
        .flatMap(Collection::stream)
        .map(Item:getId)
        .collect(Collectors.toList());
    List<Item> items = itemService.selectListByIds(itemIds); // 查询商品信息，并按 id 分组
    Map<Integer, Item> itemMap = items.stream().
        .collect(Collectors.toMap(Item::getId, e -> e));
    orders.forEach(order -> {
        List<Item> itemsOfOrder = order.getItems();
        itemsOfOrder.forEach(itemOfOrder -> { // 遍历订单中的管理商品，并找到对应的商品信息
            Item item = itemMap.get(itemOfOrder.getId());
            if (Objects.nonNull(item)) {
                itemOfOrder.setName(item.getName());
                itemOfOrder.setType(item.getType());
            }
        })
    });
}
~~~

上面代码用于为 `Order` 对象填充订单类型、关联客户与关联商品三类信息，实际场景中需要的可能数据远远不止这些，此处已经做了简化，不过依然显得十分繁琐。

## 2.使用选项式风格配置

你可以基于选项式风格的配置，通过 crane4j 更优雅的完成上述字段填充逻辑：

~~~java
@Data
public class Order {
    private Integer id;

    // 将订单类型编码转为订单类型值
    @AssembleEnum(
        type = OrderType.class, enumKey = "code", // 填充数据源为 OrderType 枚举
        props = @Mapping(src = "name") // OrderType.name -> Order.orderType
    )
    private String orderType;


    // 2、填充关联客户信息
    @AssembleMethod(
        targetType = CustomerService.class, // 填充数据源为 CustomerService#listByIds 方法
        method = @ContainerMethod(bindMethod = "listByIds", resultType = Customer.class),  
        prop = {
            "name:customerName", // Customer.name -> Order.customerName
            "type:customerType" // Customer.type -> Order.customerType
        }
    )
    private Integer customerId;
    private String customerName;
    private String customerType;
    
    @Disassemble(type = Item.class) // 嵌套填充商品信息
    private List<Item> items;
}

@Data
public class Item {
    
    // 3、填充关联商品信息
    @AssembleMethod(
        targetType = ItemService.class, // 填充数据源为 CustomerService#listByIds 方法
        method = @ContainerMethod(bindMethod = "listByIds", resultType = Item.class),
        prop = {
            "name", // Item.name -> Item.name
            "type" // Item.type -> Item.type
        },
        handlerType = ManyToManyAssembleOperationHandler.class // 多对多
    )
    private id;
    private String name;
    private String type;
}
~~~

而原来的代码仅需**保留核心业务逻辑**即可：

~~~java
@AutoOperate
public List<Oder> listOrder(List<Integer> ids) {
    return orderService.selectList(ids);
}
~~~

## 3.使用组合式风格配置

而如果要使用组合式风格的配置，则在第一步需要**先配置数据源**：

~~~java
@ContainerEnum(namespace = "order_type", enumKey = "code")
public enum OrderType {}

@ContainerMethod(namespace = "customer", bindMethod = "listByIds", resultType = Customer.class)
public interface CustomerService {
    List<Customer> listByIds(Collection<Integer> ids);
}

@ContainerMethod(namespace = "item", bindMethod = "listByIds", resultType = Item.class)
public interface ItemService {
    List<Item> listByIds(Collection<Integer> ids);
}
~~~

然后再**在类属性上统一使用 `@Assemble` 注解引用数据源**，将它们组合到一起：

~~~java
@Data
public class Order {

    // 将订单类型编码转为订单类型值
    @Assemble(
        namespace = "order_type", 
        props = @Mapping(src = "name") // OrderType.name -> Order.orderType
    )
    private String orderType;


    // 2、填充关联客户信息
    @Assemble(
        namesapce = "customer", 
        props = {
            @Mapping(src = "name", ref = "customerName"), // Customer.name -> Order.customerName
            @Mapping(src = "type", ref = "customerType") // Customer.type -> Order.customerType
        }
    )
    private Integer customerId;
    private String customerName;
    private String customerType;

    @Disassemble(type = Item.class) // 嵌套填充商品信息
    private List<Item> items;
}

@Data
public class Item {

    // 3、填充关联商品信息
    @Assemble(
        namespace = "item", 
        prop = {
            "name", // Item.name -> Item.name
            "type" // Item.type -> Item.type
        },
        handlerType = ManyToManyAssembleOperationHandler.class
    )
    private Integer id;
    private String name;
    private String type;
}
~~~

## 4.使用组合注解简化配置

在上述两种风格的基础上，我们还可以进一步通过组合组件简化代码，我们以组合式风格的注解为例：

~~~java
// 将订单类型编码转为订单类型值
@Assemble(
    namespace = "order_type", 
    props = @Mapping(src = "name", ref = "orderType") // OrderType.name -> Order.orderType
)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AssembleOrderType { }

// 填充关联客户信息
@Assemble(
    namesapce = "customer", 
    props = {
        @Mapping(src = "name", ref = "customerName"), // Customer.name -> Order.customerName
        @Mapping(src = "type", ref = "customerType") // Customer.type -> Order.customerType
    }
)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AssembleCustomer { }

// 填充关联商品信息
@Assemble(
    namespace = "item", 
    props = {
        @Mapping("name"), // Item.name -> Item.name
        @Mapping("type") // Item.type -> Item.type
    },
    handlerType = ManyToManyAssembleOperationHandler.class
)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AssembleItem { }
~~~

在将原本的注解封装为新的组合注解后，我们可以直接使用**组合注解**：

~~~java
@Data
public class Order {

    // 将订单类型编码转为订单类型值
    @AssembleOrderType
    private Integer orderTypeCode;
    private String orderTypeName;


    // 2、填充关联客户信息
    @AssembleCustomer
    private Integer customerId;
    private String customerName;
    private String customerType;

    // 嵌套填充商品信息
    @Disassemble(type = Item.class)
    private List<Item> items;
}

@Data
public class Item {

    // 3、填充关联商品信息
    @AssembleItem
    private id;
    private String name;
    private String type;
}
~~~
