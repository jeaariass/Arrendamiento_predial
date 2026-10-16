package com.arriendaya.app.config;

import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea un usuario de ejemplo con acceso al panel de administracion
 * (es_admin = true). No crea Propietario ni Cliente para el: el acceso de
 * administracion es independiente de esos roles.
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final String EMAIL_ADMIN_DEMO = "admin.demo@arriendaya.co";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByEmail(EMAIL_ADMIN_DEMO)) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombre("Admin");
        admin.setApellido("Demo");
        admin.setEmail(EMAIL_ADMIN_DEMO);
        admin.setPasswordHash(passwordEncoder.encode("admin1234"));
        admin.setEsAdmin(true);
        usuarioRepository.save(admin);
    }
}
