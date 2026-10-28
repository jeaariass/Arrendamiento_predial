package com.arriendaya.app.config;

import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el usuario administrador (es_admin = true) a partir de ADMIN_EMAIL y
 * ADMIN_PASSWORD. No tiene email ni clave por defecto: en un servidor publico
 * no debe existir una cuenta de administrador con credenciales conocidas, asi
 * que si falta cualquiera de las dos variables, simplemente no se crea admin.
 * No crea Propietario ni Cliente para el: el acceso de administracion es
 * independiente de esos roles.
 */
@Component
@Order(4)
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return;
        }
        if (usuarioRepository.existsByEmail(adminEmail)) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombre("Admin");
        admin.setApellido("Arriendaya");
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setEsAdmin(true);
        usuarioRepository.save(admin);
    }
}
