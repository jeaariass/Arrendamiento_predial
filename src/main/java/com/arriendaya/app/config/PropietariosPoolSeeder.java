package com.arriendaya.app.config;

import com.arriendaya.app.model.Propietario;
import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.repository.PropietarioRepository;
import com.arriendaya.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Crea un grupo chico de propietarios (menos duenos que inmuebles, para que
 * cada uno termine con varias propiedades), que luego usa ImportadorAirbnbSeeder
 * para repartir los 500 alojamientos reales en vez de crear un dueno por cada
 * host de Inside Airbnb (quedaban 380+ cuentas sin uso real).
 *
 * Siempre corre (los inmuebles importados necesitan un propietario_id), pero
 * la clave solo es conocida (y aparece en la lista del panel de admin) cuando
 * SEED_DEMO_USERS=true; si esta en false, cada uno recibe una clave aleatoria
 * para que no quede un login publico conocido en un servidor en Internet.
 */
@Component
@Order(0)
@RequiredArgsConstructor
public class PropietariosPoolSeeder implements CommandLineRunner {

    public static final int TAMANO_POOL = 20;

    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SEED_DEMO_USERS:true}")
    private boolean seedDemoUsers;

    public static String email(int indice) {
        return String.format("dueno%02d@arriendaya.demo", indice);
    }

    public static String claveDemo(int indice) {
        return String.format("dueno%02d1234", indice);
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByEmail(email(1))) {
            return;
        }
        for (int i = 1; i <= TAMANO_POOL; i++) {
            Usuario usuario = new Usuario();
            usuario.setNombre("Dueno");
            usuario.setApellido("Numero " + i);
            usuario.setEmail(email(i));
            String clave = seedDemoUsers ? claveDemo(i) : UUID.randomUUID().toString();
            usuario.setPasswordHash(passwordEncoder.encode(clave));
            usuario = usuarioRepository.save(usuario);
            propietarioRepository.save(new Propietario(usuario));
        }
    }

    /** Propietarios del pool ya guardados, para repartir inmuebles entre ellos. */
    public List<Propietario> cargarPool() {
        List<Propietario> pool = new ArrayList<>();
        for (int i = 1; i <= TAMANO_POOL; i++) {
            usuarioRepository.findByEmail(email(i))
                    .flatMap(u -> propietarioRepository.findById(u.getId()))
                    .ifPresent(pool::add);
        }
        return pool;
    }
}
