package com.vendorcompliance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vendorcompliance.dto.RiskScoreResponse;
import com.vendorcompliance.dto.VendorApprovalDecisionRequest;
import com.vendorcompliance.dto.VendorApprovalRequest;
import com.vendorcompliance.dto.VendorOnboardingRequest;
import com.vendorcompliance.dto.VendorResponse;
import com.vendorcompliance.entity.VendorStatus;
import com.vendorcompliance.service.RiskService;
import com.vendorcompliance.service.VendorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendorService;
    private final RiskService riskService;

    public VendorController(VendorService vendorService, RiskService riskService) {
        this.vendorService = vendorService;
        this.riskService = riskService;
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<VendorResponse> onboardVendor(
            @Valid @RequestBody VendorOnboardingRequest request,
            Authentication authentication
    ) {
        String actor = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorService.onboardVendor(request, actor));
    }

    @GetMapping("/{vendorId}")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR_MANAGER','AUDITOR','VENDOR')")
    public ResponseEntity<VendorResponse> getVendor(@PathVariable Long vendorId) {
        return ResponseEntity.ok(vendorService.getVendorById(vendorId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR_MANAGER','AUDITOR')")
    public ResponseEntity<List<VendorResponse>> listVendors(
            @RequestParam(required = false) VendorStatus status
    ) {
        return ResponseEntity.ok(vendorService.listVendors(status));
    }

    @PatchMapping("/{vendorId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR_MANAGER')")
    public ResponseEntity<VendorResponse> approveVendor(
            @PathVariable Long vendorId,
            @Valid @RequestBody(required = false) VendorApprovalRequest request,
            Authentication authentication
    ) {
        String actor = authentication.getName();
        return ResponseEntity.ok(vendorService.approveVendor(vendorId, request, actor));
    }

    @PatchMapping("/{vendorId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR_MANAGER')")
    public ResponseEntity<VendorResponse> rejectVendor(
            @PathVariable Long vendorId,
            @Valid @RequestBody(required = false) VendorApprovalRequest request,
            Authentication authentication
    ) {
        String actor = authentication.getName();
        return ResponseEntity.ok(vendorService.rejectVendor(vendorId, request, actor));
    }

    @PostMapping("/{vendorId}/approval")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR_MANAGER')")
    public ResponseEntity<VendorResponse> reviewVendor(
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorApprovalDecisionRequest request,
            Authentication authentication
    ) {
        String actor = authentication.getName();
        return ResponseEntity.ok(vendorService.reviewVendor(vendorId, request, actor));
    }

    @GetMapping("/{vendorId}/risk-score")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR_MANAGER','AUDITOR')")
    public ResponseEntity<RiskScoreResponse> calculateRisk(
            @PathVariable Long vendorId,
            Authentication authentication
    ) {
        String actor = authentication.getName();
        return ResponseEntity.ok(riskService.calculateRiskScore(vendorId, actor));
    }
}
