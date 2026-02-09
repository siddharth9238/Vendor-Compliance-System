package com.vendorcompliance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendorcompliance.entity.Vendor;
import com.vendorcompliance.entity.VendorStatus;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    List<Vendor> findByStatusOrderByCreatedAtDesc(VendorStatus status);

    List<Vendor> findAllByOrderByCreatedAtDesc();

    boolean existsByRegistrationNumber(String registrationNumber);

    List<Vendor> findByRiskScoreGreaterThanEqual(Integer riskScore);
}
