package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Pcesthtt;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface PcesthttRepository extends JpaRepository<Pcesthtt, String> {

    @Query("SELECT p.status AS status, COUNT(p) AS totalRows " +
            "FROM Pcesthtt p " +
            "GROUP BY p.status")
    List<Object[]> getRowCountByStatus();
}