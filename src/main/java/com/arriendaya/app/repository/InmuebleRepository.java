package com.arriendaya.app.repository;

import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.TipoInmueble;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InmuebleRepository extends JpaRepository<Inmueble, Long> {

    List<Inmueble> findByPropietarioId(Long propietarioId);

    /** Bloquea la fila del inmueble durante el pago, para que dos pagos a la vez no confirmen fechas que se cruzan. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inmueble i where i.id = :id")
    Optional<Inmueble> buscarParaBloquear(@Param("id") Long id);

    @Query("""
        select i from Inmueble i
        join i.ubicacion u
        where (:ciudad = '' or lower(u.ciudad) like lower(concat('%', :ciudad, '%')))
          and (:capacidad is null or i.capacidad >= :capacidad)
          and (:tipo is null or i.tipo = :tipo)
          and i.estado = 'DISPONIBLE'
          and i.id not in (
            select r.inmueble.id from Reserva r
            where r.estado = 'CONFIRMADA'
              and cast(:fechaInicio as date) is not null and cast(:fechaFin as date) is not null
              and r.fechaInicio <= :fechaFin and r.fechaFin >= :fechaInicio
          )
        order by i.id desc
        """)
    List<Inmueble> buscar(@Param("ciudad") String ciudad,
                           @Param("capacidad") Integer capacidad,
                           @Param("fechaInicio") java.time.LocalDate fechaInicio,
                           @Param("fechaFin") java.time.LocalDate fechaFin,
                           @Param("tipo") TipoInmueble tipo);

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
            where r.estado = 'CONFIRMADA'
              and cast(:fechaInicio as date) is not null and cast(:fechaFin as date) is not null
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
        where i.estado = 'DISPONIBLE'
          and i.latitud is not null and i.longitud is not null
          and i.latitud between :minLat and :maxLat
          and i.longitud between :minLon and :maxLon
          and i.id <> :excluirId
        """)
    List<Inmueble> candidatosCercanos(@Param("minLat") double minLat,
                                       @Param("maxLat") double maxLat,
                                       @Param("minLon") double minLon,
                                       @Param("maxLon") double maxLon,
                                       @Param("excluirId") Long excluirId);

    @Query("""
        select i from Inmueble i
        join i.ubicacion u
        join i.propietario prop
        join prop.usuario pu
        where (:ciudad = '' or lower(u.ciudad) like lower(concat('%', :ciudad, '%')))
          and (
            :estadoFiltro = ''
            or (:estadoFiltro = 'OCUPADO' and exists (
                select 1 from Reserva r
                where r.inmueble = i and r.estado = 'CONFIRMADA'
                  and r.fechaInicio <= current_date and r.fechaFin >= current_date
            ))
            or (:estadoFiltro = 'LIBRE' and not exists (
                select 1 from Reserva r
                where r.inmueble = i and r.estado = 'CONFIRMADA'
                  and r.fechaInicio <= current_date and r.fechaFin >= current_date
            ))
          )
          and (
            :q = ''
            or lower(i.titulo) like lower(concat('%', :q, '%'))
            or lower(pu.nombre) like lower(concat('%', :q, '%'))
            or lower(pu.apellido) like lower(concat('%', :q, '%'))
            or lower(pu.email) like lower(concat('%', :q, '%'))
          )
        order by i.id asc
        """)
    Page<Inmueble> paraAdmin(@Param("ciudad") String ciudad,
                             @Param("estadoFiltro") String estadoFiltro,
                             @Param("q") String q,
                             Pageable pageable);

    @Query("""
        select i.latitud, i.longitud, count(r)
        from Inmueble i join Reserva r on r.inmueble = i
        where r.estado in ('PENDIENTE', 'CONFIRMADA')
          and i.latitud is not null and i.longitud is not null
        group by i.id, i.latitud, i.longitud
        """)
    List<Object[]> puntosDeReservasActivas();
}
