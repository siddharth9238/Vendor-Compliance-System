package com.vendorcompliance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VendorOnboardingRequest {

    @NotBlank(message = "Legal name is required")
    @Size(min = 2, max = 180, message = "Legal name must be between 2 and 180 characters")
    private String legalName;

    @Size(min = 2, max = 180, message = "Trading name must be between 2 and 180 characters")
    private String tradingName;

    @NotBlank(message = "Registration number is required")
    @Size(min = 3, max = 80, message = "Registration number must be between 3 and 80 characters")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Registration number must contain only uppercase letters, numbers, and hyphens")
    private String registrationNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid format (e.g., vendor@company.com)")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    private String email;

    @Size(max = 40, message = "Phone number must not exceed 40 characters")
    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$", 
             message = "Phone number must be in a valid format", 
             allowEmptyString = true)
    private String phone;

    @Size(min = 5, max = 500, message = "Address must be between 5 and 500 characters")
    private String address;

    @Size(max = 600, message = "Onboarding notes must not exceed 600 characters")
    private String onboardingNotes;

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

    public String getOnboardingNotes() {
        return onboardingNotes;
    }

    public void setOnboardingNotes(String onboardingNotes) {
        this.onboardingNotes = onboardingNotes;
    }
}
