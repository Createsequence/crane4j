package cn.crane4j.extension.mybatis.plus;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.After;
import org.junit.Before;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author huangchengxing
 */
public abstract class MpBaseTest {

    private static final String schema = "DROP TABLE IF EXISTS foo;"
        + "CREATE TABLE `foo` ("
        + "`id` int(11) NOT NULL AUTO_INCREMENT,"
        + "`name` varchar(255) DEFAULT '',"
        + "`age` int(3) DEFAULT NULL,"
        + "`sex` int(1) DEFAULT NULL,"
        + "PRIMARY KEY (`id`)"
        + ");";
    private static final String data = "DELETE FROM foo;"
        + "INSERT INTO `foo`(`id`, `name`, `age`, `sex`) VALUES"
        + "(1, '小明', 18, 1),"
        + "(2, '小红', 18, 0),"
        + "(3, '小刚', 17, 1),"
        + "(4, '小李', 19, 0);";

    protected FooMapper fooMapper;
    private SqlSession sqlSession;

    @SneakyThrows
    @Before
    public void init() {
        if (Objects.nonNull(fooMapper)) {
            return;
        }

        // init data source and environment
        Map<String, String> properties = new HashMap<>();
        properties.put("url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL;INIT=SET NAMES 'UTF-8'");
        properties.put("username", "crane4j");
        properties.put("password", "crane4j-test");
        properties.put("driverClassName", "org.h2.Driver");
        DataSource dataSource = DruidDataSourceFactory.createDataSource(properties);

        // init schema and data
        @Cleanup
        Connection connection = dataSource.getConnection();
        @Cleanup
        Statement statement = connection.createStatement();
        statement.execute(schema);
        statement.execute(data);

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.addMapper(FooMapper.class);
        configuration.setLogImpl(Slf4jImpl.class);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setSqlInjector(new DefaultSqlInjector());
        globalConfig.setSuperMapperClass(BaseMapper.class);
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);

        // create mapper proxy
        configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), dataSource));
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        sqlSession = sqlSessionFactory.openSession();
        fooMapper = sqlSession.getMapper(FooMapper.class);

        afterInit();
    }

    public void afterInit() {
        // do nothing
    }
    @After
    public void close() {
        sqlSession.close();
    }
}
