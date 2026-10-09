//package com.example.SPSProjectBackend.repository;
//
//import com.example.SPSProjectBackend.model.SpestedyCons;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface SpestedyConsRepository extends JpaRepository<SpestedyCons, String> {
//
//    @Query("SELECT s.estimateNo FROM SpestedyCons s")
//    List<String> findAllEstimateNumbers();
//}