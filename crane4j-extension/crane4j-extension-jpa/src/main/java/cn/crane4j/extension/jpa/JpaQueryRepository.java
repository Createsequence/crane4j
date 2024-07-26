package cn.crane4j.extension.jpa;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.query.QueryRepository;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ReflectUtils;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.Repository;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author huangchengxing
 */
public class JpaQueryRepository implements QueryRepository<Repository<?, ?>> {

    @Getter
    private final Repository<?, ?> target;
    @Getter
    private final Class<?> entityType;
    @Getter
    private final String tableName;
    @Getter
    private final String primaryKeyProperty;
    private final Map<String, String> propertyToColumnMap;

    public JpaQueryRepository(
        @NonNull Repository<?, ?> target, @NonNull AnnotationFinder annotationFinder) {
        this.target = target;
        this.entityType = resolveEntityTypeFromRepository(target);

        // table
        Table table = annotationFinder.findAnnotation(entityType, Table.class);
        Asserts.isNotNull(table, "@Table annotation not found in entity type: {}", target);
        this.tableName = table.name();

        // columns
        Field[] fields = ReflectUtils.getDeclaredFields(entityType);
        this.propertyToColumnMap = new HashMap<>();
        for (Field field : fields) {
            Column column = annotationFinder.getAnnotation(field, Column.class);
            if (Objects.nonNull(column)) {
                propertyToColumnMap.put(field.getName(), column.name());
            }
        }

        // primary key
        this.primaryKeyProperty = Stream.of(entityType.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Id.class))
            .findFirst()
            .map(Field::getName)
            .orElseThrow(() -> new Crane4jException("No primary key found in entity type: {}", target));
    }

    protected Class<?> resolveEntityTypeFromRepository(@NonNull Repository<?, ?> target) {
        return Arrays.stream(target.getClass().getInterfaces())
            .filter(Repository.class::isAssignableFrom)
            .filter(itf -> !itf.getName().startsWith("org.springframework.data.repository.Repository."))
            .map(itf -> ResolvableType.forClass(Repository.class, itf).getGeneric(0).resolve())
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new Crane4jException("cannot determine entity type from repository: {}", target.getClass()));
    }

    /**
     * Get the column name of the repository.
     *
     * @param propertyOrColumn property or column
     * @return column name
     */
    @Override
    public String resolveToColumn(String propertyOrColumn) {
        return propertyToColumnMap.getOrDefault(propertyOrColumn, propertyOrColumn);
    }
}
