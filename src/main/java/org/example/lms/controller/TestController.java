package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Diagnostics", description = "Simple diagnostic endpoint retained from the existing project.")
@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }

}
