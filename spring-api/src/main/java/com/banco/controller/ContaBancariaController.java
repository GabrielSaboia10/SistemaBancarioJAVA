package com.banco.controller;

import com.banco.model.ContaBancaria;
import com.banco.service.ContaBancariaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contas")
public class ContaBancariaController {

    private final ContaBancariaService service;

    public ContaBancariaController(ContaBancariaService service) {
        this.service = service;
    }

    @GetMapping
    public List<ContaBancaria> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ContaBancaria buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/numero/{numero}")
    public ContaBancaria buscarPorNumero(@PathVariable int numero) {
        return service.buscarPorNumero(numero);
    }

    @PostMapping
    public ResponseEntity<ContaBancaria> incluir(@RequestBody Map<String, Object> body) {
        int numero = (int) body.get("numContaCorrente");
        double limite = ((Number) body.get("limiteChequeEspecial")).doubleValue();
        double saldo = ((Number) body.get("saldo")).doubleValue();
        Long pessoaId = ((Number) body.get("pessoaId")).longValue();
        Long agenciaId = ((Number) body.get("agenciaId")).longValue();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.incluir(numero, limite, saldo, pessoaId, agenciaId));
    }

    @PutMapping("/{id}")
    public ContaBancaria alterar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        double limite = ((Number) body.get("limiteChequeEspecial")).doubleValue();
        Long agenciaId = ((Number) body.get("agenciaId")).longValue();
        return service.alterar(id, limite, agenciaId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/depositar")
    public ContaBancaria depositar(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        return service.depositar(id, body.get("valor"));
    }

    @PostMapping("/{id}/sacar")
    public ContaBancaria sacar(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        return service.sacar(id, body.get("valor"));
    }

    @PostMapping("/{id}/transferir")
    public ContaBancaria transferir(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long destinoId = ((Number) body.get("destinoId")).longValue();
        double valor = ((Number) body.get("valor")).doubleValue();
        return service.transferir(id, destinoId, valor);
    }
}
