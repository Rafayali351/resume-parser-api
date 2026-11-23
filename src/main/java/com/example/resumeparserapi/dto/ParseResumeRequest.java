package com.example.resumeparserapi.dto;

import jakarta.validation.constraints.NotBlank;

public class ParseResumeRequest {

    @NotBlank(message = "Resume text must not be empty or blank")
    private String text;

    public ParseResumeRequest() {
    }

    public ParseResumeRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

