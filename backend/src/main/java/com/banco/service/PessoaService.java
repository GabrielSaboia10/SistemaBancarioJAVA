package com.banco.service;

import com.banco.dto.PageResponse;
import com.banco.dto.PessoaDtos.*;
import com.banco.exception.ConflitoException;
import com.banco.exception.RecursoNaoEncontradoException;
import com.banco.model.Pessoa;
import com.banco.model.Role;
import com.banco.model.Usuario;
import com.banco.repository.ContaBancariaRepository;
import com.banco.repository.PessoaRepository;
import com.banco.repository.RefreshTokenRepository;
import com.banco.repository.UsuarioRepository;
import com.banco.util.Cpf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PessoaService {

    private static final String ALFABETO_SENHA = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ENTIDADE = "PESSOA";

    private final PessoaRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final ContaBancariaRepository contaRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;

    public PessoaService(PessoaRepository repo, UsuarioRepository usuarioRepo, ContaBancariaRepository contaRepo,
                         RefreshTokenRepository refreshRepo, PasswordEncoder encoder, AuditoriaService auditoria) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
        this.contaRepo = contaRepo;
        this.refreshRepo = refreshRepo;
        this.encoder = encoder;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public PageResponse<PessoaResponse> listar(String busca, Pageable pageable) {
        Page<Pessoa> page = (busca == null || busca.isBlank())
                ? repo.findAll(pageable)
                : repo.findByNomeContainingIgnoreCaseOrCpfContaining(busca.trim(), busca.trim(), pageable);

        // Uma consulta para os logins da página inteira, em vez de uma por pessoa
        Map<Long, Usuario> usuarios = usuarioRepo
                .findByPessoa_IdIn(page.getContent().stream().map(Pessoa::getId).toList()).stream()
                .collect(Collectors.toMap(Usuario::getPessoaId, Function.identity()));
        return PageResponse.de(page, p -> PessoaResponse.de(p, usuarios.get(p.getId())));
    }

    @Transactional(readOnly = true)
    public PessoaResponse buscar(Long id) {
        return PessoaResponse.de(buscarEntidade(id), usuarioRepo.findByPessoa_Id(id).orElse(null));
    }

    /** Cria a pessoa e o login dela (perfil CLIENTE) com uma senha temporária aleatória. */
    @Transactional
    public PessoaCriadaResponse criar(CriarPessoaRequest req) {
        String cpf = Cpf.formatar(req.cpf());
        if (repo.existsByCpf(cpf) || usuarioRepo.existsByCpf(cpf)) {
            throw new ConflitoException("Já existe um cadastro com este CPF");
        }
        Pessoa pessoa = repo.save(new Pessoa(cpf, req.nome().trim(), req.idade()));
        String senhaTemporaria = gerarSenhaTemporaria();
        Usuario usuario = new Usuario(cpf, null, Role.CLIENTE, pessoa);
        usuario.definirSenhaTemporaria(encoder.encode(senhaTemporaria));
        usuarioRepo.save(usuario);

        auditoria.registrar("CRIAR", ENTIDADE, pessoa.getId(), "Cadastrou " + pessoa.getNome() + " (" + cpf + ")");
        return new PessoaCriadaResponse(PessoaResponse.de(pessoa, usuario), senhaTemporaria);
    }

    @Transactional
    public PessoaResponse atualizar(Long id, AtualizarPessoaRequest req) {
        Pessoa pessoa = buscarEntidade(id);
        String antes = pessoa.getNome() + ", " + pessoa.getIdade() + " anos";
        pessoa.setNome(req.nome().trim());
        pessoa.setIdade(req.idade());
        auditoria.registrar("ATUALIZAR", ENTIDADE, id,
                "Alterou " + antes + " para " + pessoa.getNome() + ", " + pessoa.getIdade() + " anos");
        return PessoaResponse.de(pessoa, usuarioRepo.findByPessoa_Id(id).orElse(null));
    }

    @Transactional
    public void remover(Long id) {
        Pessoa pessoa = buscarEntidade(id);
        if (contaRepo.existsByCorrentistaId(id)) {
            throw new ConflitoException("Esta pessoa possui contas. Encerre as contas antes de excluí-la.");
        }
        usuarioRepo.deleteByPessoaId(id);
        repo.delete(pessoa);
        auditoria.registrar("EXCLUIR", ENTIDADE, id, "Excluiu " + pessoa.getNome() + " (" + pessoa.getCpf() + ")");
    }

    /** Bloqueia ou libera o acesso. Ao bloquear, as sessões abertas são encerradas. */
    @Transactional
    public PessoaResponse alterarBloqueio(Long id, boolean ativo) {
        Pessoa pessoa = buscarEntidade(id);
        Usuario usuario = buscarLogin(id);
        usuario.setAtivo(ativo);
        if (!ativo) refreshRepo.deleteByUsuarioId(usuario.getId());
        auditoria.registrar(ativo ? "DESBLOQUEAR" : "BLOQUEAR", ENTIDADE, id,
                (ativo ? "Liberou" : "Bloqueou") + " o acesso de " + pessoa.getNome());
        return PessoaResponse.de(pessoa, usuario);
    }

    /** Gera uma nova senha temporária (ex.: cliente esqueceu a senha) e encerra as sessões abertas. */
    @Transactional
    public SenhaRedefinidaResponse redefinirSenha(Long id) {
        Pessoa pessoa = buscarEntidade(id);
        Usuario usuario = buscarLogin(id);
        String senhaTemporaria = gerarSenhaTemporaria();
        usuario.definirSenhaTemporaria(encoder.encode(senhaTemporaria));
        refreshRepo.deleteByUsuarioId(usuario.getId());
        auditoria.registrar("REDEFINIR_SENHA", ENTIDADE, id, "Gerou nova senha temporária para " + pessoa.getNome());
        return new SenhaRedefinidaResponse(usuario.getCpf(), senhaTemporaria);
    }

    Pessoa buscarEntidade(Long id) {
        return repo.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa", id));
    }

    private Usuario buscarLogin(Long pessoaId) {
        return usuarioRepo.findByPessoa_Id(pessoaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Login da pessoa", pessoaId));
    }

    private static String gerarSenhaTemporaria() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(ALFABETO_SENHA.charAt(RANDOM.nextInt(ALFABETO_SENHA.length())));
        }
        return sb.toString();
    }
}
