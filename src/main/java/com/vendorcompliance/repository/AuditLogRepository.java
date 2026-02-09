package com.vendorcompliance.repository;

import com.vendorcompliance.entity.AuditAction;
import com.vendorcompliance.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByVendorIdOrderByCreatedAtDesc(Long vendorId);

    List<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action);

    List<AuditLog> findByVendorIdAndActionOrderByCreatedAtDesc(Long vendorId, AuditAction action);

    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
