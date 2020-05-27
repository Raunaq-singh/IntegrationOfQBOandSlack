package com.intuit.developer.helloworld.classes;

import com.intuit.ipp.data.Fault;

import java.util.List;

import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.Error;
import com.intuit.ipp.services.BatchOperation;
import com.intuit.ipp.services.CallbackHandler;
import com.intuit.ipp.services.CallbackMessage;
import com.intuit.ipp.services.QueryResult;

import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class AsyncCallBackBatch implements CallbackHandler{
    SlackResponse slackResponse = new SlackResponse("All the customers are created!", null);
    ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());
    RestTemplate restTemplate = new RestTemplate(requestFactory);

    String responseURL;
    
    public AsyncCallBackBatch(String responseURL) {
        this.responseURL = responseURL;
    }

    @Override
    public void execute(CallbackMessage callbackMessage) {
        System.out.println("In AsyncCallBackFind callback...");
        BatchOperation batchOperation = callbackMessage.getBatchOperation();
        List<String> bIds = batchOperation.getBIds();
        for(String bId : bIds) {
            if(batchOperation.isFault(bId)) {
                Fault fault = batchOperation.getFault(bId);
                Error error = fault.getError().get(0);
                slackResponse.setText("Fault error :" + error.getCode() + ", " + error.getDetail() + ", " + error.getMessage());
                break;
            } else if(batchOperation.isEntity(bId)) {
                System.out.println("Entity : " + ((Customer)batchOperation.getEntity(bId)).getDisplayName());
            } else if(batchOperation.isQuery(bId)) {
                QueryResult queryResult = batchOperation.getQueryResponse(bId);
                System.out.println("Query : " + queryResult.getTotalCount());
            } else if(batchOperation.isReport(bId)) {
                //Report report = batchOperation.getReport(bId);
                //System.out.println("Report : " + report.getName().value());
            } else {
                slackResponse.setText("Something wrong!...");
            }
        }
        restTemplate.postForObject(responseURL, new HttpEntity<>(slackResponse, getHeaders()), String.class); 
    }
    HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}