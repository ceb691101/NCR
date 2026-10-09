package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Pcesthmt;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JobRevisionHmtRepository extends JpaRepository<Pcesthmt, String> {
    @Query("SELECT DISTINCT p.projectNo FROM Pcesthmt p")
    List<String> findDistinctProNo();

    List<Pcesthmt> findByProjectNo(String projectNo);
   }