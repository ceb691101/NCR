package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreFuncm;
import com.example.SPSProjectBackend.model.NcreRoleFunc;
import com.example.SPSProjectBackend.model.NcreRoleFuncId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NcreRoleFuncRepository extends JpaRepository<NcreRoleFunc, NcreRoleFuncId> {

       @Query("SELECT rf FROM NcreRoleFunc rf WHERE TRIM(rf.userId) = TRIM(:userId) AND TRIM(rf.applId) = TRIM(:applId)")
       List<NcreRoleFunc> findByTrimmedUserIdAndTrimmedApplId(@Param("userId") String userId,
                                                                                                         @Param("applId") String applId);

    @Query("SELECT f FROM NcreFuncm f, NcreRoleFunc rf " +
           "WHERE TRIM(rf.funcId) = TRIM(f.funcId) " +
           "  AND TRIM(rf.applId) = TRIM(f.applId) " +
           "  AND TRIM(rf.subFuncId) = TRIM(f.subFuncId) " +
           "  AND TRIM(rf.userId) = TRIM(:userId) " +
           "  AND TRIM(f.applId) = TRIM(:applId) " +
           "  AND TRIM(f.status) = '2' " +
           "  AND TRIM(rf.status) = '2' " +
           "ORDER BY f.seqNo ASC, f.funcId ASC, f.subFuncId ASC")
    List<NcreFuncm> findActiveUserPermissions(@Param("userId") String userId, @Param("applId") String applId);

    @Query("SELECT COUNT(rf) FROM NcreRoleFunc rf, NcreFuncm f " +
           "WHERE TRIM(rf.funcId) = TRIM(f.funcId) " +
           "  AND TRIM(rf.applId) = TRIM(f.applId) " +
           "  AND TRIM(rf.subFuncId) = TRIM(f.subFuncId) " +
           "  AND TRIM(rf.userId) = TRIM(:userId) " +
           "  AND TRIM(rf.funcId) = TRIM(:funcId) " +
           "  AND TRIM(rf.subFuncId) = TRIM(:subFuncId) " +
           "  AND TRIM(f.applId) = TRIM(:applId) " +
           "  AND TRIM(f.status) = '2' " +
           "  AND TRIM(rf.status) = '2'")
    long countActiveUserPermission(@Param("userId") String userId,
                                   @Param("applId") String applId,
                                   @Param("funcId") String funcId,
                                   @Param("subFuncId") String subFuncId);
}
