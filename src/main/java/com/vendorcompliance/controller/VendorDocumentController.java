package com.vendorcompliance.controller;

import com.vendorcompliance.dto.VendorDocumentResponse;
import com.vendorcompliance.entity.DocumentType;
import com.vendorcompliance.service.VendorDocumentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vendors")
public class VendorDocumentController {

    private final VendorDocumentService vendorDocumentService;

    public VendorDocumentController(VendorDocumentService vendorDocumentService) {
        this.vendorDocumentService = vendorDocumentService;
    }

    @PostMapping(value = "/{vendorId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR_MANAGER','VENDOR')")
    public ResponseEntity<VendorDocumentResponse> uploadDocument(
            @PathVariable Long vendorId,
            @RequestParam DocumentType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        String actor = authentication.getName();
        VendorDocumentResponse response = vendorDocumentService.uploadDocument(vendorId, type, file, expiryDate, actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{vendorId}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR_MANAGER','AUDITOR','VENDOR')")
    public ResponseEntity<List<VendorDocumentResponse>> listVendorDocuments(@PathVariable Long vendorId) {
        return ResponseEntity.ok(vendorDocumentService.listVendorDocuments(vendorId));
    }
}
