package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.User;
import com.example.SPSProjectBackend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByNicno(String nicno) {
        return userRepository.findByNicno(nicno)
                .orElseThrow(() -> new RuntimeException("User not found with NIC: " + nicno));
    }

    public User updateUser(User user) {
        user.setPassword(hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

//    public String authenticate(String email, String password, HttpSession session) {
//        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User does not exist in the database"));
//
//
//        if (user == null) {
//            throw new RuntimeException("User does not exist in the database");
//        }
//
//
//        if (!user.isVerified()) {
//            throw new RuntimeException("Email not verified. Please check your inbox.");
//        }
//
//
//        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
//        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
//            throw new RuntimeException("The password is incorrect");
//        }
//
//
//        // Store user details in session
//        session.setAttribute("userId", user.getId());
//        session.setAttribute("username", user.getEmail());
//        session.setAttribute("userLevel", user.getUserlevel());
//        session.setAttribute("eAccountNo", user.getEAccountNo());
//
//
//        // Set session timeout to 1 minute (60 seconds)
//        session.setMaxInactiveInterval(600000000);
//
//
    ////        return true;
//        return user.getUserlevel();
//    }

    public User authenticate(String email, String password, HttpSession session) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist in the database"));

        if (!user.isVerified()) {
            throw new RuntimeException("Email not verified. Please check your inbox.");
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("The password is incorrect");
        }

        session.setAttribute("email", user.getEmail());
        session.setAttribute("userLevel", user.getUserlevel());
        session.setAttribute("deptId", user.getDeptId());
        session.setAttribute("eAccountNo", user.getEAccountNo());
        session.setMaxInactiveInterval(600); // 10 minutes

        System.out.println("DeptId set in session: " + user.getDeptId());

        return user;
    }




    public String logout(HttpSession session) {
        session.invalidate();
        return "User logged out, session invalidated!";
    }

    private String hashPassword(String password) {
        // Implement your password hashing logic here
        return password; // Replace with actual hashing
    }
}