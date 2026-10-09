package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreFuncm;
import com.example.SPSProjectBackend.model.NcreFuncmId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NcreFuncmRepository extends JpaRepository<NcreFuncm, NcreFuncmId> {
    List<NcreFuncm> findByApplIdAndStatus(String applId, String status);

    List<NcreFuncm> findAllByOrderByApplIdAscSeqNoAscFuncIdAscSubFuncIdAsc();

    List<NcreFuncm> findByApplIdOrderBySeqNoAscFuncIdAscSubFuncIdAsc(String applId);
}
