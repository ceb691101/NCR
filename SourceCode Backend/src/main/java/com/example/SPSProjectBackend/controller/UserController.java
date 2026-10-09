package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.LoginRequest;
import com.example.SPSProjectBackend.model.User;
import com.example.SPSProjectBackend.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> getUsers(){
        return userService.getUsers();
    }

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable("id") Integer id){
        return userService.getUser(id);
    }

    @PutMapping("/user/{id}")
    public User updateUser(@RequestBody() User user, @PathVariable("id") Long id){
        return userService.updateUser(user);
    }

    @GetMapping("/search")
    public User getUserByNicno(@RequestParam String nicno) {
        return userService.getUserByNicno(nicno);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring!";
    }

    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable("id") Integer id){
        userService.deleteUser(id);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            // Authenticate user and get user
            User user = userService.authenticate(loginRequest.getEmail(), loginRequest.getPassword(), session);


            // Set deptId in the session dynamically
            session.setAttribute("deptId", user.getDeptId());


            // Create response JSON with all user details needed for sessionStorage
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login was successful!");

            response.put("email", user.getEmail());
            response.put("userLevel", user.getUserlevel());
            response.put("eAccountNo", user.getEAccountNo());
            response.put("deptId", user.getDeptId());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An unknown error occurred"));
        }
    }


    @GetMapping("/session")
    public ResponseEntity<String> checkSession(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body("Session expired");
        }
        return ResponseEntity.ok("Session active");
    }


    @PostMapping("/logout")
    public String logout(HttpSession session) {
        return userService.logout(session);
    }


    @GetMapping("/sessioninfo")
    public ResponseEntity<Map<String, Object>> getSessionInfo(HttpSession session) {
//      if (session.getAttribute("userId") == null) {
        if (session.getAttribute("email") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No active session"));
//         if (session.getAttribute("userId") == null) {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Session expired"));
        }
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("userId", session.getAttribute("userId"));
        sessionInfo.put("userLevel", session.getAttribute("userLevel"));
        sessionInfo.put("eAccountNo", session.getAttribute("eAccountNo"));
        sessionInfo.put("deptId", session.getAttribute("deptId"));
        sessionInfo.put("sessionTimeout", session.getMaxInactiveInterval());
        return ResponseEntity.ok(sessionInfo);
    }
}