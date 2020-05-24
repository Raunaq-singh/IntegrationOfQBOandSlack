package com.intuit.developer.helloworld.controller;

import java.util.List;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.developer.helloworld.helper.QBOServiceHelper;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.services.DataService;
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
    public QBOServiceHelper helper;

    private static final Logger logger = Logger.getLogger(QBOController.class);
    
    @ResponseBody
    @PostMapping("/slack/events")
    public String slashCommandResponse(final HttpSession session, @RequestParam("text") final String text) {
        final String realmId = "4620816365061552710";
        if (StringUtils.isEmpty(realmId)) {
            return new JSONObject()
                    .put("response", "No realm ID.  QBO calls only work if the accounting scope was passed!")
                    .toString();
        }
        final String accessToken = (String) session.getAttribute("access_token");

        try {
            // get DataService
            final DataService service = helper.getDataService(realmId, accessToken);

            // add customer
            final Customer customer = getCustomerWithAllFields();
            customer.setDisplayName(text);
            final Customer savedCustomer = service.add(customer);       
            return createResponse(savedCustomer);
        } catch (final InvalidTokenException e) {
            return new JSONObject().put("response", "InvalidToken - Refreshtoken and try again").toString();
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