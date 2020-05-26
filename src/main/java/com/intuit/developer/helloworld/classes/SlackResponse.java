package com.intuit.developer.helloworld.classes;

public class SlackResponse {
    String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public SlackResponse(String text) {
        this.text = text;
    }
    
}