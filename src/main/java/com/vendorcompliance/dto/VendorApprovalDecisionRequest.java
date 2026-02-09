package com.vendorcompliance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VendorApprovalDecisionRequest {

    public enum Decision {
        APPROVE,
        REJECT
    }

    @NotNull(message = "Decision is required")
    private Decision decision;

    @Size(max = 600, message = "Comments must not exceed 600 characters")
    private String comments;

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
