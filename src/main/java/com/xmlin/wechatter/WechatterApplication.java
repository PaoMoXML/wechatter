package com.xmlin.wechatter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties
@EnableAspectJAutoProxy
public class WechatterApplication implements CommandLineRunner
{

    public static void main(String[] args) {
        SpringApplication.run(WechatterApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
    }
}
