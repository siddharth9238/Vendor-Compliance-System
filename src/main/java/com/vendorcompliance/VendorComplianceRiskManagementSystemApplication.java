package com.vendorcompliance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VendorComplianceRiskManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(VendorComplianceRiskManagementSystemApplication.class, args);
    }
}
