package com.arriendaya.app.repository;

import com.arriendaya.app.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByClienteIdOrderByFechaReservaDesc(Long clienteId);
    List<Reserva> findByInmueble_Propietario_IdOrderByFechaReservaDesc(Long propietarioId);

    @Query("""
        select r from Reserva r
        where r.inmueble.id = :inmuebleId
          and r.estado in ('PENDIENTE', 'CONFIRMADA')
          and r.fechaFin >= :hoy
        order by r.fechaInicio asc
        """)
    List<Reserva> futurasPorInmueble(@Param("inmuebleId") Long inmuebleId, @Param("hoy") LocalDate hoy);
}
