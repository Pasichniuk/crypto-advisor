package com.crypto.advisor.controller;

import com.crypto.advisor.model.Constants;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping(Constants.ERROR_PATH)
    public ModelAndView handleError(HttpServletRequest request) {
        String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (message != null && message.isEmpty()) {
            message = null;
        }

        return new ModelAndView(Constants.ERROR_PATH)
                .addObject("message", message);
    }
}
