package com.banco.controller;

import com.banco.model.AgenciaBancaria;
import com.banco.service.AgenciaBancariaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agencias")
public class AgenciaBancariaController {

    private final AgenciaBancariaService service;

    public AgenciaBancariaController(AgenciaBancariaService service) {
        this.service = service;
    }

    @GetMapping
    public List<AgenciaBancaria> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public AgenciaBancaria buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/numero/{numero}")
    public AgenciaBancaria buscarPorNumero(@PathVariable int numero) {
        return service.buscarPorNumero(numero);
    }

    @PostMapping
    public ResponseEntity<AgenciaBancaria> incluir(@Valid @RequestBody AgenciaBancaria agencia) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.incluir(agencia));
    }

    @PutMapping("/{id}")
    public AgenciaBancaria alterar(@PathVariable Long id, @Valid @RequestBody AgenciaBancaria dados) {
        return service.alterar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
