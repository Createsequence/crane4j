package cn.crane4j.core.parser.operation;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

/**
 * <p>表示一个由指定的键触发的装配操作。<br />
 * 它通常通常用来描述通过下述步骤完成的依次填充操作：
 * <ol>
 *     <li>提取目标对象的键字段值；</li>
 *     <li>通过指定的数据源容器将键值转换为对应的数据源对象；</li>
 *     <li>将数据源对象的特定属性值映射到目标对象的属性中；</li>
 * </ol>
 *
 * <p>完成上述操作所需的组件或配置可以通过方法获取：
 * <ol>
 *     <li>{@link #getKey()}：目标对象的哪个字段是键字段；</li>
 *     <li>{@link #getContainer()}：通过键字段值获取对应的数据源对象的容器；</li>
 *     <li>{@link #getPropertyMappings()}：获取数据源对象后，哪些属性应该填充到目标对象的哪些属性中；</li>
 *     <li>{@link #getAssembleOperationHandler()}：如何将这些属性值插入；</li>
 *     <li>{@link #getKeyResolver()}：如何解析键；</li>
 *     <li>{@link #getKeyDescription()}：键的描述；</li>
 *     <li>{@link #getPropertyMappingStrategy()}：属性映射策略；</li>
 *     <li>{@link #getKeyType()}：键的类型；</li>
 * </ol>
 *
 * <hr/>
 *
 * <p>The assembly operation triggered by the specified key.<br />
 * An example is usually used to describe one of the following processes:
 * <ol>
 *     <li>extract a key field value of the target object;</li>
 *     <li>convert the key value to the corresponding data source object through the specified data source container;</li>
 *     <li>map the specific attribute values of the data source object to the attributes of the target object;</li>
 * </ol>
 *
 * <p>The necessary components for completing the above operations can be obtained through the instance:
 * <ol>
 *     <li>{@link #getKey()}: which field of the target object is the key field;</li>
 *     <li>{@link #getContainer()}: which container to get the corresponding data source object through the key field value;</li>
 *     <li>
 *         {@link #getPropertyMappings()}: after getting the data source object, which attributes
 *         should be stuffed into which attributes of the target object;
 *     </li>
 *     <li>{@link #getAssembleOperationHandler()}: how to plug these attribute values;</li>
 *     <li>{@link #getKeyResolver()}: how to resolve the key;</li>
 *     <li>{@link #getKeyDescription()}: description of the key;</li>
 *     <li>{@link #getPropertyMappingStrategy()}: property mapping strategy;</li>
 *     <li>{@link #getKeyType()}: key type;</li>
 * </ol>
 *
 * @author huangchengxing
 * @see AssembleOperationHandler
 * @see Container
 * @see PropertyMapping
 * @see SimpleAssembleOperation
 */
public interface AssembleOperation extends KeyTriggerOperation {

    /**
     * Get property mapping.
     *
     * @return mapping
     */
    Set<PropertyMapping> getPropertyMappings();

    /**
     * Get the type of key property.
     *
     * @return key type
     */
    @Nullable
    Class<?> getKeyType();

    /**
     * Set key property type.
     *
     * @param keyType key type
     */
    void setKeyType(Class<?> keyType);

    /**
     * Get key resolver.
     *
     * @return key resolver
     * @since 2.7.0
     */
    KeyResolver getKeyResolver();

    /**
     * Set key resolver.
     *
     * @param keyResolver key resolver
     * @since 2.7.0
     */
    void setKeyResolver(@NonNull KeyResolver keyResolver);

    /**
     * Some description of the key which
     * helps {@link #getKeyResolver() resolver} to resolve the key.
     *
     * @return description
     * @since 2.7.0
     */
    @Nullable
    String getKeyDescription();

    /**
     * Set key description.
     *
     * @param keyDescription description
     * @since 2.7.0
     */
    void setKeyDescription(@NonNull String keyDescription);

    /**
     * Get the namespace of data source container.
     *
     * @return container
     */
    String getContainer();

    /**
     * Get operation handler.
     *
     * @return handler
     */
    AssembleOperationHandler getAssembleOperationHandler();

    /**
     * Get property mapping strategy.
     *
     * @return strategy
     * @since 2.1.0
     */
    PropertyMappingStrategy getPropertyMappingStrategy();

    /**
     * Set property mapping strategy.
     * @param strategy strategy name
     * @since 2.1.0
     */
    void setPropertyMappingStrategy(@NonNull PropertyMappingStrategy strategy);
}
