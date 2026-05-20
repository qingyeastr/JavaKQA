package com.qingyun.userportal.service;

import com.qingyun.userportal.web.PortalUserSession;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PortalAuthService {

    private static final String DEMO_PASSWORD = "123456";

    public Optional<PortalUserSession> authenticate(String username, String password) {
        if (!StringUtils.hasText(username) || !DEMO_PASSWORD.equals(password)) {
            return Optional.empty();
        }

        String normalizedUsername = username.trim();
        if (!StringUtils.hasText(normalizedUsername)) {
            return Optional.empty();
        }

        String displayName = normalizedUsername.length() > 12
                ? normalizedUsername.substring(0, 12) + "..."
                : normalizedUsername;
        return Optional.of(new PortalUserSession(normalizedUsername, displayName));
    }
}