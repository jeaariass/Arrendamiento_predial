package com.ctglobal.arriendo.repository;

import com.ctglobal.arriendo.model.Giro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiroRepository extends JpaRepository<Giro, Long> {
    List<Giro> findByPropietarioIdOrderByFechaGiroDesc(Long propietarioId);
}
