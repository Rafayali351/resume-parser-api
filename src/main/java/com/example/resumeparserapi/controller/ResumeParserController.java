package com.example.resumeparserapi.controller;

import com.example.resumeparserapi.dto.ErrorResponse;
import com.example.resumeparserapi.dto.ParseResumeRequest;
import com.example.resumeparserapi.service.ResumeParserService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/resume")
public class ResumeParserController {

    private final ResumeParserService resumeParserService;

    public ResumeParserController(ResumeParserService resumeParserService) {
        this.resumeParserService = resumeParserService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @PostMapping(value = "/parse", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> parseResume(@RequestBody(required = false) ParseResumeRequest request) {
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Resume text must not be empty or blank"));
        }

        try {
            JsonNode parsedResume = resumeParserService.parseResume(request.getText());
            return ResponseEntity.ok(parsedResume);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to parse resume: " + ex.getMessage()));
        }
    }
}

