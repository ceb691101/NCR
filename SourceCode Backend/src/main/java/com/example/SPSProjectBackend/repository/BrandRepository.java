package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, String> {

    @Query("SELECT b FROM Brand b WHERE (:status IS NULL OR TRIM(b.brStatus) = :status) ORDER BY b.brCode")
    List<Brand> findByStatus(@Param("status") String status);
}
