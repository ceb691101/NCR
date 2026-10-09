package com.example.SPSProjectBackend.service;



import com.example.SPSProjectBackend.dto.ApplicantDTO;
import com.example.SPSProjectBackend.model.Applicant;
import com.example.SPSProjectBackend.repository.ApplicantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApplicantService {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Transactional
    public Applicant saveApplicant(Applicant applicant) {
        if (applicant.getFirstName() == null) {
            throw new IllegalArgumentException("First name cannot be null");
        }
        return applicantRepository.save(applicant);
    }

    // Convert Entity to DTO
    private ApplicantDTO convertToDTO(Applicant applicant) {
        ApplicantDTO dto = new ApplicantDTO();
        dto.setIdNo(applicant.getIdNo());
        dto.setIdType(applicant.getIdType());
        dto.setFirstName(applicant.getFirstName());
        dto.setLastName(applicant.getLastName());
        dto.setStreetAddress(applicant.getStreetAddress());
        dto.setSuburb(applicant.getSuburb());
        dto.setCity(applicant.getCity());
        dto.setPostalCode(applicant.getPostalCode());
        dto.setEmail(applicant.getEmail());
        dto.setTelephoneNo(applicant.getTelephoneNo());
        dto.setMobileNo(applicant.getMobileNo());
        dto.setCebEmployee(applicant.getCebEmployee());
        dto.setPreferredLanguage(applicant.getPreferredLanguage());
        dto.setStatus(applicant.getStatus());
        dto.setAddUser(applicant.getAddUser());
        dto.setAddDate(applicant.getAddDate());
        dto.setAddTime(applicant.getAddTime());
        dto.setUpdUser(applicant.getUpdUser());
        dto.setUpdDate(applicant.getUpdDate());
        dto.setUpdTime(applicant.getUpdTime());
        dto.setEntitledForLoan(applicant.getEntitledForLoan());
        dto.setMemberOfSamurdhi(applicant.getMemberOfSamurdhi());
        dto.setSamurdhiId(applicant.getSamurdhiId());
        dto.setSharePrice(applicant.getSharePrice());
        dto.setNoOfShares(applicant.getNoOfShares());
        dto.setLoanReference(applicant.getLoanReference());
        dto.setLoanAmount(applicant.getLoanAmount());
        dto.setCompanyName(applicant.getCompanyName());
        dto.setDeptId(applicant.getDeptId());
        dto.setFullName(applicant.getFullName());
        dto.setPersonalCorporate(applicant.getPersonalCorporate());
        return dto;
    }

    // Convert DTO to Entity
    private Applicant convertToEntity(ApplicantDTO dto) {
        Applicant applicant = new Applicant();
        applicant.setIdNo(dto.getIdNo());
        applicant.setIdType(dto.getIdType());
        applicant.setFirstName(dto.getFirstName());
        applicant.setLastName(dto.getLastName());
        applicant.setStreetAddress(dto.getStreetAddress());
        applicant.setSuburb(dto.getSuburb());
        applicant.setCity(dto.getCity());
        applicant.setPostalCode(dto.getPostalCode());
        applicant.setEmail(dto.getEmail());
        applicant.setTelephoneNo(dto.getTelephoneNo());
        applicant.setMobileNo(dto.getMobileNo());
        applicant.setCebEmployee(dto.getCebEmployee());
        applicant.setPreferredLanguage(dto.getPreferredLanguage());
        applicant.setStatus(dto.getStatus());
        applicant.setAddUser(dto.getAddUser());
        applicant.setAddDate(dto.getAddDate());
        applicant.setAddTime(dto.getAddTime());
        applicant.setUpdUser(dto.getUpdUser());
        applicant.setUpdDate(dto.getUpdDate());
        applicant.setUpdTime(dto.getUpdTime());
        applicant.setEntitledForLoan(dto.getEntitledForLoan());
        applicant.setMemberOfSamurdhi(dto.getMemberOfSamurdhi());
        applicant.setSamurdhiId(dto.getSamurdhiId());
        applicant.setSharePrice(dto.getSharePrice());
        applicant.setNoOfShares(dto.getNoOfShares());
        applicant.setLoanReference(dto.getLoanReference());
        applicant.setLoanAmount(dto.getLoanAmount());
        applicant.setCompanyName(dto.getCompanyName());
        applicant.setDeptId(dto.getDeptId());
        applicant.setFullName(dto.getFullName());
        applicant.setPersonalCorporate(dto.getPersonalCorporate());
        return applicant;
    }

    // Get all applicants
    public List<ApplicantDTO> getAllApplicants() {
        List<Applicant> applicants = applicantRepository.findAll();
        return applicants.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get applicant by ID
    public Optional<ApplicantDTO> getApplicantById(String idNo) {
//      Optional<Applicant> applicant = applicantRepository.findById(String.valueOf(Integer.valueOf(idNo)));
//       return applicant.map(this::convertToDTO);
        // return applicantRepository.findByIdNo(idNo);
        try {
            Long applicantId = Long.parseLong(idNo);
            Optional<Applicant> applicant = applicantRepository.findById(String.valueOf(applicantId));
            return applicant.map(this::convertToDTO);
        } catch (NumberFormatException e) {
            // Handle the exception
            throw new IllegalArgumentException("Invalid applicant ID: " + idNo, e);
        }
    }

    // Save applicant
    public ApplicantDTO saveApplicant(ApplicantDTO applicantDTO) {
        Applicant applicant = convertToEntity(applicantDTO);
        return convertToDTO(applicantRepository.save(applicant));
    }

    // Get applicant info with NIC
    public Optional<ApplicantDTO> getApplicantByIdNo(String idNo) {
        Optional<Applicant> applicant = applicantRepository.findByIdNo(idNo);
        return applicant.map(this::convertToDTO);
    }



    // Delete applicant
    public void deleteApplicant(String idNo) {
        applicantRepository.deleteById(String.valueOf(Integer.valueOf(idNo)));
    }


    // Update applicant details
    public ApplicantDTO updateApplicant(String idNo, ApplicantDTO applicantDTO) {
        // Check if applicant exists
        Optional<Applicant> existingApplicantOptional = applicantRepository.findById(idNo);

        if (existingApplicantOptional.isPresent()) {
            Applicant existingApplicant = existingApplicantOptional.get();

            // Update existing applicant fields
            existingApplicant.setFirstName(applicantDTO.getFirstName());
            existingApplicant.setLastName(applicantDTO.getLastName());
            existingApplicant.setStreetAddress(applicantDTO.getStreetAddress());
            existingApplicant.setSuburb(applicantDTO.getSuburb());
            existingApplicant.setCity(applicantDTO.getCity());
            existingApplicant.setPostalCode(applicantDTO.getPostalCode());
            existingApplicant.setEmail(applicantDTO.getEmail());
            existingApplicant.setTelephoneNo(applicantDTO.getTelephoneNo());
            existingApplicant.setMobileNo(applicantDTO.getMobileNo());
            existingApplicant.setCebEmployee(applicantDTO.getCebEmployee());
            existingApplicant.setPreferredLanguage(applicantDTO.getPreferredLanguage());
            existingApplicant.setStatus(applicantDTO.getStatus());
            existingApplicant.setAddUser(applicantDTO.getAddUser());
            existingApplicant.setAddDate(applicantDTO.getAddDate());
            existingApplicant.setAddTime(applicantDTO.getAddTime());
            existingApplicant.setUpdUser(applicantDTO.getUpdUser());
            existingApplicant.setUpdDate(applicantDTO.getUpdDate());
            existingApplicant.setUpdTime(applicantDTO.getUpdTime());
            existingApplicant.setEntitledForLoan(applicantDTO.getEntitledForLoan());
            existingApplicant.setMemberOfSamurdhi(applicantDTO.getMemberOfSamurdhi());
            existingApplicant.setSamurdhiId(applicantDTO.getSamurdhiId());
            existingApplicant.setSharePrice(applicantDTO.getSharePrice());
            existingApplicant.setNoOfShares(applicantDTO.getNoOfShares());
            existingApplicant.setLoanReference(applicantDTO.getLoanReference());
            existingApplicant.setLoanAmount(applicantDTO.getLoanAmount());
            existingApplicant.setCompanyName(applicantDTO.getCompanyName());
            existingApplicant.setDeptId(applicantDTO.getDeptId());
            existingApplicant.setFullName(applicantDTO.getFullName());
            existingApplicant.setPersonalCorporate(applicantDTO.getPersonalCorporate());

            // Save and return the updated applicant
            return convertToDTO(applicantRepository.save(existingApplicant));
        } else {
            // Applicant not found, throw exception or return an appropriate response
            throw new RuntimeException("Applicant not found with ID: " + idNo);
        }
    }
}