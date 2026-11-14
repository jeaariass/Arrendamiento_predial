package com.arriendaya.app.dto;

/** Un punto del mapa de calor: donde hay reservas activas y cuantas. */
public record PuntoCalor(Double lat, Double lon, Long intensidad) {
}
