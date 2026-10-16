package com.ctglobal.arriendo.service;

import com.ctglobal.arriendo.model.Inmueble;
import com.ctglobal.arriendo.repository.InmuebleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InmuebleService {

    private final InmuebleRepository inmuebleRepository;

    public List<Inmueble> buscar(String ciudad, Integer capacidad, LocalDate fechaInicio, LocalDate fechaFin) {
        return inmuebleRepository.buscar(
                (ciudad == null || ciudad.isBlank()) ? null : ciudad.trim(),
                capacidad,
                fechaInicio,
                fechaFin);
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
}
