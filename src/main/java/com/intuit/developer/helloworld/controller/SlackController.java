package com.intuit.developer.helloworld.controller;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.developer.helloworld.classes.credentialsClass;
import com.intuit.developer.helloworld.client.OAuth2PlatformClientFactory;
import com.intuit.developer.helloworld.helper.QBOServiceHelper;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.services.DataService;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SlackController {
    @Autowired
    OAuth2PlatformClientFactory factory;
    
    @Autowired
    public QBOServiceHelper helper;
    
    @Autowired
    credentialsClass credentials;

    private static final String failureMsg = "Failed";

    private static final Logger logger = Logger.getLogger(SlackController.class);

    @ResponseBody
    @PostMapping("/slack/events")
    public String slashCommandResponse(@RequestParam("text") final String text, @RequestParam("response-url") String responseURL) {
        
        return responseURL;
        
        if (StringUtils.isEmpty(credentials.getRealmID())) {
            return new JSONObject()
                    .put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!")
                    .toString();
        }
        try {
            // get DataService
            final DataService service = helper.getDataService(credentials.getRealmID(), credentials.getAccessToken());

            // add customer
            final Customer customer = getCustomerWithAllFields();
            customer.setDisplayName(text);
            final Customer savedCustomer = service.add(customer);
            return createResponse(savedCustomer);
        } catch (final InvalidTokenException e) {
            logger.error("Error while calling executeQuery :: " + e.getMessage());
            // refresh tokens
            logger.info("received 401 during companyinfo call, refreshing tokens now");
            OAuth2PlatformClient client = factory.getOAuth2PlatformClient();

            try {
                BearerTokenResponse bearerTokenResponse = client.refreshToken(credentials.getRefreshToken());
                credentials.setAccessToken(bearerTokenResponse.getAccessToken());
                credentials.setRefreshToken(bearerTokenResponse.getRefreshToken());
		        //call company info again using new tokens
		        logger.info("calling companyinfo using new tokens");
		        DataService service = helper.getDataService(credentials.getRealmID(), credentials.getAccessToken());
				
				// get all companyinfo
				final Customer customer = getCustomerWithAllFields();
                customer.setDisplayName(text);
                final Customer savedCustomer = service.add(customer);       
                return createResponse(savedCustomer);
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

    private Customer getCustomerWithAllFields() {
        final Customer customer = new Customer();
        customer.setDisplayName(RandomStringUtils.randomAlphanumeric(6));
        customer.setCompanyName("ABC Corporations");

        final EmailAddress emailAddr = new EmailAddress();
		emailAddr.setAddress("testconceptsample@mailinator.com");
		customer.setPrimaryEmailAddr(emailAddr);

		return customer;
    }
    
    private String createResponse(Object entity) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString;
		try {
			jsonInString = mapper.writeValueAsString(entity);
		} catch (JsonProcessingException e) {
			return createErrorResponse(e);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
		return jsonInString;
	}

	private String createErrorResponse(Exception e) {
		logger.error("Exception while calling QBO ", e);
		return new JSONObject().put("response","Failed").toString();
    }
}