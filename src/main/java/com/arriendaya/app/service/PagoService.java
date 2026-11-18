package com.arriendaya.app.service;

import com.arriendaya.app.model.Banco;
import com.arriendaya.app.model.Cliente;
import com.arriendaya.app.model.EstadoReserva;
import com.arriendaya.app.model.Pago;
import com.arriendaya.app.model.Reserva;
import com.arriendaya.app.repository.BancoRepository;
import com.arriendaya.app.repository.InmuebleRepository;
import com.arriendaya.app.repository.PagoRepository;
import com.arriendaya.app.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final ReservaRepository reservaRepository;
    private final InmuebleRepository inmuebleRepository;
    private final BancoRepository bancoRepository;
    private final PagoRepository pagoRepository;

    @Transactional
    public Pago pagar(Cliente cliente, Long reservaId, Long bancoId, String metodoPago, String referencia) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservaId));
        if (!reserva.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Esa reserva no es tuya");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalArgumentException("Esta reserva no esta pendiente de pago");
        }
        if (pagoRepository.findByReservaId(reservaId).isPresent()) {
            throw new IllegalArgumentException("Esta reserva ya tiene un pago registrado");
        }
        Banco banco = bancoRepository.findById(bancoId)
                .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado: " + bancoId));
        if ("TARJETA".equals(metodoPago) && (referencia == null || referencia.isBlank())) {
            throw new IllegalArgumentException("Para pagar con tarjeta necesitamos el numero de tarjeta");
        }

        // Bloquea el inmueble para que dos pagos a la vez (de reservas PENDIENTE distintas, en fechas
        // cruzadas) no confirmen ambos: una PENDIENTE no reserva el cupo, asi que se revalida aqui.
        inmuebleRepository.buscarParaBloquear(reserva.getInmueble().getId());
        if (reservaRepository.existeSolapadaExcluyendo(reserva.getInmueble().getId(), reservaId,
                reserva.getFechaInicio(), reserva.getFechaFin())) {
            throw new IllegalArgumentException(
                    "Esas fechas ya quedaron confirmadas con otra reserva mientras esta esperaba el pago");
        }

        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setBanco(banco);
        pago.setMonto(reserva.getValorTotal());
        pago.setMetodoPago(metodoPago);
        pago.setReferencia("TARJETA".equals(metodoPago) ? referencia : null);
        pago = pagoRepository.save(pago);

        reserva.setEstado(EstadoReserva.CONFIRMADA);
        return pago;
    }
}
