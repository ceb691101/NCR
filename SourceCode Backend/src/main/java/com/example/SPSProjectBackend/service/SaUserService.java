package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.SaUser;
import com.example.SPSProjectBackend.repository.SaUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import util.common.Encryption;

import java.util.Date;
import java.util.Optional;

//@Service
//public class SaUserService {
//
//    private final SaUserRepository saUserRepository;
//
//    @Autowired
//    public SaUserService(SaUserRepository saUserRepository) {
//        this.saUserRepository = saUserRepository;
//    }
//
//    //    public SaUser login(String userId, String password) {
////        try {
////            // Encrypt the password using the Encryption class
////            Encryption encryption = new Encryption();
////            String encryptedPassword = encryption.checkPass(password);
////
////            // Fetch the user from the database
////            Optional<SaUser> user = saUserRepository.findByUserIdAndPassword(userId, encryptedPassword);
////
////            if (user.isPresent()) {
////                SaUser saUser = user.get();
////
////                // Check if the user is inactive
////                if (saUser.getStatus() != null && saUser.getStatus() == 1) {
////                    throw new RuntimeException("User is inactive");
////                }
////
////                // Check if the user account has expired
////                if (saUser.getExpiryDate().before(new java.util.Date())) {
////                    throw new RuntimeException("User account has expired");
////                }
////
////                return saUser;
////            } else {
////                throw new RuntimeException("Invalid user ID or password");
////            }
////        } catch (Exception e) {
////            throw new RuntimeException("Error during login: " + e.getMessage());
////        }
////    }
//
//
//
////    public SaUser login(String userId) {
////        if (userId == null || userId.isEmpty()) {
////            throw new RuntimeException("User ID must not be empty");
////        }
////
////        try {
////            // Fetch the user from the database
////            Optional<SaUser> user = saUserRepository.findByUserId(userId.trim().toUpperCase());
////
////            if (user.isPresent()) {
////                SaUser saUser = user.get();
////
////                // Check if the user is inactive
////                if (saUser.getStatus() != null && saUser.getStatus() == 1) {
////                    throw new RuntimeException("User is inactive");
////                }
////
////                // Check if the user account has expired
////                if (saUser.getExpiryDate().before(new java.util.Date())) {
////                    throw new RuntimeException("User account has expired");
////                }
////
////                return saUser;
////            } else {
////                throw new RuntimeException("Invalid user ID");
////            }
////        } catch (Exception e) {
////            throw new RuntimeException("Error during login: " + e.getMessage());
////        }
////    }
//
//    public SaUser login(String userId, String rawPassword) {
//        if (userId == null || userId.isEmpty()) {
//            throw new RuntimeException("User ID must not be empty");
//        }
//        if (rawPassword == null || rawPassword.isEmpty()) {
//            throw new RuntimeException("Password must not be empty");
//        }
//
//        try {
//            Optional<SaUser> userOpt = saUserRepository.findByUserId(userId.trim().toUpperCase());
//
//            if (userOpt.isEmpty()) {
//                throw new RuntimeException("Invalid user ID or password");
//            }
//
//            SaUser saUser = userOpt.get();
//
//            Encryption encryption = new Encryption();
//            String encodedInputPassword = encryption.checkPass(rawPassword);
//
//            // Now compare encoded password
//            if (!encodedInputPassword.equals(saUser.getPassword())) {
//                throw new RuntimeException("Invalid user ID or password");
//            }
//
//            if (saUser.getStatus() != null && saUser.getStatus() == 1) {
//                throw new RuntimeException("User is inactive");
//            }
//
//            if (saUser.getExpiryDate().before(new java.util.Date())) {
//                throw new RuntimeException("User account has expired");
//            }
//
//            return saUser;
//        } catch (Exception e) {
//            throw new RuntimeException("Error during login: " + e.getMessage());
//        }
//    }
//
//}


@Service
public class SaUserService {

    private final SaUserRepository saUserRepository;
    private final Encryption encryption;

    @Autowired
    public SaUserService(SaUserRepository saUserRepository, Encryption encryption) {
        this.saUserRepository = saUserRepository;
        this.encryption = encryption;
    }

    @Transactional
    public SaUser login(String userId, String password) {
        if (userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
            throw new RuntimeException("User ID and password must not be empty");
        }

        Optional<SaUser> userOptional = saUserRepository.findByUserId(userId.trim().toUpperCase());

        if (userOptional.isPresent()) {
            SaUser user = userOptional.get();

            if (user.getStatus() != null && user.getStatus() == 1) {
                throw new RuntimeException("User is inactive");
            }

            if (user.getExpiryDate() != null && user.getExpiryDate().before(new Date())) {
                throw new RuntimeException("User account has expired");
            }

            try {
                boolean isValid = encryption.validateLogin(userId, password, user.getPassword());
                if (!isValid) {
                    throw new RuntimeException("Invalid password");
                }
            } catch (Exception e) {
                throw new RuntimeException("Error validating password: " + e.getMessage());
            }

            return user;

        } else {
            throw new RuntimeException("Invalid user ID");
        }
    }
}