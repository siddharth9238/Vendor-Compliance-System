package com.vendorcompliance.dto;

import jakarta.validation.constraints.Size;

public class VendorApprovalRequest {

    @Size(min = 5, max = 600, message = "Comments must be between 5 and 600 characters if provided")
    private String comments;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
