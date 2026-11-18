package com.arriendaya.app.repository;

import com.arriendaya.app.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByReservaId(Long reservaId);

    @Query("select coalesce(sum(p.monto), 0) from Pago p")
    BigDecimal sumarTotalPagado();

    /** Cuanto ha generado un inmueble: suma de pagos de sus reservas confirmadas. */
    @Query("select coalesce(sum(p.monto), 0) from Pago p where p.reserva.inmueble.id = :inmuebleId and p.reserva.estado = 'CONFIRMADA'")
    BigDecimal sumarPorInmueble(@Param("inmuebleId") Long inmuebleId);

    /** Pagos ya hechos por el cliente que la plataforma aun no le ha girado al propietario. */
    @Query("""
        select p from Pago p
        where p.reserva.inmueble.id = :inmuebleId
          and p.reserva.estado = 'CONFIRMADA'
          and not exists (select 1 from Giro g where g.pago = p)
        """)
    List<Pago> pendientesDeGiroPorInmueble(@Param("inmuebleId") Long inmuebleId);
}
