package cn.crane4j.core.executor.handler.key;

import cn.crane4j.core.parser.operation.AssembleOperation;

/**
 * Introspection key resolver.
 *
 * @author huangchengxing
 */
public class IntrospectionKeyResolver implements KeyResolver {

    public static final IntrospectionKeyResolver INSTANCE = new IntrospectionKeyResolver();

    /**
     * Resolve the key of the operation.
     *
     * @param target    target
     * @param operation operation
     * @return key
     */
    @Override
    public Object resolve(Object target, AssembleOperation operation) {
        return target;
    }
}
