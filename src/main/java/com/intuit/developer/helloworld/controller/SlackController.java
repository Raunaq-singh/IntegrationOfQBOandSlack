package com.intuit.developer.helloworld.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SlackController {
    @ResponseBody
    @PostMapping("/slack/events")
    public String slashCommandResponse(@RequestParam("text") String text){
        return text;
    }
}