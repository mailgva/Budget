package com.gorbatenko.budget.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("rest")
public class TestRestController {
//
//    @Autowired
//    private RequestMappingHandlerMapping requestHandlerMapping;
//
////    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @GetMapping(value = "/endpoints")
//    public List<String> endpoints() {
//        return requestHandlerMapping.getHandlerMethods()
//                .entrySet()
//                .stream()
//                .map(entry -> entry.getKey().getPathPatternsCondition() + " " + entry.getKey().getMethodsCondition())
//                //.map(entry -> entry.getKey().toString() + ": " + entry.getValue().getBeanType().getName() + "#" + entry.getValue().getMethod().getName())
//                .sorted()
//                .collect(Collectors.toList());
//    }
}
