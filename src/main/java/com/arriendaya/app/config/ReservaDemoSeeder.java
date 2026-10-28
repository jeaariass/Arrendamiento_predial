package com.arriendaya.app.config;

import com.arriendaya.app.model.Cliente;
import com.arriendaya.app.model.EstadoReserva;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.Reserva;
import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.repository.ClienteRepository;
import com.arriendaya.app.repository.InmuebleRepository;
import com.arriendaya.app.repository.ReservaRepository;
import com.arriendaya.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Crea un cliente de ejemplo y unas pocas reservas de muestra, para que el
 * panel de administracion tenga casos reales de "ocupado ahora", "ocupado
 * mas adelante" y "libre" que mostrar. Sin esto, con 0 reservas en la BD
 * todo se ve "libre" y no se puede demostrar nada.
 */
@Component
@Order(5)
@RequiredArgsConstructor
public class ReservaDemoSeeder implements CommandLineRunner {

    private static final String EMAIL_CLIENTE_DEMO = "cliente.demo@arriendaya.co";

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final InmuebleRepository inmuebleRepository;
    private final ReservaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SEED_DEMO_USERS:true}")
    private boolean seedDemoUsers;

    @Value("${CLIENTE_DEMO_PASSWORD:cliente1234}")
    private String clienteDemoPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedDemoUsers || usuarioRepository.existsByEmail(EMAIL_CLIENTE_DEMO)) {
            return;
        }

        List<Inmueble> inmuebles = inmuebleRepository.findAll(PageRequest.of(0, 3)).getContent();
        if (inmuebles.size() < 3) {
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNombre("Cliente");
        usuario.setApellido("Demo");
        usuario.setEmail(EMAIL_CLIENTE_DEMO);
        usuario.setPasswordHash(passwordEncoder.encode(clienteDemoPassword));
        usuario = usuarioRepository.save(usuario);
        Cliente cliente = clienteRepository.save(new Cliente(usuario));

        LocalDate hoy = LocalDate.now();

        // Inmueble 0: ocupado ahora mismo y de nuevo mas adelante (deja un hueco libre entre medio).
        reserva(cliente, inmuebles.get(0), hoy.minusDays(2), hoy.plusDays(3), EstadoReserva.CONFIRMADA);
        reserva(cliente, inmuebles.get(0), hoy.plusDays(20), hoy.plusDays(25), EstadoReserva.CONFIRMADA);

        // Inmueble 1: libre ahora, ocupado mas adelante.
        reserva(cliente, inmuebles.get(1), hoy.plusDays(10), hoy.plusDays(15), EstadoReserva.PENDIENTE);

        // Inmueble 2: completamente libre (sin reservas), para contraste.
    }

    private void reserva(Cliente cliente, Inmueble inmueble, LocalDate inicio, LocalDate fin, EstadoReserva estado) {
        long noches = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin);
        Reserva r = new Reserva();
        r.setCliente(cliente);
        r.setInmueble(inmueble);
        r.setFechaInicio(inicio);
        r.setFechaFin(fin);
        r.setEstado(estado);
        r.setValorTotal(inmueble.getPrecioNoche().multiply(BigDecimal.valueOf(noches)));
        reservaRepository.save(r);
    }
}
