// package com.example.resumeparserapi.service;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.web.client.RestClient;

// import static org.junit.jupiter.api.Assertions.*;

// class ResumeParserServiceTest {

//   private ResumeParserService service;
//   private ObjectMapper objectMapper;

//   @BeforeEach
//   void setUp() {
//     objectMapper = new ObjectMapper();
//     RestClient restClient = RestClient.builder().build();
//     service = new ResumeParserService(restClient, objectMapper, "dummy-key", "https://dummy.url");
//   }

//   @Test
//   @DisplayName("stripMarkdownFences should clean ```json ... ``` blocks")
//   void testStripMarkdownFencesWithJsonFence() {
//     String fenced = "```json\n{\"name\":\"John Doe\"}\n```";
//     String cleaned = ResumeParserService.stripMarkdownFences(fenced);
//     assertEquals("{\"name\":\"John Doe\"}", cleaned);
//   }

//   @Test
//   @DisplayName("stripMarkdownFences should clean generic ``` ... ``` blocks")
//   void testStripMarkdownFencesWithGenericFence() {
//     String fenced = "```\n{\"name\":\"John Doe\"}\n```";
//     String cleaned = ResumeParserService.stripMarkdownFences(fenced);
//     assertEquals("{\"name\":\"John Doe\"}", cleaned);
//   }

//   @Test
//   @DisplayName("stripMarkdownFences should return raw json unchanged if no fences")
//   void testStripMarkdownFencesWithNoFences() {
//     String raw = "{\"name\":\"John Doe\"}";
//     String cleaned = ResumeParserService.stripMarkdownFences(raw);
//     assertEquals("{\"name\":\"John Doe\"}", cleaned);
//   }

//   @Test
//   @DisplayName("extractAndCleanJson should parse valid Gemini response body")
//   void testExtractAndCleanJson() {
//     String geminiResponse = """
//         {
//           "candidates": [
//             {
//               "content": {
//                 "parts": [
//                   {
//                     "text": "```json\\n{\\"name\\":\\"Jane\\",\\"skills\\":[\\"Java\\"]}\\n```"
//                   }
//                 ]
//               }
//             }
//           ]
//         }
//         """;

//     JsonNode result = service.extractAndCleanJson(geminiResponse);
//     assertNotNull(result);
//     assertEquals("Jane", result.path("name").asText());
//     assertEquals("Java", result.path("skills").get(0).asText());
//   }

//   @Test
//   @DisplayName("extractAndCleanJson should throw exception when candidates are missing")
//   void testExtractAndCleanJsonMissingCandidates() {
//     String invalidResponse = "{\"candidates\": []}";
//     assertThrows(RuntimeException.class, () -> service.extractAndCleanJson(invalidResponse));
//   }
// }
package com.example.resumeparserapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

class ResumeParserServiceTest {

  private ResumeParserService service;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    RestClient restClient = RestClient.builder().build();
    service = new ResumeParserService(
        restClient,
        objectMapper,
        "dummy-key",
        "gemini-3.8-flash",
        "https://dummy.url");
  }

  @Test
  @DisplayName("stripMarkdownFences should clean ```json ... ``` blocks")
  void testStripMarkdownFencesWithJsonFence() {
    String fenced = "```json\n{\"name\":\"John Doe\"}\n```";
    String cleaned = ResumeParserService.stripMarkdownFences(fenced);
    assertEquals("{\"name\":\"John Doe\"}", cleaned);
  }

  @Test
  @DisplayName("stripMarkdownFences should clean generic ``` ... ``` blocks")
  void testStripMarkdownFencesWithGenericFence() {
    String fenced = "```\n{\"name\":\"John Doe\"}\n```";
    String cleaned = ResumeParserService.stripMarkdownFences(fenced);
    assertEquals("{\"name\":\"John Doe\"}", cleaned);
  }

  @Test
  @DisplayName("stripMarkdownFences should return raw json unchanged if no fences")
  void testStripMarkdownFencesWithNoFences() {
    String raw = "{\"name\":\"John Doe\"}";
    String cleaned = ResumeParserService.stripMarkdownFences(raw);
    assertEquals("{\"name\":\"John Doe\"}", cleaned);
  }

  @Test
  @DisplayName("extractAndCleanJson should parse valid Gemini response body")
  void testExtractAndCleanJson() {
    String geminiResponse = """
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  {
                    "text": "```json\\n{\\"name\\":\\"Jane\\",\\"skills\\":[\\"Java\\"]}\\n```"
                  }
                ]
              }
            }
          ]
        }
        """;

    JsonNode result = service.extractAndCleanJson(geminiResponse);
    assertNotNull(result);
    assertEquals("Jane", result.path("name").asText());
    assertEquals("Java", result.path("skills").get(0).asText());
  }

  @Test
  @DisplayName("extractAndCleanJson should throw exception when candidates are missing")
  void testExtractAndCleanJsonMissingCandidates() {
    String invalidResponse = "{\"candidates\": []}";
    assertThrows(RuntimeException.class, () -> service.extractAndCleanJson(invalidResponse));
  }
}