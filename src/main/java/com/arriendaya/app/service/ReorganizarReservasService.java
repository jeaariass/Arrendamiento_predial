package com.arriendaya.app.service;

import com.arriendaya.app.dto.ResultadoReorganizacion;
import com.arriendaya.app.model.Cliente;
import com.arriendaya.app.model.EstadoReserva;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.Reserva;
import com.arriendaya.app.repository.ClienteRepository;
import com.arriendaya.app.repository.GiroRepository;
import com.arriendaya.app.repository.InmuebleRepository;
import com.arriendaya.app.repository.PagoRepository;
import com.arriendaya.app.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * Borra todas las reservas y vuelve a crear unas nuevas al azar, repartidas
 * en una ventana de -90 a +90 dias desde hoy, dejando algunos inmuebles
 * libres y otros ocupados en 1 a 3 bloques separados por 2 a 3 dias de hueco
 * entre uno y el siguiente. Pensado para correr desde el panel de admin cada
 * vez que se quiera una foto nueva de "quien tiene que" para la demo.
 */
@Service
@RequiredArgsConstructor
public class ReorganizarReservasService {

    private static final int DIAS_ATRAS = 90;
    private static final int DIAS_ADELANTE = 90;
    private static final double PROBABILIDAD_INMUEBLE_OCUPADO = 0.45;

    private final InmuebleRepository inmuebleRepository;
    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final GiroRepository giroRepository;

    @Transactional
    public ResultadoReorganizacion reorganizar() {
        pagoRepository.deleteAll();
        giroRepository.deleteAll();
        reservaRepository.deleteAll();

        List<Cliente> clientes = clienteRepository.findAll();
        if (clientes.isEmpty()) {
            return new ResultadoReorganizacion(0, 0);
        }

        List<Inmueble> inmuebles = inmuebleRepository.findAll();
        Random random = new Random();
        LocalDate hoy = LocalDate.now();
        LocalDate inicioVentana = hoy.minusDays(DIAS_ATRAS);
        LocalDate finVentana = hoy.plusDays(DIAS_ADELANTE);

        int inmueblesConReserva = 0;
        int reservasCreadas = 0;

        for (Inmueble inmueble : inmuebles) {
            if (random.nextDouble() > PROBABILIDAD_INMUEBLE_OCUPADO) {
                continue;
            }

            int bloques = 1 + random.nextInt(3);
            int diasNecesarios = bloques * 9; // tope por bloque: 6 noches + 3 de brecha
            int margen = Math.max(1, (DIAS_ATRAS + DIAS_ADELANTE) - diasNecesarios);
            LocalDate cursor = inicioVentana.plusDays(random.nextInt(margen));
            boolean algunaCreada = false;

            for (int b = 0; b < bloques && cursor.isBefore(finVentana); b++) {
                int noches = 2 + random.nextInt(5);
                LocalDate inicio = cursor;
                LocalDate fin = inicio.plusDays(noches);
                if (fin.isAfter(finVentana)) {
                    break;
                }

                Cliente cliente = clientes.get(random.nextInt(clientes.size()));
                Reserva reserva = new Reserva();
                reserva.setCliente(cliente);
                reserva.setInmueble(inmueble);
                reserva.setFechaInicio(inicio);
                reserva.setFechaFin(fin);
                reserva.setEstado(fin.isBefore(hoy) ? EstadoReserva.FINALIZADA : EstadoReserva.CONFIRMADA);
                reserva.setValorTotal(inmueble.getPrecioNoche().multiply(BigDecimal.valueOf(noches)));
                reservaRepository.save(reserva);
                reservasCreadas++;
                algunaCreada = true;

                int brecha = 2 + random.nextInt(2);
                cursor = fin.plusDays(brecha);
            }

            if (algunaCreada) {
                inmueblesConReserva++;
            }
        }

        return new ResultadoReorganizacion(inmueblesConReserva, reservasCreadas);
    }
}
