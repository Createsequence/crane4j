package cn.crane4j.core.parser.operation;

import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.handler.strategy.OverwriteNotNullMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

/**
 * Basic implementation of {@link AssembleOperation}.
 *
 * @author huangchengxing
 */
@SuperBuilder
@Getter
@Setter
public class SimpleAssembleOperation extends SimpleKeyTriggerOperation implements AssembleOperation {

    @Singular
    private final Set<PropertyMapping> propertyMappings;
    private final String container;
    private final AssembleOperationHandler assembleOperationHandler;
    @Builder.Default
    private PropertyMappingStrategy propertyMappingStrategy = OverwriteNotNullMappingStrategy.INSTANCE;
    @Nullable
    private Class<?> keyType;
    private KeyResolver keyResolver;
    private String keyDescription;
}
