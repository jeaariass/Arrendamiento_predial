package com.arriendaya.app.service;

import com.arriendaya.app.dto.ResultadoGiro;
import com.arriendaya.app.model.EstadoReserva;
import com.arriendaya.app.model.Giro;
import com.arriendaya.app.model.Pago;
import com.arriendaya.app.model.Reserva;
import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.repository.GiroRepository;
import com.arriendaya.app.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Giro = la plataforma le paga al propietario lo que el cliente ya pago por una reserva.
 * Es la contraparte de Pago (cliente -> plataforma); Giro es plataforma -> propietario.
 */
@Service
@RequiredArgsConstructor
public class GiroService {

    private final PagoRepository pagoRepository;
    private final GiroRepository giroRepository;
    private final UsuarioService usuarioService;

    /** Paga de una sola vez todos los pagos confirmados de un inmueble que la plataforma aun no le ha girado al propietario. */
    @Transactional
    public ResultadoGiro pagarPendientesDeInmueble(Long inmuebleId, Long adminUsuarioId) {
        List<Pago> pendientes = pagoRepository.pendientesDeGiroPorInmueble(inmuebleId);
        if (pendientes.isEmpty()) {
            throw new IllegalArgumentException("No hay pagos pendientes de girar para este inmueble");
        }
        Usuario admin = usuarioService.porId(adminUsuarioId)
                .orElseThrow(() -> new IllegalStateException("Usuario invalido"));

        BigDecimal total = BigDecimal.ZERO;
        for (Pago pago : pendientes) {
            Reserva reserva = pago.getReserva();
            Giro giro = new Giro();
            giro.setReserva(reserva);
            giro.setBanco(pago.getBanco());
            giro.setPropietario(reserva.getInmueble().getPropietario());
            giro.setPago(pago);
            giro.setRealizadoPor(admin);
            giro.setMonto(pago.getMonto());
            giro.setEstado("PAGADO");
            giroRepository.save(giro);
            total = total.add(pago.getMonto());
        }
        return new ResultadoGiro(pendientes.size(), total);
    }

    /** Como va el pago de una reserva, en palabras simples para el propietario/admin. */
    public String estadoPago(Reserva reserva) {
        if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
            return "Esperando pago del cliente";
        }
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            return "-";
        }
        return pagoRepository.findByReservaId(reserva.getId())
                .map(pago -> giroRepository.existsByPagoId(pago.getId())
                        ? "Ya se le giro al propietario"
                        : "Pagado por el cliente, pendiente de girar")
                .orElse("Pagado por el cliente, pendiente de girar");
    }
}
