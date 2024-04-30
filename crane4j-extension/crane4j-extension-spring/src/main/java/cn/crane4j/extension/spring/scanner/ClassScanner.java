package cn.crane4j.extension.spring.scanner;

import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>A class scanner based on {@link ResourcePatternResolver} and {@link MetadataReaderFactory}
 * that scans all classes under the specified package.
 *
 * @author huangchengxing
 * @see ResourcePatternResolver
 * @see MetadataReaderFactory
 */
@Slf4j
@RequiredArgsConstructor
public class ClassScanner {

    public static final ClassScanner INSTANCE = new ClassScanner();
    public static final String CLASSPATH = "classpath";
    public static final String CLASS_SUFFIX = ".class";
    public static final String ALL = "*";
    public static final String ALL_RECURSIVE = "**";

    @SuppressWarnings("all")
    private final MetadataReaderFactory metadataReaderFactory;
    private final ResourcePatternResolver resourcePatternResolver;

    /**
     * Create a {@link ClassScanner} instance.
     */
    public ClassScanner() {
        this(new CachingMetadataReaderFactory(), new PathMatchingResourcePatternResolver());
    }

    /**
     * <p>Scan all classes under the specified package.
     *
     * @param basePackages the specified package
     * @return a set of classes
     */
    public Set<Class<?>> scan(String... basePackages) {
        return scan(Objects::nonNull, basePackages);
    }

    /**
     * <p>Scan all classes under the specified package.
     *
     * @param basePackages the specified package
     * @param classMetadataPredicate the predicate to filter classes
     * @return a set of classes
     */
    public Set<Class<?>> scan(Predicate<ClassMetadata> classMetadataPredicate, String... basePackages) {
        return Stream.of(basePackages)
            .filter(StringUtils::isNotEmpty)
            .map(this::resolvePath)
            .map(path -> doScan(path, classMetadataPredicate))
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String resolvePath(String path) {
        // fix https://github.com/opengoofy/crane4j/issues/267
        if (!path.startsWith(CLASSPATH)) {
            path = CLASSPATH + "*:" + path;
        }
        // it already specifies the path of the class
        if (path.endsWith(CLASS_SUFFIX)) {
            String pathNotWithClassSuffix = path.substring(0, path.length() - CLASS_SUFFIX.length());
            return ClassUtils.packageToPath(pathNotWithClassSuffix) + CLASS_SUFFIX;
        }
        path = ClassUtils.packageToPath(path);
        if (path.endsWith(ALL_RECURSIVE)) {
            return path + "/*.class";
        }
        if (path.endsWith(ALL)) {
            return path + CLASS_SUFFIX;
        }
        return path + "/**/*.class";
    }

    private Set<Class<?>> doScan(String packageSearchPath, Predicate<ClassMetadata> classMetadataPredicate) {
        try {
            return Stream.of(resourcePatternResolver.getResources(packageSearchPath))
                .filter(Resource::isReadable)
                .map(this::getMetadataReader)
                .filter(Objects::nonNull)
                .map(MetadataReader::getClassMetadata)
                .filter(classMetadataPredicate)
                .map(ClassMetadata::getClassName)
                .map(ClassUtils::forName)
                .collect(Collectors.toSet());
        } catch (IOException ex) {
            log.error("scan path [{}] failed", packageSearchPath, ex);
        }
        return Collections.emptySet();
    }

    private MetadataReader getMetadataReader(Resource resource) {
        try {
            return metadataReaderFactory.getMetadataReader(resource);
        } catch (IOException e) {
            log.error("get metadata reader from resource [{}] failed", resource, e);
        }
        return null;
    }
}
