package com.ctglobal.arriendo.repository;

import com.ctglobal.arriendo.model.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
    Optional<Ubicacion> findByCiudadIgnoreCaseAndPaisIgnoreCase(String ciudad, String pais);
    List<Ubicacion> findByCiudadContainingIgnoreCase(String ciudad);

    @org.springframework.data.jpa.repository.Query(
        "select distinct u.ciudad from Ubicacion u order by u.ciudad")
    List<String> findDistinctCiudades();
}
