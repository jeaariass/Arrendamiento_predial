package com.ctglobal.arriendo.repository;

import com.ctglobal.arriendo.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByClienteIdOrderByFechaReservaDesc(Long clienteId);
    List<Reserva> findByInmueble_Propietario_IdOrderByFechaReservaDesc(Long propietarioId);
}
