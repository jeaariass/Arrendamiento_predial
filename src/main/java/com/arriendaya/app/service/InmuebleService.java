package com.arriendaya.app.service;

import com.arriendaya.app.dto.PuntoMapa;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.TipoInmueble;
import com.arriendaya.app.model.Ubicacion;
import com.arriendaya.app.repository.InmuebleRepository;
import com.arriendaya.app.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InmuebleService {

    private static final int MAX_CIUDADES_TOP = 5;
    private static final int MAX_SUGERENCIAS = 30;
    private static final int MAX_PUNTOS_MAPA = 1000;

    private final InmuebleRepository inmuebleRepository;
    private final UbicacionRepository ubicacionRepository;

    public List<Inmueble> buscar(String ciudad, Integer capacidad, LocalDate fechaInicio, LocalDate fechaFin,
                                  TipoInmueble tipo, String orden) {
        List<Inmueble> resultados = inmuebleRepository.buscar(
                normalizarCiudad(ciudad),
                capacidad,
                fechaInicio,
                fechaFin,
                tipo);
        if ("precio_asc".equals(orden)) {
            return resultados.stream().sorted(Comparator.comparing(Inmueble::getPrecioNoche)).toList();
        }
        if ("precio_desc".equals(orden)) {
            return resultados.stream().sorted(Comparator.comparing(Inmueble::getPrecioNoche).reversed()).toList();
        }
        return resultados;
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

    public void eliminar(Long id) {
        inmuebleRepository.deleteById(id);
    }

    public Ubicacion obtenerOCrearUbicacion(String ciudad, String pais) {
        return ubicacionRepository.findByCiudadIgnoreCaseAndPaisIgnoreCase(ciudad.trim(), pais.trim())
                .orElseGet(() -> {
                    Ubicacion u = new Ubicacion();
                    u.setCiudad(ciudad.trim());
                    u.setPais(pais.trim());
                    return ubicacionRepository.save(u);
                });
    }

    public List<String> ciudadesTop() {
        return inmuebleRepository.ciudadesPorCantidadDesc().stream()
                .limit(MAX_CIUDADES_TOP)
                .toList();
    }

    /** Todas las ciudades con inmuebles publicados, para selects que no deben limitarse al top 5. */
    public List<String> todasLasCiudades() {
        return ubicacionRepository.findDistinctCiudades();
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

    /** Los `limite` inmuebles mas cercanos a (lat, lon) por distancia real, sin importar la ciudad. */
    public List<PuntoMapa> puntosCercanos(Long excluirId, double lat, double lon, int limite) {
        double delta = 3.0;
        List<Inmueble> candidatos = inmuebleRepository.candidatosCercanos(
                lat - delta, lat + delta, lon - delta, lon + delta, excluirId);

        double coslat = Math.cos(Math.toRadians(lat));
        Comparator<Inmueble> porDistancia = Comparator.comparingDouble(i -> {
            double dx = (i.getLongitud() - lon) * coslat;
            double dy = i.getLatitud() - lat;
            return dx * dx + dy * dy;
        });

        return candidatos.stream()
                .sorted(porDistancia)
                .limit(limite)
                .map(i -> new PuntoMapa(i.getId(), i.getTitulo(), i.getPrecioNoche(), i.getLatitud(), i.getLongitud(), i.getImagenUrl()))
                .toList();
    }
}
