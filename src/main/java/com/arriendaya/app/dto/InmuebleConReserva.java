package com.arriendaya.app.dto;

import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.Reserva;

/** Un inmueble del dueno junto con su reserva activa mas cercana (o null si esta libre). */
public record InmuebleConReserva(Inmueble inmueble, Reserva reservaActual) {
}
