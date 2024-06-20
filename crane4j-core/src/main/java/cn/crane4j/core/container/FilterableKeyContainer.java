package cn.crane4j.core.container;

import cn.crane4j.core.util.CollectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An implementation of {@link ContainerDelegate} that can filter key
 * before querying the delegate container.
 *
 * @author huangchengxing
 */
@Setter
@RequiredArgsConstructor
public class FilterableKeyContainer<K> implements ContainerDelegate<K> {

    @Getter
    private final Container<K> container;
    private boolean filterNullKey;
    private boolean skipQueryIfKeyCollIsEmpty;

    /**
     * Enter a batch of key values to return data source objects grouped by key values.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    @Override
    public Map<K, ?> get(Collection<K> keys) {
        // filter null if needed
        if (filterNullKey && Objects.nonNull(keys)) {
            keys = keys.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
        return skipQueryIfKeyCollIsEmpty && CollectionUtils.isEmpty(keys) ?
            Collections.emptyMap() : container.get(keys);
    }
}
