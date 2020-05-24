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
        final String accessToken = "eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiZGlyIn0..zHnETbIdYStTKbtNE1BY-w.lC4aVFGnUlRIf5y1hpZaAxFy_h07iHh0MZD7oAnpHFZQ36JYfrC7ELN7c5B6KhL6aZQ0OqmAeCqi3pic7KWX4jdKJw_V3htR5SwIfTZ9FRY8RIbMMUkD-z8xFD2g0o5duKk2CsMT2AgkUEpwoO4lOOBWGRv9PYEh0lQ4giOGRaMVlN_gRxsqCVNVDf0ZGtzv2AV_mSrUzmWM61Vj5UPppNKwdyttNrvyR3r7UtZQaU27WtxNmq-JenAHLPAyqLNSu3CVxYAJnQksh-0K0kCVj1-D5BXB-EpmmfxYXE8PC6rWQ34gcLAZi_CVpqlTq99KZF05h7AYzntvwZ7H5pQUB2NvChG5CIIrnQd_u5_uwsFmXKFt6evW9aTPLlomthZeENRTPYn9JMMm2a-VdxN01jLVggHv4MaVgz5gz6dF8fPlPCE3Ih9BtxeoqVs1PYHIlYGUEOrAFlsEdP4-Re1ykmTXa_YodidK6iHKyZQ7RzgZrHQU_AHmaDbFPGJJZUT-zAYlmFmKLu7361U-mbbJ-Ee5UPyNQeqp45FD1FsHJXSSsh1aFE5wet37S5QZBQLDh5lPeoFs4BtCoLK_BnQWNUEupbCM23K2lJIeJ19sg1Kn6eHJhFTcdAWPtBB_QLU9u0MbynsQtC1tvVvLqZ68lx3SolCRaR4FOvxo96dVMzI_CfRYU0PM5h70ZYrXsXJBDyt2PxrpmxiZwKtCKRwD_GpIjJLMdKgWnV4eTEF2Q_X1AmdNpqi8u2zr_oMjjP7aoZ8Ollv2LSQHVW64SdSaQGrfgE6e-qZN8kKdo8eQtM0Ay0TceKcSETxvk3RCfHbR7N1jKonYA1eXXhu2EibbgVOWr0NS6xTZPsv4XKJxGW2KBCGg9B8LkgaC4NjkexVmUZLEsRFR9FWVSWMDKRGy1g.0oiJezcjytpq4i520UKw4Q";

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