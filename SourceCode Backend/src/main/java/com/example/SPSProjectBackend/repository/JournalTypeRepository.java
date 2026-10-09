package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.JournalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalTypeRepository extends JpaRepository<JournalType, String> {

    @Query("SELECT jt FROM JournalType jt WHERE jt.jnltStatus = 'A' ORDER BY jt.jnlType")
    List<JournalType> findAllOrderedByType();
}
