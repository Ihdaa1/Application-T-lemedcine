package com.telemedicine.config;

import com.telemedicine.entity.User;
import com.telemedicine.entity.UserRole;
import com.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting admin user initialization...");
        
        try {
            // Vérifier si un compte admin existe déjà
            var existingUser = userRepository.findByEmail("Admin.Telemed@gmail.com");
            
            if (existingUser.isEmpty()) {
                log.info("Creating admin user...");
                
                // Créer le compte administrateur par défaut
                User adminUser = new User();
                adminUser.setEmail("Admin.Telemed@gmail.com");
                // Encoder le mot de passe avec le PasswordEncoder
                adminUser.setPassword(passwordEncoder.encode("ADMIN123@"));
                adminUser.setFirstName("Admin");
                adminUser.setLastName("Telemed");
                adminUser.setRole(UserRole.ADMIN);
                adminUser.setActive(true);

                User savedUser = userRepository.save(adminUser);
                log.info("Admin user created successfully with ID: {} and email: {}", 
                         savedUser.getId(), savedUser.getEmail());
            } else {
                User user = existingUser.get();
                log.info("Admin user already exists with ID: {} and email: {}", 
                         user.getId(), user.getEmail());
                
                // Vérifier si le rôle est correctement défini
                if (user.getRole() != UserRole.ADMIN) {
                    user.setRole(UserRole.ADMIN);
                    userRepository.save(user);
                    log.info("Updated user role to ADMIN for: {}", user.getEmail());
                }
            }
        } catch (Exception e) {
            log.error("Error during admin user initialization: {}", e.getMessage(), e);
        }
    }
}