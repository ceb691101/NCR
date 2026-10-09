package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreInvRdngs;
import com.example.SPSProjectBackend.model.NcreInvRdngsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NcreInvRdngsRepository extends JpaRepository<NcreInvRdngs, NcreInvRdngsId> {

    // Retrieve readings by account number and area code (trimmed), excluding finalized invoices (where ncre_invoice_create has is_create = 1)
    @Query("SELECT n FROM NcreInvRdngs n WHERE TRIM(n.accNbr) = TRIM(:accNbr) AND TRIM(n.areaCd) = TRIM(:areaCd) " +
           "AND NOT EXISTS (SELECT ic FROM NcreInvoiceCreate ic, NcreDeveloper d WHERE TRIM(d.accNbr) = TRIM(n.accNbr) AND ic.folioNo = d.folioNo AND CAST(ic.billCycle AS string) = TRIM(n.addedBlcy) AND ic.isCreate = 1) " +
           "ORDER BY n.addedBlcy DESC")
    List<NcreInvRdngs> findByAccNbrAndAreaCdTrimmed(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd);

    // Retrieve readings by area code and bill cycle (trimmed), excluding finalized invoices (strictly where ncre_invoice_create has is_create = 0)
    @Query("SELECT n FROM NcreInvRdngs n WHERE TRIM(n.areaCd) = TRIM(:areaCd) AND TRIM(n.addedBlcy) = TRIM(:addedBlcy) " +
           "AND EXISTS (SELECT ic FROM NcreInvoiceCreate ic, NcreDeveloper d WHERE TRIM(d.accNbr) = TRIM(n.accNbr) AND ic.folioNo = d.folioNo AND CAST(ic.billCycle AS string) = TRIM(n.addedBlcy) AND ic.isCreate = 0) " +
           "ORDER BY n.accNbr")
    List<NcreInvRdngs> findByAreaCdAndAddedBlcyTrimmed(@Param("areaCd") String areaCd, @Param("addedBlcy") String addedBlcy);

    // Retrieve readings by account, area, and bill cycle (trimmed), excluding finalized invoices (strictly where ncre_invoice_create has is_create = 0)
    @Query("SELECT n FROM NcreInvRdngs n WHERE TRIM(n.accNbr) = TRIM(:accNbr) AND TRIM(n.areaCd) = TRIM(:areaCd) AND TRIM(n.addedBlcy) = TRIM(:addedBlcy) " +
           "AND EXISTS (SELECT ic FROM NcreInvoiceCreate ic, NcreDeveloper d WHERE TRIM(d.accNbr) = TRIM(n.accNbr) AND ic.folioNo = d.folioNo AND CAST(ic.billCycle AS string) = TRIM(n.addedBlcy) AND ic.isCreate = 0)")
    List<NcreInvRdngs> findByAccNbrAndAreaCdAndAddedBlcyTrimmed(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd, @Param("addedBlcy") String addedBlcy);

    // Retrieve readings by area code (trimmed), excluding finalized invoices (strictly where ncre_invoice_create has is_create = 0)
    @Query("SELECT n FROM NcreInvRdngs n WHERE TRIM(n.areaCd) = TRIM(:areaCd) " +
           "AND EXISTS (SELECT ic FROM NcreInvoiceCreate ic, NcreDeveloper d WHERE TRIM(d.accNbr) = TRIM(n.accNbr) AND ic.folioNo = d.folioNo AND CAST(ic.billCycle AS string) = TRIM(n.addedBlcy) AND ic.isCreate = 0) " +
           "ORDER BY n.accNbr")
    List<NcreInvRdngs> findByAreaCdTrimmed(@Param("areaCd") String areaCd);

    // Retrieve ALL readings by area code (trimmed), INCLUDING finalized invoices
    @Query("SELECT n FROM NcreInvRdngs n WHERE TRIM(n.areaCd) = TRIM(:areaCd) ORDER BY n.accNbr")
    List<NcreInvRdngs> findAllByAreaCdTrimmed(@Param("areaCd") String areaCd);

    // Retrieve ALL readings by account number and area code (trimmed), INCLUDING finalized invoices (for previous reading lookups)
    @Query("SELECT n FROM NcreInvRdngs n WHERE TRIM(n.accNbr) = TRIM(:accNbr) AND TRIM(n.areaCd) = TRIM(:areaCd) ORDER BY n.addedBlcy DESC")
    List<NcreInvRdngs> findAllByAccNbrAndAreaCdTrimmed(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd);

    // Retrieve ALL readings by area code and bill cycle (trimmed), INCLUDING finalized invoices (for previous reading lookups)
    @Query("SELECT n FROM NcreInvRdngs n WHERE TRIM(n.areaCd) = TRIM(:areaCd) AND TRIM(n.addedBlcy) = TRIM(:addedBlcy) ORDER BY n.accNbr")
    List<NcreInvRdngs> findAllByAreaCdAndAddedBlcyTrimmed(@Param("areaCd") String areaCd, @Param("addedBlcy") String addedBlcy);

       @Query("SELECT n FROM NcreInvRdngs n WHERE n.areaCd IN :areaCodes AND n.addedBlcy = :addedBlcy ORDER BY n.areaCd, n.accNbr")
       List<NcreInvRdngs> findAllByAreaCodesAndAddedBlcyTrimmed(@Param("areaCodes") List<String> areaCodes,
                                                                                                           @Param("addedBlcy") String addedBlcy);

    // Retrieve ALL readings by account, area, and bill cycle (trimmed), INCLUDING finalized invoices (for previous reading lookups)
    @Query("SELECT n FROM NcreInvRdngs n WHERE TRIM(n.accNbr) = TRIM(:accNbr) AND TRIM(n.areaCd) = TRIM(:areaCd) AND TRIM(n.addedBlcy) = TRIM(:addedBlcy)")
    List<NcreInvRdngs> findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd, @Param("addedBlcy") String addedBlcy);

    // Retrieve ALL (areaCd, addedBlcy, accNbr) tuples in a single bulk query for dashboard card calculation
    @Query("SELECT DISTINCT n.areaCd, n.addedBlcy, n.accNbr FROM NcreInvRdngs n WHERE n.accNbr IS NOT NULL AND n.areaCd IS NOT NULL AND n.addedBlcy IS NOT NULL")
    List<Object[]> findAllActiveReadingsTuples();

     /**
     * Check if customer has readings for specific bill cycle
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NcreInvRdngs n " +
           "WHERE n.accNbr = :accNbr AND TRIM(n.areaCd) = TRIM(:areaCd) AND TRIM(n.addedBlcy) = TRIM(:billCycle)")
    boolean hasReadingsForBillCycle(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd, 
                                   @Param("billCycle") String billCycle);
    
}
