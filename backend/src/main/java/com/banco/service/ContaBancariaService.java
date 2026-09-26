package com.banco.service;

import com.banco.dto.ContaDtos.*;
import com.banco.dto.PageResponse;
import com.banco.exception.ConflitoException;
import com.banco.exception.RecursoNaoEncontradoException;
import com.banco.exception.RegraNegocioException;
import com.banco.model.*;
import com.banco.repository.AgenciaBancariaRepository;
import com.banco.repository.ContaBancariaRepository;
import com.banco.repository.MovimentacaoRepository;
import com.banco.repository.PessoaRepository;
import com.banco.security.UsuarioAutenticado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ContaBancariaService {

    private static final String ENTIDADE = "CONTA";
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final Map<TipoMovimentacao, String> ROTULOS = Map.of(
            TipoMovimentacao.DEPOSITO, "Depósito",
            TipoMovimentacao.SAQUE, "Saque",
            TipoMovimentacao.TRANSFERENCIA_ENVIADA, "Transferência enviada",
            TipoMovimentacao.TRANSFERENCIA_RECEBIDA, "Transferência recebida");

    private final ContaBancariaRepository repo;
    private final MovimentacaoRepository movRepo;
    private final PessoaService pessoaService;
    private final AgenciaBancariaService agenciaService;
    private final PessoaRepository pessoaRepo;
    private final AgenciaBancariaRepository agenciaRepo;
    private final AuditoriaService auditoria;

    public ContaBancariaService(ContaBancariaRepository repo, MovimentacaoRepository movRepo,
                                PessoaService pessoaService, AgenciaBancariaService agenciaService,
                                PessoaRepository pessoaRepo, AgenciaBancariaRepository agenciaRepo,
                                AuditoriaService auditoria) {
        this.repo = repo;
        this.movRepo = movRepo;
        this.pessoaService = pessoaService;
        this.agenciaService = agenciaService;
        this.pessoaRepo = pessoaRepo;
        this.agenciaRepo = agenciaRepo;
        this.auditoria = auditoria;
    }

    // ---------- Consultas ----------

    /**
     * ADMIN vê todas as contas; CLIENTE só as próprias. A busca aceita o número exato da conta
     * ou parte do nome do correntista (o nome só faz diferença para o admin).
     */
    @Transactional(readOnly = true)
    public PageResponse<ContaResponse> listar(UsuarioAutenticado usuario, String busca, Pageable pageable) {
        String termo = busca == null ? "" : busca.trim();
        Page<ContaBancaria> page;
        if (termo.matches("\\d{1,5}")) {
            int numero = Integer.parseInt(termo);
            page = usuario.isAdmin()
                    ? repo.findByNumero(numero, pageable)
                    : repo.findByCorrentistaIdAndNumero(usuario.pessoaId(), numero, pageable);
        } else if (!termo.isEmpty() && usuario.isAdmin()) {
            page = repo.findByCorrentistaNomeContainingIgnoreCase(termo, pageable);
        } else {
            page = usuario.isAdmin()
                    ? repo.findAllBy(pageable)
                    : repo.findByCorrentistaId(usuario.pessoaId(), pageable);
        }
        return PageResponse.de(page, ContaResponse::de);
    }

    @Transactional(readOnly = true)
    public ContaResponse buscar(Long id, UsuarioAutenticado usuario) {
        ContaBancaria conta = repo.findWithDetalhesById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta", id));
        verificarDono(conta, usuario);
        return ContaResponse.de(conta);
    }

    /** Extrato paginado, opcionalmente limitado a um período (datas no fuso de Brasília, inclusivas). */
    @Transactional(readOnly = true)
    public PageResponse<MovimentacaoResponse> extrato(Long id, LocalDate de, LocalDate ate,
                                                      UsuarioAutenticado usuario, Pageable pageable) {
        ContaBancaria conta = repo.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Conta", id));
        verificarDono(conta, usuario);
        Instant[] periodo = periodo(de, ate);
        return PageResponse.de(movRepo.findByContaIdAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByDataHoraDescIdDesc(
                id, periodo[0], periodo[1], pageable), MovimentacaoResponse::de);
    }

    /**
     * Extrato do período em CSV (separador ";" e vírgula decimal, que é o que o Excel em português espera).
     * Limitado a 10.000 linhas por arquivo.
     */
    @Transactional(readOnly = true)
    public String extratoCsv(Long id, LocalDate de, LocalDate ate, UsuarioAutenticado usuario) {
        ContaBancaria conta = repo.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Conta", id));
        verificarDono(conta, usuario);
        Instant[] periodo = periodo(de, ate);
        var movimentacoes = movRepo.findByContaIdAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByDataHoraDescIdDesc(
                id, periodo[0], periodo[1], PageRequest.of(0, 10_000)).getContent();

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(FUSO);
        StringBuilder csv = new StringBuilder("﻿Data;Tipo;Valor;Saldo após;Conta contraparte\r\n");
        for (Movimentacao m : movimentacoes) {
            boolean saida = m.getTipo() == TipoMovimentacao.SAQUE || m.getTipo() == TipoMovimentacao.TRANSFERENCIA_ENVIADA;
            csv.append(formato.format(m.getDataHora())).append(';')
                    .append(ROTULOS.get(m.getTipo())).append(';')
                    .append(decimal(saida ? m.getValor().negate() : m.getValor())).append(';')
                    .append(decimal(m.getSaldoApos())).append(';')
                    .append(m.getNumeroContaContraparte() != null ? m.getNumeroContaContraparte() : "")
                    .append("\r\n");
        }
        return csv.toString();
    }

    private static Instant[] periodo(LocalDate de, LocalDate ate) {
        if (de != null && ate != null && de.isAfter(ate)) {
            throw new RegraNegocioException("A data inicial deve ser anterior ou igual à data final");
        }
        Instant inicio = de != null ? de.atStartOfDay(FUSO).toInstant() : Instant.EPOCH;
        Instant fim = ate != null ? ate.plusDays(1).atStartOfDay(FUSO).toInstant() : Instant.now().plusSeconds(60);
        return new Instant[]{inicio, fim};
    }

    private static String decimal(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_EVEN).toPlainString().replace('.', ',');
    }

    @Transactional(readOnly = true)
    public StatsResponse stats(UsuarioAutenticado usuario) {
        if (usuario.isAdmin()) {
            return new StatsResponse(pessoaRepo.count(), agenciaRepo.count(), repo.count(), repo.somarSaldos());
        }
        return new StatsResponse(null, null, repo.countByCorrentistaId(usuario.pessoaId()),
                repo.somarSaldosPorPessoa(usuario.pessoaId()));
    }

    // ---------- Cadastro (ADMIN) ----------

    @Transactional
    public ContaResponse criar(CriarContaRequest req) {
        if (repo.existsByNumero(req.numero())) {
            throw new ConflitoException("Já existe uma conta com o número " + req.numero());
        }
        Pessoa pessoa = pessoaService.buscarEntidade(req.pessoaId());
        AgenciaBancaria agencia = agenciaService.buscarEntidade(req.agenciaId());
        ContaBancaria conta = repo.save(new ContaBancaria(req.numero(), req.limiteChequeEspecial(), pessoa, agencia));

        BigDecimal saldoInicial = req.saldoInicial() == null ? BigDecimal.ZERO : req.saldoInicial();
        if (saldoInicial.signum() > 0) {
            conta.depositar(saldoInicial);
            movRepo.save(new Movimentacao(conta, TipoMovimentacao.DEPOSITO, saldoInicial, null));
        }
        auditoria.registrar("CRIAR", ENTIDADE, conta.getId(), "Abriu a conta " + conta.getNumero() + " para "
                + pessoa.getNome() + " com depósito inicial de " + ContaBancaria.formatarReais(saldoInicial));
        return ContaResponse.de(conta);
    }

    @Transactional
    public ContaResponse atualizar(Long id, AtualizarContaRequest req) {
        ContaBancaria conta = travar(id);
        String limiteAnterior = ContaBancaria.formatarReais(conta.getLimiteChequeEspecial());
        conta.alterarLimite(req.limiteChequeEspecial());
        conta.setAgencia(agenciaService.buscarEntidade(req.agenciaId()));
        auditoria.registrar("ATUALIZAR", ENTIDADE, id, "Conta " + conta.getNumero() + ": limite de " + limiteAnterior
                + " para " + ContaBancaria.formatarReais(conta.getLimiteChequeEspecial())
                + ", agência " + conta.getAgencia().getNumero());
        return ContaResponse.de(conta);
    }

    /** Só é possível encerrar uma conta com saldo zerado, como em um banco de verdade. */
    @Transactional
    public void remover(Long id) {
        ContaBancaria conta = travar(id);
        if (conta.getSaldo().signum() != 0) {
            throw new RegraNegocioException("Zere o saldo da conta antes de encerrá-la (saldo atual: "
                    + ContaBancaria.formatarReais(conta.getSaldo()) + ")");
        }
        movRepo.deleteByContaId(id);
        repo.delete(conta);
        auditoria.registrar("EXCLUIR", ENTIDADE, id, "Encerrou a conta " + conta.getNumero());
    }

    // ---------- Operações ----------

    @Transactional
    public ContaResponse depositar(Long id, BigDecimal valor, UsuarioAutenticado usuario) {
        ContaBancaria conta = travar(id);
        verificarDono(conta, usuario);
        conta.depositar(valor);
        movRepo.save(new Movimentacao(conta, TipoMovimentacao.DEPOSITO, valor, null));
        auditarOperacaoDoAdmin(usuario, conta, "DEPOSITO", "Depósito de " + ContaBancaria.formatarReais(valor));
        return ContaResponse.de(conta);
    }

    @Transactional
    public ContaResponse sacar(Long id, BigDecimal valor, UsuarioAutenticado usuario) {
        ContaBancaria conta = travar(id);
        verificarDono(conta, usuario);
        conta.sacar(valor);
        movRepo.save(new Movimentacao(conta, TipoMovimentacao.SAQUE, valor, null));
        auditarOperacaoDoAdmin(usuario, conta, "SAQUE", "Saque de " + ContaBancaria.formatarReais(valor));
        return ContaResponse.de(conta);
    }

    /**
     * As duas contas são travadas sempre na mesma ordem (menor id primeiro). Assim, duas
     * transferências cruzadas (A→B e B→A) ao mesmo tempo não causam deadlock.
     */
    @Transactional
    public ContaResponse transferir(Long origemId, TransferenciaRequest req, UsuarioAutenticado usuario) {
        // Confere o dono antes de procurar o destino, para não revelar quais números de conta existem
        verificarDono(repo.findById(origemId).orElseThrow(() -> new RecursoNaoEncontradoException("Conta", origemId)),
                usuario);
        Long destinoId = repo.findByNumero(req.numeroContaDestino())
                .map(ContaBancaria::getId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta de destino", req.numeroContaDestino()));
        if (destinoId.equals(origemId)) {
            throw new RegraNegocioException("Não é possível transferir para a mesma conta");
        }

        ContaBancaria primeira = travar(Math.min(origemId, destinoId));
        ContaBancaria segunda = travar(Math.max(origemId, destinoId));
        ContaBancaria origem = primeira.getId().equals(origemId) ? primeira : segunda;
        ContaBancaria destino = origem == primeira ? segunda : primeira;

        verificarDono(origem, usuario);
        origem.sacar(req.valor());
        destino.depositar(req.valor());
        movRepo.save(new Movimentacao(origem, TipoMovimentacao.TRANSFERENCIA_ENVIADA, req.valor(), destino.getNumero()));
        movRepo.save(new Movimentacao(destino, TipoMovimentacao.TRANSFERENCIA_RECEBIDA, req.valor(), origem.getNumero()));
        auditarOperacaoDoAdmin(usuario, origem, "TRANSFERENCIA", "Transferência de "
                + ContaBancaria.formatarReais(req.valor()) + " para a conta " + destino.getNumero());
        return ContaResponse.de(origem);
    }

    // ---------- Auxiliares ----------

    /** O cliente movimentando a própria conta já fica no extrato; o admin mexendo em conta alheia vai para a auditoria. */
    private void auditarOperacaoDoAdmin(UsuarioAutenticado usuario, ContaBancaria conta, String acao, String descricao) {
        if (usuario.isAdmin()) {
            auditoria.registrar(acao, ENTIDADE, conta.getId(), descricao + " na conta " + conta.getNumero());
        }
    }

    private ContaBancaria travar(Long id) {
        return repo.findByIdParaAtualizacao(id).orElseThrow(() -> new RecursoNaoEncontradoException("Conta", id));
    }

    /** CLIENTE só pode ver e movimentar contas das quais é o correntista. */
    private static void verificarDono(ContaBancaria conta, UsuarioAutenticado usuario) {
        if (!usuario.isAdmin() && !conta.getCorrentista().getId().equals(usuario.pessoaId())) {
            throw new AccessDeniedException("Conta pertence a outro cliente");
        }
    }
}
