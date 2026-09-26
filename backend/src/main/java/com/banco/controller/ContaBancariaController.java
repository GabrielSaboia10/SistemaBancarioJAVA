package com.banco.controller;

import com.banco.dto.ContaDtos.*;
import com.banco.dto.PageResponse;
import com.banco.security.UsuarioAutenticado;
import com.banco.service.ContaBancariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/contas")
@Tag(name = "Contas")
public class ContaBancariaController {

    private final ContaBancariaService service;

    public ContaBancariaController(ContaBancariaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "ADMIN: todas as contas. CLIENTE: só as próprias. Busca por número ou nome do correntista.")
    public PageResponse<ContaResponse> listar(@AuthenticationPrincipal Jwt jwt,
                                              @RequestParam(required = false) String busca,
                                              @PageableDefault(size = 20, sort = "numero") Pageable pageable) {
        return service.listar(UsuarioAutenticado.de(jwt), busca, pageable);
    }

    @GetMapping("/stats")
    @Operation(summary = "Resumo para o dashboard")
    public StatsResponse stats(@AuthenticationPrincipal Jwt jwt) {
        return service.stats(UsuarioAutenticado.de(jwt));
    }

    @GetMapping("/{id}")
    public ContaResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.buscar(id, UsuarioAutenticado.de(jwt));
    }

    @GetMapping("/{id}/extrato")
    @Operation(summary = "Movimentações da conta, da mais recente para a mais antiga. Período opcional (yyyy-MM-dd).")
    public PageResponse<MovimentacaoResponse> extrato(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
                                                      @RequestParam(required = false) LocalDate de,
                                                      @RequestParam(required = false) LocalDate ate,
                                                      @PageableDefault(size = 20) Pageable pageable) {
        return service.extrato(id, de, ate, UsuarioAutenticado.de(jwt), pageable);
    }

    @GetMapping(value = "/{id}/extrato.csv", produces = "text/csv")
    @Operation(summary = "Extrato do período em CSV (abre no Excel)")
    public ResponseEntity<String> extratoCsv(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
                                             @RequestParam(required = false) LocalDate de,
                                             @RequestParam(required = false) LocalDate ate) {
        String csv = service.extratoCsv(id, de, ate, UsuarioAutenticado.de(jwt));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"extrato-conta-" + id + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    @PostMapping
    @Operation(summary = "Abre uma conta (ADMIN)")
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody CriarContaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Altera limite e agência (ADMIN)")
    public ContaResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarContaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Encerra uma conta com saldo zero (ADMIN)")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/depositar")
    public ContaResponse depositar(@PathVariable Long id, @Valid @RequestBody OperacaoRequest req,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.depositar(id, req.valor(), UsuarioAutenticado.de(jwt));
    }

    @PostMapping("/{id}/sacar")
    public ContaResponse sacar(@PathVariable Long id, @Valid @RequestBody OperacaoRequest req,
                               @AuthenticationPrincipal Jwt jwt) {
        return service.sacar(id, req.valor(), UsuarioAutenticado.de(jwt));
    }

    @PostMapping("/{id}/transferir")
    @Operation(summary = "Transfere para outra conta, informada pelo número")
    public ContaResponse transferir(@PathVariable Long id, @Valid @RequestBody TransferenciaRequest req,
                                    @AuthenticationPrincipal Jwt jwt) {
        return service.transferir(id, req, UsuarioAutenticado.de(jwt));
    }
}
