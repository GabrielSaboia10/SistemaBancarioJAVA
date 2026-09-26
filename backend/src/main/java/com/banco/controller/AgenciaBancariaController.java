package com.banco.controller;

import com.banco.dto.AgenciaDtos.*;
import com.banco.dto.PageResponse;
import com.banco.service.AgenciaBancariaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Acesso restrito a ADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/api/agencias")
@Tag(name = "Agências (ADMIN)")
public class AgenciaBancariaController {

    private final AgenciaBancariaService service;

    public AgenciaBancariaController(AgenciaBancariaService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<AgenciaResponse> listar(@RequestParam(required = false) String busca,
                                                @PageableDefault(size = 20, sort = "numero") Pageable pageable) {
        return service.listar(busca, pageable);
    }

    @GetMapping("/{id}")
    public AgenciaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<AgenciaResponse> criar(@Valid @RequestBody AgenciaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PutMapping("/{id}")
    public AgenciaResponse atualizar(@PathVariable Long id, @Valid @RequestBody AgenciaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
