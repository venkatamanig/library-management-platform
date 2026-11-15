package org.example.lms.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Public authentication, API documentation and health probes
                        .requestMatchers(
                                "/auth/register", "/auth/login",
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/actuator/health",
                                "/actuator/health/**")
                        .permitAll()

                        // Book permissions
                        .requestMatchers(HttpMethod.GET, "/book", "/book/**")
                        .hasAnyRole("ADMIN", "LIBRARIAN", "PATRON")

                        .requestMatchers(HttpMethod.POST, "/book", "/book/**")
                        .hasAnyRole("ADMIN", "LIBRARIAN")

                        .requestMatchers(HttpMethod.PUT, "/book/**")
                        .hasAnyRole("ADMIN", "LIBRARIAN")

                        .requestMatchers(HttpMethod.PATCH, "/book/**")
                        .hasAnyRole("ADMIN", "LIBRARIAN")

                        .requestMatchers(HttpMethod.DELETE, "/book/**")
                        .hasAnyRole("ADMIN", "LIBRARIAN")

                        // Librarians and Admins can view policies
                        .requestMatchers(HttpMethod.GET, "/admin/policies")
                        .hasAnyRole("ADMIN", "LIBRARIAN")

                        // Other admin endpoints
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // Librarian and Admin endpoints
                        .requestMatchers("/librarian/**")
                        .hasAnyRole("ADMIN", "LIBRARIAN")

                        // Borrowing endpoints
                        .requestMatchers("/borrow/**")
                        .hasAnyRole("ADMIN", "LIBRARIAN", "PATRON")

                        // Patron endpoints
                        .requestMatchers("/patron/**")
                        .hasAnyRole("ADMIN", "LIBRARIAN", "PATRON")

                        // Patron management
                        .requestMatchers("/patron-management/**")
                        .hasAnyRole("ADMIN", "LIBRARIAN")

                        // Default protection
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter()
                        ))
                );

        return http.build();
    }


    @Bean
    public SecretKey jwtSecretKey(
            @Value("${jwt.secret}") String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must contain at least 32 UTF-8 bytes for HS256.");
        }

        return new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(
                new ImmutableSecret<>(jwtSecretKey)
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }
}
