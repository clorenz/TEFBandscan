package de.christophlorenz.tefbandscan.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class SQLiteDataSourceConfig {

    @Autowired
    Environment env;

    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:sqlite:default.sqlite");
        //dataSource.setDriverClassName(env.getProperty("driverClassName"));
        //dataSource.setUrl(env.getProperty("url"));
        //dataSource.setUsername(env.getProperty("user"));
        //dataSource.setPassword(env.getProperty("password"));
        return dataSource;
    }
}
