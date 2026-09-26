package com.banco.service;

import com.banco.config.AppProperties;
import com.banco.dto.AuthDtos.*;
import com.banco.exception.AcessoBloqueadoException;
import com.banco.exception.CredenciaisInvalidasException;
import com.banco.exception.MuitasTentativasException;
import com.banco.exception.RecursoNaoEncontradoException;
import com.banco.exception.RegraNegocioException;
import com.banco.model.RefreshToken;
import com.banco.model.Usuario;
import com.banco.repository.RefreshTokenRepository;
import com.banco.repository.UsuarioRepository;
import com.banco.security.RateLimiter;
import com.banco.security.TokenService;
import com.banco.util.Cpf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String MSG_CREDENCIAIS = "CPF ou senha inválidos";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    private final RateLimiter rateLimiter;
    private final AppProperties props;
    /** Hash usado quando o CPF não existe, para o tempo de resposta não revelar quais CPFs são clientes. */
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarioRepo, RefreshTokenRepository refreshRepo, PasswordEncoder encoder,
                       TokenService tokenService, RateLimiter rateLimiter, AppProperties props) {
        this.usuarioRepo = usuarioRepo;
        this.refreshRepo = refreshRepo;
        this.encoder = encoder;
        this.tokenService = tokenService;
        this.rateLimiter = rateLimiter;
        this.props = props;
        this.hashFalso = encoder.encode("senha-inexistente");
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        String cpf = Cpf.formatar(req.cpf());
        if (cpf == null) throw new CredenciaisInvalidasException(MSG_CREDENCIAIS);

        String chaveCpf = "login-cpf:" + cpf;
        RateLimiter.Resultado bloqueio = rateLimiter.consultar(chaveCpf, props.rateLimit().loginCpf());
        if (!bloqueio.permitido()) {
            throw new MuitasTentativasException("Muitas tentativas de login para este CPF. Tente novamente em "
                    + bloqueio.segundosParaLiberar() + " segundos.");
        }

        Usuario usuario = usuarioRepo.findByCpf(cpf).orElse(null);
        boolean senhaOk = encoder.matches(req.senha(), usuario != null ? usuario.getSenha() : hashFalso);
        if (usuario == null || !senhaOk) {
            rateLimiter.consumir(chaveCpf, props.rateLimit().loginCpf());
            log.warn("Falha de login para CPF {}", mascarar(cpf));
            throw new CredenciaisInvalidasException(MSG_CREDENCIAIS);
        }
        // Só informa o bloqueio depois da senha correta, para não revelar o status a terceiros
        if (!usuario.isAtivo()) {
            throw new AcessoBloqueadoException();
        }

        rateLimiter.limpar(chaveCpf);
        log.info("Login do usuário {} ({})", usuario.getId(), usuario.getRole());
        return emitirTokens(usuario);
    }

    /** Troca um refresh token válido por um novo par de tokens (o antigo é invalidado: rotação). */
    @Transactional
    public TokenResponse refresh(RefreshRequest req) {
        RefreshToken atual = refreshRepo.findByTokenHash(hash(req.refreshToken()))
                .orElseThrow(() -> new CredenciaisInvalidasException("Sessão expirada. Faça login novamente."));
        refreshRepo.delete(atual);
        if (atual.isExpirado() || !atual.getUsuario().isAtivo()) {
            throw new CredenciaisInvalidasException("Sessão expirada. Faça login novamente.");
        }
        return emitirTokens(atual.getUsuario());
    }

    @Transactional
    public void logout(RefreshRequest req) {
        refreshRepo.deleteByTokenHash(hash(req.refreshToken()));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse me(Long usuarioId) {
        return usuarioRepo.findWithPessoaById(usuarioId)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", usuarioId));
    }

    /** Troca a senha e derruba todas as sessões abertas do usuário. */
    @Transactional
    public void trocarSenha(Long usuarioId, TrocarSenhaRequest req) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", usuarioId));
        if (!encoder.matches(req.senhaAtual(), usuario.getSenha())) {
            throw new RegraNegocioException("Senha atual incorreta");
        }
        if (req.senhaAtual().equals(req.novaSenha())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da atual");
        }
        usuario.definirSenha(encoder.encode(req.novaSenha()));
        refreshRepo.deleteByUsuarioId(usuarioId);
        log.info("Usuário {} trocou a senha; sessões encerradas", usuarioId);
    }

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void limparRefreshTokensExpirados() {
        int removidos = refreshRepo.deleteExpirados(Instant.now());
        if (removidos > 0) log.info("{} refresh tokens expirados removidos", removidos);
    }

    private TokenResponse emitirTokens(Usuario usuario) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expira = Instant.now().plus(Duration.ofDays(props.jwt().refreshTokenDias()));
        refreshRepo.save(new RefreshToken(hash(refreshToken), usuario, expira));

        return new TokenResponse(tokenService.gerarAccessToken(usuario), refreshToken,
                tokenService.getDuracaoSegundos(), UsuarioResponse.de(usuario));
    }

    private static String hash(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String mascarar(String cpf) {
        return "***." + cpf.substring(4, 7) + ".***-**";
    }
}
