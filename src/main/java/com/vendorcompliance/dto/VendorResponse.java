package com.vendorcompliance.dto;

import com.vendorcompliance.entity.VendorStatus;

import java.time.LocalDateTime;

public class VendorResponse {

    private Long id;
    private String legalName;
    private String tradingName;
    private String registrationNumber;
    private String email;
    private String phone;
    private String address;
    private VendorStatus status;
    private Integer riskScore;
    private String onboardingNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastRiskCalculatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getTradingName() {
        return tradingName;
    }

    public void setTradingName(String tradingName) {
        this.tradingName = tradingName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public VendorStatus getStatus() {
        return status;
    }

    public void setStatus(VendorStatus status) {
        this.status = status;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getOnboardingNotes() {
        return onboardingNotes;
    }

    public void setOnboardingNotes(String onboardingNotes) {
        this.onboardingNotes = onboardingNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastRiskCalculatedAt() {
        return lastRiskCalculatedAt;
    }

    public void setLastRiskCalculatedAt(LocalDateTime lastRiskCalculatedAt) {
        this.lastRiskCalculatedAt = lastRiskCalculatedAt;
    }
}
