package com.arriendaya.app.dto;

import java.math.BigDecimal;

/** Numeros grandes para la barra de metricas del panel admin. */
public record ResumenAdmin(long totalInmuebles, long reservasActivas, BigDecimal deudaPendiente, BigDecimal totalPagado) {
}
