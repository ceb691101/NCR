package com.example.SPSProjectBackend.service;

 import com.example.SPSProjectBackend.repository.PcesthmtRepository;
 import com.example.SPSProjectBackend.service.EstimateResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;

@Service
public class PcesthmtService {

     @Autowired
     private PcesthmtRepository pcesthmtRepository;

     public List<String> getAllEstimateNumbers() {
         return pcesthmtRepository.findAllEstimateNumbers();
     }

     public EstimateResponse checkEstimateAndGetDate(String estimateNo) {
         Object[] result = pcesthmtRepository.findEstimateAndProjectDates(estimateNo);

         System.out.println("Raw Query Result: " + Arrays.toString(result)); // Debug log

         boolean exists = result != null && result.length > 0;
         String formattedEtimateDt = null;
         String formattedPrjAssDt = null;

         if (exists) {
             // Extract the inner array (first row of the result)
             Object[] row = (Object[]) result[0];

             // Process each element of the row
             if (row[0] instanceof String) {
                 formattedEtimateDt = (String) row[0];
             }
             if (row.length > 1 && row[1] instanceof String) {
                 formattedPrjAssDt = (String) row[1];
             }
         }

         System.out.println("Query Result: " + Arrays.toString(result));
         return new EstimateResponse(exists, formattedEtimateDt, formattedPrjAssDt);
     }
 }
