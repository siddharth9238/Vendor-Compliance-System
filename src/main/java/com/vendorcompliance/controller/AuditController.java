package com.vendorcompliance.controller;

import com.vendorcompliance.dto.AuditLogResponse;
import com.vendorcompliance.entity.AuditAction;
import com.vendorcompliance.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) AuditAction action
    ) {
        return ResponseEntity.ok(auditService.getLogs(vendorId, action));
    }
}
