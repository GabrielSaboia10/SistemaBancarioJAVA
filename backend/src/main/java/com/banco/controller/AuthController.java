package com.banco.controller;

import com.banco.dto.AuthDtos.*;
import com.banco.security.UsuarioAutenticado;
import com.banco.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @Operation(summary = "Login com CPF e senha. Devolve access token (15 min) e refresh token (7 dias).")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Troca o refresh token por um novo par de tokens")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return service.refresh(req);
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalida o refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        service.logout(req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário logado")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return service.me(UsuarioAutenticado.de(jwt).id());
    }

    @PutMapping("/senha")
    @Operation(summary = "Troca a senha do usuário logado e encerra as outras sessões")
    public ResponseEntity<Void> trocarSenha(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TrocarSenhaRequest req) {
        service.trocarSenha(UsuarioAutenticado.de(jwt).id(), req);
        return ResponseEntity.noContent().build();
    }
}
