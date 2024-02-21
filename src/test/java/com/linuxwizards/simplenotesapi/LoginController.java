package com.linuxwizards.simplenotesapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        // If the user is authenticated and authorized - this will always succeed (Spring Security)
        return ResponseEntity.ok().build();
    }
}
