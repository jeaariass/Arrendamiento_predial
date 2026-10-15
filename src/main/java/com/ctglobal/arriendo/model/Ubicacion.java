package com.ctglobal.arriendo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Objeto real: ciudad + pais. Varios inmuebles comparten la misma ubicacion. */
@Entity
@Table(name = "ubicacion", uniqueConstraints = @UniqueConstraint(columnNames = {"ciudad", "pais"}))
@Getter
@Setter
@NoArgsConstructor
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(nullable = false, length = 100)
    private String pais;

    public Ubicacion(String ciudad, String pais) {
        this.ciudad = ciudad;
        this.pais = pais;
    }

    @Override
    public String toString() {
        return ciudad + ", " + pais;
    }
}
