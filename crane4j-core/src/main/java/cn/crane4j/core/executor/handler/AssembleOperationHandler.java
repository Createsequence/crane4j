package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.NamedComponent;

import java.util.Collection;

/**
 * <p>Handler of assembly operation.<br />
 * Enter the assembly operation and the corresponding type of object to be processed.
 * then it will complete the following operations according to the configuration：
 * <ol>
 *     <li>extract key value from target objects；</li>
 *     <li>convert the key value to the corresponding data source object through the data source container；</li>
 *     <li>complete the mapping of data source object attributes and pending object attributes；</li>
 * </ol>
 * For performance reasons, the implementation class needs to minimize
 * the reading and writing of beans and the requests for data source containers.
 *
 * @author huangchengxing
 * @see OneToOneAssembleOperationHandler
 * @see OneToManyAssembleOperationHandler
 * @see ManyToManyAssembleOperationHandler
 */
public interface AssembleOperationHandler extends NamedComponent {

    /**
     * Determine key resolver for the operation.
     *
     * @param operation operation
     * @return key resolver
     * @since 2.7.0
     */
    KeyResolver determineKeyResolver(AssembleOperation operation);

    /**
     * Perform assembly operation.
     *
     * @param container container
     * @param executions operations to be performed
     */
    void process(Container<?> container, Collection<AssembleExecution> executions);
}
