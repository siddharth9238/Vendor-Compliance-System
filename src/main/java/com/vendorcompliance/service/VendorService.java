package com.vendorcompliance.service;

import com.vendorcompliance.dto.VendorApprovalRequest;
import com.vendorcompliance.dto.VendorApprovalDecisionRequest;
import com.vendorcompliance.dto.VendorOnboardingRequest;
import com.vendorcompliance.dto.VendorResponse;
import com.vendorcompliance.entity.Vendor;
import com.vendorcompliance.entity.VendorStatus;
import com.vendorcompliance.exception.BadRequestException;
import com.vendorcompliance.exception.ResourceNotFoundException;
import com.vendorcompliance.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final AuditService auditService;

    public VendorService(VendorRepository vendorRepository, AuditService auditService) {
        this.vendorRepository = vendorRepository;
        this.auditService = auditService;
    }

    @Transactional
    public VendorResponse onboardVendor(VendorOnboardingRequest request, String actor) {
        if (vendorRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new BadRequestException("Vendor with registration number already exists");
        }

        Vendor vendor = new Vendor();
        vendor.setLegalName(request.getLegalName());
        vendor.setTradingName(request.getTradingName());
        vendor.setRegistrationNumber(request.getRegistrationNumber());
        vendor.setEmail(request.getEmail());
        vendor.setPhone(request.getPhone());
        vendor.setAddress(request.getAddress());
        vendor.setOnboardingNotes(request.getOnboardingNotes());
        vendor.setStatus(VendorStatus.PENDING);
        vendor.setCreatedBy(actor);
        vendor.setUpdatedBy(actor);

        Vendor savedVendor = vendorRepository.save(vendor);
        auditService.logVendorOnboardingSubmitted(actor, savedVendor.getId());
        return toResponse(savedVendor);
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendorById(Long vendorId) {
        return toResponse(findVendorOrThrow(vendorId));
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> listVendors(VendorStatus status) {
        List<Vendor> vendors = status == null
                ? vendorRepository.findAllByOrderByCreatedAtDesc()
                : vendorRepository.findByStatusOrderByCreatedAtDesc(status);
        return vendors.stream().map(this::toResponse).toList();
    }

    @Transactional
    public VendorResponse approveVendor(Long vendorId, VendorApprovalRequest request, String actor) {
        return updateStatus(vendorId, VendorStatus.APPROVED, request, actor);
    }

    @Transactional
    public VendorResponse rejectVendor(Long vendorId, VendorApprovalRequest request, String actor) {
        return updateStatus(vendorId, VendorStatus.REJECTED, request, actor);
    }

    @Transactional
    public VendorResponse reviewVendor(Long vendorId, VendorApprovalDecisionRequest request, String actor) {
        VendorApprovalRequest approvalRequest = new VendorApprovalRequest();
        approvalRequest.setComments(request.getComments());
        if (request.getDecision() == VendorApprovalDecisionRequest.Decision.APPROVE) {
            return approveVendor(vendorId, approvalRequest, actor);
        }
        return rejectVendor(vendorId, approvalRequest, actor);
    }

    @Transactional
    public void updateRiskScore(Long vendorId, Integer riskScore) {
        Vendor vendor = findVendorOrThrow(vendorId);
        vendor.setRiskScore(riskScore);
        vendor.setLastRiskCalculatedAt(java.time.LocalDateTime.now());
        vendorRepository.save(vendor);
    }

    @Transactional(readOnly = true)
    public Vendor findVendorOrThrow(Long vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorId));
    }

    private VendorResponse updateStatus(Long vendorId, VendorStatus status, VendorApprovalRequest request, String actor) {
        Vendor vendor = findVendorOrThrow(vendorId);
        if (vendor.getStatus() == status) {
            throw new BadRequestException("Vendor is already in status " + status);
        }

        vendor.setStatus(status);
        vendor.setUpdatedBy(actor);
        if (request != null && request.getComments() != null && !request.getComments().isBlank()) {
            vendor.setOnboardingNotes(request.getComments());
        }

        Vendor savedVendor = vendorRepository.save(vendor);
        if (status == VendorStatus.APPROVED) {
            auditService.logVendorApproved(actor, vendorId, request != null ? request.getComments() : null);
        } else {
            auditService.logVendorRejected(actor, vendorId, request != null ? request.getComments() : null);
        }
        return toResponse(savedVendor);
    }

    private VendorResponse toResponse(Vendor vendor) {
        VendorResponse response = new VendorResponse();
        response.setId(vendor.getId());
        response.setLegalName(vendor.getLegalName());
        response.setTradingName(vendor.getTradingName());
        response.setRegistrationNumber(vendor.getRegistrationNumber());
        response.setEmail(vendor.getEmail());
        response.setPhone(vendor.getPhone());
        response.setAddress(vendor.getAddress());
        response.setStatus(vendor.getStatus());
        response.setRiskScore(vendor.getRiskScore());
        response.setOnboardingNotes(vendor.getOnboardingNotes());
        response.setCreatedAt(vendor.getCreatedAt());
        response.setUpdatedAt(vendor.getUpdatedAt());
        response.setLastRiskCalculatedAt(vendor.getLastRiskCalculatedAt());
        return response;
    }
}
