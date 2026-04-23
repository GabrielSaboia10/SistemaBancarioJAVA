package com.banco.controller;

import com.banco.model.Pessoa;
import com.banco.service.PessoaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pessoas")
public class PessoaController {

    private final PessoaService service;

    public PessoaController(PessoaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Pessoa> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public Pessoa buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/cpf/{cpf}")
    public Pessoa buscarPorCpf(@PathVariable String cpf) {
        return service.buscarPorCpf(cpf);
    }

    @PostMapping
    public ResponseEntity<Pessoa> incluir(@Valid @RequestBody Pessoa pessoa) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.incluir(pessoa));
    }

    @PutMapping("/{id}")
    public Pessoa alterar(@PathVariable Long id, @Valid @RequestBody Pessoa dados) {
        return service.alterar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
