package com.anonboard.util;

import com.anonboard.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AnonymousNameGenerator {

    private static final String[] PREFIXES = {
            "Anonymous", "Student", "User", "Scholar",
            "Learner", "Campus", "Anon", "Unknown",
            "Thinker", "Seeker", "Reader", "Curious"
    };

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;

    public AnonymousNameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateUniqueName() {
        String name;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            String prefix = PREFIXES[random.nextInt(PREFIXES.length)];
            String suffix = generateRandomSuffix(4);
            name = prefix + "_" + suffix;
            attempts++;

            if (attempts >= maxAttempts) {
                // Fall back to longer suffix
                suffix = generateRandomSuffix(6);
                name = prefix + "_" + suffix;
                break;
            }
        } while (userRepository.existsByAnonymousName(name));

        return name;
    }

    private String generateRandomSuffix(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
