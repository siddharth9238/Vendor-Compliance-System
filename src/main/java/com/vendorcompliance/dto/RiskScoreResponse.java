package com.vendorcompliance.dto;

import com.vendorcompliance.entity.DocumentType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RiskScoreResponse {

    private Long vendorId;
    private Integer riskScore;
    private String riskLevel;
    private List<DocumentType> missingDocuments = new ArrayList<>();
    private List<DocumentType> expiredDocuments = new ArrayList<>();
    private LocalDateTime evaluatedAt;

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<DocumentType> getMissingDocuments() {
        return missingDocuments;
    }

    public void setMissingDocuments(List<DocumentType> missingDocuments) {
        this.missingDocuments = missingDocuments;
    }

    public List<DocumentType> getExpiredDocuments() {
        return expiredDocuments;
    }

    public void setExpiredDocuments(List<DocumentType> expiredDocuments) {
        this.expiredDocuments = expiredDocuments;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
