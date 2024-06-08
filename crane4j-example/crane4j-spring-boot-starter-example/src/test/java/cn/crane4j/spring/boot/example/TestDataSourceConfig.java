package cn.crane4j.spring.boot.example;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huangchengxing
 */
@Configuration
public class TestDataSourceConfig {

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

    protected DataSource dataSource;

    @Bean
    @SneakyThrows
    public DataSource dataSource() {
        // init data source and environment
        Map<String, String> properties = new HashMap<>();
        properties.put("url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL;INIT=SET NAMES 'UTF-8'");
        properties.put("username", "crane4j");
        properties.put("password", "crane4j-test");
        properties.put("driverClassName", "org.h2.Driver");
        dataSource = DruidDataSourceFactory.createDataSource(properties);

        // init schema and data
        @Cleanup Connection connection = dataSource.getConnection();
        @Cleanup Statement statement = connection.createStatement();
        statement.execute(schema);
        statement.execute(data);

        afterDataSourceInitialized(dataSource);
        return dataSource;
    }

    protected void afterDataSourceInitialized(DataSource dataSource) {
        // override this method to do something after data source initialized
    }
}
