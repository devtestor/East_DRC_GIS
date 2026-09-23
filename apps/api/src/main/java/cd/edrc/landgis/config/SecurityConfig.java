package cd.edrc.landgis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Arrays;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/actuator/**", "/v3/api-docs/**"))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'; base-uri 'none'"))
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        .permissionsPolicy(permissions -> permissions.policy("camera=(), microphone=(), geolocation=()")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/platform/disclaimer").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/platform/pilot-readiness").hasRole("STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/identity/users").permitAll()
                        .requestMatchers("/api/v1/administrative-units/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/devices/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/documents/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/disputes/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/parties/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/parcels/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/pilots/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/workflow/**").hasRole("STAFF")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${edrc.security.cors-allowed-origins}") String configuredOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(configuredOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-Id", "Idempotency-Key"));
        configuration.setExposedHeaders(List.of("X-Correlation-Id"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
