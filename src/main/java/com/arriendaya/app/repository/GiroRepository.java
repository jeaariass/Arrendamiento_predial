package com.arriendaya.app.repository;

import com.arriendaya.app.model.Giro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiroRepository extends JpaRepository<Giro, Long> {
    List<Giro> findByPropietarioIdOrderByFechaGiroDesc(Long propietarioId);
    boolean existsByPagoId(Long pagoId);
}
