package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.UserAccSecInfoDTO;
import com.example.SPSProjectBackend.model.NcreRoleFunc;
import com.example.SPSProjectBackend.model.NcreRoleFuncId;
import com.example.SPSProjectBackend.model.SecInfoSessionData;
import com.example.SPSProjectBackend.model.UserAccSecInfo;
import com.example.SPSProjectBackend.repository.NcreRoleFuncRepository;
import com.example.SPSProjectBackend.repository.SecInfoSessionRepository;
import com.example.SPSProjectBackend.repository.UserAccSecInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserAccSecInfoService {

    @Autowired
    private UserAccSecInfoRepository userAccSecInfoRepository;

    @Autowired
    private NcreRoleFuncRepository ncreRoleFuncRepository;

    @Autowired
    private SecInfoSessionRepository secInfoSessionRepository;

    /**
     * Validates the region/province/area hierarchy.
     *
     * A NULL code widens the user's scope to everything below it, so a province code is
     * meaningless without a region, and an area code is meaningless without a province.
     * Any other combination is valid, regardless of the user category.
     */
    private void validateLocationCodes(UserAccSecInfoDTO userAccSecInfoDTO) {
        String regionCode = UserAreaPermissionService.normalizeCode(userAccSecInfoDTO.getRegionCode());
        String provinceCode = UserAreaPermissionService.normalizeCode(userAccSecInfoDTO.getProvinceCode());
        String areaCode = UserAreaPermissionService.normalizeCode(userAccSecInfoDTO.getAreaCode());

        if (regionCode == null && (provinceCode != null || areaCode != null)) {
            throw new RuntimeException(
                    "Region code is required when a province or area code is set (a null region grants all regions)");
        }
        if (provinceCode == null && areaCode != null) {
            throw new RuntimeException(
                    "Province code is required when an area code is set (a null province grants all provinces in the region)");
        }
    }

    // Get all user accounts (including status)
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getAllUserAccSecInfos() {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findAllNcreUsers();
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user accounts: " + e.getMessage(), e);
        }
    }

    // Get all active users only
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getAllActiveUsers() {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findAllActiveUsers();
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve active user accounts: " + e.getMessage(), e);
        }
    }

    // Get all inactive users only
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getAllInactiveUsers() {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findAllInactiveUsers();
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve inactive user accounts: " + e.getMessage(), e);
        }
    }

    // Get user account by ID - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public Optional<UserAccSecInfoDTO> getUserAccSecInfoById(String userId) {
        try {
            // Use trimmed version for lookup
            Optional<UserAccSecInfo> account = userAccSecInfoRepository.findByIdTrimmed(userId);
            return account.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user account with ID: " + userId + " - " + e.getMessage(), e);
        }
    }

    // Create new user account (status defaults to 1 - active, class defaults to 1)
    @Transactional
    public UserAccSecInfoDTO createUserAccSecInfo(UserAccSecInfoDTO userAccSecInfoDTO, String sessionId) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                throw new RuntimeException("A valid login session is required to register a user");
            }
            SecInfoSessionData registeringSession = secInfoSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("A valid login session is required to register a user"));
            UserAccSecInfo registeringUser = userAccSecInfoRepository.findByIdTrimmed(registeringSession.getUserId())
                    .filter(user -> user.getStatus() != null && user.getStatus() == 1)
                    .orElseThrow(() -> new RuntimeException("The registering user is not active"));
            String registeringEpf = registeringUser.getEpfNum();
            if (registeringEpf == null || registeringEpf.trim().isEmpty()) {
                throw new RuntimeException("The registering user's EPF number is required to register a user");
            }

            // Validate required fields
            if (userAccSecInfoDTO.getUserId() == null || userAccSecInfoDTO.getUserId().trim().isEmpty()) {
                throw new RuntimeException("User ID is required");
            }
            if (userAccSecInfoDTO.getUserId().trim().length() > 10) {
                throw new RuntimeException("User ID must not exceed 10 characters");
            }
            
            if (userAccSecInfoDTO.getUserName() == null || userAccSecInfoDTO.getUserName().trim().isEmpty()) {
                throw new RuntimeException("User name is required");
            }
            
            if (userAccSecInfoDTO.getUserCat() == null || userAccSecInfoDTO.getUserCat().trim().isEmpty()) {
                throw new RuntimeException("User category is required");
            }

            // Validate EPF number (required)
            if (userAccSecInfoDTO.getEpfNum() == null || userAccSecInfoDTO.getEpfNum().trim().isEmpty()) {
                throw new RuntimeException("EPF number is required");
            }

            // Validate location codes based on user category
            validateLocationCodes(userAccSecInfoDTO);

            // Check if user ID already exists - USING TRIMMED VERSION
            if (userAccSecInfoRepository.existsByUserIdTrimmed(userAccSecInfoDTO.getUserId())) {
                throw new RuntimeException("User ID already exists: " + userAccSecInfoDTO.getUserId());
            }

            // Check if username already exists - USING TRIMMED VERSION
            if (userAccSecInfoRepository.existsByUserNameTrimmed(userAccSecInfoDTO.getUserName())) {
                throw new RuntimeException("Username already exists: " + userAccSecInfoDTO.getUserName());
            }

            // Check if EPF number already exists - USING TRIMMED VERSION
            if (userAccSecInfoRepository.existsByEpfNumTrimmed(userAccSecInfoDTO.getEpfNum())) {
                throw new RuntimeException("EPF number already exists: " + userAccSecInfoDTO.getEpfNum());
            }

            // Convert DTO to Entity (trimming happens automatically in entity setters)
            UserAccSecInfo userAccSecInfo = new UserAccSecInfo();
            userAccSecInfo.setUserId(userAccSecInfoDTO.getUserId());
            userAccSecInfo.setUserName(userAccSecInfoDTO.getUserName());
            userAccSecInfo.setUserCat(userAccSecInfoDTO.getUserCat());
            userAccSecInfo.setEpfNum(userAccSecInfoDTO.getEpfNum());
            
            // Set status to active (1) for new users - user doesn't need to pass status
            userAccSecInfo.setStatus(1);
            
            // Set class field to 1 always - this field is not used by our application
            userAccSecInfo.setClassField(1);
            
            // Set location codes based on user category
            setLocationCodesOnEntity(userAccSecInfo, userAccSecInfoDTO);
            
            userAccSecInfo.setPasswd(UUID.randomUUID().toString());

            UserAccSecInfo savedAccount = userAccSecInfoRepository.save(userAccSecInfo);
            
            // Force flush to ensure data is saved
            userAccSecInfoRepository.flush();

            String newUserId = userAccSecInfo.getUserId();
            NcreRoleFuncId roleFuncId = new NcreRoleFuncId(newUserId, "DB", "NCR", "DASBRD");
            if (!ncreRoleFuncRepository.existsById(roleFuncId)) {
                NcreRoleFunc roleFunc = new NcreRoleFunc();
                roleFunc.setUserId(newUserId);
                roleFunc.setFuncId("DB");
                roleFunc.setApplId("NCR");
                roleFunc.setSubFuncId("DASBRD");
                roleFunc.setStatus("2");
                roleFunc.setEntBy(registeringEpf);
                roleFunc.setEntDt(Date.valueOf(LocalDate.now()));
                roleFunc.setModiBy(null);
                roleFunc.setModiDt(null);
                ncreRoleFuncRepository.save(roleFunc);
                ncreRoleFuncRepository.flush();
            }
            
            return convertToDTO(savedAccount);
        } catch (RuntimeException e) {
            throw e; // Re-throw runtime exceptions as-is
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user account: " + e.getMessage(), e);
        }
    }

    /**
     * Stores the permission codes exactly as supplied.
     *
     * The codes define the user's area scope, where NULL means "all of the level below",
     * so they must not be blanked out based on the user category.
     */
    private void setLocationCodesOnEntity(UserAccSecInfo entity, UserAccSecInfoDTO dto) {
        entity.setRegionCode(UserAreaPermissionService.normalizeCode(dto.getRegionCode()));
        entity.setProvinceCode(UserAreaPermissionService.normalizeCode(dto.getProvinceCode()));
        entity.setAreaCode(UserAreaPermissionService.normalizeCode(dto.getAreaCode()));
    }

    // Update user account
    public UserAccSecInfoDTO updateUserAccSecInfo(String userId, UserAccSecInfoDTO userAccSecInfoDTO) {
        try {
            // Use trimmed version for lookup
            UserAccSecInfo existingAccount = userAccSecInfoRepository.findByIdTrimmed(userId)
                    .orElseThrow(() -> new RuntimeException("User account not found with ID: " + userId));

            // Update fields
            if (userAccSecInfoDTO.getUserName() != null && !userAccSecInfoDTO.getUserName().isEmpty()) {
                // Check if new username already exists (excluding current user) - USING TRIMMED VERSION
                Optional<UserAccSecInfo> userWithSameName = userAccSecInfoRepository.findByUserNameTrimmed(userAccSecInfoDTO.getUserName());
                if (userWithSameName.isPresent() && !userWithSameName.get().getUserId().equals(userId)) {
                    throw new RuntimeException("Username already exists: " + userAccSecInfoDTO.getUserName());
                }
                existingAccount.setUserName(userAccSecInfoDTO.getUserName());
            }

            if (userAccSecInfoDTO.getUserCat() != null && !userAccSecInfoDTO.getUserCat().isEmpty()) {
                existingAccount.setUserCat(userAccSecInfoDTO.getUserCat());
                
                // Validate and update location codes based on new user category
                validateLocationCodes(userAccSecInfoDTO);
                setLocationCodesOnEntity(existingAccount, userAccSecInfoDTO);
            }

            // Update status if provided
            if (userAccSecInfoDTO.getStatus() != null) {
                if (userAccSecInfoDTO.getStatus() == 0 || userAccSecInfoDTO.getStatus() == 1) {
                    existingAccount.setStatus(userAccSecInfoDTO.getStatus());
                } else {
                    throw new RuntimeException("Status must be 0 (inactive) or 1 (active)");
                }
            }

            // Always ensure class field is set to 1 (even if someone tries to change it)
            existingAccount.setClassField(1);

            UserAccSecInfo updatedAccount = userAccSecInfoRepository.save(existingAccount);
            
            // Force flush to ensure data is saved
            userAccSecInfoRepository.flush();
            
            return convertToDTO(updatedAccount);
        } catch (RuntimeException e) {
            throw e; // Re-throw runtime exceptions as-is
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user account: " + e.getMessage(), e);
        }
    }

    // NEW METHOD: Toggle user status (activate/deactivate)
    @Transactional
    public UserAccSecInfoDTO toggleUserStatus(String userId) {
        try {
            // Use trimmed version for lookup
            UserAccSecInfo existingAccount = userAccSecInfoRepository.findByIdTrimmed(userId)
                    .orElseThrow(() -> new RuntimeException("User account not found with ID: " + userId));

            // Toggle status: 1 -> 0, 0 -> 1
            Integer newStatus = existingAccount.getStatus() == 1 ? 0 : 1;
            existingAccount.setStatus(newStatus);

            // Ensure class field remains 1
            existingAccount.setClassField(1);

            UserAccSecInfo updatedAccount = userAccSecInfoRepository.save(existingAccount);
            userAccSecInfoRepository.flush();
            
            return convertToDTO(updatedAccount);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to toggle user status: " + e.getMessage(), e);
        }
    }

    // NEW METHOD: Set user status explicitly
    @Transactional
    public UserAccSecInfoDTO setUserStatus(String userId, Integer status) {
        try {
            if (status != 0 && status != 1) {
                throw new RuntimeException("Status must be 0 (inactive) or 1 (active)");
            }

            // Use trimmed version for lookup
            UserAccSecInfo existingAccount = userAccSecInfoRepository.findByIdTrimmed(userId)
                    .orElseThrow(() -> new RuntimeException("User account not found with ID: " + userId));

            existingAccount.setStatus(status);

            // Ensure class field remains 1
            existingAccount.setClassField(1);

            UserAccSecInfo updatedAccount = userAccSecInfoRepository.save(existingAccount);
            userAccSecInfoRepository.flush();
            
            return convertToDTO(updatedAccount);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user status: " + e.getMessage(), e);
        }
    }

    // Check if user is active - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public boolean isUserActive(String userId) {
        try {
            return userAccSecInfoRepository.isUserActiveByIdTrimmed(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check user status: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getUsersByCategory(String category) {
        try {
            List<UserAccSecInfo> accounts;
            if ("Electrical Engineer".equalsIgnoreCase(category) || "EE".equalsIgnoreCase(category)) {
                accounts = new java.util.ArrayList<>(userAccSecInfoRepository.findByUserCat("Electrical Engineer"));
                accounts.addAll(userAccSecInfoRepository.findByUserCat("EE"));
            } else {
                accounts = userAccSecInfoRepository.findByUserCat(category);
            }
            return accounts.stream()
                    .distinct()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve users by category: " + e.getMessage(), e);
        }
    }

    // Get users by region code
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getUsersByRegionCode(String regionCode) {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findByRegionCode(regionCode);
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve users by region code: " + e.getMessage(), e);
        }
    }

    // Get users by province code
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getUsersByProvinceCode(String provinceCode) {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findByProvinceCode(provinceCode);
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve users by province code: " + e.getMessage(), e);
        }
    }

    // Get users by area code
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getUsersByAreaCode(String areaCode) {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findByAreaCode(areaCode);
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve users by area code: " + e.getMessage(), e);
        }
    }

    // Get user by EPF number - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public Optional<UserAccSecInfoDTO> getUserByEpfNum(String epfNum) {
        try {
            Optional<UserAccSecInfo> account = userAccSecInfoRepository.findByEpfNumTrimmed(epfNum);
            return account.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user by EPF number: " + e.getMessage(), e);
        }
    }

    // NEW METHOD: Check if user can login (separate from password validation) - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public boolean canUserLogin(String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return false;
            }
            return userAccSecInfoRepository.isUserActiveByIdTrimmed(userId.trim());
        } catch (Exception e) {
            System.err.println("Failed to check if user can login: " + e.getMessage());
            return false;
        }
    }

    // Helper method to convert Entity to DTO (updated to include status)
    private UserAccSecInfoDTO convertToDTO(UserAccSecInfo userAccSecInfo) {
        try {
            if (userAccSecInfo == null) {
                return null;
            }
            
            UserAccSecInfoDTO dto = new UserAccSecInfoDTO();
            dto.setUserId(userAccSecInfo.getUserId()); // Already trimmed by getter
            dto.setUserName(userAccSecInfo.getUserName()); // Already trimmed by getter
            // Don't include password in DTO for security
            dto.setUserCat(userAccSecInfo.getUserCat()); // Already trimmed by getter
            // Include location codes in DTO
            dto.setRegionCode(userAccSecInfo.getRegionCode()); // Already trimmed by getter
            dto.setProvinceCode(userAccSecInfo.getProvinceCode()); // Already trimmed by getter
            dto.setAreaCode(userAccSecInfo.getAreaCode()); // Already trimmed by getter
            // Include EPF number in DTO
            dto.setEpfNum(userAccSecInfo.getEpfNum()); // Already trimmed by getter
            // Include status in DTO
            dto.setStatus(userAccSecInfo.getStatus());
            // Note: class field is not included in DTO as it's not used by frontend
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert entity to DTO: " + e.getMessage(), e);
        }
    }
}