package cn.crane4j.extension.mybatis.plus;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author huangchengxing
 */
public abstract class MpBaseTest {

    protected FooMapper fooMapper;
    private SqlSession sqlSession;

    @SneakyThrows
    @Before
    public void init() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.addMapper(FooMapper.class);
        configuration.setLogImpl(Slf4jImpl.class);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setSqlInjector(new DefaultSqlInjector());
        globalConfig.setSuperMapperClass(BaseMapper.class);
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);

        Map<String, String> properties = new HashMap<>();
        properties.put("url", "jdbc:mysql://gz-cynosdbmysql-grp-nl9mays3.sql.tencentcdb.com:22327/crane4j-example?characterEncoding=utf8&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai&&allowMultiQueries=true");
        properties.put("username", "crane4j");
        properties.put("password", "crane4j-test");
        properties.put("driverClassName", "com.mysql.cj.jdbc.Driver");
        DataSource dataSource = DruidDataSourceFactory.createDataSource(properties);
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
