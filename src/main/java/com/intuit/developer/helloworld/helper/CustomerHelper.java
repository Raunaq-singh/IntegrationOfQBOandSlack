package com.intuit.developer.helloworld.helper;

import java.util.List;

import com.intuit.developer.helloworld.classes.credentialsClass;
import com.intuit.developer.helloworld.client.OAuth2PlatformClientFactory;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.services.DataService;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomerHelper {
    @Autowired
    public QBOServiceHelper helper;
    
    @Autowired
    credentialsClass credentials;

    @Autowired
    OAuth2PlatformClientFactory factory;

    private static final Logger logger = Logger.getLogger(CustomerHelper.class);
    private static final String failureMsg = "Failed";
    
    public String addCustomer(String text){
        try {
            // get DataService
            final DataService service = helper.getDataService(credentials.getRealmID(), credentials.getAccessToken());
            // add customer
            final Customer customer = getCustomerWithAllFields();
            customer.setDisplayName(text);
            service.add(customer);
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
                service.add(customer);       
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
    private Customer getCustomerWithAllFields() {
        final Customer customer = new Customer();
        customer.setDisplayName(RandomStringUtils.randomAlphanumeric(6));
        customer.setCompanyName("ABC Corporations");

        final EmailAddress emailAddr = new EmailAddress();
		emailAddr.setAddress("testconceptsample@mailinator.com");
		customer.setPrimaryEmailAddr(emailAddr);

		return customer;
    }
}