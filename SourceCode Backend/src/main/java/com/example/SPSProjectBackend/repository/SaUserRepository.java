package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.SaUser;
import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

//@Repository
//public interface SaUserRepository extends JpaRepository<SaUser, String> {
//    Optional<SaUser> findByUserIdAndPassword(String userId, String password);
//}

public interface SaUserRepository extends CrudRepository<SaUser, String> {
        //    @Query("SELECT u FROM SaUser u WHERE TRIM(u.userId) = :userId AND u.password = :password")
//    Optional<SaUser> findByUserIdAndPassword(@Param("userId") String userId, @Param("password") String password);
        @Query("SELECT u FROM SaUser u WHERE TRIM(u.userId) = :userId")
        Optional<SaUser> findByUserId(@Param("userId") String userId);
}