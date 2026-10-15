package com.ctglobal.arriendo.repository;

import com.ctglobal.arriendo.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
