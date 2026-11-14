package com.arriendaya.app.repository;

import com.arriendaya.app.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByReservaId(Long reservaId);

    @Query("select coalesce(sum(p.monto), 0) from Pago p")
    BigDecimal sumarTotalPagado();
}
