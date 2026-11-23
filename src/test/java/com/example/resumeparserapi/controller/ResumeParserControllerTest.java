package com.example.resumeparserapi.controller;

import com.example.resumeparserapi.service.ResumeParserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResumeParserController.class)
class ResumeParserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeParserService resumeParserService;

    @Test
    @DisplayName("GET /api/v1/resume/health should return UP status")
    void healthCheckShouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/v1/resume/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("POST /api/v1/resume/parse with empty text should return HTTP 400")
    void parseResumeWithEmptyTextShouldReturnBadRequest() throws Exception {
        String requestJson = "{\"text\": \"\"}";

        mockMvc.perform(post("/api/v1/resume/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/v1/resume/parse with blank text should return HTTP 400")
    void parseResumeWithBlankTextShouldReturnBadRequest() throws Exception {
        String requestJson = "{\"text\": \"   \"}";

        mockMvc.perform(post("/api/v1/resume/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/v1/resume/parse with missing text field should return HTTP 400")
    void parseResumeWithMissingTextShouldReturnBadRequest() throws Exception {
        String requestJson = "{}";

        mockMvc.perform(post("/api/v1/resume/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/v1/resume/parse with valid text should return HTTP 200 and parsed JSON")
    void parseResumeWithValidTextShouldReturnOk() throws Exception {
        String mockParsedJson = """
                {
                  "name": "Jane Doe",
                  "email": "jane@example.com",
                  "phone": "+1-234-567-8900",
                  "skills": ["Java", "Spring Boot", "REST API"],
                  "experience": [
                    {
                      "role": "Senior Engineer",
                      "company": "Tech Corp"
                    }
                  ]
                }
                """;

        when(resumeParserService.parseResume(anyString()))
                .thenReturn(objectMapper.readTree(mockParsedJson));

        String requestJson = "{\"text\": \"Jane Doe, Software Engineer, Java expert. Contact: jane@example.com\"}";

        mockMvc.perform(post("/api/v1/resume/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.phone").value("+1-234-567-8900"))
                .andExpect(jsonPath("$.skills[0]").value("Java"))
                .andExpect(jsonPath("$.experience[0].role").value("Senior Engineer"));
    }

    @Test
    @DisplayName("POST /api/v1/resume/parse when service throws exception should return HTTP 500")
    void parseResumeWhenServiceFailsShouldReturnInternalServerError() throws Exception {
        when(resumeParserService.parseResume(anyString()))
                .thenThrow(new RuntimeException("Gemini API connection error"));

        String requestJson = "{\"text\": \"Some resume text\"}";

        mockMvc.perform(post("/api/v1/resume/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to parse resume: Gemini API connection error"));
    }
}

