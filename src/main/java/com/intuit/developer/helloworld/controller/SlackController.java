package com.intuit.developer.helloworld.controller;

import java.util.Arrays;

import com.google.gson.Gson;
import com.intuit.developer.helloworld.classes.SlackResponse;
import com.intuit.developer.helloworld.classes.SlackResponseAttachment;
import com.intuit.developer.helloworld.classes.credentialsClass;
import com.intuit.developer.helloworld.helper.CustomerHelper;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class SlackController {
    @Autowired
    credentialsClass credentials;

    CustomerHelper customerHelper;

    private static final Logger logger = Logger.getLogger(SlackController.class);
    Gson gson;
    SlackResponse slackResponse = new SlackResponse("We're completing your request....", Arrays.asList(new SlackResponseAttachment("Wait here!")));
    ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());
    RestTemplate restTemplate = new RestTemplate(requestFactory);

    @ResponseBody
    @RequestMapping(value = "/slack/events", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String slashCommandResponse(@RequestParam("text") String text,
            @RequestParam("response_url") String responseURL) {
        if (StringUtils.isEmpty(credentials.getRealmID())) {
            return new JSONObject()
                    .put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!")
                    .toString();
        }
        //restTemplate.postForEntity(responseURL, new HttpEntity<>(slackResponse, getHeaders()), String.class);
        //int numberOfCustomersToBeCreated = Integer.parseInt(text);
        logger.info("In the method!");
        return customerHelper.addCustomer(text);
    }
    
    HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}