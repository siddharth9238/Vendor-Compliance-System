package com.vendorcompliance.config;

import com.vendorcompliance.entity.AppUser;
import com.vendorcompliance.entity.Role;
import com.vendorcompliance.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap.admin-username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap.admin-email:admin@vendorcompliance.local}")
    private String adminEmail;

    @Value("${app.bootstrap.admin-password:Admin@12345}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!bootstrapEnabled
                || userRepository.existsByUsername(adminUsername)
                || userRepository.existsByEmail(adminEmail)) {
            return;
        }

        AppUser admin = new AppUser();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setFullName("System Administrator");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRoles(Set.of(Role.ADMIN));
        admin.setEnabled(true);
        userRepository.save(admin);

        LOGGER.info("Bootstrap admin user created: {}", adminUsername);
    }
}
