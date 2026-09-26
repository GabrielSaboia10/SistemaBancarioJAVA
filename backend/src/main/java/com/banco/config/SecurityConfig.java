package com.banco.config;

import com.banco.repository.UsuarioRepository;
import com.banco.security.RateLimitFilter;
import com.banco.security.UsuarioStatusFilter;
import com.banco.security.RateLimiter;
import com.banco.security.RestAuthHandlers;
import com.banco.security.TokenService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {

    /**
     * Regras de acesso. Tudo que não estiver listado aqui é negado (deny by default).
     * A checagem de "dono da conta" para CLIENTE fica no ContaBancariaService.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, RestAuthHandlers handlers, RateLimiter rateLimiter,
                                            UsuarioRepository usuarioRepo, AppProperties props) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // API stateless com Bearer token, sem cookies
                .cors(cors -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Público
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // desligado no perfil prod
                        .requestMatchers("/error").permitAll()

                        // Só ADMIN
                        .requestMatchers("/api/pessoas", "/api/pessoas/**").hasRole("ADMIN")
                        .requestMatchers("/api/agencias", "/api/agencias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/auditoria").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/contas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/contas/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/contas/*").hasRole("ADMIN")

                        // Qualquer usuário logado (CLIENTE só enxerga as próprias contas)
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/senha").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/contas", "/api/contas/stats",
                                "/api/contas/*", "/api/contas/*/extrato", "/api/contas/*/extrato.csv").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/contas/*/depositar", "/api/contas/*/sacar",
                                "/api/contas/*/transferir").authenticated()

                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(handlers)
                        .accessDeniedHandler(handlers))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(handlers)
                        .accessDeniedHandler(handlers))
                // Depois do JWT validado: primeiro bloqueio/senha temporária, depois o rate limit
                .addFilterAfter(new UsuarioStatusFilter(usuarioRepo), BearerTokenAuthenticationFilter.class)
                .addFilterAfter(new RateLimitFilter(rateLimiter, props.rateLimit()), UsuarioStatusFilter.class);
        return http.build();
    }

    /** A claim "role" do JWT (ADMIN/CLIENTE) vira a authority ROLE_ADMIN/ROLE_CLIENTE. */
    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    JwtEncoder jwtEncoder(AppProperties props) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveJwt(props)));
    }

    @Bean
    JwtDecoder jwtDecoder(AppProperties props) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chaveJwt(props))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.ISSUER));
        return decoder;
    }

    private static SecretKey chaveJwt(AppProperties props) {
        String secret = props.jwt().secret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 caracteres");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(AppProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(props.cors().allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Retry-After", "X-RateLimit-Limit", "X-RateLimit-Remaining",
                "Content-Disposition"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
