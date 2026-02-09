package com.vendorcompliance.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendorcompliance.dto.AuditLogResponse;
import com.vendorcompliance.entity.AuditAction;
import com.vendorcompliance.entity.AuditLog;
import com.vendorcompliance.entity.DocumentType;
import com.vendorcompliance.repository.AuditLogRepository;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(AuditAction action, String actorUsername, Long vendorId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setActorUsername(actorUsername);
        log.setVendorId(vendorId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    @Transactional
    public void log(AuditAction action, String actorUsername, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setActorUsername(actorUsername);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogs(Long vendorId, AuditAction action) {
        List<AuditLog> logs;
        if (vendorId != null && action != null) {
            logs = auditLogRepository.findByVendorIdAndActionOrderByCreatedAtDesc(vendorId, action);
        } else if (vendorId != null) {
            logs = auditLogRepository.findByVendorIdOrderByCreatedAtDesc(vendorId);
        } else if (action != null) {
            logs = auditLogRepository.findByActionOrderByCreatedAtDesc(action);
        } else {
            logs = auditLogRepository.findAllByOrderByCreatedAtDesc();
        }
        return logs.stream().map(this::toResponse).toList();
    }

    @Transactional
    public void logVendorOnboardingSubmitted(String actorUsername, Long vendorId) {
        log(AuditAction.VENDOR_ONBOARD_SUBMITTED, actorUsername, vendorId,
                "Vendor onboarding submitted with status PENDING");
    }

    @Transactional
    public void logLogin(String actorUsername) {
        log(AuditAction.LOGIN, actorUsername, "User successfully authenticated");
    }

    @Transactional
    public void logVendorApproved(String actorUsername, Long vendorId, String comments) {
        log(AuditAction.VENDOR_APPROVED, actorUsername, vendorId, comments);
    }

    @Transactional
    public void logVendorRejected(String actorUsername, Long vendorId, String comments) {
        log(AuditAction.VENDOR_REJECTED, actorUsername, vendorId, comments);
    }

    @Transactional
    public void logDocumentUploaded(
            String actorUsername,
            Long vendorId,
            DocumentType documentType,
            String fileName,
            LocalDate expiryDate
    ) {
        String details = "Document uploaded: type=" + documentType
                + ", fileName=" + fileName
                + ", expiryDate=" + expiryDate;
        log(AuditAction.DOCUMENT_UPLOADED, actorUsername, vendorId, details);
    }

    @Transactional
    public void logRiskScoreCalculated(
            String actorUsername,
            Long vendorId,
            int riskScore,
            int missingDocumentCount,
            int expiredDocumentCount
    ) {
        String details = "Risk calculated: riskScore=" + riskScore
                + ", missingDocuments=" + missingDocumentCount
                + ", expiredDocuments=" + expiredDocumentCount;
        log(AuditAction.RISK_SCORE_CALCULATED, actorUsername, vendorId, details);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setAction(log.getAction().name());
        response.setActorUsername(log.getActorUsername());
        response.setVendorId(log.getVendorId());
        response.setDetails(log.getDetails());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
