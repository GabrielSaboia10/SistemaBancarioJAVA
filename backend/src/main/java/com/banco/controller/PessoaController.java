package com.banco.controller;

import com.banco.dto.PageResponse;
import com.banco.dto.PessoaDtos.*;
import com.banco.service.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Acesso restrito a ADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/api/pessoas")
@Tag(name = "Pessoas (ADMIN)")
public class PessoaController {

    private final PessoaService service;

    public PessoaController(PessoaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista paginada, com busca opcional por nome ou CPF")
    public PageResponse<PessoaResponse> listar(@RequestParam(required = false) String busca,
                                               @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.listar(busca, pageable);
    }

    @GetMapping("/{id}")
    public PessoaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra a pessoa e cria o login dela com uma senha temporária")
    public ResponseEntity<PessoaCriadaResponse> criar(@Valid @RequestBody CriarPessoaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PutMapping("/{id}")
    public PessoaResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarPessoaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/bloqueio")
    @Operation(summary = "Bloqueia ({\"ativo\": false}) ou libera ({\"ativo\": true}) o acesso do cliente")
    public PessoaResponse alterarBloqueio(@PathVariable Long id, @Valid @RequestBody BloqueioRequest req) {
        return service.alterarBloqueio(id, req.ativo());
    }

    @PostMapping("/{id}/redefinir-senha")
    @Operation(summary = "Gera uma nova senha temporária; o cliente é obrigado a trocá-la no próximo acesso")
    public SenhaRedefinidaResponse redefinirSenha(@PathVariable Long id) {
        return service.redefinirSenha(id);
    }
}
