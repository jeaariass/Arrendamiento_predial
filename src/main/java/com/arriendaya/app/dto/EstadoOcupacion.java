package com.arriendaya.app.dto;

import com.arriendaya.app.model.Inmueble;

import java.util.List;

public record EstadoOcupacion(Inmueble inmueble, boolean ocupadoHoy, List<RangoFecha> ocupados, List<RangoFecha> libres) {
}
