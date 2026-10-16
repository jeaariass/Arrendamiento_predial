package com.arriendaya.app.service;

import com.arriendaya.app.dto.EstadoOcupacion;
import com.arriendaya.app.dto.RangoFecha;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.Reserva;
import com.arriendaya.app.repository.InmuebleRepository;
import com.arriendaya.app.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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

    public Page<Inmueble> listarPagina(String ciudad, int pagina) {
        String query = (ciudad == null || ciudad.isBlank()) ? "" : ciudad.trim();
        return inmuebleRepository.paraAdmin(query, PageRequest.of(Math.max(pagina, 0), TAMANO_PAGINA));
    }

    public EstadoOcupacion calcularOcupacion(Inmueble inmueble) {
        LocalDate hoy = LocalDate.now();
        LocalDate finVentana = hoy.plusDays(DIAS_VENTANA);

        List<Reserva> reservas = reservaRepository.futurasPorInmueble(inmueble.getId(), hoy);

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

        return new EstadoOcupacion(inmueble, ocupadoHoy, ocupados, libres);
    }
}
