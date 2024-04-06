package cn.crane4j.core.executor.handler.key;

import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.parser.operation.AssembleOperation;

/**
 * <p>Key resolver, which is used to resolve the key of the operation.
 *
 * @author huangchengxing
 * @see AssembleOperationHandler#determineKeyResolver
 * @since 2.7.0
 */
public interface KeyResolver {

    /**
     * Resolve the key of the operation.
     *
     * @param target    target
     * @param operation operation
     * @return key
     */
    Object resolve(Object target, AssembleOperation operation);

    /**
     * Callback after key resolver instantiation.
     *
     * @author huangchengxing
     * @since 2.7.0
     */
    interface OperationAware {

        /**
         * Set assemble operation.
         *
         * @param operation operation
         */
        void setAssembleOperation(AssembleOperation operation);
    }
}
