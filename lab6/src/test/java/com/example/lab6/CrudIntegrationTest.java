package com.example.lab6;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:lab6test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class CrudIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void clearDatabase() {
        jdbc.update("DELETE FROM students");
        jdbc.update("DELETE FROM disciplines");
    }

    @ParameterizedTest(name = "CRUD: {0}")
    @ValueSource(strings = {"students", "disciplines"})
    void completesCrudAndPersistsChanges(String resource) throws Exception {
        String url = "/api/" + resource;
        ObjectNode input = valid(resource);
        String created = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(input.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(input.get("name").asText()))
                .andReturn().getResponse().getContentAsString();
        int id = mapper.readTree(created).path("data").path("id").asInt();
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM " + resource + " WHERE id = ?", Integer.class, id));
        mvc.perform(get(url + "/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id));
        mvc.perform(get(url)).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));
        input.put("id", id).put("name", "Новое имя");
        mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(input.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Новое имя"));
        assertEquals("Новое имя", jdbc.queryForObject("SELECT name FROM " + resource + " WHERE id = ?", String.class, id));
        mvc.perform(delete(url + "/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.message").value("Запись удалена"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM " + resource, Integer.class));
        mvc.perform(get(url + "/" + id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"students", "disciplines"})
    void returnsSuccessfulEmptyList(String resource) throws Exception {
        mvc.perform(get("/api/" + resource)).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"students", "disciplines"})
    void rejectsMissingRecordsWithoutCreatingThem(String resource) throws Exception {
        String url = "/api/" + resource;
        mvc.perform(get(url + "/999")).andExpect(status().isNotFound());
        mvc.perform(delete(url + "/999")).andExpect(status().isNotFound());
        mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(valid(resource).put("id", 999).toString()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.success").value(false));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM " + resource, Integer.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"students", "disciplines"})
    void checksIdForCreateAndUpdate(String resource) throws Exception {
        String url = "/api/" + resource;
        mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(valid(resource).put("id", 1).toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(valid(resource).toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        mvc.perform(get(url + "/0")).andExpect(status().isBadRequest());
        mvc.perform(get(url + "/abc")).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({"students,age,0", "students,age,121", "disciplines,hours,0",
            "disciplines,hours,-1", "disciplines,semester,0", "disciplines,semester,13"})
    void validatesNumericFields(String resource, String field, int value) throws Exception {
        mvc.perform(post("/api/" + resource).contentType(MediaType.APPLICATION_JSON)
                        .content(valid(resource).put(field, value).toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM " + resource, Integer.class));
    }

    @ParameterizedTest
    @CsvSource({"students,name", "students,surname", "students,faculty", "students,age",
            "disciplines,name", "disciplines,hours", "disciplines,semester"})
    void rejectsMissingFields(String resource, String field) throws Exception {
        ObjectNode input = valid(resource);
        input.remove(field);
        mvc.perform(post("/api/" + resource).contentType(MediaType.APPLICATION_JSON).content(input.toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"students", "disciplines"})
    void rejectsBlankLongAndMalformedInput(String resource) throws Exception {
        String url = "/api/" + resource;
        for (String name : new String[]{" ", "x".repeat(101)}) {
            mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(valid(resource).put("name", name).toString()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        }
        mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
    }

    private ObjectNode valid(String resource) {
        ObjectNode input = mapper.createObjectNode();
        if (resource.equals("students")) {
            return input.put("name", "Иван").put("surname", "Иванов").put("faculty", "ИТ").put("age", 25);
        }
        return input.put("name", "Java Spring Boot").put("hours", 72).put("semester", 2);
    }
}
