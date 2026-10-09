package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.SpestedyCon;
import com.example.SPSProjectBackend.model.SpestedyConId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpestedyConRepository extends JpaRepository<SpestedyCon, SpestedyConId> {
    List<SpestedyCon> findByIdDeptId(String deptId); // filter by deptId
}
