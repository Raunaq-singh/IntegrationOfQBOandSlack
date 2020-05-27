package com.intuit.developer.helloworld.controller;

import com.intuit.developer.helloworld.classes.credentialsClass;
import com.intuit.developer.helloworld.helper.CustomerHelper;

//import org.apache.log4j.Logger;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
//import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SlackController {
    @Autowired
    credentialsClass credentials;

    @Autowired
    CustomerHelper customerHelper;

    //private static final Logger logger = Logger.getLogger(SlackController.class);
    @ResponseBody
    @RequestMapping(value = "/slack/events", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String slashCommandResponse(@RequestParam("text") String text, @RequestParam("response_url") String responseURL) {
        if (StringUtils.isEmpty(credentials.getRealmID())) {
            return new JSONObject()
                    .put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!")
                    .toString();
        }
        //restTemplate.postForEntity(responseURL, new HttpEntity<>(gson.toJson(ResponseEntity.ok("Wait for a while...")), getHeaders()), String.class);
        int numberOfCustomersToBeCreated = 1;
        if(NumberUtils.isNumber(text) == true){
            numberOfCustomersToBeCreated = Integer.parseInt(text);
            text = RandomStringUtils.randomAlphanumeric(6);
        }
        if(numberOfCustomersToBeCreated < 0 || numberOfCustomersToBeCreated > 100000){
            return "Number of customers invalid!";
        }
        customerHelper.addCustomer(responseURL, text, numberOfCustomersToBeCreated);
        return "Wait for a while! The customers are being created!";
    } 
}