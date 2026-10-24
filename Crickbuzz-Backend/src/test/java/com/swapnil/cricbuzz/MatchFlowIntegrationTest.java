package com.swapnil.cricbuzz;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatchFlowIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private long create(String url, String body) throws Exception {
        String res = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(res).get("id").asLong();
    }

    @Test
    void fullMatchLifecycle() throws Exception {
        long t1 = create("/api/v1/teams", "{\"name\":\"India\",\"shortName\":\"ind\",\"country\":\"India\"}");
        long t2 = create("/api/v1/teams", "{\"name\":\"Australia\",\"shortName\":\"aus\",\"country\":\"Australia\"}");
        create("/api/v1/players", "{\"name\":\"Virat Kohli\",\"role\":\"BATSMAN\",\"jerseyNumber\":18,\"teamId\":" + t1 + "}");

        long m = create("/api/v1/matches", "{\"team1Id\":" + t1 + ",\"team2Id\":" + t2
                + ",\"venue\":\"Wankhede\",\"matchDate\":\"2026-11-01T14:00:00\",\"matchType\":\"T20\"}");

        String score = "{\"team1\":{\"runs\":180,\"wickets\":5,\"overs\":20},\"team2\":{\"runs\":170,\"wickets\":8,\"overs\":20}}";

        mvc.perform(patch("/api/v1/matches/" + m + "/score").contentType(MediaType.APPLICATION_JSON).content(score))
                .andExpect(status().isConflict());

        mvc.perform(patch("/api/v1/matches/" + m + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"LIVE\"}")).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/matches/" + m + "/score").contentType(MediaType.APPLICATION_JSON).content(score))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/v1/matches/" + m + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("India won by 10 runs"));

        mvc.perform(get("/api/v1/players?teamId=" + t1 + "&role=BATSMAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Virat Kohli"));
    }

    @Test
    void validationAndDuplicateErrors() throws Exception {
        mvc.perform(post("/api/v1/teams").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
        create("/api/v1/teams", "{\"name\":\"England\",\"shortName\":\"eng\",\"country\":\"England\"}");
        mvc.perform(post("/api/v1/teams").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"England\",\"shortName\":\"eng\",\"country\":\"England\"}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/teams/99999")).andExpect(status().isNotFound());
    }
}
