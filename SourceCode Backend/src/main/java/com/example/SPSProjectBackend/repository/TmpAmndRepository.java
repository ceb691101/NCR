package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.TmpAmnd;
import com.example.SPSProjectBackend.model.TmpAmndPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TmpAmndRepository extends JpaRepository<TmpAmnd, TmpAmndPk> {
    // Find amendments by status (for fetching pending amendments only)
    List<TmpAmnd> findByStatus(String status);

    // Find by status and areaCd for filtering
    List<TmpAmnd> findByStatusAndAreaCd(String status, String areaCd);

    // Custom query to find all by accNbr (since composite)
    @Query("SELECT t FROM TmpAmnd t WHERE t.id.accNbr = :accNbr")
    List<TmpAmnd> findByIdAccNbr(String accNbr);

    // Exists by accNbr (partial)
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TmpAmnd t WHERE t.id.accNbr = :accNbr")
    boolean existsByIdAccNbr(String accNbr);
}