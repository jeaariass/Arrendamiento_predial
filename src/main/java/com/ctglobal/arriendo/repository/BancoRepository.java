package com.ctglobal.arriendo.repository;

import com.ctglobal.arriendo.model.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BancoRepository extends JpaRepository<Banco, Long> {
}
