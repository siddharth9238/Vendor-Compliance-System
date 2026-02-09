package com.vendorcompliance.service;

import com.vendorcompliance.dto.VendorDocumentResponse;
import com.vendorcompliance.entity.DocumentType;
import com.vendorcompliance.entity.Vendor;
import com.vendorcompliance.entity.VendorDocument;
import com.vendorcompliance.exception.BadRequestException;
import com.vendorcompliance.repository.VendorDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class VendorDocumentService {

    private final VendorService vendorService;
    private final VendorDocumentRepository vendorDocumentRepository;
    private final AuditService auditService;
    private final RiskService riskService;

    public VendorDocumentService(
            VendorService vendorService,
            VendorDocumentRepository vendorDocumentRepository,
            AuditService auditService,
            RiskService riskService
    ) {
        this.vendorService = vendorService;
        this.vendorDocumentRepository = vendorDocumentRepository;
        this.auditService = auditService;
        this.riskService = riskService;
    }

    @Transactional
    public VendorDocumentResponse uploadDocument(
            Long vendorId,
            DocumentType type,
            MultipartFile file,
            LocalDate expiryDate,
            String actor
    ) {
        Vendor vendor = vendorService.findVendorOrThrow(vendorId);

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Document file is required");
        }
        if (type == null) {
            throw new BadRequestException("Document type is required");
        }
        if (expiryDate == null) {
            throw new BadRequestException("Expiry date is required");
        }

        try {
            VendorDocument document = new VendorDocument();
            document.setVendor(vendor);
            document.setType(type);
            document.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.bin");
            document.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            document.setContent(file.getBytes());
            document.setExpiryDate(expiryDate);
            document.setUploadedBy(actor);

            VendorDocument savedDocument = vendorDocumentRepository.save(document);
            auditService.logDocumentUploaded(actor, vendorId, type, savedDocument.getFileName(), expiryDate);
            
            // Recalculate risk when document is uploaded
            riskService.recalculateRiskForVendor(vendorId, actor);
            
            return toResponse(savedDocument);
        } catch (IOException ex) {
            throw new BadRequestException("Failed to read uploaded document");
        }
    }

    @Transactional(readOnly = true)
    public List<VendorDocumentResponse> listVendorDocuments(Long vendorId) {
        vendorService.findVendorOrThrow(vendorId);
        return vendorDocumentRepository.findByVendorIdOrderByUploadedAtDesc(vendorId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VendorDocument> listVendorDocumentsForRisk(Long vendorId) {
        return vendorDocumentRepository.findByVendorIdOrderByUploadedAtDesc(vendorId);
    }

    private VendorDocumentResponse toResponse(VendorDocument document) {
        VendorDocumentResponse response = new VendorDocumentResponse();
        response.setId(document.getId());
        response.setType(document.getType());
        response.setFileName(document.getFileName());
        response.setMimeType(document.getMimeType());
        response.setExpiryDate(document.getExpiryDate());
        response.setExpired(document.getExpiryDate().isBefore(LocalDate.now()));
        response.setUploadedBy(document.getUploadedBy());
        response.setUploadedAt(document.getUploadedAt());
        return response;
    }
}
