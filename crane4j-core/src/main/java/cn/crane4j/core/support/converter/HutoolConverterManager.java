package cn.crane4j.core.support.converter;

import cn.hutool.core.convert.Convert;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.BiFunction;

/**
 * A {@link ConverterManager} implementation based on Hutool {@link Convert}.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
public class HutoolConverterManager implements ConverterManager {

    public static final HutoolConverterManager INSTANCE = new HutoolConverterManager();

    /**
     * Get converter from target type to result type.
     *
     * @param targetType target type
     * @param resultType result type
     * @param <T>        target type
     * @param <R>        result type
     * @return converter
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T, R> BiFunction<T, R, R> getConverter(Class<T> targetType, Class<R> resultType) {
        return (source, defaultValue) -> {
            if (resultType.isInstance(source)) {
                return (R)source;
            }
            return Convert.convert(resultType, source, defaultValue);
        };
    }
}
