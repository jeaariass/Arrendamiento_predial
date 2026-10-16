package com.arriendaya.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Objeto real: una persona registrada en la plataforma.
 * Un mismo Usuario puede tener, ademas, un registro en Propietario y/o en Cliente
 * (especializacion solapada): no hereda por tabla, se relaciona 1 a 1 opcional.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /**
     * Bandera de acceso al panel de administracion. No es un objeto del
     * modelo conceptual (no hay entidad "Administrador"): es un dato propio
     * de Usuario, igual que password_hash.
     */
    @Column(name = "es_admin", nullable = false)
    private boolean esAdmin = false;
}
