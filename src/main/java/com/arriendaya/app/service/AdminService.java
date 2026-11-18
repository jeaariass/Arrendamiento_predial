package com.arriendaya.app.service;

import com.arriendaya.app.config.PropietariosPoolSeeder;
import com.arriendaya.app.dto.CredencialDemo;
import com.arriendaya.app.dto.EstadoOcupacion;
import com.arriendaya.app.dto.PersonaResumen;
import com.arriendaya.app.dto.PuntoCalor;
import com.arriendaya.app.dto.RangoFecha;
import com.arriendaya.app.dto.ResumenAdmin;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.Reserva;
import com.arriendaya.app.repository.ClienteRepository;
import com.arriendaya.app.repository.InmuebleRepository;
import com.arriendaya.app.repository.PagoRepository;
import com.arriendaya.app.repository.PropietarioRepository;
import com.arriendaya.app.repository.ReservaRepository;
import com.arriendaya.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int TAMANO_PAGINA = 30;
    private static final int DIAS_VENTANA = 90;

    private final InmuebleRepository inmuebleRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PropietarioRepository propietarioRepository;
    private final PagoRepository pagoRepository;

    @Value("${SEED_DEMO_USERS:true}")
    private boolean seedDemoUsers;

    public Page<Inmueble> listarPagina(String ciudad, String estado, String q, int pagina) {
        String ciudadQuery = (ciudad == null || ciudad.isBlank()) ? "" : ciudad.trim();
        String estadoQuery = (estado == null) ? "" : estado.trim().toUpperCase();
        if (!estadoQuery.equals("LIBRE") && !estadoQuery.equals("OCUPADO")) {
            estadoQuery = "";
        }
        String qQuery = (q == null || q.isBlank()) ? "" : q.trim();
        return inmuebleRepository.paraAdmin(ciudadQuery, estadoQuery, qQuery, PageRequest.of(Math.max(pagina, 0), TAMANO_PAGINA));
    }

    /** Lista de usuario/clave de los duenos del pool demo, solo visible si el demo esta prendido. */
    public List<CredencialDemo> listaArrendadores() {
        if (!seedDemoUsers) {
            return List.of();
        }
        List<CredencialDemo> lista = new ArrayList<>();
        for (int i = 1; i <= PropietariosPoolSeeder.TAMANO_POOL; i++) {
            lista.add(new CredencialDemo(PropietariosPoolSeeder.email(i), PropietariosPoolSeeder.claveDemo(i)));
        }
        return lista;
    }

    public List<PersonaResumen> buscarPersonas(String q) {
        String query = (q == null) ? "" : q.trim();
        if (query.isEmpty()) {
            return List.of();
        }
        return usuarioRepository
                .findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, query)
                .stream()
                .map(u -> new PersonaResumen(u, clienteRepository.existsById(u.getId()), propietarioRepository.existsById(u.getId())))
                .toList();
    }

    public ResumenAdmin resumen() {
        return new ResumenAdmin(
                inmuebleRepository.count(),
                reservaRepository.contarActivas(),
                reservaRepository.sumarDeudaPendiente(),
                pagoRepository.sumarTotalPagado());
    }

    public List<PuntoCalor> puntosDeCalor() {
        return inmuebleRepository.puntosDeReservasActivas().stream()
                .map(fila -> new PuntoCalor((Double) fila[0], (Double) fila[1], (Long) fila[2]))
                .toList();
    }

    public EstadoOcupacion calcularOcupacion(Inmueble inmueble) {
        LocalDate hoy = LocalDate.now();
        LocalDate finVentana = hoy.plusDays(DIAS_VENTANA);

        // Solo las reservas CONFIRMADAS (pagadas) ocupan de verdad un inmueble; una PENDIENTE no lo bloquea.
        List<Reserva> reservas = reservaRepository.confirmadasFuturasPorInmueble(inmueble.getId(), hoy);

        List<RangoFecha> ocupados = new ArrayList<>();
        for (Reserva r : reservas) {
            LocalDate inicio = r.getFechaInicio().isBefore(hoy) ? hoy : r.getFechaInicio();
            if (!inicio.isAfter(finVentana)) {
                ocupados.add(new RangoFecha(inicio, r.getFechaFin()));
            }
        }

        boolean ocupadoHoy = ocupados.stream()
                .anyMatch(r -> !r.inicio().isAfter(hoy) && !r.fin().isBefore(hoy));

        List<RangoFecha> libres = new ArrayList<>();
        LocalDate cursor = hoy;
        for (RangoFecha r : ocupados) {
            if (r.inicio().isAfter(cursor)) {
                libres.add(new RangoFecha(cursor, r.inicio().minusDays(1)));
            }
            if (r.fin().plusDays(1).isAfter(cursor)) {
                cursor = r.fin().plusDays(1);
            }
        }
        if (cursor.isBefore(finVentana)) {
            libres.add(new RangoFecha(cursor, finVentana));
        }

        String clienteActual = reservas.stream()
                .filter(r -> !r.getFechaInicio().isAfter(hoy) && !r.getFechaFin().isBefore(hoy))
                .findFirst()
                .map(r -> r.getCliente().getUsuario().getNombre() + " " + r.getCliente().getUsuario().getApellido())
                .orElse(null);

        BigDecimal ganancia = pagoRepository.sumarPorInmueble(inmueble.getId());
        BigDecimal montoPendienteGiro = pagoRepository.pendientesDeGiroPorInmueble(inmueble.getId()).stream()
                .map(com.arriendaya.app.model.Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new EstadoOcupacion(inmueble, ocupadoHoy, ocupados, libres, clienteActual, ganancia, montoPendienteGiro);
    }
}
