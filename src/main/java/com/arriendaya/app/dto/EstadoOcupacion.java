package com.arriendaya.app.dto;

import com.arriendaya.app.model.Inmueble;

import java.math.BigDecimal;
import java.util.List;

public record EstadoOcupacion(Inmueble inmueble, boolean ocupadoHoy, List<RangoFecha> ocupados, List<RangoFecha> libres,
                               String clienteActual, BigDecimal ganancia, BigDecimal montoPendienteGiro) {
}
