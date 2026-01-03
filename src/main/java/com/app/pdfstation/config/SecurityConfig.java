package com.app.pdfstation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SSL/TLS Security Configuration
 *
 * This configuration enables HTTPS enforcement for the application.
 * It redirects all HTTP requests to HTTPS automatically.
 *
 * Control via application.properties:
 * app.security.require-https=true  (default: false)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Security filter chain that enforces HTTPS for all requests
     *
     * What it does:
     * - Requires all channels to use secure (HTTPS) protocol
     * - Automatically redirects HTTP to HTTPS
     * - Protects against man-in-the-middle attacks
     *
     * Enabled by: app.security.require-https=true in application.properties
     */
    @Bean
    @ConditionalOnProperty(name = "pdfstation.security.require-https", havingValue = "true")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.requiresChannel(channel -> channel.anyRequest().requiresSecure());

        return http.build();
    }
    /**
     * Default security filter chain (when HTTPS is not required)
     * Applies when: pdfstation.security.require-https is false or not set
     */
    @Bean
    @ConditionalOnProperty(name = "pdfstation.security.require-https", havingValue = "false", matchIfMissing = true)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}

