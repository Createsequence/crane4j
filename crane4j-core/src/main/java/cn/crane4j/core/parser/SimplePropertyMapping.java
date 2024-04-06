package cn.crane4j.core.parser;

import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Simple implementation of {@link PropertyMapping}.
 *
 * @author huangchengxing
 */
@EqualsAndHashCode
public class SimplePropertyMapping implements PropertyMapping {

    public static final String PROPERTY_NAME_SEPARATOR = ",";
    public static final String PROPERTY_NAME_MAPPER = ":";

    @EqualsAndHashCode.Include
    @Getter
    private final String source;
    private final boolean hasSource;
    @EqualsAndHashCode.Include
    @Getter
    private final String reference;

    /**
     * Create a property mapping configuration.
     *
     * @param source fields in the data source object
     * @param reference field in the target object to reference the field in the data source object
     */
    public SimplePropertyMapping(String source, String reference) {
        this.source = source;
        this.hasSource = StringUtils.isNotEmpty(source);
        this.reference = reference;
    }

    /**
     * <p>Resolve the property mappings from string.<br/>
     * the string format is "source1:reference1,source2:reference2,source3:reference3",
     * also can be "reference1,reference2,reference3" if the source property is same as the reference property.
     *
     * @param propertyMappings property mappings string
     * @return property mappings
     * @see #of
     */
    public static Set<PropertyMapping> from(String propertyMappings) {
        if (StringUtils.isEmpty(propertyMappings)) {
            return Collections.emptySet();
        }
        String[] mappings = propertyMappings.split(PROPERTY_NAME_SEPARATOR);
        Set<PropertyMapping> results= new LinkedHashSet<>(mappings.length);
        for (String mapping : mappings) {
            PropertyMapping propertyMapping = of(mapping);
            results.add(propertyMapping);
        }
        return results;
    }

    /**
     * <p>Resolve the property mapping from string.<br/>
     * the string format is "source:reference".
     *
     * @param mapping property mapping
     * @return property mapping
     */
    public static PropertyMapping of(String mapping) {
        mapping = mapping.trim();
        Asserts.isNotEmpty(mapping, "The property mappings is illegal: {}", mapping);
        String[] pair = mapping.split(PROPERTY_NAME_MAPPER);
        Asserts.isFalse(pair.length > 2, "The property mappings is illegal: {}", mapping);
        return pair.length < 2 ?
            new SimplePropertyMapping(pair[0].trim(), pair[0].trim()) :
            new SimplePropertyMapping(pair[0].trim(), pair[1].trim());
    }

    /**
     * Whether {@link #getSource()} is empty.
     *
     * @return boolean
     */
    @Override
    public boolean hasSource() {
        return hasSource;
    }

    /**
     * Get string as "s.source -> t.reference"
     *
     * @return string
     */
    @Override
    public String toString() {
        return "s" + (hasSource() ? "." + getSource() : "") + " -> t." + reference;
    }
}
