package com.example.SPSProjectBackend.repository;



import com.example.SPSProjectBackend.model.StandardRates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StandardRatesRepository extends JpaRepository<StandardRates, String> {
}

