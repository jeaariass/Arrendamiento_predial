package com.arriendaya.app.repository;

import com.arriendaya.app.model.Inmueble;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InmuebleRepository extends JpaRepository<Inmueble, Long> {

    List<Inmueble> findByPropietarioId(Long propietarioId);

    @Query("""
        select i from Inmueble i
        join i.ubicacion u
        where (:ciudad = '' or lower(u.ciudad) like lower(concat('%', :ciudad, '%')))
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

    @Query("""
        select u.ciudad
        from Inmueble i join i.ubicacion u
        group by u.ciudad
        order by count(i) desc
        """)
    List<String> ciudadesPorCantidadDesc();

    @Query("""
        select i from Inmueble i
        join i.ubicacion u
        where i.estado = 'DISPONIBLE'
          and i.latitud is not null and i.longitud is not null
          and i.latitud between :minLat and :maxLat
          and i.longitud between :minLon and :maxLon
          and (:ciudad = '' or lower(u.ciudad) like lower(concat('%', :ciudad, '%')))
          and i.id not in (
            select r.inmueble.id from Reserva r
            where r.estado in ('PENDIENTE', 'CONFIRMADA')
              and :fechaInicio is not null and :fechaFin is not null
              and r.fechaInicio <= :fechaFin and r.fechaFin >= :fechaInicio
          )
        """)
    List<Inmueble> buscarEnMapa(@Param("minLat") double minLat,
                                 @Param("maxLat") double maxLat,
                                 @Param("minLon") double minLon,
                                 @Param("maxLon") double maxLon,
                                 @Param("ciudad") String ciudad,
                                 @Param("fechaInicio") java.time.LocalDate fechaInicio,
                                 @Param("fechaFin") java.time.LocalDate fechaFin);

    @Query("""
        select i from Inmueble i
        join i.ubicacion u
        where (:ciudad = '' or lower(u.ciudad) like lower(concat('%', :ciudad, '%')))
        order by i.id asc
        """)
    Page<Inmueble> paraAdmin(@Param("ciudad") String ciudad, Pageable pageable);
}
