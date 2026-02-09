package com.efcon.rating.config;

import liquibase.Liquibase;
import liquibase.ext.mongodb.database.MongoClientDriver;
import liquibase.ext.mongodb.database.MongoConnection;
import liquibase.integration.spring.SpringResourceAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class LiquibaseMongoInitializer implements CommandLineRunner {
    @Value("${spring.data.mongodb.uri}")
    private String mongodbUrl;

    @Value("${spring.liquibase.change-log}")
    private String changeLog;

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public void run(String... args) throws Exception {
        MongoConnection mongoConnection = new MongoConnection();
        mongoConnection.open(mongodbUrl, new MongoClientDriver(), new Properties());

        Liquibase liquibase = new Liquibase(changeLog, new SpringResourceAccessor(resourceLoader), mongoConnection);

        liquibase.update("");
    }
}