
package com.example.SPSProjectBackend.dto;
public class LoginRequest {
    private String userId;
    private String email;
    private String password;

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    public String getEmail() {
//        return "";
//    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}



//package com.example.SPSProjectBackend.dto;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//@Setter
//@Getter
//public class LoginRequest {
//
//    private Long id;
//    private String password;
//    private String email;
//    private String userlevel;
//}