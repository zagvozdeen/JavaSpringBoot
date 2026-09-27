package com.example.lab1.hello;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HelloControllerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new HelloController()).build();
    }

    @Test
    void greetsDefaultAndProvidedName() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"));
        mockMvc.perform(get("/hello").param("name", "Petya"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Petya!"));
    }

    @Test
    void startsWithEmptyCollectionsAndRejectsMissingValues() throws Exception {
        mockMvc.perform(get("/update-array")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/update-map")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/show-array"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        mockMvc.perform(get("/show-map"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
        mockMvc.perform(get("/show-all-length"))
                .andExpect(status().isOk())
                .andExpect(content().string("ArrayList: 0, HashMap: 0"));
    }

    @Test
    void storesFirstListValueAndPreservesDuplicates() throws Exception {
        mockMvc.perform(get("/update-array").param("s", "Java"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/show-array"))
                .andExpect(content().json("[\"Java\"]"));
        mockMvc.perform(get("/update-array").param("s", "Java"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/show-array"))
                .andExpect(content().json("[\"Java\",\"Java\"]"));
        mockMvc.perform(get("/show-all-length"))
                .andExpect(content().string("ArrayList: 2, HashMap: 0"));
    }

    @Test
    void assignsMapKeysFromOneAndCountsBothCollections() throws Exception {
        mockMvc.perform(get("/update-map").param("s", "Spring"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/show-map"))
                .andExpect(content().json("{\"1\":\"Spring\"}"));
        mockMvc.perform(get("/update-map").param("s", "Boot"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/update-array").param("s", "Java"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/show-map"))
                .andExpect(content().json("{\"1\":\"Spring\",\"2\":\"Boot\"}"));
        mockMvc.perform(get("/show-all-length"))
                .andExpect(content().string("ArrayList: 1, HashMap: 2"));
    }
}
