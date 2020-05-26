package com.intuit.developer.helloworld.classes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackResponse {
    @JsonProperty("text")
    private String text;

    @JsonProperty("response_type")
    private String responseType = "ephemeral";

    @JsonProperty("replace_original")
    private boolean replaceOriginal = true;

    public SlackResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public boolean isReplaceOriginal() {
        return replaceOriginal;
    }

    public void setReplaceOriginal(boolean replaceOriginal) {
        this.replaceOriginal = replaceOriginal;
    }
}