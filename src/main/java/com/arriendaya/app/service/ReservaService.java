package com.arriendaya.app.service;

import com.arriendaya.app.model.Cliente;
import com.arriendaya.app.model.EstadoReserva;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.Reserva;
import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private static final Set<EstadoReserva> ESTADOS_QUE_DEBEN = Set.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

    private final ReservaRepository reservaRepository;
    private final InmuebleService inmuebleService;
    private final UsuarioService usuarioService;

    public BigDecimal calcularValor(Long inmuebleId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null || !fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("Las fechas de la reserva no son validas");
        }
        if (fechaInicio.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de llegada no puede ser en el pasado");
        }
        Inmueble inmueble = inmuebleService.obtener(inmuebleId);
        if (reservaRepository.existeSolapada(inmuebleId, fechaInicio, fechaFin)) {
            throw new IllegalArgumentException("El inmueble ya esta reservado en esas fechas");
        }
        long noches = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        return inmueble.getPrecioNoche().multiply(BigDecimal.valueOf(noches));
    }

    @Transactional
    public Reserva reservar(Cliente cliente, Long inmuebleId, LocalDate fechaInicio, LocalDate fechaFin) {
        BigDecimal valorTotal = calcularValor(inmuebleId, fechaInicio, fechaFin);
        Inmueble inmueble = inmuebleService.obtener(inmuebleId);

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setInmueble(inmueble);
        reserva.setFechaInicio(fechaInicio);
        reserva.setFechaFin(fechaFin);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setValorTotal(valorTotal);
        return reservaRepository.save(reserva);
    }

    /** El admin asigna un inmueble libre a una persona ya registrada, con un valor que el decide. */
    @Transactional
    public Reserva asignarPorAdmin(String emailCliente, Long inmuebleId, LocalDate fechaInicio, LocalDate fechaFin,
                                    BigDecimal valorTotal) {
        if (fechaInicio == null || fechaFin == null || !fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("Las fechas de la reserva no son validas");
        }
        if (valorTotal == null || valorTotal.signum() <= 0) {
            throw new IllegalArgumentException("El valor a pagar debe ser mayor a cero");
        }
        Usuario usuario = usuarioService.porEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("No existe una persona registrada con ese correo"));
        Inmueble inmueble = inmuebleService.obtener(inmuebleId);
        if (reservaRepository.existeSolapada(inmuebleId, fechaInicio, fechaFin)) {
            throw new IllegalArgumentException("El inmueble ya esta reservado en esas fechas");
        }
        Cliente cliente = usuarioService.convertirEnCliente(usuario);

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setInmueble(inmueble);
        reserva.setFechaInicio(fechaInicio);
        reserva.setFechaFin(fechaFin);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setValorTotal(valorTotal);
        return reservaRepository.save(reserva);
    }

    public List<Reserva> deCliente(Long clienteId) {
        return reservaRepository.findByClienteIdOrderByFechaReservaDesc(clienteId);
    }

    public List<Reserva> deInmueblesDePropietario(Long propietarioId) {
        return reservaRepository.findByInmueble_Propietario_IdOrderByFechaReservaDesc(propietarioId);
    }

    /** Lo que debe un cliente ahora mismo: suma de reservas pendientes/confirmadas (canceladas no cuentan). */
    public BigDecimal deudaPendiente(List<Reserva> reservasDelCliente) {
        return reservasDelCliente.stream()
                .filter(r -> ESTADOS_QUE_DEBEN.contains(r.getEstado()))
                .map(Reserva::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Reserva cambiarEstado(Long reservaId, EstadoReserva estado) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservaId));
        reserva.setEstado(estado);
        return reserva;
    }
}
