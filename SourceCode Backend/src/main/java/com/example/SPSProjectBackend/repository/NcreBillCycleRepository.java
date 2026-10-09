package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreBillCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NcreBillCycleRepository extends JpaRepository<NcreBillCycle, Short> {

    @Query("SELECT bc FROM NcreBillCycle bc WHERE bc.isCurrent = 1 ORDER BY bc.billCycle DESC")
    List<NcreBillCycle> findCurrentBillCycles();

    @Query(value = "SELECT * FROM dbadmin.ncre_bill_cycle WHERE is_current = 1 ORDER BY bill_cycle DESC", nativeQuery = true)
    List<NcreBillCycle> findCurrentBillCyclesNative();

    @Query(value = "SELECT bill_cycle FROM dbadmin.ncre_bill_cycle WHERE is_current = 1 ORDER BY bill_cycle DESC", nativeQuery = true)
    List<Number> findCurrentBillCycleNumbersNative();

    default Optional<NcreBillCycle> findCurrentBillCycle() {
        try {
            List<NcreBillCycle> list = findCurrentBillCycles();
            if (list != null && !list.isEmpty()) {
                return Optional.of(list.get(0));
            }
        } catch (Exception ignored) {}

        try {
            List<NcreBillCycle> nativeList = findCurrentBillCyclesNative();
            if (nativeList != null && !nativeList.isEmpty()) {
                return Optional.of(nativeList.get(0));
            }
        } catch (Exception ignored) {}

        return Optional.empty();
    }

    default Optional<Integer> findCurrentBillCycleNumber() {
        Optional<NcreBillCycle> cycleOpt = findCurrentBillCycle();
        if (cycleOpt.isPresent() && cycleOpt.get().getBillCycle() != null) {
            return Optional.of(cycleOpt.get().getBillCycle().intValue());
        }
        try {
            List<Number> numbers = findCurrentBillCycleNumbersNative();
            if (numbers != null && !numbers.isEmpty() && numbers.get(0) != null) {
                return Optional.of(numbers.get(0).intValue());
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }
}
