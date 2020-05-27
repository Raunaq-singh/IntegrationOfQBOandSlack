package com.intuit.developer.helloworld.helper;

import java.util.List;

//import com.intuit.developer.helloworld.classes.AsyncCallBack;
import com.intuit.developer.helloworld.classes.AsyncCallBackBatch;
import com.intuit.developer.helloworld.classes.SlackResponse;
import com.intuit.developer.helloworld.classes.credentialsClass;
import com.intuit.developer.helloworld.client.OAuth2PlatformClientFactory;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.OperationEnum;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.services.BatchOperation;
import com.intuit.ipp.services.DataService;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomerHelperForBatch {
    @Autowired
    QBOServiceHelper helper;
    
    @Autowired
    credentialsClass credentials;

    @Autowired
    OAuth2PlatformClientFactory factory;

    DataService service;
    BatchOperation batchOperation = new BatchOperation();

    SlackResponse slackResponse = new SlackResponse("We're completing your request....", null);
    ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());
    RestTemplate restTemplate = new RestTemplate(requestFactory);
    private static final Logger logger = Logger.getLogger(CustomerHelper.class);
    private static final String failureMsg = "Failed";
    
    public Boolean addCustomer(String responseURL, String text, int numberOfCustomersToBeCreated){
        String status = "Zero Customers";
        Boolean check = true;
        for(int i = 1; i <= numberOfCustomersToBeCreated; i++){
            Customer customer = getCustomerWithAllFields();
            customer.setDisplayName(text);
            batchOperation.addEntity(customer, OperationEnum.CREATE, "bID"+i);
            /*
            status = addSingleCustomer(text);
            if(status != ("Customer Created!")){
                check = false;
                break;
            }
            */
            text = RandomStringUtils.randomAlphanumeric(8);
        }
        try {
            // get DataService
            service = helper.getDataService(credentials.getRealmID(), credentials.getAccessToken());
            // add customer
        } catch (final InvalidTokenException e) {
            logger.error("Error while calling executeQuery :: " + e.getMessage());
            // refresh tokens
            logger.info("received 401 during companyinfo call, refreshing tokens now");
            OAuth2PlatformClient client = factory.getOAuth2PlatformClient();
            try {
                BearerTokenResponse bearerTokenResponse = client.refreshToken(credentials.getRefreshToken());
                credentials.setAccessToken(bearerTokenResponse.getAccessToken());
                credentials.setRefreshToken(bearerTokenResponse.getRefreshToken());
                
                service = helper.getDataService(credentials.getRealmID(), credentials.getAccessToken());
            } catch (OAuthException e1) {
                logger.error("Error while calling bearer token :: " + e.getMessage());
                check = false;
                status = new JSONObject().put("response",failureMsg).toString();
            } catch (FMSException e1) {
                logger.error("Error while calling company currency :: " + e.getMessage());
                check = false;
                status = new JSONObject().put("response",failureMsg).toString();
            }
        } catch (final FMSException e) {
            final List<com.intuit.ipp.data.Error> list = e.getErrorList();
            list.forEach(error -> logger.error("Error while calling the API :: " + error.getMessage()));
            check = false;
            status = new JSONObject().put("response", "Failed").toString();
        }
        if(check){
            try {
                service.executeBatchAsync(batchOperation, new AsyncCallBackBatch(responseURL));
            } catch (FMSException e) {
                final List<com.intuit.ipp.data.Error> list = e.getErrorList();
                list.forEach(error -> logger.error("Error while calling the API :: " + error.getMessage()));
                check = false;
                status = new JSONObject().put("response", "Failed").toString();
            }
        }
        if(check == false){
            slackResponse.setText(status);
            restTemplate.postForObject(responseURL, new HttpEntity<>(slackResponse, getHeaders()), String.class);
        }
        return check;
    }
    /*
    public String addSingleCustomer(String text){
        try {
            // get DataService
            final DataService service = helper.getDataService(credentials.getRealmID(), credentials.getAccessToken());
            // add customer
            final Customer customer = getCustomerWithAllFields();
            customer.setDisplayName(text);
            service.addAsync(customer, new AsyncCallBack());
            return "Customer Created!";
            //return createResponse(savedCustomer);
        } catch (final InvalidTokenException e) {
            logger.error("Error while calling executeQuery :: " + e.getMessage());
            // refresh tokens
            logger.info("received 401 during companyinfo call, refreshing tokens now");
            OAuth2PlatformClient client = factory.getOAuth2PlatformClient();
            try {
                BearerTokenResponse bearerTokenResponse = client.refreshToken(credentials.getRefreshToken());
                credentials.setAccessToken(bearerTokenResponse.getAccessToken());
                credentials.setRefreshToken(bearerTokenResponse.getRefreshToken());
                
                DataService service = helper.getDataService(credentials.getRealmID(), credentials.getAccessToken());
                final Customer customer = getCustomerWithAllFields();
                customer.setDisplayName(text);
                service.addAsync(customer, new AsyncCallBack());       
                return "Customer Created!";
            } catch (OAuthException e1) {
                logger.error("Error while calling bearer token :: " + e.getMessage());
                return new JSONObject().put("response",failureMsg).toString();
            } catch (FMSException e1) {
                logger.error("Error while calling company currency :: " + e.getMessage());
                return new JSONObject().put("response",failureMsg).toString();
            }
        } catch (final FMSException e) {
            final List<com.intuit.ipp.data.Error> list = e.getErrorList();
            list.forEach(error -> logger.error("Error while calling the API :: " + error.getMessage()));
            return new JSONObject().put("response", "Failed").toString();
        }
    }
    */
    private Customer getCustomerWithAllFields() {
        final Customer customer = new Customer();
        customer.setDisplayName(RandomStringUtils.randomAlphanumeric(6));
        customer.setCompanyName("ABC Corporations");

        final EmailAddress emailAddr = new EmailAddress();
		emailAddr.setAddress("testconceptsample@mailinator.com");
		customer.setPrimaryEmailAddr(emailAddr);

		return customer;
    }

    HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}