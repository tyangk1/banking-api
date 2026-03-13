package com.banking.api.service.impl;

import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.model.dto.request.CreateBeneficiaryRequest;
import com.banking.api.model.dto.request.UpdateBeneficiaryRequest;
import com.banking.api.model.dto.response.BeneficiaryResponse;
import com.banking.api.model.entity.Beneficiary;
import com.banking.api.model.entity.User;
import com.banking.api.repository.BeneficiaryRepository;
import com.banking.api.repository.UserRepository;
import com.banking.api.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    @Override
    @CacheEvict(value = "beneficiaries", key = "#userId")
    public BeneficiaryResponse createBeneficiary(CreateBeneficiaryRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check duplicate
        if (beneficiaryRepository.existsByUserIdAndAccountNumber(userId, request.getAccountNumber())) {
            throw new IllegalArgumentException("Beneficiary with account number " + request.getAccountNumber() + " already exists");
        }

        Beneficiary beneficiary = Beneficiary.builder()
                .user(user)
                .nickname(request.getNickname())
                .accountNumber(request.getAccountNumber())
                .accountHolderName(request.getAccountHolderName())
                .bankName(request.getBankName() != null ? request.getBankName() : "Premium Banking")
                .build();

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("Created beneficiary '{}' for user {}", request.getNickname(), userId);
        return toResponse(saved);
    }

    @Override
    @CacheEvict(value = "beneficiaries", key = "#userId")
    public BeneficiaryResponse updateBeneficiary(String beneficiaryId, UpdateBeneficiaryRequest request, String userId) {
        Beneficiary beneficiary = findByIdAndUser(beneficiaryId, userId);

        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            beneficiary.setNickname(request.getNickname());
        }
        if (request.getFavorite() != null) {
            beneficiary.setFavorite(request.getFavorite());
        }

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        log.info("Updated beneficiary {} for user {}", beneficiaryId, userId);
        return toResponse(updated);
    }

    @Override
    @CacheEvict(value = "beneficiaries", key = "#userId")
    public void deleteBeneficiary(String beneficiaryId, String userId) {
        Beneficiary beneficiary = findByIdAndUser(beneficiaryId, userId);
        beneficiaryRepository.delete(beneficiary);
        log.info("Deleted beneficiary {} for user {}", beneficiaryId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaryResponse getBeneficiaryById(String beneficiaryId, String userId) {
        return toResponse(findByIdAndUser(beneficiaryId, userId));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "beneficiaries", key = "#userId + '-page-' + #pageable.pageNumber")
    public Page<BeneficiaryResponse> getBeneficiaries(String userId, Pageable pageable) {
        return beneficiaryRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getFavorites(String userId) {
        return beneficiaryRepository.findByUserIdAndFavoriteTrue(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BeneficiaryResponse> searchBeneficiaries(String userId, String keyword, Pageable pageable) {
        return beneficiaryRepository.searchByUserIdAndKeyword(userId, keyword, pageable)
                .map(this::toResponse);
    }

    // ==================== Private Helpers ====================

    private Beneficiary findByIdAndUser(String beneficiaryId, String userId) {
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary", "id", beneficiaryId));
        if (!beneficiary.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Beneficiary", "id", beneficiaryId);
        }
        return beneficiary;
    }

    private BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .nickname(beneficiary.getNickname())
                .accountNumber(beneficiary.getAccountNumber())
                .accountHolderName(beneficiary.getAccountHolderName())
                .bankName(beneficiary.getBankName())
                .verified(beneficiary.isVerified())
                .favorite(beneficiary.isFavorite())
                .transferCount(beneficiary.getTransferCount())
                .lastUsedAt(beneficiary.getLastUsedAt())
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }
}
