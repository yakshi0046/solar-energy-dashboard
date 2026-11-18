package com.solardash;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSmokeTest {
    @Autowired
    MockMvc mvc;

    @Test
    void listsSixSeededSystems() throws Exception {
        mvc.perform(get("/api/systems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void raisesThreeOpenAlerts() throws Exception {
        mvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void returnsReadingsForASystem() throws Exception {
        mvc.perform(get("/api/systems/1/readings?hours=24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(23)));
    }

    @Test
    void unknownSystemIs404() throws Exception {
        mvc.perform(get("/api/systems/9999/readings")).andExpect(status().isNotFound());
    }
}
