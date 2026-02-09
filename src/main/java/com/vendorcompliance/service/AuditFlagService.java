package com.vendorcompliance.service;

import com.vendorcompliance.entity.AuditFlag;
import com.vendorcompliance.entity.Vendor;
import com.vendorcompliance.exception.ResourceNotFoundException;
import com.vendorcompliance.repository.AuditFlagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditFlagService {

    private final AuditFlagRepository auditFlagRepository;
    private final VendorService vendorService;
    private final RiskService riskService;

    public AuditFlagService(
            AuditFlagRepository auditFlagRepository,
            VendorService vendorService,
            RiskService riskService
    ) {
        this.auditFlagRepository = auditFlagRepository;
        this.vendorService = vendorService;
        this.riskService = riskService;
    }

    @Transactional
    public AuditFlag addAuditFlag(Long vendorId, String description, String actor) {
        Vendor vendor = vendorService.findVendorOrThrow(vendorId);

        AuditFlag flag = new AuditFlag();
        flag.setVendor(vendor);
        flag.setDescription(description);
        flag.setResolved(false);

        AuditFlag savedFlag = auditFlagRepository.save(flag);

        // Recalculate risk when new audit flag is added
        riskService.recalculateRiskForVendor(vendorId, actor);

        return savedFlag;
    }

    @Transactional
    public AuditFlag resolveAuditFlag(Long flagId, String actor) {
        AuditFlag flag = auditFlagRepository.findById(flagId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit flag not found: " + flagId));

        flag.setResolved(true);
        flag.setResolvedAt(LocalDateTime.now());
        AuditFlag savedFlag = auditFlagRepository.save(flag);

        // Recalculate risk when audit flag is resolved
        riskService.recalculateRiskForVendor(flag.getVendor().getId(), actor);

        return savedFlag;
    }

    @Transactional(readOnly = true)
    public List<AuditFlag> getUnresolvedFlags(Long vendorId) {
        return auditFlagRepository.findByVendorIdAndResolvedFalse(vendorId);
    }

    @Transactional(readOnly = true)
    public long countUnresolvedFlags(Long vendorId) {
        return auditFlagRepository.countByVendorIdAndResolvedFalse(vendorId);
    }
}
