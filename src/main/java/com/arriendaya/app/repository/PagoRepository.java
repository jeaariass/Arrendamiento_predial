package com.arriendaya.app.repository;

import com.arriendaya.app.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByReservaId(Long reservaId);
}
