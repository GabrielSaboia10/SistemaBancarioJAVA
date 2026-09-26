package com.banco.config;

import com.banco.model.Role;
import com.banco.model.Usuario;
import com.banco.repository.UsuarioRepository;
import com.banco.util.Cpf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Cria o primeiro administrador a partir de ADMIN_CPF / ADMIN_SENHA, se ainda não existir nenhum. */
@Component
@Order(1)
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder;
    private final AppProperties props;

    public AdminInitializer(UsuarioRepository usuarioRepo, PasswordEncoder encoder, AppProperties props) {
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepo.existsByRole(Role.ADMIN)) return;

        String cpf = Cpf.formatar(props.admin().cpf());
        String senha = props.admin().senha();
        if (cpf == null || senha == null || senha.isBlank()) {
            log.warn("Nenhum administrador cadastrado. Defina ADMIN_CPF e ADMIN_SENHA para criar o primeiro.");
            return;
        }
        usuarioRepo.save(new Usuario(cpf, encoder.encode(senha), Role.ADMIN, null));
        log.info("Administrador inicial criado (CPF {})", cpf);
    }
}
