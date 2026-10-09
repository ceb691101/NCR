package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Netmeter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NetmeterRepository extends JpaRepository<Netmeter, String> {
}
