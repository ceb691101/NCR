package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.SecInfoSessionData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecInfoSessionRepository extends CrudRepository<SecInfoSessionData, String> {
    
    // Find session by user ID
    Optional<SecInfoSessionData> findByUserId(String userId);
    
    // Find all sessions by user ID (in case multiple sessions are allowed)
    List<SecInfoSessionData> findAllByUserId(String userId);
    
    // Find session by user ID and session ID
    Optional<SecInfoSessionData> findByUserIdAndSessionId(String userId, String sessionId);
    
    // Delete all sessions by user ID
    void deleteByUserId(String userId);
    
    // Check if session exists by user ID
    boolean existsByUserId(String userId);
    
    // Check if session exists by session ID
    boolean existsBySessionId(String sessionId);
    
    // Find sessions by user category (for admin purposes)
    List<SecInfoSessionData> findByUserCategory(String userCategory);
}
