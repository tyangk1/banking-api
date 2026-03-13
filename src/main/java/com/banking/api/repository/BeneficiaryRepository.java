package com.banking.api.repository;

import com.banking.api.model.entity.Beneficiary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, String> {

    Page<Beneficiary> findByUserId(String userId, Pageable pageable);

    List<Beneficiary> findByUserIdAndFavoriteTrue(String userId);

    Optional<Beneficiary> findByUserIdAndAccountNumber(String userId, String accountNumber);

    @Query("SELECT b FROM Beneficiary b WHERE b.user.id = :userId AND " +
           "(LOWER(b.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.accountHolderName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "b.accountNumber LIKE CONCAT('%', :keyword, '%'))")
    Page<Beneficiary> searchByUserIdAndKeyword(
            @Param("userId") String userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    long countByUserId(String userId);

    boolean existsByUserIdAndAccountNumber(String userId, String accountNumber);
}
