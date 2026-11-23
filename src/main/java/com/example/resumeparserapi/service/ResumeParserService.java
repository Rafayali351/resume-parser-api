// package com.example.resumeparserapi.service;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestClient;
// import org.springframework.web.client.RestClientResponseException;

// import java.util.List;
// import java.util.Map;

// @Service
// public class ResumeParserService {

//     private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);

//     private final RestClient restClient;
//     private final ObjectMapper objectMapper;
//     private final String apiKey;
//     private final String apiUrlTemplate;

//     public ResumeParserService(
//             RestClient restClient,
//             ObjectMapper objectMapper,
//             @Value("${gemini.api.key}") String apiKey,
//             @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={apiKey}}") String apiUrlTemplate) {
//         this.restClient = restClient;
//         this.objectMapper = objectMapper;
//         this.apiKey = apiKey;
//         this.apiUrlTemplate = apiUrlTemplate;
//     }

//     public JsonNode parseResume(String resumeText) {
//         String prompt = """
//                 You are an expert AI resume parser. Extract the candidate's details from the resume text provided below into a structured JSON object.
//                 The JSON object MUST contain the following fields:
//                 - "name": String (full name of candidate, or null if not available)
//                 - "email": String (email address, or null if not available)
//                 - "phone": String (phone number, or null if not available)
//                 - "skills": Array of strings (list of skills extracted from resume)
//                 - "experience": Array of objects or strings (work experience, roles, employment history)

//                 IMPORTANT INSTRUCTIONS:
//                 - Output ONLY valid, raw JSON.
//                 - Do NOT use markdown formatting or code blocks (no ```json or ``` fences).
//                 - Do NOT include any conversational text, greetings, or explanations before or after the JSON.

//                 Resume Text:
//                 """ + resumeText;

//         Map<String, Object> textPart = Map.of("text", prompt);
//         Map<String, Object> contentPart = Map.of("parts", List.of(textPart));
//         Map<String, Object> generationConfig = Map.of("responseMimeType", "application/json");

//         Map<String, Object> requestPayload = Map.of(
//                 "contents", List.of(contentPart),
//                 "generationConfig", generationConfig
//         );

//         String targetUrl = resolveTargetUrl(apiUrlTemplate, apiKey);
//         log.info("Calling Gemini API for resume parsing...");

//         String responseBody;
//         try {
//             responseBody = restClient.post()
//                     .uri(targetUrl)
//                     .contentType(MediaType.APPLICATION_JSON)
//                     .body(requestPayload)
//                     .retrieve()
//                     .body(String.class);
//         } catch (RestClientResponseException ex) {
//             log.error("Gemini API call failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
//             throw new RuntimeException("Gemini API error: " + ex.getResponseBodyAsString(), ex);
//         } catch (Exception ex) {
//             log.error("Gemini API call failed: {}", ex.getMessage());
//             throw new RuntimeException("Failed to call Gemini API: " + ex.getMessage(), ex);
//         }

//         return extractAndCleanJson(responseBody);
//     }

//     private String resolveTargetUrl(String template, String key) {
//         if (template.contains("{apiKey}")) {
//             return template.replace("{apiKey}", key);
//         } else if (!template.contains("key=")) {
//             return template + (template.contains("?") ? "&" : "?") + "key=" + key;
//         }
//         return template;
//     }

//     public JsonNode extractAndCleanJson(String responseBody) {
//         try {
//             JsonNode root = objectMapper.readTree(responseBody);
//             JsonNode candidates = root.path("candidates");
//             if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
//                 if (root.has("error")) {
//                     throw new RuntimeException("Gemini API returned error: " + root.path("error").path("message").asText());
//                 }
//                 throw new RuntimeException("No candidates returned from Gemini API");
//             }

//             JsonNode parts = candidates.get(0).path("content").path("parts");
//             if (parts.isMissingNode() || !parts.isArray() || parts.isEmpty()) {
//                 throw new RuntimeException("No content parts found in candidate");
//             }

//             String rawText = parts.get(0).path("text").asText();
//             String cleaned = stripMarkdownFences(rawText);

//             return objectMapper.readTree(cleaned);
//         } catch (Exception e) {
//             log.error("Failed to parse response JSON: {}", e.getMessage());
//             throw new RuntimeException("Failed to extract valid JSON: " + e.getMessage(), e);
//         }
//     }

//     public static String stripMarkdownFences(String raw) {
//         if (raw == null) {
//             return "";
//         }
//         String cleaned = raw.trim();
//         // Remove opening ```json or ```
//         if (cleaned.startsWith("```")) {
//             int firstNewline = cleaned.indexOf('\n');
//             if (firstNewline != -1) {
//                 cleaned = cleaned.substring(firstNewline + 1).trim();
//             } else {
//                 cleaned = cleaned.replaceFirst("^```[a-zA-Z]*", "").trim();
//             }
//         }
//         // Remove trailing ```
//         if (cleaned.endsWith("```")) {
//             cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
//         }
//         return cleaned;
//     }
// }

package com.example.resumeparserapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class ResumeParserService {

    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrlTemplate;

    public ResumeParserService(
            RestClient restClient,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model:gemini-3.8-flash}") String modelName,
            @Value("${gemini.api.url:}") String apiUrlOverride) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        if (apiUrlOverride != null && !apiUrlOverride.isBlank()) {
            this.apiUrlTemplate = apiUrlOverride;
        } else {
            this.apiUrlTemplate = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName
                    + ":generateContent?key={apiKey}";
        }
    }

    public JsonNode parseResume(String resumeText) {
        String prompt = """
                                You are an expert AI resume parser. Extract the candidate's details from the resume text provided below into a structured JSON object.
                                The JSON object MUST contain the following fields:
                - "name": String (full name of candidate, or null if not available)
                - "email": String (email address, or null if not available)
                - "phone": String (phone number, or null if not available)
                - "skills": Array of strings (list of skills extracted from resume)
                - "experience": Array of objects or strings (work experience, roles, employment history)

                                IMPORTANT INSTRUCTIONS:
                - Output ONLY valid, raw JSON.
                - Do NOT use markdown formatting or code blocks (no ```json or ``` fences).
                - Do NOT include any conversational text, greetings, or explanations before or after the JSON.

                                Resume Text:
                                """
                + resumeText;

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> contentPart = Map.of("parts", List.of(textPart));
        Map<String, Object> generationConfig = Map.of("responseMimeType", "application/json");

        Map<String, Object> requestPayload = Map.of(
                "contents", List.of(contentPart),
                "generationConfig", generationConfig);

        String targetUrl = resolveTargetUrl(apiUrlTemplate, apiKey);
        log.info("Calling Gemini API for resume parsing...");

        String responseBody;
        try {
            responseBody = restClient.post()
                    .uri(targetUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException ex) {
            log.error("Gemini API call failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Gemini API error: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            log.error("Gemini API call failed: {}", ex.getMessage());
            throw new RuntimeException("Failed to call Gemini API: " + ex.getMessage(), ex);
        }

        return extractAndCleanJson(responseBody);
    }

    private String resolveTargetUrl(String template, String key) {
        if (template.contains("{apiKey}")) {
            return template.replace("{apiKey}", key);
        } else if (!template.contains("key=")) {
            return template + (template.contains("?") ? "&" : "?") + "key=" + key;
        }
        return template;
    }

    public JsonNode extractAndCleanJson(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
                if (root.has("error")) {
                    throw new RuntimeException(
                            "Gemini API returned error: " + root.path("error").path("message").asText());
                }
                throw new RuntimeException("No candidates returned from Gemini API");
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isMissingNode() || !parts.isArray() || parts.isEmpty()) {
                throw new RuntimeException("No content parts found in candidate");
            }

            String rawText = parts.get(0).path("text").asText();
            String cleaned = stripMarkdownFences(rawText);

            return objectMapper.readTree(cleaned);
        } catch (Exception e) {
            log.error("Failed to parse response JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to extract valid JSON: " + e.getMessage(), e);
        }
    }

    public static String stripMarkdownFences(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim();
        // Remove opening ```json or ```
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline != -1) {
                cleaned = cleaned.substring(firstNewline + 1).trim();
            } else {
                cleaned = cleaned.replaceFirst("^```[a-zA-Z]*", "").trim();
            }
        }
        // Remove trailing ```
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }
        return cleaned;
    }
}