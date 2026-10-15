package com.ctglobal.arriendo.repository;

import com.ctglobal.arriendo.model.Inmueble;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InmuebleRepository extends JpaRepository<Inmueble, Long> {

    List<Inmueble> findByPropietarioId(Long propietarioId);

    @Query("""
        select i from Inmueble i
        join i.ubicacion u
        where (:ciudad is null or lower(u.ciudad) like lower(concat('%', :ciudad, '%')))
          and (:capacidad is null or i.capacidad >= :capacidad)
          and i.estado = 'DISPONIBLE'
          and i.id not in (
            select r.inmueble.id from Reserva r
            where r.estado in ('PENDIENTE', 'CONFIRMADA')
              and :fechaInicio is not null and :fechaFin is not null
              and r.fechaInicio <= :fechaFin and r.fechaFin >= :fechaInicio
          )
        order by i.id desc
        """)
    List<Inmueble> buscar(@Param("ciudad") String ciudad,
                           @Param("capacidad") Integer capacidad,
                           @Param("fechaInicio") java.time.LocalDate fechaInicio,
                           @Param("fechaFin") java.time.LocalDate fechaFin);

    List<Inmueble> findTop8ByEstadoOrderByIdDesc(String estado);
}
