package com.vendorcompliance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendorcompliance.entity.AuditFlag;

public interface AuditFlagRepository extends JpaRepository<AuditFlag, Long> {
    List<AuditFlag> findByVendorIdAndResolvedFalse(Long vendorId);

    long countByVendorIdAndResolvedFalse(Long vendorId);
}
