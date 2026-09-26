package com.banco.controller;

import com.banco.dto.AuditoriaResponse;
import com.banco.dto.PageResponse;
import com.banco.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Acesso restrito a ADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/api/auditoria")
@Tag(name = "Auditoria (ADMIN)")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Ações administrativas, da mais recente para a mais antiga. Filtro: PESSOA, AGENCIA, CONTA")
    public PageResponse<AuditoriaResponse> listar(@RequestParam(required = false) String entidade,
                                                  @PageableDefault(size = 20) Pageable pageable) {
        return service.listar(entidade, pageable);
    }
}
