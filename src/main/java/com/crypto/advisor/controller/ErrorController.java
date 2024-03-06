package com.crypto.advisor.controller;

import com.crypto.advisor.model.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(Exception.class)
    protected ModelAndView handle(Exception e) {
        log.error("An unhandled exception has occurred", e);
        return new ModelAndView(Constants.ERROR_PATH)
            .addObject("message", e.getMessage());
    }
}
