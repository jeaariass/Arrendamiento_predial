package com.arriendaya.app.repository;

import com.arriendaya.app.model.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
    Optional<Ubicacion> findByCiudadIgnoreCaseAndPaisIgnoreCase(String ciudad, String pais);
    List<Ubicacion> findByCiudadContainingIgnoreCase(String ciudad);

    @Query("select distinct u.ciudad from Ubicacion u order by u.ciudad")
    List<String> findDistinctCiudades();

    @Query("select distinct u.ciudad from Ubicacion u where lower(u.ciudad) like lower(concat('%', :q, '%')) order by u.ciudad")
    List<String> buscarCiudades(@Param("q") String q);
}
