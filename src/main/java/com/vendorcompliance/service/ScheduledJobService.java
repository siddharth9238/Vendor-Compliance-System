package com.vendorcompliance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendorcompliance.entity.AuditFlag;
import com.vendorcompliance.entity.Vendor;
import com.vendorcompliance.entity.VendorDocument;
import com.vendorcompliance.repository.AuditFlagRepository;
import com.vendorcompliance.repository.VendorDocumentRepository;
import com.vendorcompliance.repository.VendorRepository;

@Service
public class ScheduledJobService {

    private static final String SYSTEM_ACTOR = "SYSTEM";
    private static final Integer HIGH_RISK_THRESHOLD = 60;

    private final VendorDocumentRepository vendorDocumentRepository;
    private final VendorRepository vendorRepository;
    private final AuditFlagRepository auditFlagRepository;
    private final RiskService riskService;

    public ScheduledJobService(
            VendorDocumentRepository vendorDocumentRepository,
            VendorRepository vendorRepository,
            AuditFlagRepository auditFlagRepository,
            RiskService riskService
    ) {
        this.vendorDocumentRepository = vendorDocumentRepository;
        this.vendorRepository = vendorRepository;
        this.auditFlagRepository = auditFlagRepository;
        this.riskService = riskService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void dailyExpiredDocumentCheck() {
        LocalDate today = LocalDate.now();
        List<VendorDocument> expiredDocuments = vendorDocumentRepository.findExpiredDocuments(today);

        if (!expiredDocuments.isEmpty()) {
            Set<Long> vendorIdsWithExpiredDocs = expiredDocuments.stream()
                    .map(doc -> doc.getVendor().getId())
                    .collect(Collectors.toSet());

            for (Long vendorId : vendorIdsWithExpiredDocs) {
                List<VendorDocument> vendorExpiredDocs = expiredDocuments.stream()
                        .filter(doc -> doc.getVendor().getId().equals(vendorId))
                        .toList();

                String docTypes = vendorExpiredDocs.stream()
                        .map(doc -> doc.getType().name())
                        .distinct()
                        .collect(Collectors.joining(", "));

                String flagDescription = "Expired documents detected: " + docTypes;

                // Check if flag already exists for this vendor
                boolean flagAlreadyExists = auditFlagRepository.findByVendorIdAndResolvedFalse(vendorId).stream()
                        .anyMatch(flag -> flag.getDescription().startsWith("Expired documents"));

                if (!flagAlreadyExists) {
                    Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
                    if (vendor != null) {
                        AuditFlag flag = new AuditFlag();
                        flag.setVendor(vendor);
                        flag.setDescription(flagDescription);
                        flag.setResolved(false);
                        auditFlagRepository.save(flag);

                        // Recalculate risk
                        riskService.recalculateRiskForVendor(vendorId, SYSTEM_ACTOR);
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void dailyHighRiskVendorCheck() {
        List<Vendor> highRiskVendors = vendorRepository.findByRiskScoreGreaterThanEqual(HIGH_RISK_THRESHOLD);

        for (Vendor vendor : highRiskVendors) {
            // Check if alert flag already exists
            boolean flagAlreadyExists = auditFlagRepository.findByVendorIdAndResolvedFalse(vendor.getId()).stream()
                    .anyMatch(flag -> flag.getDescription().contains("High risk"));

            if (!flagAlreadyExists) {
                String flagDescription = "Vendor risk score exceeded threshold: " + vendor.getRiskScore() + "/100";
                AuditFlag flag = new AuditFlag();
                flag.setVendor(vendor);
                flag.setDescription(flagDescription);
                flag.setResolved(false);
                auditFlagRepository.save(flag);
            }
        }
    }
}
