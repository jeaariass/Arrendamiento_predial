package com.arriendaya.app.service;

import com.arriendaya.app.dto.PuntoMapa;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.repository.InmuebleRepository;
import com.arriendaya.app.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InmuebleService {

    private static final int MAX_CIUDADES_TOP = 5;
    private static final int MAX_SUGERENCIAS = 8;
    private static final int MAX_PUNTOS_MAPA = 1000;

    private final InmuebleRepository inmuebleRepository;
    private final UbicacionRepository ubicacionRepository;

    public List<Inmueble> buscar(String ciudad, Integer capacidad, LocalDate fechaInicio, LocalDate fechaFin) {
        return inmuebleRepository.buscar(
                normalizarCiudad(ciudad),
                capacidad,
                fechaInicio,
                fechaFin);
    }

    private String normalizarCiudad(String ciudad) {
        return (ciudad == null || ciudad.isBlank()) ? "" : ciudad.trim();
    }

    public List<Inmueble> destacados() {
        return inmuebleRepository.findTop8ByEstadoOrderByIdDesc("DISPONIBLE");
    }

    public Inmueble obtener(Long id) {
        return inmuebleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inmueble no encontrado: " + id));
    }

    public List<Inmueble> deMisPropiedades(Long propietarioId) {
        return inmuebleRepository.findByPropietarioId(propietarioId);
    }

    public Inmueble guardar(Inmueble inmueble) {
        return inmuebleRepository.save(inmueble);
    }

    public List<String> ciudadesTop() {
        return inmuebleRepository.ciudadesPorCantidadDesc().stream()
                .limit(MAX_CIUDADES_TOP)
                .toList();
    }

    public List<String> sugerirCiudades(String q) {
        String query = (q == null) ? "" : q.trim();
        return ubicacionRepository.buscarCiudades(query).stream()
                .limit(MAX_SUGERENCIAS)
                .toList();
    }

    public List<PuntoMapa> puntosEnMapa(double minLat, double maxLat, double minLon, double maxLon,
                                         String ciudad, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Inmueble> inmuebles = inmuebleRepository.buscarEnMapa(
                minLat, maxLat, minLon, maxLon,
                normalizarCiudad(ciudad),
                fechaInicio, fechaFin);
        return inmuebles.stream()
                .limit(MAX_PUNTOS_MAPA)
                .map(i -> new PuntoMapa(i.getId(), i.getTitulo(), i.getPrecioNoche(), i.getLatitud(), i.getLongitud(), i.getImagenUrl()))
                .toList();
    }
}
