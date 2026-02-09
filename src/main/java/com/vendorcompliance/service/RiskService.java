package com.vendorcompliance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendorcompliance.dto.RiskScoreResponse;
import com.vendorcompliance.entity.DocumentType;
import com.vendorcompliance.entity.VendorDocument;
import com.vendorcompliance.repository.AuditFlagRepository;

@Service
public class RiskService {

    private static final Set<DocumentType> REQUIRED_DOCUMENTS = EnumSet.allOf(DocumentType.class);
    private static final int MISSING_DOC_WEIGHT = 20;
    private static final int EXPIRED_DOC_WEIGHT = 30;
    private static final int AUDIT_FLAG_WEIGHT = 25;

    private final VendorService vendorService;
    private final VendorDocumentService vendorDocumentService;
    private final AuditService auditService;
    private final AuditFlagRepository auditFlagRepository;

    public RiskService(
            VendorService vendorService,
            VendorDocumentService vendorDocumentService,
            AuditService auditService,
            AuditFlagRepository auditFlagRepository
    ) {
        this.vendorService = vendorService;
        this.vendorDocumentService = vendorDocumentService;
        this.auditService = auditService;
        this.auditFlagRepository = auditFlagRepository;
    }

    @Transactional
    public RiskScoreResponse calculateRiskScore(Long vendorId, String actor) {
        vendorService.findVendorOrThrow(vendorId);

        List<VendorDocument> documents = vendorDocumentService.listVendorDocumentsForRisk(vendorId);
        Map<DocumentType, VendorDocument> latestByType = mapLatestDocuments(documents);

        List<DocumentType> missingDocuments = new ArrayList<>();
        List<DocumentType> expiredDocuments = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (DocumentType requiredType : REQUIRED_DOCUMENTS) {
            VendorDocument latest = latestByType.get(requiredType);
            if (latest == null) {
                missingDocuments.add(requiredType);
            } else if (latest.getExpiryDate().isBefore(today)) {
                expiredDocuments.add(requiredType);
            }
        }

        long unresolvedAuditFlags = auditFlagRepository.countByVendorIdAndResolvedFalse(vendorId);

        int riskScore = Math.min(100, (int) (missingDocuments.size() * MISSING_DOC_WEIGHT
                + expiredDocuments.size() * EXPIRED_DOC_WEIGHT
                + unresolvedAuditFlags * AUDIT_FLAG_WEIGHT));
        vendorService.updateRiskScore(vendorId, riskScore);
        auditService.logRiskScoreCalculated(actor, vendorId, riskScore, missingDocuments.size(), expiredDocuments.size());

        RiskScoreResponse response = new RiskScoreResponse();
        response.setVendorId(vendorId);
        response.setRiskScore(riskScore);
        response.setRiskLevel(toRiskLevel(riskScore));
        response.setMissingDocuments(missingDocuments);
        response.setExpiredDocuments(expiredDocuments);
        response.setEvaluatedAt(LocalDateTime.now());
        return response;
    }

    @Transactional
    public void recalculateRiskForVendor(Long vendorId, String actor) {
        calculateRiskScore(vendorId, actor);
    }

    private Map<DocumentType, VendorDocument> mapLatestDocuments(List<VendorDocument> documents) {
        Map<DocumentType, VendorDocument> latestByType = new EnumMap<>(DocumentType.class);
        for (VendorDocument document : documents) {
            latestByType.putIfAbsent(document.getType(), document);
        }
        return latestByType;
    }

    private String toRiskLevel(int score) {
        if (score <= 20) {
            return "LOW";
        }
        if (score <= 50) {
            return "MEDIUM";
        }
        return "HIGH";
    }
}
