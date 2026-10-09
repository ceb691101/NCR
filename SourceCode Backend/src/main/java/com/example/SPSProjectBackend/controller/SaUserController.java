package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.LoginRequest;
import com.example.SPSProjectBackend.model.SaUser;
import com.example.SPSProjectBackend.service.SaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
// @RequestMapping("/api/v1/auth")
public class SaUserController {

    private final SaUserService saUserService;


    @Autowired
    public SaUserController(SaUserService saUserService) {
        this.saUserService = saUserService;
    }

//    @PostMapping("/login")
//    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
//        try {
//            SaUser user = saUserService.login(loginRequest.getUserId(), loginRequest.getPassword());
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Login successful");
//            response.put("userId", user.getUserId());
//            response.put("userName", user.getUserName());
//            response.put("userLevel", user.getUserLevel());
//
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
//        }
//    }

    //@PostMapping("/login")
//    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
//        String userId = loginRequest.getUserId();
//        String password = loginRequest.getPassword();
//
//        if (userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "User ID and password must not be empty"));
//        }
//
//        try {
//            SaUser user = saUserService.login(userId, password);
//
//            // Set session attributes
//            request.getSession().setAttribute("loggedUser", user.getUserId().toUpperCase());
//            request.getSession().setAttribute("loggedUserRole", user.getUserLevel());
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Login successful");
//            response.put("userId", user.getUserId());
//            response.put("userName", user.getUserName());
//            response.put("userLevel", user.getUserLevel());
//
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
//        }
//    }

//

    // @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String userId = loginRequest.getUserId();
        String password = loginRequest.getPassword();

        if (userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "User ID and password must not be empty"));
        }

        try {
            SaUser user = saUserService.login(userId, password);

            request.getSession().setAttribute("loggedUser", user.getUserId().toUpperCase());
            request.getSession().setAttribute("loggedUserRole", user.getUserLevel());
            request.getSession().setAttribute("loggedusercostcenter", user.getRptUser());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("userId", user.getUserId());
            response.put("userName", user.getUserName());
            response.put("userLevel", user.getUserLevel());
            response.put("costcenter", user.getRptUser());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }


}