package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.JurnlAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JurnlAuthRepository extends JpaRepository<JurnlAuth, String> {

    @Query("SELECT ja FROM JurnlAuth ja WHERE ja.acStatus = 'A' ORDER BY ja.authCode")
    List<JurnlAuth> findAllActive();
}
