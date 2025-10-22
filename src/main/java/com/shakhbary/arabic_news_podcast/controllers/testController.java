package com.shakhbary.arabic_news_podcast.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testController {
    
    @GetMapping("/")
    public String home() {
        return "Welcome to the public API 🚀 (no auth required)";
    }

    @GetMapping("/user/hello")
    public String userHello() {
        return "Hello USER 👤 - you are authenticated with ROLE_USER or ROLE_ADMIN";
    }

    @GetMapping("/admin/hello")
    public String adminHello() {
        return "Hello ADMIN 👑 - only ROLE_ADMIN can see this!";
    }
}
