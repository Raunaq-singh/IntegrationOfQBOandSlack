package com.intuit.developer.helloworld.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SlackController {
    @ResponseBody
    @RequestMapping("/slack/events")
    public String slashCommandResponse(){
        return "Success";
    }
}