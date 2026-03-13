package com.banking.api.service;

import com.banking.api.model.dto.request.CreateBeneficiaryRequest;
import com.banking.api.model.dto.request.UpdateBeneficiaryRequest;
import com.banking.api.model.dto.response.BeneficiaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BeneficiaryService {

    BeneficiaryResponse createBeneficiary(CreateBeneficiaryRequest request, String userId);

    BeneficiaryResponse updateBeneficiary(String beneficiaryId, UpdateBeneficiaryRequest request, String userId);

    void deleteBeneficiary(String beneficiaryId, String userId);

    BeneficiaryResponse getBeneficiaryById(String beneficiaryId, String userId);

    Page<BeneficiaryResponse> getBeneficiaries(String userId, Pageable pageable);

    List<BeneficiaryResponse> getFavorites(String userId);

    Page<BeneficiaryResponse> searchBeneficiaries(String userId, String keyword, Pageable pageable);
}
