package com.arriendaya.app.repository;

import com.arriendaya.app.model.EstadoReserva;
import com.arriendaya.app.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByClienteIdOrderByFechaReservaDesc(Long clienteId);
    List<Reserva> findByInmueble_Propietario_IdOrderByFechaReservaDesc(Long propietarioId);
    boolean existsByInmuebleId(Long inmuebleId);

    @Query("select count(r) from Reserva r where r.estado in ('PENDIENTE', 'CONFIRMADA')")
    long contarActivas();

    @Query("select coalesce(sum(r.valorTotal), 0) from Reserva r where r.estado = 'PENDIENTE'")
    java.math.BigDecimal sumarDeudaPendiente();

    @Query("select r from Reserva r where (:estado is null or r.estado = :estado) order by r.fechaReserva desc")
    List<Reserva> paraAdmin(@Param("estado") EstadoReserva estado);

    @Query("""
        select r from Reserva r
        where r.inmueble.id = :inmuebleId
          and r.estado in ('PENDIENTE', 'CONFIRMADA')
          and r.fechaFin >= :hoy
        order by r.fechaInicio asc
        """)
    List<Reserva> futurasPorInmueble(@Param("inmuebleId") Long inmuebleId, @Param("hoy") LocalDate hoy);

    @Query("""
        select count(r) > 0 from Reserva r
        where r.inmueble.id = :inmuebleId
          and r.estado in ('PENDIENTE', 'CONFIRMADA')
          and r.fechaInicio <= :fechaFin and r.fechaFin >= :fechaInicio
        """)
    boolean existeSolapada(@Param("inmuebleId") Long inmuebleId,
                            @Param("fechaInicio") LocalDate fechaInicio,
                            @Param("fechaFin") LocalDate fechaFin);
}
