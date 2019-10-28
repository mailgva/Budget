package com.gorbatenko.budget.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@ControllerAdvice
public class ErrorController {

    private static Logger logger = LoggerFactory.getLogger(ErrorController.class);

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String exception(final Throwable throwable, final Model model, HttpServletRequest request) {
        logger.error("Exception during execution Budget application", throwable);

        String errorMessage = (throwable != null ? throwable.getLocalizedMessage() : "Unknown error");
        if (throwable instanceof BindException) {
            FieldError fieldError = ((BindException) throwable).getBindingResult().getFieldError();
            errorMessage = "Поле  <b>" + fieldError.getField() + " </b>  " +
                    fieldError.getDefaultMessage();
        }
        model.addAttribute("errorMessage", errorMessage);
        return "error";
    }

}