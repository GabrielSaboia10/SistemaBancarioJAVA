package com.banco.security;

import com.banco.config.AppProperties;
import com.banco.model.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    public static final String ISSUER = "sistema-bancario-api";

    private final JwtEncoder encoder;
    private final Duration duracao;

    public TokenService(JwtEncoder encoder, AppProperties props) {
        this.encoder = encoder;
        this.duracao = Duration.ofMinutes(props.jwt().accessTokenMinutos());
    }

    public String gerarAccessToken(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(agora)
                .expiresAt(agora.plus(duracao))
                .claim("role", usuario.getRole().name())
                .claim("cpf", usuario.getCpf());
        if (usuario.getPessoaId() != null) {
            claims.claim("pessoaId", usuario.getPessoaId());
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    public long getDuracaoSegundos() {
        return duracao.toSeconds();
    }
}
