package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.core.util.Asserts;
import cn.crane4j.extension.query.QueryRepository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.Getter;
import org.springframework.core.ResolvableType;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A repository for mybatis-plus {@link BaseMapper}.
 *
 * @author huangchengxing
 * @since 2.9.0
 */
public class BaseMapperQueryRepository implements QueryRepository<BaseMapper<?>> {

    @Getter
    private final BaseMapper<?> target;
    private final TableInfo tableInfo;
    private final Map<String, TableFieldInfo> propertyToFieldInfoMap;
    @Getter
    private final Map<String, TableFieldInfo> columnToFieldInfoMap;

    public BaseMapperQueryRepository(String id, BaseMapper<?> target) {
        this.target = target;
        Class<?> entityType = ResolvableType.forClass(BaseMapper.class, target.getClass())
            .getGeneric(0).getRawClass();
        Asserts.isNotNull(entityType, "cannot determine entity type from BaseMapper: {}({})", id, target.getClass());
        this.tableInfo = TableInfoHelper.getTableInfo(entityType);
        this.propertyToFieldInfoMap = this.tableInfo.getFieldList().stream()
            .collect(Collectors.toMap(TableFieldInfo::getProperty, Function.identity()));
        this.columnToFieldInfoMap = this.tableInfo.getFieldList().stream()
            .collect(Collectors.toMap(TableFieldInfo::getColumn, Function.identity()));
    }

    /**
     * Get the primary key of the repository.
     *
     * @return primary key
     */
    @Override
    public String getPrimaryKeyProperty() {
        return tableInfo.getKeyProperty();
    }

    /**
     * Get the column name of the repository.
     *
     * @param propertyOrColumn property or column
     * @return column name
     */
    @Override
    public String resolveToColumn(String propertyOrColumn) {
        TableFieldInfo fieldInfo = propertyToFieldInfoMap.get(propertyOrColumn);
        return Objects.isNull(fieldInfo) ? propertyOrColumn : fieldInfo.getColumn();
    }

    /**
     * Get the entity type of the repository.
     *
     * @return entity type
     */
    @Override
    public Class<?> getEntityType() {
        return tableInfo.getEntityType();
    }
}
