package com.ctglobal.arriendo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rol de Usuario. id_usuario es a la vez PK y FK (relacion 1 a 0..1 con Usuario),
 * asi un mismo usuario puede ser Propietario y Cliente al mismo tiempo.
 */
@Entity
@Table(name = "propietario")
@Getter
@Setter
@NoArgsConstructor
public class Propietario {

    @Id
    @Column(name = "usuario_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "cuenta_bancaria", length = 40)
    private String cuentaBancaria;

    public Propietario(Usuario usuario) {
        this.usuario = usuario;
    }
}
