package com.intuit.developer.helloworld.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SlackController {
    @ResponseBody
    @PostMapping("/slack/events")
    public String slashCommandResponse(HttpSession session, @RequestParam("text") String text){
        return text;
    }
}