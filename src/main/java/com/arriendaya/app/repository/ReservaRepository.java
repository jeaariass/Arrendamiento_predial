package com.arriendaya.app.repository;

import com.arriendaya.app.model.EstadoReserva;
import com.arriendaya.app.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * join fetch porque mi_cuenta.html lee inmueble.ubicacion.ciudad fuera de la sesion de
     * Hibernate que abrio esta consulta (ver PagoService.pagar, que puede re-renderizar esta
     * pantalla desde una transaccion distinta tras rechazar un pago); sin el fetch, eso tira
     * LazyInitializationException.
     */
    @Query("""
        select r from Reserva r
        join fetch r.inmueble i
        join fetch i.ubicacion
        where r.cliente.id = :clienteId
        order by r.fechaReserva desc
        """)
    List<Reserva> findByClienteIdOrderByFechaReservaDesc(@Param("clienteId") Long clienteId);

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

    /**
     * Solo las reservas CONFIRMADAS (ya pagadas) bloquean fechas de verdad.
     * Una PENDIENTE sin pagar no le quita el cupo a otro cliente.
     */
    @Query("""
        select r from Reserva r
        where r.inmueble.id = :inmuebleId
          and r.estado = 'CONFIRMADA'
          and r.fechaFin >= :hoy
        order by r.fechaInicio asc
        """)
    List<Reserva> confirmadasFuturasPorInmueble(@Param("inmuebleId") Long inmuebleId, @Param("hoy") LocalDate hoy);

    @Query("""
        select count(r) > 0 from Reserva r
        where r.inmueble.id = :inmuebleId
          and r.estado = 'CONFIRMADA'
          and r.fechaInicio <= :fechaFin and r.fechaFin >= :fechaInicio
        """)
    boolean existeSolapada(@Param("inmuebleId") Long inmuebleId,
                            @Param("fechaInicio") LocalDate fechaInicio,
                            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Igual que existeSolapada pero ignorando una reserva puntual: se usa al pagar,
     * para revisar contra otras CONFIRMADAS sin chocar con la reserva que se esta confirmando.
     */
    @Query("""
        select count(r) > 0 from Reserva r
        where r.inmueble.id = :inmuebleId
          and r.id <> :reservaId
          and r.estado = 'CONFIRMADA'
          and r.fechaInicio <= :fechaFin and r.fechaFin >= :fechaInicio
        """)
    boolean existeSolapadaExcluyendo(@Param("inmuebleId") Long inmuebleId,
                                      @Param("reservaId") Long reservaId,
                                      @Param("fechaInicio") LocalDate fechaInicio,
                                      @Param("fechaFin") LocalDate fechaFin);
}
