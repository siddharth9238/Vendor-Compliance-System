package com.vendorcompliance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.vendorcompliance.entity.DocumentType;
import com.vendorcompliance.entity.VendorDocument;

public interface VendorDocumentRepository extends JpaRepository<VendorDocument, Long> {
    List<VendorDocument> findByVendorIdOrderByUploadedAtDesc(Long vendorId);

    List<VendorDocument> findByVendorIdAndType(Long vendorId, DocumentType type);

    Optional<VendorDocument> findTopByVendorIdAndTypeOrderByUploadedAtDesc(Long vendorId, DocumentType type);

    @Query("SELECT d FROM VendorDocument d WHERE d.expiryDate <= :date AND d.vendor.status = 'APPROVED'")
    List<VendorDocument> findExpiredDocuments(LocalDate date);
}
