//package com.example.SPSProjectBackend.service;
//import com.example.SPSProjectBackend.model.Sauserm;
//import com.example.SPSProjectBackend.repository.SausermRepository;
//import com.example.SPSProjectBackend.service.SecurityService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//
//@Service
//public class SecurityServiceImpl implements SecurityService {
//
//    @Autowired
//    private SausermRepository sausermRepository;
//
//    @Override
//    public boolean validateLogin(String userId, String pswrd) throws Exception {
//        return validate(userId, pswrd);
//    }
//
//    @Override
//    public String getPassword(String userId) {
//        return sausermRepository.findByUserIdIgnoreCase(userId)
//                .map(Sauserm::getPswrd)
//                .orElse(null);
//    }
//
//    @Override
//    public String getCostCenter(String userId) {
//        return sausermRepository.findByUserIdIgnoreCase(userId)
//                .map(Sauserm::getRptUser)
//                .orElse(null);
//    }
//
//    @Override
//    public String getAuthorizedLevel(String userId) {
//        return sausermRepository.findByUserIdIgnoreCase(userId)
//                .map(Sauserm::getUserLevel)
//                .orElse(null);
//    }
//
//    @Override
//    public Sauserm getSauserm(String userId) {
//        return sausermRepository.findByUserIdIgnoreCase(userId).orElse(null);
//    }
//
//    private boolean validate(String userId, String pswrd) throws Exception {
//        String encryptedPass = encrypt(pswrd);
//        return sausermRepository.findByUserIdIgnoreCaseAndPswrd(userId, encryptedPass).isPresent();
//    }
//
//    private String encrypt(String input) {
//        // Same CheckPass logic from your DAO (can be moved to utility)
//        return CheckPass(input);
//    }
//
//    private String CheckPass(String literal) {
//        // copy CheckPass logic here
//        return "123456"; // dummy; replace with actual
//    }
//}
