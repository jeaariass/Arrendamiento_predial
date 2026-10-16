package com.arriendaya.app.service;

import com.arriendaya.app.model.Cliente;
import com.arriendaya.app.model.EstadoReserva;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.Reserva;
import com.arriendaya.app.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final InmuebleService inmuebleService;

    @Transactional
    public Reserva reservar(Cliente cliente, Long inmuebleId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null || !fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("Las fechas de la reserva no son validas");
        }
        Inmueble inmueble = inmuebleService.obtener(inmuebleId);

        long noches = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        BigDecimal valorTotal = inmueble.getPrecioNoche().multiply(BigDecimal.valueOf(noches));

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setInmueble(inmueble);
        reserva.setFechaInicio(fechaInicio);
        reserva.setFechaFin(fechaFin);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setValorTotal(valorTotal);
        return reservaRepository.save(reserva);
    }

    public List<Reserva> deCliente(Long clienteId) {
        return reservaRepository.findByClienteIdOrderByFechaReservaDesc(clienteId);
    }

    public List<Reserva> deInmueblesDePropietario(Long propietarioId) {
        return reservaRepository.findByInmueble_Propietario_IdOrderByFechaReservaDesc(propietarioId);
    }

    @Transactional
    public Reserva cambiarEstado(Long reservaId, EstadoReserva estado) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservaId));
        reserva.setEstado(estado);
        return reserva;
    }
}
