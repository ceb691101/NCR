package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.dto.ApplicationDTO;
import com.example.SPSProjectBackend.model.ApplicationModel;
import com.example.SPSProjectBackend.model.ApplicationModelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationModel, ApplicationModelId> {
    Optional<ApplicationModel> findById_ApplicationId(String applicationId);

    @Query("SELECT a.applicationNo FROM ApplicationModel a")
    List<String> findAllApplicationNos();

    @Query("SELECT COUNT(a) > 0 FROM ApplicationModel a WHERE a.applicationNo = :applicationNo")
    boolean existsByApplicationNo(String applicationNo);
  
    @Query(value = """
        SELECT a.APPLICATION_NO, a.DEPT_ID, a.APPLICATION_TYPE, ap.FULL_NAME, a.DESCRIPTION
        FROM SPSNEW.APPLICATIONS a
        JOIN SPSNEW.APPLICANT ap ON TRIM(a.ID_NO) = ap.ID_NO
        JOIN SPSNEW.WIRING_LAND_DETAIL_CON con ON TRIM(a.APPLICATION_ID) = con.APPLICATION_ID
        JOIN SPSNEW.APPLICATION_REFERENCE ref ON TRIM(a.APPLICATION_ID) = ref.APPLICATION_ID
        WHERE TRIM(a.APPLICATION_NO) = :applicationNo
        ORDER BY a.APPLICATION_NO
        """, nativeQuery = true)
    List<Object[]> findApplicationDetailsByApplicationNo(@Param("applicationNo") String applicationNo);

    @Query("SELECT a.status, COUNT(a) FROM ApplicationModel a WHERE a.deptId = :deptId GROUP BY a.status")
    List<Object[]> getStatusCountsByDeptId(@Param("deptId") String deptId);

}