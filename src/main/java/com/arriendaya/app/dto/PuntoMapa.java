package com.arriendaya.app.dto;

import java.math.BigDecimal;

public record PuntoMapa(Long id, String titulo, BigDecimal precioNoche, Double lat, Double lon, String imagenUrl) {
}
