
package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.TmpCustomerNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TmpCustomerRepository extends JpaRepository<TmpCustomerNew, String> {
    // Find all customers where acc_nbr is null (updated table reference)
    @Query("SELECT t FROM TmpCustomerNew t WHERE t.accNbr IS NULL")
    List<TmpCustomerNew> findAllWhereAccNbrIsNull();
}