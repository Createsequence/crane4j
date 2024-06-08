package cn.crane4j.extension.query;

import cn.crane4j.core.exception.Crane4jException;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

/**
 * test for {@link DataSourceProvider}
 *
 * @author huangchengxing
 */
public class DataSourceProviderTest extends DataSourceTestSupport {

    @Test
    public void testMultiDataSourceProvider() {
        Map<String, DataSource> dataSources = Collections.singletonMap("test", dataSource);
        DataSourceProvider provider = DataSourceProvider.of(dataSources);
        Assert.assertTrue(provider instanceof DataSourceProvider.MultiDataSourceProvider);
        Assert.assertNotNull(provider.getConnection("test"));
        Assert.assertThrows(Crane4jException.class, () -> provider.getConnection("none"));
    }

    @Test
    public void testSingleDataSourceProvider() {
        DataSourceProvider provider = DataSourceProvider.of(dataSource);
        Assert.assertTrue(provider instanceof DataSourceProvider.SingleDataSourceProvider);
        Assert.assertNotNull(provider.getConnection("test"));
        Assert.assertNotNull(provider.getConnection("none"));
    }
}
