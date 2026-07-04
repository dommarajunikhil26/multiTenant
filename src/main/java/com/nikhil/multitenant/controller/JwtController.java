package com.nikhil.multitenant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protected")
public class JwtController {

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("You are authenticated");
    }
}
