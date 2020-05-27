package com.intuit.developer.helloworld.classes;

import com.intuit.ipp.services.CallbackHandler;
import com.intuit.ipp.services.CallbackMessage;

public class AsyncCallBack implements CallbackHandler {
    @Override
    public void execute(CallbackMessage callbackMessage) {
      // Implement a logic to handle the obtained response
        System.out.println(callbackMessage.toString());
    }
}