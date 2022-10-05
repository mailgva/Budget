package com.gorbatenko.budget.web;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.UndeclaredThrowableException;

@Slf4j
@ControllerAdvice
public class ErrorController extends AbstractWebController {

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String exception(final Throwable throwable, final Model model, HttpServletRequest request) {
        log.error("Exception during execution BudgetItem application", throwable);

        String errorMessage = (throwable != null ? throwable.getLocalizedMessage() : "Unknown error");

        if (throwable instanceof UndeclaredThrowableException) {
            errorMessage = (throwable.getCause().getLocalizedMessage() != null ?
                    throwable.getCause().getLocalizedMessage() : throwable.getCause().getMessage());
        }
        if (throwable instanceof BindException) {
            FieldError fieldError = ((BindException) throwable).getBindingResult().getFieldError();
            errorMessage = "Поле  <b>" + fieldError.getField() + " </b>  " +
                    fieldError.getDefaultMessage();
        }
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("pageName", "Ошибка");
        log.error(errorMessage);
        return "error";
    }

}