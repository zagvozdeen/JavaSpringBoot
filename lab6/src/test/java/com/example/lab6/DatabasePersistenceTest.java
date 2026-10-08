package com.example.lab6;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabasePersistenceTest {
    @TempDir
    Path directory;

    @Test
    void retainsBothTablesAfterRestart() {
        String url = "jdbc:h2:file:" + directory.resolve("students");
        try (ConfigurableApplicationContext context = start(url)) {
            JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
            jdbc.update("INSERT INTO students(name, surname, faculty, age) VALUES ('Иван', 'Иванов', 'ИТ', 25)");
            jdbc.update("INSERT INTO disciplines(name, hours, semester) VALUES ('Java', 72, 2)");
        }
        try (ConfigurableApplicationContext context = start(url)) {
            JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
            assertEquals("Иван", jdbc.queryForObject("SELECT name FROM students", String.class));
            assertEquals(72, jdbc.queryForObject("SELECT hours FROM disciplines", Integer.class));
        }
    }

    private ConfigurableApplicationContext start(String url) {
        return new SpringApplicationBuilder(Lab6Application.class).web(WebApplicationType.NONE)
                .run("--spring.datasource.url=" + url, "--spring.main.banner-mode=off");
    }
}
