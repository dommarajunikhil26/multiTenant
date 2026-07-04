package com.nikhil.multitenant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/admin/dashboard")
    public ResponseEntity<String> adminDasboard() {
        return ResponseEntity.ok("Admin Dasboard");
    }

    @GetMapping("/manager/projects")
    public ResponseEntity<String> managerProject() {
        return ResponseEntity.ok("Manager Project");
    }
}
