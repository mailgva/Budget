package com.gorbatenko.budget.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("rest")
public class TestRestController {
    /*
    @Autowired
    private RequestMappingHandlerMapping requestHandlerMapping;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/endpoints")
    public List<String> endpoints() {
        return requestHandlerMapping.getHandlerMethods()
                .entrySet()
                .stream()
                .map(entry -> entry.getKey().toString() + ": " + entry.getValue().getBeanType().getName() + "#" + entry.getValue().getMethod().getName())
                .collect(Collectors.toList());
    }
    */
}
